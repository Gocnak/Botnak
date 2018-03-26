package irc.message;

import lib.pircbot.User;
import util.settings.Settings;

/**
 * Created by Nick on 3/21/2014.
 */
public class Message {

    private MessageType type = null;
    private String content = null;
    private String channel = null;
    private String senderName = null;
    private long senderID = -1L;
    private Object extra = null;

    /**
     * Constructs a default, blank message.
     */
    public Message() {

    }

    /**
     * Constructs a message of a given type.
     *
     * @param content The content of the message.
     * @param type    The type of the message.
     */
    public Message(String content, MessageType type) {
        this.type = type;
        this.content = content;
    }

    /**
     *
     * @param channel The channel of the message.
     * @param content The message's contents.
     * @param type    The type of the message.
     */
    public Message(String channel, String content, MessageType type)
    {
        this.channel = channel;
        this.content = content;
        this.type = type;
    }

    /**
     * Constructs either an Action or Normal chat message.
     *
     * @param channel  The channel the message is in.
     * @param senderName   The sender of the message.
     * @param content  The content of the message.
     * @param isAction If the message is a /me message or not.
     */
    public Message(String channel, String senderName, String content, boolean isAction)
    {
        this.content = content;
        this.channel = channel;
        this.senderName = senderName;
        type = (isAction ? MessageType.ACTION_MESSAGE : MessageType.NORMAL_MESSAGE);
    }

    /**
     * Copy constructor.
     *
     * @param other The other message.
     */
    public Message(Message other)
    {
        this.channel = other.channel;
        this.content = other.content;
        this.type = other.type;
        this.senderName = other.senderName;
        this.extra = other.extra;
        this.senderID = other.senderID;
    }

    public String getContent() {
        return content;
    }

    public String getChannel() {
        return channel;
    }

    public Message setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public Object getExtra() {
        return extra;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    public Message setType(MessageType type) {
        this.type = type;
        return this;
    }

    public Message setSender(User senderUser)
    {
        this.senderName = senderUser.getNick();
        this.senderID = senderUser.getUserID();
        return this;
    }

    public Message setSenderName(String senderName)
    {
        this.senderName = senderName;
        return this;
    }

    public Message setSenderID(long id)
    {
        this.senderID = id;
        return this;
    }

    public Message setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public long getSenderID()
    {
        return senderID;
    }

    public enum MessageType {
        NORMAL_MESSAGE,
        ACTION_MESSAGE,
        LOG_MESSAGE,
        SUB_NOTIFY,
        BAN_NOTIFY,
        HOSTING_NOTIFY,
        HOSTED_NOTIFY,
        DONATION_NOTIFY,
        JTV_NOTIFY,
        WHISPER_MESSAGE,
        CLEAR_TEXT,
        CHEER_MESSAGE
    }

    public static class ClearChatMessage extends Message
    {
        public ClearChatMessage(String channel)
        {
            setChannel(channel);
            setContent("The chat was cleared by a moderator" + (Settings.actuallyClearChat.getValue() ? " (Prevented by Botnak)." : "."));
        }
    }

    public static class BanMessage extends Message
    {
        private String reason, recipient;
        protected String action;

        public BanMessage(String channel, String recipient, String reason)
        {
            setType(MessageType.BAN_NOTIFY);
            setChannel(channel);
            this.reason = reason != null ? " Reason: " + reason : "";
            this.recipient = determineBanName(recipient);
            this.action = "";
        }

        private String determineBanName(String name)
        {
            if (name.equalsIgnoreCase(Settings.accountManager.getViewer().getNick()))
                return "You have ";
            else if (name.equalsIgnoreCase(Settings.accountManager.getBot().getNick()))
                return "Your bot has ";
            else
                return name + " has ";
        }

        @Override
        public String getContent()
        {
            return recipient + "been " + action + reason;
        }
    }

    public static class PermaBanMessage extends BanMessage
    {
        public PermaBanMessage(String channel, String recipient, String reason)
        {
            super(channel, recipient, reason);
            action = "permanently banned.";
        }
    }

    public static class TimeoutMessage extends BanMessage
    {
        public TimeoutMessage(String channel, String recipient, String reason, int duration)
        {
            super(channel, recipient, reason);
            action = determineAction(duration);
        }

        private String determineAction(int duration)
        {
            if (duration == 1)
                return "purged.";
            else
                return "timed out for " + duration + " seconds.";
        }
    }
}