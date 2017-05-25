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

import face.FaceManager;
import gui.forms.GUIMain;
import irc.message.MessageHandler;
import util.Constants;
import util.Utils;
import util.settings.Settings;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * PircBot is a Java framework for writing IRC bots quickly and easily.
 * <p>
 * It provides an event-driven architecture to handle common IRC
 * events, flood protection, DCC support, ident support, and more.
 * The comprehensive logfile format is suitable for use with pisg to generate
 * channel statistics.
 * <p>
 * Methods of the PircBot class can be called to send events to the IRC server
 * that it connects to.  For example, calling the sendMessage method will
 * send a message to a channel or user on the IRC server.  Multiple servers
 * can be supported using multiple instances of PircBot.
 * <p>
 * To perform an action when the PircBot receives a normal message from the IRC
 * server, you would override the onMessage method defined in the PircBot
 * class.  All on<i>XYZ</i> methods in the PircBot class are automatically called
 * when the event <i>XYZ</i> happens, so you would override these if you wish
 * to do something when it does happen.
 * <p>
 * Some event methods, such as onPing, should only really perform a specific
 * function (i.e. respond to a PING from the server).  For your convenience, such
 * methods are already correctly implemented in the PircBot and should not
 * normally need to be overridden.  Please read the full documentation for each
 * method to see which ones are already implemented by the PircBot class.
 * <p>
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
public class PircBot {

    private PircBotConnection connection;

    public ChannelManager getChannelManager() {
        return Settings.channelManager;
    }

    private MessageHandler handler;

    public MessageHandler getMessageHandler() {
        return handler;
    }

    /**
     * Constructs a PircBot with the default settings.  Your own constructors
     * in classes which extend the PircBot abstract class should be responsible
     * for changing the default settings if required.
     */
    public PircBot(MessageHandler messageHandler) {
        handler = messageHandler;
        connection = new PircBotConnection(this, PircBotConnection.ConnectionType.NORMAL);
    }

    /**
     * Attempt to connect to the specified IRC server using the supplied
     * password.
     * The onConnect method is called upon success.
     *
     * @param hostname The hostname of the server to connect to.
     * @param port     The port number to connect to on the server.
     */
    public boolean connect() {
        if (connection.connect()) {
            getMessageHandler().onConnect();
            return true;
        }
        return false;
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
    public void disconnect() {
        quitServer();
    }


    /**
     * Joins a channel.
     *
     * @param channel The name of the channel to join (eg "#cs").
     */
    public void joinChannel(String channel) {
        sendRawLine("JOIN " + channel);
        getChannelManager().addChannel(new Channel(channel));
    }


    /**
     * Parts a channel.
     *
     * @param channel The name of the channel to leave.
     */
    public void partChannel(String channel) {
        sendRawLine("PART " + channel);
        getChannelManager().removeChannel(channel);
    }


    /**
     * Quits from the IRC server.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     */
    public void quitServer() {
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
    public void quitServer(String reason) {
        sendRawLine("QUIT :" + reason);
    }


    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    public void sendRawLine(String line) {
        if (isConnected()) {
            connection.getOutputThread().sendRawLine(line);
        }
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public PircBotConnection getConnection() {
        return connection;
    }

    /**
     * Sends a message to a channel or a private message to a user.  These
     * messages are added to the outgoing message queue and sent at the
     * earliest possible opportunity.
     * <p>
     * Some examples: -
     * <pre>    // Send the message "Hello!" to the channel #cs.
     *    sendMessage("#cs", "Hello!");
     * <p>
     *    // Send a private message to Paul that says "Hi".
     *    sendMessage("Paul", "Hi");</pre>
     * <p>
     * You may optionally apply colours, boldness, underlining, etc to
     * the message by using the <code>Colors</code> class.
     *
     * @param target  The name of the channel or user nick to send to.
     * @param message The message to send.
     */
    public void sendMessage(String target, String message) {
        if (message.startsWith("/w")) {
            String[] split = message.split(" ", 3);
            sendWhisper(split[1], split[2]);
            getMessageHandler().onWhisper(getNick(), split[1], split[2]);
        } else {
            sendRawMessage(target, message);
            if (message.startsWith("/me")) {
                getMessageHandler().onAction(getNick(), target, message.substring(4));
            } else {
                getMessageHandler().onMessage(target, getNick(), message);
            }
        }
    }


    public void sendWhisper(String target, String message) {
        sendRawWhisper("/w " + target + " " + message);
    }

    public void sendRawWhisper(String raw) {
        if (isConnected())
            connection.getOutQueue().add("PRIVMSG #jtv :" + raw);
        else log("Whisper not connected!");
    }

    /**
     * Sends a message that does not show up in the main GUI.
     *
     * @param channel The channel to send to.
     * @param message The message to send.
     */
    public void sendRawMessage(String channel, String message) {
        if (isConnected())
            connection.getOutQueue().add("PRIVMSG " + channel + " :" + message);
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
     * <p>
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
     * then calling the appropriate method in the PircBot.
     *
     * @param line The raw line of text from the server.
     */
    public void handleLine(String line) {
        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";
        StringTokenizer tokenizer = new StringTokenizer(line);
        String tags = null;
        String content = null;
        if (line.startsWith("@")) {
            tags = tokenizer.nextToken();
            if (line.contains("USERSTATE")) {
                parseUserstate(line);
                return;
            } else {
                content = line.substring(line.indexOf(" :", line.indexOf(" :") + 2) + 2);
            }
        } else {
            content = line.substring(line.indexOf(" :") + 2);
        }
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;

        if (checkCommand(command, tags, line, content, tokenizer, senderInfo)) return;

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
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        command = command.toUpperCase();
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }
        String _channelPrefixes = "#&+!";

        parseTags(tags, sourceNick, target);
        // Check for CTCP requests.
        if ("PRIVMSG".equals(command) && line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
            String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
            if (request.startsWith("ACTION ")) {
                // ACTION request
                getMessageHandler().onAction(sourceNick, target, request.substring(7));
            }
        } else if (command.equals("PRIVMSG") && _channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            //This message is a cheer message!
            if (tags != null && tags.contains("bits="))
            {
                HashMap<String, String> tagsMap = Utils.parseTagsToMap(tags);
                if (!tagsMap.isEmpty())
                {
                    int bitAmount = Integer.parseInt(tagsMap.get("bits"));
                    getMessageHandler().onCheer(target, sourceNick, bitAmount, content);
                }
                return;
            } else
                // This is a normal message to a channel.
                getMessageHandler().onMessage(target, sourceNick, content);
        } else if ("PRIVMSG".equals(command)) {
            if (sourceNick.equals("jtv")) {
                if (line.contains("now hosting you")) {
                    getMessageHandler().onBeingHosted(content);//KEEP THIS
                }
            }
            // This is a private message to us.
            getMessageHandler().onPrivateMessage(sourceNick, sourceLogin, sourceHostname, content);
        } else {
            // If we reach this point, then we've found something that the PircBot
            // Doesn't currently deal with.
            onUnknown(line);
        }
    }

    private boolean checkCommand(String command, String tags, String line, String content, StringTokenizer tokenizer, String senderInfo) {
        String target;
        HashMap<String, String> tagsMap = Utils.parseTagsToMap(tags);
        switch (command)
        {
            case "CLEARCHAT":
                target = tokenizer.nextToken();
                if (tagsMap.isEmpty())
                    getMessageHandler().onClearChat(target);
                else if (!tagsMap.containsKey("ban-duration"))
                    getMessageHandler().onUserPermaBanned(target, content, tagsMap.get("ban-reason"));
                else
                    getMessageHandler().onUserTimedOut(target, content, Integer.parseInt(tagsMap.get("ban-duration")), tagsMap.get("ban-reason"));
                return true;
            case "HOSTTARGET":
                target = tokenizer.nextToken();
                String[] split = content.split(" ");
                getMessageHandler().onHosting(target.substring(1), split[0], split[1]);
                return true;
            case "ROOMSTATE":
                target = tokenizer.nextToken();
                getMessageHandler().onRoomstate(target, tags);
                parseTags(null, target, tagsMap);
                return true;
            case "NOTICE":
                if (tags.contains("room_mods"))
                {
                    target = tokenizer.nextToken();
                    buildMods(target, content);
                    return true;
                } else if (!tags.contains("host_on") && !tags.contains("host_off"))
                {//handled above
                    target = tokenizer.nextToken();
                    getMessageHandler().onJTVMessage(target.substring(1), content, tags);
                    return true;
                }
                break;
            case "WHISPER":
                target = tokenizer.nextToken();
                String nick = senderInfo.substring(1, senderInfo.indexOf('!'));
                parseTags(nick, null, tagsMap);
                getMessageHandler().onWhisper(nick, target, content);
                return true;
            case "RECONNECT"://We need to reconnect to this server
                GUIMain.logCurrent("Detected a RECONNECT command, currently reconnecting the connection for: " + _nick + "!");
                getConnection().dispose();
                Settings.accountManager.createReconnectThread(getConnection());
                return true;
            case "USERNOTICE": //User has (re)subscribed to this channel (for X months)
                target = tokenizer.nextToken();
                String user = tagsMap.get("login");
                parseTags(user, target, tagsMap);
                String type = tagsMap.get("msg-id");
                if (type != null)
                {
                    if ("sub".equals(type))
                    {
                        // A new sub??
                        getChannelManager().handleSubscriber(target, user);
                        getMessageHandler().onNewSubscriber(target, tagsMap.get("system-msg"), user);
                    } else if ("resub".equals(type))
                    {
                        getMessageHandler().onResubscribe(target, user, tagsMap.get("system-msg"));
                    }
                }

                //Only send their message if there is one
                if (content != null && line.indexOf(" :", line.indexOf(" :") + 2) > -1)
                    getMessageHandler().onMessage(target, user, content);
                return true;
            default:
                return false;
        }
        return false;
    }

    private void parseUserstate(String line) {
        String[] parts = line.split(" ");
        String tags = parts[0];
        String channel = parts[3];
        parseTags(tags, getNick(), channel);
    }

    private void parseTags(String line, String user, String channel) {
        parseTags(user, channel, Utils.parseTagsToMap(line));
    }

    private void parseTags(String user, String channel, HashMap<String, String> tags)
    {
        if (!tags.isEmpty())
        {
            Set<Map.Entry<String, String>> entries = tags.entrySet();
            for (Map.Entry<String, String> tag : entries)
            {
                switch (tag.getKey())
                {
                    case "color":
                        handleColor(tag.getValue(), user);
                        break;
                    case "display-name":
                        handleDisplayName(tag.getValue(), user);
                        break;
                    case "emotes":
                        handleEmotes(tag.getValue(), user);
                        break;
                    case "subscriber":
                        if ("1".equals(tag.getValue()))
                        {
                            handleSpecial(channel, tag.getKey(), user);
                        }
                        break;
                    case "turbo":
                        if ("1".equals(tag.getValue()))
                        {
                            handleSpecial(null, tag.getKey(), user);
                        }
                        break;
                    case "user-type":
                        handleSpecial(channel, tag.getValue(), user);
                        break;
                    case "r9k":
                        if ("1".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel, "This room is in r9k mode.", tag.getKey());
                        }
                        break;
                    case "slow":
                        if (!"0".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel,
                                    "This room is in slow mode. You may send messages every " + tag.getValue() + " seconds.", tag.getKey());
                        }
                        break;
                    case "subs-only":
                        if ("1".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel, "This room is in subscribers-only mode.", tag.getKey());
                        }
                        break;
                    case "emote-sets":
                        FaceManager.handleEmoteSet(tag.getValue());
                        break;
                    case "badges": // Although user-type handles most of this, we need it for bits status
                        String badges = tag.getValue();
                        // Bit donor
                        if (badges.contains("bits"))
                        {
                            Matcher m = Constants.PATTERN_BITS.matcher(badges);
                            if (m.find())
                                getChannelManager().getChannel(channel).setCheer(user, Integer.parseInt(m.group(1)));
                        }
                        // Prime
                        if (badges.contains("premium"))
                            getChannelManager().getUser(user, true).setPrime(true);
                        // Verified
                        if (badges.contains("partner"))
                            getChannelManager().getUser(user, true).setVerified(true);
                        break;
                    case "bits":
                        //This message contains a cheer!
                        //This is handled above, don't worry.
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void buildMods(String channel, String line) {
        if (!line.equals("")) {
            String init = line.substring(line.indexOf(":") + 1);
            String[] upMods = init.replaceAll(" ", "").split(",");
            getChannelManager().getChannel(channel).addMods(upMods);
        }
    }

    /**
     * Determines if a user is admin, staff, turbo, or subscriber and sets their prefix accordingly.
     *
     * @param channel The channel it's for
     * @param type    The user type
     * @param user    The user
     */
    public void handleSpecial(String channel, String type, String user) {
        if (user != null) {
            Channel c = getChannelManager().getChannel(channel);
            switch (type) {
                case "mod":
                    if (c != null) c.addMods(user);
                    break;
                case "subscriber":
                    if (c != null) c.addSubscriber(user);
                    break;
                case "turbo":
                    getChannelManager().getUser(user, true).setTurbo(true);
                    break;
                case "admin":
                    getChannelManager().getUser(user, true).setAdmin(true);
                    break;
                case "global_mod":
                    getChannelManager().getUser(user, true).setGlobalMod(true);
                    break;
                case "staff":
                    getChannelManager().getUser(user, true).setStaff(true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This method is called by the PircBot when a numeric response
     * is received from the IRC server.  We use this method to
     * allow PircBot to process various responses from the server
     * before then passing them on to the onServerResponse method.
     * <p>
     * Note that this method is private and should not appear in any
     * of the javadoc generated documenation.
     *
     * @param code     The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     */
    public void processServerResponse(int code, String response) {
        if (code == 366) {//"END OF NAMES"
            int channelEndIndex = response.indexOf(" :");
            String channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);
            sendRawMessage(channel, ".mods");//start building mod list
        }
    }

    /**
     * This method is called whenever we receive a line from the server that
     * the PircBot has not been programmed to recognise.
     * <p>
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
        if (connection != null)
            connection.setName(_nick);
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
     * <p>
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
                " Connected{" + connection.isConnected() + "}" +
                " Server{" + connection.getServer() + "}" +
                " Port{" + connection.getPort() + "}" +
                " Password{" + _password + "}";
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
    public final String[] getChannels() {
        return getChannelManager().getChannelNames();
    }

    public void dispose() {
        if (connection != null) connection.dispose();
    }

    public void handleColor(String color, String user) {
        if (color != null) {
            Color c;
            try {
                c = Color.decode(color);
            } catch (Exception ignored) {
                return;
            }
            getChannelManager().getUser(user, true).setColor(c);
        }
    }

    public void handleDisplayName(String name, String user) {
        if (name != null) {
            getChannelManager().getUser(user, true).setDisplayName(name.replaceAll("\\\\s", " ").trim());
        }
    }

    public void handleEmotes(String numbers, String user) {
        try {
            String[] parts = numbers.split("/");
            User u = getChannelManager().getUser(user, true);
            for (String emote : parts) {
                String emoteID = emote.split(":")[0];
                try {
                    int id = Integer.parseInt(emoteID);
                    u.addEmote(id);
                } catch (Exception e) {
                    GUIMain.log("Cannot parse emote ID given by IRCv3 tags!");
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String _password = null;

    // Outgoing message stuff.
    private long _messageDelay = 1000;

    // Default settings for the PircBot.
    private boolean _verbose = false;
    private String _nick = null;
    private String _version = "Botnak " + Constants.VERSION;
}