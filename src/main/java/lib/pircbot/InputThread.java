/* 
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

*/


package lib.pircbot;

import gui.forms.GUIMain;

import java.io.BufferedReader;
import java.io.InterruptedIOException;
import java.net.Socket;

/**
 * A Thread which reads lines from the IRC server.  It then
 * passes these lines to the PircBot without changing them.
 * This running Thread also detects disconnection from the server
 * and is thus used by the OutputThread to send lines to the server.
 *
 * @author Paul James Mutton,
 *         <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version 1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 */
public class InputThread extends Thread {

    /**
     * The InputThread reads lines from the IRC server and allows the
     * PircBot to handle them.
     *
     * @param conn    The instance of the connection.
     * @param breader The BufferedReader that reads lines from the server.
     */
    InputThread(PircBotConnection conn, Socket socket, BufferedReader breader) {
        connection = conn;
        _socket = socket;
        _breader = breader;
        this.setName(this.getClass() + "-Thread");
    }


    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    void sendRawLine(String line) {
        connection.sendRawLine(line);
    }


    /**
     * Returns true if this InputThread is connected to an IRC server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     *
     * @return True if still connected.
     */
    boolean isConnected() {
        return _isConnected;
    }


    /**
     * Called to start this Thread reading lines from the IRC server.
     * When a line is read, this method calls the handleLine method
     * in the PircBot, which may subsequently call an 'onXxx' method
     * in the PircBot subclass.  If any subclass of Throwable (i.e.
     * any Exception or Error) is thrown by your method, then this
     * method will print the stack trace to the standard output.  It
     * is probable that the PircBot may still be functioning normally
     * after such a problem, but the existance of any uncaught exceptions
     * in your code is something you should really fix.
     */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                try {
                    String line;
                    while (((line = _breader.readLine()) != null) && !GUIMain.shutDown) {
                        try {
                            connection.getBot().log(line);
                            if (line.startsWith("PING ")) {
                                // Respond to the ping and return immediately.
                                sendRawLine("PONG " + line.substring(5));
                            } else {
                                line = line.replaceAll("\\s+", " ");
                                connection.getBot().handleLine(line);
                            }
                        } catch (Throwable t) {
                            // Stick the whole stack trace into a String so we can output it nicely.
                            GUIMain.log(t);
                        }
                    }
                    running = false;
                } catch (InterruptedIOException iioe) {
                    // This will happen if we haven't received anything from the server for a while.
                    // So we shall send it a ping to check that we are still connected.
                    sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        } catch (Exception e) {
            // Do nothing.
        }

        // If we reach this point, then we must have disconnected.
        try {
            _socket.close();
        } catch (Exception e) {
            // Just assume the socket was already closed.
        }
        if (!_disposed) {
            connection.getBot().log("*** Disconnected.");
            _isConnected = false;
            connection.getBot().getMessageHandler().onDisconnect();
        }
    }


    /**
     * Closes the socket without onDisconnect being called subsequently.
     */
    public void dispose() {
        try {
            _disposed = true;
            _socket.close();
        } catch (Exception e) {
            // Do nothing.
        }
    }

    private PircBotConnection connection = null;
    private Socket _socket = null;
    private BufferedReader _breader = null;
    private boolean _isConnected = true;
    private boolean _disposed = false;
}