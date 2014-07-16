/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014 Caprica Software Limited.
 */

package lib.vlcj.java.uk.co.caprica.vlcj.log;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.LibC;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.LibVlc;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.internal.libvlc_log_cb;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.internal.libvlc_log_level_e;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.internal.libvlc_log_t;
import lib.vlcj.java.uk.co.caprica.vlcj.logger.Logger;
import lib.vlcj.java.uk.co.caprica.vlcj.version.LibVlcVersion;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Encapsulation of the vlc native log.
 * <p/>
 * The native library specifies that implementations of native log handlers (like
 * that encapsulated within this class) must be thread-safe.
 * <p/>
 * The default log level is {@link libvlc_log_level_e#NOTICE}, this can be changed
 * by invoking {@link #setLevel(libvlc_log_level_e)}.
 * <p/>
 * <strong>The native log requires vlc 2.1.0 or later.</strong>
 */
public class NativeLog {

    /**
     * Default string buffer size.
     * <p/>
     * Plus one for the null terminator.
     */
    private static final int BUFFER_SIZE = 200 + 1;

    /**
     * List of registered event listeners.
     */
    private final List<LogEventListener> eventListenerList = new ArrayList<LogEventListener>();

    /**
     * Background thread to send event notifications to listeners.
     * <p/>
     * The single-threaded nature of this executor service ensures that events are delivered to
     * listeners in a thread-safe manner and in their proper sequence.
     */
    private final ExecutorService listenersService = Executors.newSingleThreadExecutor();

    /**
     * Native library instance.
     */
    private final LibVlc libvlc;

    /**
     * LibVlc instance.
     */
    private final libvlc_instance_t instance;

    /**
     * Native log callback.
     */
    private libvlc_log_cb callback;

    /**
     * Set to true when the log has been released.
     */
    private final AtomicBoolean released = new AtomicBoolean();

    /**
     * Log level.
     * <p/>
     * Set to <code>null</code> to suppress all log messages.
     */
    private libvlc_log_level_e logLevel = libvlc_log_level_e.NOTICE;

    /**
     * Create a new native log component.
     *
     * @param libvlc   native library instance
     * @param instance libvlc instance
     */
    public NativeLog(LibVlc libvlc, libvlc_instance_t instance) {
        if (LibVlcVersion.getVersion().atLeast(LibVlcVersion.LIBVLC_210)) {
            this.libvlc = libvlc;
            this.instance = instance;
            createInstance();
        } else {
            throw new RuntimeException("Native log requires libvlc 2.1.0 or later");
        }
    }

    /**
     * Add a component to be notified of log messages.
     *
     * @param listener component to add
     */
    public final void addLogListener(LogEventListener listener) {
        Logger.debug("addLogListener(listener={})", listener);
        eventListenerList.add(listener);
    }

    /**
     * Remove a component previously added so it is no longer notified of log messages.
     *
     * @param listener component to remove
     */
    public final void removeLogListener(LogEventListener listener) {
        Logger.debug("removeLogListener(listener={})", listener);
        eventListenerList.remove(listener);
    }

    /**
     * Set the log threshold level.
     * <p/>
     * Only log messages that are equal to or exceed this threshold are notified to
     * listeners.
     *
     * @param logLevel log threshold level
     */
    public final void setLevel(libvlc_log_level_e logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Get the log threshold level.
     *
     * @return level
     */
    public final libvlc_log_level_e getLevel() {
        return logLevel;
    }

    /**
     * Release the native log component.
     */
    public final void release() {
        Logger.debug("release()");
        if (released.compareAndSet(false, true)) {
            destroyInstance();
        }
    }

    /**
     * Create the native resources and prepare the log component.
     */
    private void createInstance() {
        Logger.debug("createInstance()");
        // Create a native callback to receive log messages
        callback = new NativeLogCallback();
        // Subscribe to the native log
        libvlc.libvlc_log_set(instance, callback, null);
    }

    /**
     * Destroy the native resources and shut down the log component.
     */
    private void destroyInstance() {
        Logger.debug("destroyInstance()");
        // Stop receiving native log messages
        libvlc.libvlc_log_unset(instance);
        // Clear all registered listeners
        eventListenerList.clear();
        // Shut down the listener service
        Logger.debug("Shut down listeners...");
        listenersService.shutdown();
        Logger.debug("Listeners shut down.");
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.debug("finalize()");
        Logger.debug("Native log has been garbage collected");
        super.finalize();
        // FIXME should this invoke release()?
    }

    /**
     * This implementation must be thread-safe.
     */
    private final class NativeLogCallback implements libvlc_log_cb {

        @Override
        public void log(Pointer data, int level, libvlc_log_t ctx, String format, Pointer args) {
            // If the log is not being suppressed...
            if (logLevel != null && level >= logLevel.intValue()) {
                // Allocate a new buffer to hold the formatted log message
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
                // Delegate to the native library to format the log message
                int size = LibC.INSTANCE.vsnprintf(byteBuffer, byteBuffer.capacity(), format, args);
                // If the message was formatted without error...
                if (size >= 0) {
                    // FIXME could reallocate a new buffer here and try again if size > capacity?
                    // Determine the number of available characters (actually number of bytes)
                    size = Math.min(size, BUFFER_SIZE);
                    // Create a new string from the byte buffer contents
                    byte[] bytes = new byte[size];
                    byteBuffer.get(bytes);
                    String message = new String(bytes);
                    if (message.length() > 0) {
                        // Get the information about the object that emitted the log statement
                        PointerByReference modulePointer = new PointerByReference();
                        PointerByReference filePointer = new PointerByReference();
                        IntByReference linePointer = new IntByReference();
                        libvlc.libvlc_log_get_context(ctx, modulePointer, filePointer, linePointer);
                        PointerByReference namePointer = new PointerByReference();
                        PointerByReference headerPointer = new PointerByReference();
                        IntByReference idPointer = new IntByReference();
                        libvlc.libvlc_log_get_object(ctx, namePointer, headerPointer, idPointer);
                        String module = getString(modulePointer);
                        String file = getString(filePointer);
                        Integer line = linePointer.getValue();
                        String name = getString(namePointer);
                        String header = getString(headerPointer);
                        Integer id = idPointer.getValue();
                        // ...send the event
                        raiseLogEvent(libvlc_log_level_e.level(level), module, file, line, name, header, id, message);
                    }
                } else {
                    Logger.error("Failed to format log message");
                }
            }
        }
    }

    /**
     * Dereference a pointer (that may be <code>null</code>) to get a string.
     *
     * @param pointer pointer
     * @return string, or <code>null</code> if the pointer is <code>null</code>
     */
    private String getString(PointerByReference pointer) {
        Pointer value = pointer.getValue();
        return value != null ? value.getString(0) : null;
    }

    /**
     * Raise a log event.
     *
     * @param level   log level
     * @param module  module
     * @param file    file
     * @param line    line number
     * @param name    name
     * @param header  header
     * @param id      object identifier
     * @param message log message
     */
    private void raiseLogEvent(libvlc_log_level_e level, String module, String file, Integer line, String name, String header, Integer id, String message) {
        Logger.trace("raiseLogEvent(level={},module={},line={},name={},header={},id={},message={}", level, module, file, line, name, header, id, message);
        // Submit a new log event so message are sent serially and asynchronously
        listenersService.submit(new NotifyEventListenersRunnable(level, module, file, line, name, header, id, message));

    }

    /**
     * A runnable task used to fire event notifications.
     * <p/>
     * Care must be taken not to re-enter the native library during an event notification so the
     * notifications are off-loaded to a separate thread.
     * <p/>
     * These events therefore do <em>not</em> run on the Event Dispatch Thread.
     */
    private final class NotifyEventListenersRunnable implements Runnable {

        /**
         * Log level.
         */
        private final libvlc_log_level_e level;

        /**
         * Module.
         */
        private final String module;

        /**
         * File.
         */
        private final String file;

        /**
         * Line number.
         */
        private final Integer line;

        /**
         * Name.
         */
        private final String name;

        /**
         * Header.
         */
        private final String header;

        /**
         * Object identifier.
         */
        private final Integer id;

        /**
         * Log message.
         */
        private final String message;

        /**
         * Create a runnable.
         *
         * @param level   log level
         * @param module  module
         * @param file    file
         * @param line    line number
         * @param name    name
         * @param header  header
         * @param id      object identifier
         * @param message log message
         */
        private NotifyEventListenersRunnable(libvlc_log_level_e level, String module, String file, Integer line, String name, String header, Integer id, String message) {
            this.level = level;
            this.module = module;
            this.file = file;
            this.line = line;
            this.name = name;
            this.header = header;
            this.id = id;
            this.message = message;
        }

        @Override
        public void run() {
            Logger.trace("run()");
            for (int i = eventListenerList.size() - 1; i >= 0; i--) {
                LogEventListener listener = eventListenerList.get(i);
                try {
                    listener.log(level, module, file, line, name, header, id, message);
                } catch (Exception e) {
                    Logger.warn("Event listener {} threw an exception", e, listener);
                    // Continue with the next listener...
                }
            }
            Logger.trace("runnable exits");
        }
    }
}
