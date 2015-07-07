package com.gocnak.sound;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A simple com.gocnak.sound system.
 *
 * @author Dr. Kegel
 */
public final class SoundPlayer implements Closeable {

    /**
     * Provides strategies how to react, if being requested to play a com.gocnak.sound file, which is already running.
     *
     * @author Dr. Kegel
     */
    public enum PlayMode {
        /**
         * Restarts the com.gocnak.sound from the beginning.
         */
        Restart,
        /**
         * Ignores the second play request.
         */
        Ignore,
        /**
         * Stops the first com.gocnak.sound.
         */
        Toggle,
        /**
         * Plays both sounds.
         */
        Force,
    }

    //TODO: Debug counter
    /**
     * SoundEntries constructed
     */
    public static AtomicInteger sector = new AtomicInteger();
    /**
     * SoundEntries played
     */
    public static AtomicInteger sepl = new AtomicInteger();
    /**
     * SoundEntries closed (should be sector - number of com.gocnak.sound files)
     */
    public static AtomicInteger secl = new AtomicInteger();

    /* immutable except counter */


    private static ConcurrentHashMap<File, SoundEntry> clips;

    public SoundPlayer() {
        clips = new ConcurrentHashMap<>();
    }

    @Override
    public void close() {
        clips.values().forEach(com.gocnak.sound.SoundEntry::close);
        clips.clear();
    }

    /**
     * Returns a {@code SoundEntry} instance based on the source file and {@link PlayMode} or creates a new one.
     *
     * @param file The source file.
     * @return a {@code SoundEntry} instance.
     * @throws IOException
     */
    private SoundEntry getClip(File file) throws IOException {
        SoundEntry result = new SoundEntry(file, clips.get(file), clips);
        clips.put(file, result);
        return result;
    }

    /**
     * Returns a {@link Collection} with all playing files.
     *
     * @return a {@link Collection} with all playing files.
     */
    public Collection<SoundEntry> getPlayingSounds() {
        return clips.values().stream().filter(entry -> entry.getClip().isRunning()).collect(Collectors.toList());
    }

    /**
     * Plays a com.gocnak.sound file.
     *
     * @param file The com.gocnak.sound file.
     * @param mode The strategy for handling the case that the com.gocnak.sound is already playing.
     * @throws IOException
     */
    public void play(File file, PlayMode mode) throws IOException {
        if (file != null) getClip(file).play(mode);
    }


    /**
     * @param file The com.gocnak.sound file.
     */
    public void stop(File file) {
        SoundEntry entry = clips.get(file);
        if (entry != null) {
            entry.stop();
        }
    }

}