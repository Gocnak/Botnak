/* 
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

*/


package lib.pircbot.org.jibble.pircbot;

import gui.GUIMain;
import irc.MessageHandler;
import util.Constants;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * PircBot is a Java framework for writing IRC bots quickly and easily.
 * <p/>
 * It provides an event-driven architecture to handle common IRC
 * events, flood protection, DCC support, ident support, and more.
 * The comprehensive logfile format is suitable for use with pisg to generate
 * channel statistics.
 * <p/>
 * Methods of the PircBot class can be called to send events to the IRC server
 * that it connects to.  For example, calling the sendMessage method will
 * send a message to a channel or user on the IRC server.  Multiple servers
 * can be supported using multiple instances of PircBot.
 * <p/>
 * To perform an action when the PircBot receives a normal message from the IRC
 * server, you would override the onMessage method defined in the PircBot
 * class.  All on<i>XYZ</i> methods in the PircBot class are automatically called
 * when the event <i>XYZ</i> happens, so you would override these if you wish
 * to do something when it does happen.
 * <p/>
 * Some event methods, such as onPing, should only really perform a specific
 * function (i.e. respond to a PING from the server).  For your convenience, such
 * methods are already correctly implemented in the PircBot and should not
 * normally need to be overridden.  Please read the full documentation for each
 * method to see which ones are already implemented by the PircBot class.
 * <p/>
 * Please visit the PircBot homepage at
 * <a href="http://www.jibble.org/pircbot.php">http://www.jibble.org/pircbot.php</a>
 * for full revision history, a beginners guide to creating your first PircBot
 * and a list of some existing Java IRC bots and clients that use the PircBot
 * framework.
 *
 * @author Paul James Mutton,
 *         <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version 1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 */
public class PircBot implements ReplyConstants {


    public static final int ADMIN = 5;
    public static final int STAFF = 6;
    public static final int TURBO = 7;


    private ChannelManager channelManager = null;

    public ChannelManager getChannelManager() {
        return isSlave ? parent.getChannelManager() : channelManager;
    }

    private MessageHandler handler;

    public MessageHandler getHandler() {
        return isSlave ? parent.getHandler() : handler;
    }


    /**
     * Constructs a PircBot with the default settings.  Your own constructors
     * in classes which extend the PircBot abstract class should be responsible
     * for changing the default settings if required.
     */
    public PircBot(MessageHandler messageHandler) {
        channelManager = new ChannelManager();
        handler = messageHandler;
    }

    /**
     * Creates a PircBot with a PircBot as a parent. This is used to add the JOIN and PARTs to
     * create and delete users in the bot.
     *
     * @param parent The parent PircBot to use. In normal cases, this is the viewer again, just
     *               being used to parse the JOIN and PART messages.
     */
    public PircBot(PircBot parent) {
        isSlave = true;
        this.parent = parent;
        setNick(parent.getNick());
        setPassword(parent.getPassword());
        System.out.println("CREATED CHILD ACCOUNT");
    }

    private PircBot parent = null;
    private PircBot child = null;
    private boolean isSlave = false;


    /**
     * Attempt to connect to the specified IRC server using the supplied
     * password.
     * The onConnect method is called upon success.
     *
     * @param hostname The hostname of the server to connect to.
     * @param port     The port number to connect to on the server.
     */
    public final synchronized boolean connect(String hostname, int port) {

        if (isConnected()) {
            return false;
        }
        _server = hostname;
        _port = port;
        if (_password == null || "".equals(_password)) return false;

        // Don't clear the outqueue - there might be something important in it!

        // Clear everything we may have know about channels.
        removeAllChannels();

        // Connect to the server.
        Socket socket;
        InputStream socketIn;
        OutputStream socketOut;
        try {
            socket = new Socket(hostname, port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
            return false;
        }

        log("*** Connected to server.");

        _inetAddress = socket.getLocalAddress();

        InputStreamReader inputStreamReader;
        OutputStreamWriter outputStreamWriter;
        if (getEncoding() != null) {
            // Assume the specified encoding is valid for this JVM.
            try {
                inputStreamReader = new InputStreamReader(socketIn, getEncoding());
                outputStreamWriter = new OutputStreamWriter(socketOut, getEncoding());
            } catch (Exception e) {
                return false;
            }
        } else {
            // Otherwise, just use the JVM's default encoding.
            inputStreamReader = new InputStreamReader(socketIn);
            outputStreamWriter = new OutputStreamWriter(socketOut);
        }

        BufferedReader breader = new BufferedReader(inputStreamReader);
        BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);
        _outputThread = new OutputThread(this, _outQueue, bwriter);
        // Attempt to join the server.
        _outputThread.sendRawLine("PASS " + _password);
        _outputThread.sendRawLine("NICK " + getNick());

        _inputThread = new InputThread(this, socket, breader);

        // Read stuff back from the server to see if we connected.
        String line;
        try {
            while ((line = breader.readLine()) != null) {
                handleLine(line);
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
                    } else if (code.equals("433")) {
                        socket.close();
                        _inputThread = null;
                        return false;
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

        log("*** Logged onto server.");

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
        getHandler().onConnect();
        if (!isSlave && child == null) {
            child = new PircBot(this);
            // if (!child.getNick().contains("bot")) child.setVerbose(true);
            if (child.connect(hostname, port)) {
                System.out.println("CHILD HAS CONNECTED!");
            }
        }
        return true;
    }


    /**
     * Reconnects to the IRC server that we were previously connected to.
     * If necessary, the appropriate port number and password will be used.
     * This method will throw an IrcException if we have never connected
     * to an IRC server previously.
     *
     * @since PircBot 0.9.9
     */
    public final synchronized void reconnect() throws IOException {
        if (getServer() == null || getPassword() == null || getPort() == -1) {
            return;
        }
        connect(getServer(), getPort());
    }


    /**
     * This method disconnects from the server cleanly by calling the
     * quitServer() method.  Providing the PircBot was connected to an
     * IRC server, the onDisconnect() will be called as soon as the
     * disconnection is made by the server.
     *
     * @see #quitServer() quitServer
     * @see #quitServer(String) quitServer
     */
    public final synchronized void disconnect() {
        if (child != null) child.disconnect();
        quitServer();
    }


    /**
     * Joins a channel.
     *
     * @param channel The name of the channel to join (eg "#cs").
     */
    public synchronized final void joinChannel(String channel) {
        if (isInChannel(channel)) return;
        sendRawLine("JOIN " + channel);
        if (child != null) child.joinChannel(channel);
        if (channelManager != null) channelManager.addChannel(new Channel(channel, new User(getNick())));
    }


    /**
     * Parts a channel.
     *
     * @param channel The name of the channel to leave.
     */
    public synchronized final void partChannel(String channel) {
        if (!isInChannel(channel)) return;
        sendRawLine("PART " + channel);
        if (child != null) child.partChannel(channel);
        if (channelManager != null) channelManager.removeChannel(channel);
    }


    /**
     * Quits from the IRC server.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     */
    public final void quitServer() {
        quitServer("");
    }


    /**
     * Quits from the IRC server with a reason.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     *
     * @param reason The reason for quitting the server.
     */
    public final void quitServer(String reason) {
        sendRawLine("QUIT :" + reason);
        if (channelManager != null) channelManager.dispose();
    }


    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLine(String line) {
        if (isConnected()) {
            _outputThread.sendRawLine(line);
        }
    }

    /**
     * Sends a raw line through the outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLineViaQueue(String line) {
        if (line == null) return;
        if (isConnected()) {
            _outQueue.add(line);
        }
    }


    /**
     * Sends a message to a channel or a private message to a user.  These
     * messages are added to the outgoing message queue and sent at the
     * earliest possible opportunity.
     * <p/>
     * Some examples: -
     * <pre>    // Send the message "Hello!" to the channel #cs.
     *    sendMessage("#cs", "Hello!");
     * <p/>
     *    // Send a private message to Paul that says "Hi".
     *    sendMessage("Paul", "Hi");</pre>
     * <p/>
     * You may optionally apply colours, boldness, underlining, etc to
     * the message by using the <code>Colors</code> class.
     *
     * @param target  The name of the channel or user nick to send to.
     * @param message The message to send.
     */
    public final void sendMessage(String target, String message) {
        _outQueue.add("PRIVMSG " + target + " :" + message);
        if (message.startsWith("/me")) {
            delayedMessage(":" + getNick() + "!" + getNick() + "@" + getNick() + ".tmi.twitch.tv PRIVMSG " + target + " :\u0001ACTION " + message.substring(4) + "\u0001");
        } else {
            delayedMessage(":" + getNick() + "!" + getNick() + "@" + getNick() + ".tmi.twitch.tv PRIVMSG " + target + " :" + message);
        }
    }

    private void delayedMessage(final String mess) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
                handleLine(mess);
            }
        };
        new Thread(r).start();
    }


    /**
     * Identify the bot with NickServ, supplying the appropriate password.
     * Some IRC Networks (such as freenode) require users to <i>register</i> and
     * <i>identify</i> with NickServ before they are able to send private messages
     * to other users, thus reducing the amount of spam.  If you are using
     * an IRC network where this kind of policy is enforced, you will need
     * to make your bot <i>identify</i> itself to NickServ before you can send
     * private messages. Assuming you have already registered your bot's
     * nick with NickServ, this method can be used to <i>identify</i> with
     * the supplied password. It usually makes sense to identify with NickServ
     * immediately after connecting to a server.
     * <p/>
     * This method issues a raw NICKSERV command to the server, and is therefore
     * safer than the alternative approach of sending a private message to
     * NickServ. The latter approach is considered dangerous, as it may cause
     * you to inadvertently transmit your password to an untrusted party if you
     * connect to a network which does not run a NickServ service and where the
     * untrusted party has assumed the nick "NickServ".  However, if your IRC
     * network is only compatible with the private message approach, you may
     * typically identify like so:
     * <pre>sendMessage("NickServ", "identify PASSWORD");</pre>
     *
     * @param password The password which will be used to identify with NickServ.
     */
    public final void identify(String password) {
        sendRawLine("NICKSERV IDENTIFY " + password); //TODO THIS WILL BE NEEDED FOR SRL
    }


    /**
     * Adds a line to the log.  This log is currently output to the standard
     * output and is in the correct format for use by tools such as pisg, the
     * Perl IRC Statistics Generator.  You may override this method if you wish
     * to do something else with log entries.
     * Each line in the log begins with a number which
     * represents the logging time (as the number of milliseconds since the
     * epoch).  This timestamp and the following log entry are separated by
     * a single space character, " ".  Outgoing messages are distinguishable
     * by a log entry that has ">>>" immediately following the space character
     * after the timestamp.  DCC events use "+++" and warnings about unhandled
     * Exceptions and Errors use "###".
     * <p/>
     * This implementation of the method will only cause log entries to be
     * output if the PircBot has had its verbose mode turned on by calling
     * setVerbose(true);
     *
     * @param line The line to add to the log.
     */
    public void log(String line) {
        if (_verbose) System.out.println(System.currentTimeMillis() + " " + line);
    }


    /**
     * This method handles events when any line of text arrives from the server,
     * then calling the appropriate method in the PircBot.  This method is
     * protected and only called by the InputThread for this instance.
     * <p/>
     * This method may not be overridden!
     *
     * @param line The raw line of text from the server.
     */
    protected synchronized void handleLine(String line) {
        log(line);

        // Check for server pings.
        if (line.startsWith("PING ")) {
            // Respond to the ping and return immediately.
            onServerPing(line.substring(5));
            return;
        }

        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";

        StringTokenizer tokenizer = new StringTokenizer(line);
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;

        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":")) {
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            } else {
                if (tokenizer.hasMoreTokens()) {
                    int code = -1;
                    try {
                        code = Integer.parseInt(command);
                    } catch (NumberFormatException ignored) {
                    }
                    if (code != -1) {
                        String response = line.substring(line.indexOf(command, senderInfo.length()) + 4, line.length());
                        processServerResponse(code, response);
                        // Return from the method.
                        return;
                    } else {
                        // This is not a server response.
                        // It must be a nick without login and hostname.
                        // (or maybe a NOTICE or suchlike from the server)
                        sourceNick = senderInfo;
                        target = command;
                    }
                } else {
                    // We don't know what this line means.
                    onUnknown(line);
                    // Return from the method;
                    return;
                }
            }
        }

        command = command.toUpperCase();
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }

        String _channelPrefixes = "#&+!";

        if (isSlave) {
            if (command.equals("JOIN")) {
                // Someone is joining a channel.
                parent.addUser(target, new User(sourceNick));
                parent.handler.onJoin(target, sourceNick);
            } else if (command.equals("PART")) {
                // Someone is parting from a channel.
                parent.removeUser(target, sourceNick);
                parent.handler.onPart(target, sourceNick);
            } else if (command.equals("PRIVMSG") && line.contains("SPECIALUSER") && !line.contains("subscriber")) {//this is for setting staff/admin
                parent.handleSpecial(null, line.substring(line.indexOf(" :") + 2));
            }
            return;
        }

        // Check for CTCP requests.
        if (command.equals("PRIVMSG") && line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
            String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
            if (request.startsWith("ACTION ")) {
                // ACTION request
                handler.onAction(sourceNick, target, request.substring(7));
            }
        } else if (command.equals("PRIVMSG") && _channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            if (sourceNick.equalsIgnoreCase("jtv")) {
                if (line.contains("SPECIALUSER")) {
                    handleSpecial(target, line.substring(line.indexOf(" :") + 2));
                }
                if (line.contains("USERCOLOR")) {
                    handleColor(target, line.substring(line.indexOf(" :") + 2));
                }
                if (line.contains("CLEARCHAT")) {
                    handler.onClearChat(target, line.substring(line.indexOf(" :") + 2));
                }
                return;
            }

            //catch the subscriber message
            if (sourceNick.equalsIgnoreCase("twitchnotify")) {
                String user = line.substring(line.indexOf(" :") + 2).split(" ")[0];
                channelManager.handleSubscriber(target, user);
                handler.onNewSubscriber(target, user);
                return;
            }
            // This is a normal message to a channel.
            handler.onMessage(target, sourceNick, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("PRIVMSG")) {
            // This is a private message to us.
            handler.onPrivateMessage(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("QUIT")) {
            // Someone has quit from the IRC server.
            if (sourceNick.equals(getNick())) {
                removeAllChannels();
            } else {
                removeUser(null, sourceNick);
            }
            onQuit(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("MODE")) {
            // Somebody is changing the mode on a channel or user.
            String mode = line.substring(line.indexOf(target, 2) + target.length() + 1);
            if (mode.startsWith(":")) {
                mode = mode.substring(1);
            }
            processMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        } else {
            // If we reach this point, then we've found something that the PircBot
            // Doesn't currently deal with.
            onUnknown(line);
        }

    }

    /**
     * This method is called by the PircBot when a numeric response
     * is received from the IRC server.  We use this method to
     * allow PircBot to process various responses from the server
     * before then passing them on to the onServerResponse method.
     * <p/>
     * Note that this method is private and should not appear in any
     * of the javadoc generated documenation.
     *
     * @param code     The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     */
    private void processServerResponse(int code, String response) {
        if (code == RPL_NAMREPLY) {
            // This is a list of nicks in a channel that we've just joined.
            int channelEndIndex = response.indexOf(" :");
            String channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);

            StringTokenizer tokenizer = new StringTokenizer(response.substring(response.indexOf(" :") + 2));
            while (tokenizer.hasMoreTokens()) {
                String nick = tokenizer.nextToken();
                addUser(channel.toLowerCase(), new User(nick));
            }
        }
        onServerResponse(code, response);
    }


    /**
     * This method is called when we receive a numeric response from the
     * IRC server.
     * <p/>
     * Numerics in the range from 001 to 099 are used for client-server
     * connections only and should never travel between servers.  Replies
     * generated in response to commands are found in the range from 200
     * to 399.  Error replies are found in the range from 400 to 599.
     * <p/>
     * For example, we can use this method to discover the topic of a
     * channel when we join it.  If we join the channel #test which
     * has a topic of &quot;I am King of Test&quot; then the response
     * will be &quot;<code>PircBot #test :I Am King of Test</code>&quot;
     * with a code of 332 to signify that this is a topic.
     * (This is just an example - note that overriding the
     * <code>onTopic</code> method is an easier way of finding the
     * topic for a channel). Check the IRC RFC for the full list of other
     * command response codes.
     * <p/>
     * PircBot implements the interface ReplyConstants, which contains
     * contstants that you may find useful here.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param code     The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     * @see ReplyConstants
     */
    protected void onServerResponse(int code, String response) {
    }


    /**
     * This method is called whenever someone (possibly us) quits from the
     * server.  We will only observe this if the user was in one of the
     * channels to which we are connected.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick     The nick of the user that quit from the server.
     * @param sourceLogin    The login of the user that quit from the server.
     * @param sourceHostname The hostname of the user that quit from the server.
     * @param reason         The reason given for quitting the server.
     */
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    }


    /**
     * Called when the mode of a channel is set.  We process this in
     * order to call the appropriate onOp, onDeop, etc method before
     * finally calling the override-able onMode method.
     * <p/>
     * Note that this method is private and is not intended to appear
     * in the javadoc generated documentation.
     * <p/>
     * YEAH I'M GUESSING IT'S BECAUSE YOU HALF-ASSED IT SO HARD
     *
     * @param target         The channel or nick that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     */
    private void processMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        if (mode.charAt(0) == '#') {
            // The mode of a channel is being changed.
            StringTokenizer tok = new StringTokenizer(mode);
            String[] params = new String[tok.countTokens()];
            if (channelManager == null) channelManager = new ChannelManager();
            if (channelManager.getChannel(target) == null) channelManager.addChannel(new Channel(target));
            int t = 0;
            while (tok.hasMoreTokens()) {
                params[t] = tok.nextToken();
                t++;
            }
            /**
             * #gocnak +o gocnak
             * PARAMS[0] IS THE CHANNEL
             * PARAMS[1] IS THE MODE
             * PARAMS[2] IS THE TARGET
             */
            target = params[0];
            char pn = ' ';
            for (int i = 0; i < params[1].length(); i++) {
                char atPos = params[1].charAt(i);
                if (atPos == '+' || atPos == '-') {
                    pn = atPos;
                } else if (atPos == 'o') {
                    if (pn == '+') {
                        if (channelManager.getUser(target, params[2]) == null) {
                            channelManager.getChannel(target).addUser(new User(params[2]));
                        }
                        channelManager.getUser(target, params[2]).setOp(true);
                        handler.onOp(target, params[2]);
                    } else {
                        if (channelManager.getUser(target, params[2]) != null) {
                            channelManager.getUser(target, params[2]).setOp(false);
                        }
                        handler.onDeop(target, params[2]);
                    }
                }
            }
            onMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        } else {
            // The mode of a user is being changed.
            onUserMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        }
    }


    /**
     * Called when the mode of a channel is set.
     * <p/>
     * You may find it more convenient to decode the meaning of the mode
     * string by overriding the onOp, onDeOp, onVoice, onDeVoice,
     * onChannelKey, onDeChannelKey, onChannelLimit, onDeChannelLimit,
     * onChannelBan or onDeChannelBan methods as appropriate.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     */
    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }


    /**
     * Called when the mode of a user is set.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param targetNick     The nick that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     * @since PircBot 1.2.0
     */
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }


    /**
     * The actions to perform when a PING request comes from the server.
     * <p/>
     * This sends back a correct response, so if you override this method,
     * be sure to either mimic its functionality or to call
     * super.onServerPing(response);
     *
     * @param response The response that should be given back in your PONG.
     */
    protected void onServerPing(String response) {
        sendRawLine("PONG " + response);
    }


    /**
     * This method is called whenever we receive a line from the server that
     * the PircBot has not been programmed to recognise.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param line The raw line that was received from the server.
     */
    protected void onUnknown(String line) {
        // And then there were none :)
    }


    /**
     * Sets the verbose mode. If verbose mode is set to true, then log entries
     * will be printed to the standard output. The default value is false and
     * will result in no output. For general development, we strongly recommend
     * setting the verbose mode to true.
     *
     * @param verbose true if verbose mode is to be used.  Default is false.
     */
    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }


    /**
     * Sets the internal nick of the bot.  This is only to be called by the
     * PircBot class in response to notification of nick changes that apply
     * to us.
     *
     * @param nick The new nick.
     */
    public void setNick(String nick) {
        _nick = nick;
    }

    public void setPassword(String password) {
        _password = password;
    }


    /**
     * Sets the internal version of the Bot.  This should be set before joining
     * any servers.
     *
     * @param version The new version of the Bot.
     */
    public void setVersion(String version) {
        _version = version;
    }


    /**
     * Returns the current nick of the bot. Note that if you have just changed
     * your nick, this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p/>
     * The nick returned by this method is maintained only by the PircBot
     * class and is guaranteed to be correct in the context of the IRC server.
     *
     * @return The current nick of the bot.
     * @since PircBot 1.0.0
     */
    public String getNick() {
        return _nick;
    }


    /**
     * Gets the internal version of the PircBot.
     *
     * @return The version of the PircBot.
     */
    public String getVersion() {
        return _version;
    }


    /**
     * Returns whether or not the PircBot is currently connected to a server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     *
     * @return True if and only if the PircBot is currently connected to a server.
     */
    public final synchronized boolean isConnected() {
        return _inputThread != null && _inputThread.isConnected();
    }


    /**
     * Sets the number of milliseconds to delay between consecutive
     * messages when there are multiple messages waiting in the
     * outgoing message queue.  This has a default value of 1000ms.
     * It is a good idea to stick to this default value, as it will
     * prevent your bot from spamming servers and facing the subsequent
     * wrath!  However, if you do need to change this delay value (<b>not
     * recommended</b>), then this is the method to use.
     *
     * @param delay The number of milliseconds between each outgoing message.
     */
    public final void setMessageDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Cannot have a negative time.");
        }
        _messageDelay = delay;
    }


    /**
     * Returns the number of milliseconds that will be used to separate
     * consecutive messages to the server from the outgoing message queue.
     *
     * @return Number of milliseconds.
     */
    public final long getMessageDelay() {
        return _messageDelay;
    }


    /**
     * Gets the number of lines currently waiting in the outgoing message Queue.
     * If this returns 0, then the Queue is empty and any new message is likely
     * to be sent to the IRC server immediately.
     *
     * @return The number of lines in the outgoing message Queue.
     * @since PircBot 0.9.9
     */
    public final int getOutgoingQueueSize() {
        return _outQueue.size();
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
        return _port;
    }


    /**
     * Returns the last password that we used when connecting to an IRC server.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server using a password.
     *
     * @return The last password that we used when connecting to an IRC server.
     * Returns null if we have not previously connected using a password.
     * @since PircBot 0.9.9
     */
    public final String getPassword() {
        return _password;
    }


    /**
     * A convenient method that accepts an IP address represented as a
     * long and returns an integer array of size 4 representing the same
     * IP address.
     *
     * @param address the long value representing the IP address.
     * @return An int[] of size 4.
     * @since PircBot 0.9.4
     */
    public int[] longToIp(long address) {
        int[] ip = new int[4];
        for (int i = 3; i >= 0; i--) {
            ip[i] = (int) (address % 256);
            address = address / 256;
        }
        return ip;
    }


    /**
     * A convenient method that accepts an IP address represented by a byte[]
     * of size 4 and returns this as a long representation of the same IP
     * address.
     *
     * @param address the byte[] of size 4 representing the IP address.
     * @return a long representation of the IP address.
     * @since PircBot 0.9.4
     */
    public long ipToLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("byte array must be of length 4");
        }
        long ipNum = 0;
        long multiplier = 1;
        for (int i = 3; i >= 0; i--) {
            int byteVal = (address[i] + 256) % 256;
            ipNum += byteVal * multiplier;
            multiplier *= 256;
        }
        return ipNum;
    }


    /**
     * Returns the encoding used to send and receive lines from
     * the IRC server, or null if not set.  Use the setEncoding
     * method to change the encoding charset.
     *
     * @return The encoding used to send outgoing messages, or
     * null if not set.
     * @since PircBot 1.0.4
     */
    public String getEncoding() {
        return "UTF-8";
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


    /**
     * Returns true if and only if the object being compared is the exact
     * same instance as this PircBot. This may be useful if you are writing
     * a multiple server IRC bot that uses more than one instance of PircBot.
     *
     * @return true if and only if Object o is a PircBot and equal to this.
     * @since PircBot 0.9.9
     */
    public boolean equals(Object o) {
        if (o instanceof PircBot) {
            PircBot other = (PircBot) o;
            return this.getNick().equals(other.getNick()) && this.getPassword().equals(other.getPassword());
        }
        return false;
    }


    /**
     * Returns the hashCode of this PircBot. This method can be called by hashed
     * collection classes and is useful for managing multiple instances of
     * PircBots in such collections.
     *
     * @return the hash code for this instance of PircBot.
     * @since PircBot 0.9.9
     */
    public int hashCode() {
        return super.hashCode();
    }


    /**
     * Returns a String representation of this object.
     * You may find this useful for debugging purposes, particularly
     * if you are using more than one PircBot instance to achieve
     * multiple server connectivity. The format of
     * this String may change between different versions of PircBot
     * but is currently something of the form
     * <code>
     * Version{PircBot x.y.z Java IRC Bot - www.jibble.org}
     * Connected{true}
     * Server{irc.dal.net}
     * Port{6667}
     * Password{}
     * </code>
     *
     * @return a String representation of this object.
     * @since PircBot 0.9.10
     */
    public String toString() {
        return "Version{" + _version + "}" +
                " Connected{" + isConnected() + "}" +
                " Server{" + _server + "}" +
                " Port{" + _port + "}" +
                " Password{" + _password + "}";
    }


    /**
     * Returns an array of all users in the specified channel.
     * <p/>
     * There are some important things to note about this method:-
     * <ul>
     * <li>This method may not return a full list of users if you call it
     * before the complete nick list has arrived from the IRC server.
     * </li>
     * <li>If you wish to find out which users are in a channel as soon
     * as you join it, then you should override the onUserList method
     * instead of calling this method, as the onUserList method is only
     * called as soon as the full user list has been received.
     * </li>
     * <li>This method will return immediately, as it does not require any
     * interaction with the IRC server.
     * </li>
     * <li>The bot must be in a channel to be able to know which users are
     * in it.
     * </li>
     * </ul>
     *
     * @param channel The name of the channel to list.
     * @return An array of User objects. This array is empty if we are not
     * in the channel.
     * @since PircBot 1.0.0
     */
    public synchronized final User[] getUsers(String channel) {
        return isInChannel(channel) ? channelManager.getChannel(channel).getUsers() : new User[]{};
    }

    public synchronized User getUser(String channel, String user) {
        if (!channel.contains("#")) channel = "#" + channel;
        return getChannelManager().getUser(channel, user);
    }


    /**
     * Returns an array of all channels that we are in.  Note that if you
     * call this method immediately after joining a new channel, the new
     * channel may not appear in this array as it is not possible to tell
     * if the join was successful until a response is received from the
     * IRC server.
     *
     * @return A String array containing the names of all channels that we
     * are in.
     * @since PircBot 1.0.0
     */
    public synchronized final String[] getChannels() {
        return channelManager.getChannelNames();
    }

    /**
     * Gets the channel object of the given String name.
     *
     * @param name The name of the channel.
     * @return The channel object or null if it doesn't exist.
     */
    public synchronized Channel getChannel(String name) {
        if (channelManager == null) return null;
        else return channelManager.getChannel(name);
    }

    public synchronized boolean isInChannel(String channel) {
        if (!channel.contains("#")) channel = "#" + channel;
        return getChannel(channel) != null;
    }

    /**
     * Disposes of all thread resources used by this PircBot. This may be
     * useful when writing bots or clients that use multiple servers (and
     * therefore multiple PircBot instances) or when integrating a PircBot
     * with an existing program.
     * <p/>
     * Each PircBot runs its own threads for dispatching messages from its
     * outgoing message queue and receiving messages from the server.
     * Calling dispose() ensures that these threads are
     * stopped, thus freeing up system resources and allowing the PircBot
     * object to be garbage collected if there are no other references to
     * it.
     * <p/>
     * Once a PircBot object has been disposed, it should not be used again.
     * Attempting to use a PircBot that has been disposed may result in
     * unpredictable behaviour.
     *
     * @since 1.2.2
     */
    public synchronized void dispose() {
        if (_outputThread != null) _outputThread.interrupt();
        if (_inputThread != null) _inputThread.dispose();
        if (child != null) child.dispose();
        if (channelManager != null) channelManager.dispose();
    }

    /**
     * Determines if a user is admin, staff, turbo, or subscriber and sets their prefix accordingly.
     *
     * @param line The line to parse.
     */
    public synchronized void handleSpecial(String channel, String line) {
        if (line != null) {//SPECIALUSER name type
            String[] split = line.split(" ");
            String user = split[1].toLowerCase();
            if (split[2].equalsIgnoreCase("admin")) {
                channelManager.updateUser(ADMIN, user);
            }
            if (split[2].equalsIgnoreCase("staff")) {
                channelManager.updateUser(STAFF, user);
            }
            if (split[2].equalsIgnoreCase("turbo")) {
                channelManager.updateUser(TURBO, user);
            }
            if (split[2].equalsIgnoreCase("subscriber")) {
                channelManager.handleSubscriber(channel, user);
            }
        }
    }

    public synchronized void handleColor(String channel, String line) {
        if (channel != null && line != null) {
            String[] split = line.split(" ");
            String user = split[1];
            String color = split[2];
            Color c = null;
            try {
                c = Color.decode(color);
            } catch (Exception ignored) {
            }
            User u = getUser(channel, user);
            if (u == null) {
                u = new User(user);
                u.setColor(c);
                addUser(channel, u);
            } else {
                u.setColor(c);
            }
        }
    }

    /**
     * Add a user to the specified channel in our memory.
     */
    private synchronized void addUser(String channel, User user) {
        getChannelManager().addUser(channel, user);
    }

    private synchronized void removeUser(String chnl, String nick) {
        getChannelManager().removeUser(chnl, nick);
    }

    /**
     * Removes an entire channel from our memory of users.
     */
    private synchronized void removeChannel(String channel) {
        channelManager.removeChannel(channel);
    }

    /**
     * Removes all channels from our memory of users.
     */
    private synchronized void removeAllChannels() {
        if (channelManager != null) channelManager.dispose();
    }


    // Connection stuff.
    private InputThread _inputThread = null;
    private OutputThread _outputThread = null;
    private InetAddress _inetAddress = null;

    // Details about the last server that we connected to.
    private String _server = null;
    private int _port = -1;
    private String _password = null;

    // Outgoing message stuff.
    private Queue<String> _outQueue = new Queue<>();
    private long _messageDelay = 1000;

    // Default settings for the PircBot.
    private boolean _verbose = false;
    private String _nick = null;
    private String _version = "Botnak " + Constants.VERSION;

}
