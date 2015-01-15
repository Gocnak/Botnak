package irc;

import gui.GUIMain;
import irc.account.Task;
import irc.message.Message;
import irc.message.MessageHandler;
import irc.message.MessageQueue;
import lib.pircbot.org.jibble.pircbot.ChannelManager;
import thread.heartbeat.BanQueue;
import util.Utils;


public class IRCViewer extends MessageHandler {

    public void doConnect(String channel) {
        channel = channel.startsWith("#") ? channel : "#" + channel;
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getViewer(), Task.Type.JOIN_CHANNEL, channel));
        if (GUIMain.currentSettings.logChat) Utils.logChat(null, channel, 0);
        if (!GUIMain.channelSet.contains(channel)) GUIMain.channelSet.add(channel);
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     */
    public void doLeave(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getViewer(), Task.Type.LEAVE_CHANNEL, channel));
        GUIMain.channelSet.remove(channel);
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget If true, will forget the user.
     */
    public synchronized void close(boolean forget) {
        GUIMain.log("Logging out of user: " + GUIMain.currentSettings.accountManager.getUserAccount().getName());
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getViewer(), Task.Type.DISCONNECT, null));
        if (forget) {
            GUIMain.currentSettings.accountManager.setUserAccount(null);
        }
        GUIMain.viewer = null;
    }

    @Override
    public void onMessage(final String channel, final String sender, final String message) {
        MessageQueue.addMessage(new Message(channel, sender, message, false));
    }

    @Override
    public void onAction(final String sender, final String channel, final String action) {
        MessageQueue.addMessage(new Message(channel, sender, action, true));
    }

    @Override
    public void onBeingHosted(final String line) {
        MessageQueue.addMessage(new Message(line, Message.MessageType.HOSTED_NOTIFY).setChannel(GUIMain.currentSettings.accountManager.getUserAccount().getName()));
    }

    @Override
    public void onHosting(final String channel, final String target) {
        if (!target.equals("-")) {
            MessageQueue.addMessage(new Message(channel + " is now hosting " + target + ".", Message.MessageType.HOSTING_NOTIFY).setChannel(channel));
        } else {
            MessageQueue.addMessage(new Message("Exited host mode.", Message.MessageType.HOSTING_NOTIFY).setChannel(channel));
        }
    }

    @Override
    public void onOp(String channel, String recepient) {
        //TODO if they have GUIMain.currentSettings.showModGrants, print out who gets modded as a log.
        //update the viewer list to re-organize, and put the name at the top.
    }

    @Override
    public void onNewSubscriber(final String channel, final String newSub) {
        if (channel.substring(1).equalsIgnoreCase(GUIMain.currentSettings.accountManager.getUserAccount().getName())) {
            if (GUIMain.currentSettings.subscriberManager.addNewSubscriber(newSub, channel)) return;
        }
        MessageQueue.addMessage(new Message(channel, newSub, Message.MessageType.SUB_NOTIFY, null));
    }

    @Override
    public void onDeop(String channel, String recipient) {
        //TODO if they have GUIMain.currentSettings.showModGrants, print out who gets de-modded as a log.
        //update the viewer list to re-organize, and put the name at the top.
    }

    public void onDisconnect() {
        //TODO create and run the reconnect listener thread  if (!GUIMain.shutdown)
    }

    public synchronized void onClearChat(String channel, String line) {
        if (line.split(" ").length > 1) {
            BanQueue.addToMap(channel, line.split(" ")[1]);
        } else {
            //TODO perhaps add the option to actually clear the chat based on user setting?
            MessageQueue.addMessage(new Message(channel, null, Message.MessageType.BAN_NOTIFY, "The chat was cleared by a moderator. (Prevented by Botnak)"));
        }
    }

    @Override
    public void onJTVMessage(String channel, String line) {
        MessageQueue.addMessage(new Message().setChannel(channel).setType(Message.MessageType.JTV_NOTIFY).setContent(line));
    }

    @Override
    public void onConnect() {
        GUIMain.currentSettings.channelManager = new ChannelManager();
        GUIMain.channelSet.forEach(this::doConnect);
        GUIMain.updateTitle(null);
    }
}
