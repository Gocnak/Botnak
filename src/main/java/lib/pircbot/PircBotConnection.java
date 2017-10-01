package lib.pircbot;

import gui.forms.GUIMain;
import util.Utils;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Nick on 9/7/2015.
 */
public class PircBotConnection {

    private InputThread _inputThread = null;
    private OutputThread _outputThread = null;
    private ArrayBlockingQueue<String> _outQueue;
    private PircBot bot;
    private ConnectionType type;
    private String _server, name;

    public OutputThread getOutputThread() {
        return _outputThread;
    }

    public ArrayBlockingQueue<String> getOutQueue()
    {
        return _outQueue;
    }

    public PircBot getBot() {
        return bot;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public ConnectionType getType() {
        return type;
    }

    private InetAddress _inetAddress = null;

    public enum ConnectionType {
        NORMAL(443, "irc.chat.twitch.tv");

        int port;
        String[] hosts;

        ConnectionType(int port, String... hosts) {
            this.port = port;
            this.hosts = hosts;
        }
    }

    public PircBotConnection(PircBot bot, ConnectionType type) {
        this.bot = bot;
        this.type = type;
        this.name = bot.getNick();
        this._outQueue = new ArrayBlockingQueue<>(500, true); // I don't know if people will use more than 500 msgs
    }

    /**
     * Returns whether or not the PircBot is currently connected to a server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     *
     * @return True if and only if the PircBot is currently connected to a server.
     */
    public boolean isConnected() {
        return _inputThread != null && _inputThread.isConnected();
    }


    public boolean connect() {
        if (isConnected()) {
            return false;
        }
        _server = type.hosts[Utils.random(0, type.hosts.length)];

        // Connect to the server.
        Socket socket;
        InputStream socketIn;
        OutputStream socketOut;
        try {
            socket = SSLSocketFactory.getDefault().createSocket(_server, type.port);
            bot.log("*** Trying to connect to " + _server + " on port " + getPort());
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
        } catch (Exception e) {
            GUIMain.log(e);
            return false;
        }

        bot.log("*** Connected to server.");

        _inetAddress = socket.getLocalAddress();

        InputStreamReader inputStreamReader;
        OutputStreamWriter outputStreamWriter;

        // Assume the specified encoding is valid for this JVM.
        try {
            inputStreamReader = new InputStreamReader(socketIn, "UTF-8");
            outputStreamWriter = new OutputStreamWriter(socketOut, "UTF-8");
        } catch (Exception e) {
            return false;
        }
        BufferedReader breader = new BufferedReader(inputStreamReader);
        BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);
        _outputThread = new OutputThread(bot, _outQueue, bwriter);
        // Attempt to join the server.
        _outputThread.sendRawLine("PASS " + bot.getPassword());
        _outputThread.sendRawLine("NICK " + bot.getNick());

        _inputThread = new InputThread(this, socket, breader);

        // Read stuff back from the server to see if we connected.
        String line;
        try {
            while ((line = breader.readLine()) != null) {
                bot.log(line);
                bot.handleLine(line);
                if (line.contains("Login unsuccessful")) {
                    socket.close();
                    _inputThread.dispose();
                    _inputThread = null;
                    return false;
                }
                int firstSpace = line.indexOf(" ");
                int secondSpace = line.indexOf(" ", firstSpace + 1);
                if (secondSpace >= 0) {
                    String code = line.substring(firstSpace + 1, secondSpace);
                    if (code.equals("004")) {
                        // We're connected to the server.
                        break;
                    } else if (code.startsWith("5") || code.startsWith("4")) {
                        socket.close();
                        _inputThread = null;
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            _inputThread = null;
            return false;
        }

        bot.log("*** Logged onto server.");

        // This makes the socket timeout on read operations after 5 minutes.
        // Maybe in some future version I will let the user change this at runtime.
        try {
            socket.setSoTimeout(5 * 60 * 1000);
        } catch (Exception e) {
            return false;
        }

        // Now start the InputThread to read all other lines from the server.
        _inputThread.start();

        // Now start the outputThread that will be used to send all messages.
        _outputThread.start();

        getOutputThread().sendRawLine("CAP REQ :twitch.tv/tags");
        getOutputThread().sendRawLine("CAP REQ :twitch.tv/commands");
        return true;
    }


    /**
     * Disposes of all thread resources used by this PircBot. This may be
     * useful when writing bots or clients that use multiple servers (and
     * therefore multiple PircBot instances) or when integrating a PircBot
     * with an existing program.
     * <p>
     * Each PircBot runs its own threads for dispatching messages from its
     * outgoing message queue and receiving messages from the server.
     * Calling dispose() ensures that these threads are
     * stopped, thus freeing up system resources and allowing the PircBot
     * object to be garbage collected if there are no other references to
     * it.
     * <p>
     * Once a PircBot object has been disposed, it should not be used again.
     * Attempting to use a PircBot that has been disposed may result in
     * unpredictable behaviour.
     *
     * @since 1.2.2
     */
    public void dispose() {
        if (_outputThread != null) _outputThread.interrupt();
        if (_inputThread != null) _inputThread.dispose();
    }

    /**
     * Returns the name of the last IRC server the PircBot tried to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The name of the last machine we tried to connect to. Returns
     * null if no connection attempts have ever been made.
     */
    public final String getServer() {
        return _server;
    }


    /**
     * Returns the port number of the last IRC server that the PircBot tried
     * to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of -1 is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The port number of the last IRC server we connected to.
     * Returns -1 if no connection attempts have ever been made.
     * @since PircBot 0.9.9
     */
    public final int getPort() {
        return type.port;
    }

    /**
     * Returns the InetAddress used by the PircBot.
     * This can be used to find the I.P. address from which the PircBot is
     * connected to a server.
     *
     * @return The current local InetAddress, or null if never connected.
     * @since PircBot 1.4.4
     */
    public InetAddress getInetAddress() {
        return _inetAddress;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PircBotConnection) &&
                (((PircBotConnection) obj).getType() == this.getType()) &&
                ((PircBotConnection) obj).getName().equals(this.getName()) &&
                ((PircBotConnection) obj).getBot().equals(this.getBot());
    }


    public void sendRawLine(String line) {
        if (getOutputThread() == null || line == null) return;
        getOutputThread().sendRawLine(line);
    }
}