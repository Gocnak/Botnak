package irc;

import gui.GUIMain;

/**
 * Created by Nick on 3/21/2014.
 */
public class Message {


    private MessageType type = null;
    private String content = null;
    private String channel = null;
    private String sender = null;

    /**
     * Constructs a default message.
     *
     * @param content The content of the message.
     */
    public Message(String content) {
        this.content = content;
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
     * Constructs a sub notify message.
     *
     * @param channel The channel the new sub is for.
     * @param newSub  The new sub's name.
     */
    public Message(String channel, String newSub) {
        content = " " + newSub + " has just subscribed! (" + (GUIMain.subCount + 1) + ")";
        this.channel = channel;
        sender = newSub;
        type = MessageType.SUB_NOTIFY;
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
        SUB_NOTIFY
    }

}