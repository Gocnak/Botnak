package irc.message;

/**
 * Created by Nick on 3/21/2014.
 */
public class Message {

    private MessageType type = null;
    private String content = null;
    private String channel = null;
    private String sender = null;
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
     * Constructs either an Action or Normal chat message.
     *
     * @param channel  The channel the message is in.
     * @param sender   The sender of the message.
     * @param content  The content of the message.
     * @param isAction If the message is a /me message or not.
     */
    public Message(String channel, String sender, String content, boolean isAction) {
        this.content = content;
        this.channel = channel;
        this.sender = sender;
        type = (isAction ? MessageType.ACTION_MESSAGE : MessageType.NORMAL_MESSAGE);
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

    public Message setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public Message setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
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
        JTV_NOTIFY
    }

}