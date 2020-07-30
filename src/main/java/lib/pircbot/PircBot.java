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

import com.sun.istack.internal.Nullable;
import face.FaceManager;
import gui.forms.GUIMain;
import irc.message.MessageHandler;
import util.Constants;
import util.Utils;
import util.settings.Settings;

import java.awt.*;
import java.util.*;
import java.util.List;
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
     * @return true if we successfully connect, otherwise false
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
            //getMessageHandler().onWhisper(getNick(), split[1], split[2]);
        } else {
            sendRawMessage(target, message);
            if (message.startsWith("/me")) {
                getMessageHandler().onAction(_userID, target, message.substring(4));
            } else {
                getMessageHandler().onMessage(target, _userID, message);
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
    public void handleLine(String line)
    {
        StringTokenizer tokenizer = new StringTokenizer(line);
        HashMap<String, String> tagsMap = new HashMap<>();
        String content = null;
        User senderUser = null;
        if (line.startsWith("@"))
        {
            Utils.parseTagsToMap(tokenizer.nextToken().substring(1), tagsMap);

            // This aims to be the central "user-creator" method, or if the user exists,
            // user-getter
            if (tagsMap.containsKey("user-id"))
            {
                long senderID = Long.parseLong(tagsMap.get("user-id"));
                senderUser = getChannelManager().getUser(senderID, true);
            }

            if (line.contains("GLOBALUSERSTATE") && senderUser != null)
            {
                setUserID(senderUser.getUserID());
                senderUser.setNick(_nick);
                handleTags(senderUser, null, tagsMap);
                return;
            } else if (line.contains("USERSTATE"))
            {
                String[] parts = line.split(" ");
                String channel = parts[3];
                handleTags(getChannelManager().getUser(_userID, true), channel, tagsMap);
                return;
            } else
            {
                content = line.substring(line.indexOf(" :", line.indexOf(" :") + 2) + 2);
            }
        } else {
            content = line.substring(line.indexOf(" :") + 2);
        }
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;
        String sourceNick = "";

        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":"))
        {
            if (exclamation > 0 && at > 0 && exclamation < at)
            {
                sourceNick = senderInfo.substring(1, exclamation);
            }
            else if (tokenizer.hasMoreTokens())
            {
                try
                {
                    int code = Integer.parseInt(command);
                    String response = line.substring(line.indexOf(command, senderInfo.length()) + 4, line.length());
                    processServerResponse(code, response);
                    return;
                } catch (NumberFormatException ignored)
                {
                    sourceNick = senderInfo;
                    //target = command;
                }
            }
        }

        if (target == null && tokenizer.hasMoreTokens())
        {
            target = tokenizer.nextToken();
            if (target.startsWith(":"))
            {
                target = target.substring(1);
            }
        }

        handleTags(senderUser, target, tagsMap);

        if (senderUser != null)
        {
            // NOTE: The user will either already have a nick from the handleTags method above
            // or will need it set here.
            senderUser.setNick(sourceNick);
        }

        command = command.toUpperCase();
        switch (command)
        {
            case "CLEARCHAT":
                if (tagsMap.isEmpty())
                    getMessageHandler().onClearChat(target);
                else if (!tagsMap.containsKey("ban-duration"))
                    getMessageHandler().onUserPermaBanned(target, content, tagsMap.get("ban-reason"));
                else
                    getMessageHandler().onUserTimedOut(target, content, Integer.parseInt(tagsMap.get("ban-duration")), tagsMap.get("ban-reason"));
                return;
            case "HOSTTARGET":
                String[] split = content.split(" ");
                getMessageHandler().onHosting(target.substring(1), split[0], split[1]);
                return;
            case "ROOMSTATE":
                getMessageHandler().onRoomstate(target, tagsMap);
                return;
            case "NOTICE":
                if (tagsMap.containsValue("room_mods"))
                {
                    buildMods(target, content);
                    return;
                }
                // Host messages are handled above in HOSTTARGET
                else if (!tagsMap.containsValue("host_on") && !tagsMap.containsValue("host_off"))
                {
                    getMessageHandler().onJTVMessage(target.substring(1), content, tagsMap);
                    return;
                }
                break;
            case "WHISPER":
                getMessageHandler().onWhisper(senderUser.getUserID(), target, content);
                return;
            case "RECONNECT"://We need to reconnect to this server
                GUIMain.logCurrent("Detected a RECONNECT command, currently reconnecting the connection for: " + _nick + "!");
                getConnection().dispose();
                Settings.accountManager.createReconnectThread(getConnection());
                return;
            case "USERNOTICE": //User has (re)subscribed to this channel (for X months)
                String type = tagsMap.get("msg-id");
                if (type != null)
                {
                    if ("sub".equals(type))
                    {
                        // A new sub??
                        getChannelManager().handleSubscriber(target, senderUser.getUserID());
                        getMessageHandler().onNewSubscriber(target, tagsMap.get("system-msg"), senderUser);
                    }
                    else if ("resub".equals(type))
                    {
                        getChannelManager().handleSubscriber(target, senderUser.getUserID());
                        getMessageHandler().onResubscribe(target, senderUser.getUserID(), tagsMap.get("system-msg"));
                    }
                    else if ("raid".equals(type))
                    {
                        getMessageHandler().onBeingRaided(tagsMap.get("msg-param-displayName"),
                                Integer.parseInt(tagsMap.getOrDefault("msg-param-viewerCount", "0")));
                    }
                }

                //Only send their message if there is one
                if (content != null && line.indexOf(" :", line.indexOf(" :") + 2) > -1)
                    getMessageHandler().onMessage(target, senderUser.getUserID(), content);
                return;
            default:
                break;
        }


        // Check for CTCP requests.
        int unicodeIndex = line.indexOf(":\u0001");
        if ("PRIVMSG".equals(command) && unicodeIndex > 0 && line.endsWith("\u0001"))
        {
            String request = line.substring(unicodeIndex + 2, line.length() - 1);
            if (request.startsWith("ACTION "))
            {
                // ACTION request
                getMessageHandler().onAction(senderUser.getUserID(), target, request.substring(7));
            }
        } else if (command.equals("PRIVMSG") && target.charAt(0) == '#')
        {
            //This message is a cheer message!
            if (tagsMap.containsKey("bits"))
            {
                int bitAmount = Integer.parseInt(tagsMap.get("bits"));
                getMessageHandler().onCheer(target, senderUser, bitAmount, content);
                return;
            } else // This is a normal message to a channel.
                getMessageHandler().onMessage(target, senderUser.getUserID(), content);
        } else if ("PRIVMSG".equals(command))
        {
            if (sourceNick.equals("jtv"))
            {
                if (line.contains("now hosting you"))
                {
                    getMessageHandler().onBeingHosted(content);//KEEP THIS
                }
            }
            // This is a private message to us.
            getMessageHandler().onPrivateMessage(sourceNick, content);
        }
    }

    /**
     * Handles the tags parsed from Twitch
     *
     * @param user (Can be null) The user to modify, if present
     * @param channel  (Can be null) The channel the tags are for, if present
     * @param tags The map of parsed tags
     */
    private void handleTags(User user, String channel, Map<String, String> tags)
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
                        user.setDisplayName(tag.getValue().replaceAll("\\\\s", " ").trim());
                        break;
                    case "emotes":
                        handleEmotes(tag.getValue(), user);
                        break;
                    case "r9k":
                        if ("1".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel, "This room is in r9k mode.", tags);
                        }
                        break;
                    case "slow":
                        if (!"0".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel,
                                    "This room is in slow mode. You may send messages every " + tag.getValue() + " seconds.", tags);
                        }
                        break;
                    case "subs-only":
                        if ("1".equals(tag.getValue()))
                        {
                            getMessageHandler().onJTVMessage(channel, "This room is in subscribers-only mode.", tags);
                        }
                        break;
                    case "emote-sets":
                        FaceManager.handleEmoteSet(tag.getValue());
                        break;
                    case "badges":
                        String badges = tag.getValue();
                        // Bit donor
                        if (badges.contains("bits"))
                        {
                            Matcher m = Constants.PATTERN_BITS.matcher(badges);
                            if (m.find())
                                user.setCheer(channel, Integer.parseInt(m.group(1)));
                        }
                        // Prime
                        if (badges.contains("premium"))
                            user.setPrime(true);

                        // Verified
                        if (badges.contains("partner"))
                            user.setVerified(true);
                        
                        if (badges.contains("admin"))
                            user.setAdmin(true);

                        if (badges.contains("staff"))
                            user.setStaff(true);

                        Channel c = getChannelManager().getChannel(channel);
                        if (c != null)
                        {
                            if (badges.contains("vip"))
                                c.addVIP(user.getUserID());

                            if (badges.contains("mod"))
                                c.addMods(user.getNick());

                            if (badges.contains("subscriber") || badges.contains("founder"))
                                c.addSubscriber(user.getUserID());
                        }
                        break;
                    case "bits":
                        //This message contains a cheer!
                        //This is handled above, don't worry.
                        break;
                    case "login":
                        if (user != null)
                            user.setNick(tag.getValue());
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


    public void setUserID(long _userID)
    {
        this._userID = _userID;
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

    public long getUserID()
    {
        return _userID;
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
     * @return A String list containing the names of all channels that we
     * are in.
     * @since PircBot 1.0.0
     */
    public final List<String> getChannels() {
        return getChannelManager().getChannelNames();
    }

    public void dispose() {
        if (connection != null) connection.dispose();
    }

    public void handleColor(String color, User user)
    {
        if (color != null) {
            try {
                user.setColor(Color.decode(color));
            } catch (Exception ignored) {
                return;
            }
        }
    }

    public void handleEmotes(String numbers, User user)
    {
        try {
            String[] parts = numbers.split("/");
            for (String emote : parts) {
                String emoteID = emote.split(":")[0];
                try {
                    int id = Integer.parseInt(emoteID);
                    user.addEmote(id);
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
    private long _userID = -1L;
    private String _version = "Botnak " + Constants.VERSION;
}