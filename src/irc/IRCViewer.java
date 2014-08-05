package irc;

import gui.GUIMain;
import util.Utils;

import java.awt.*;


public class IRCViewer extends MessageHandler {

    BanQueue bq = null;

    public IRCViewer() {
        if (bq == null || !bq.isAlive()) {
            bq = new BanQueue();
            bq.start();
        }
    }

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
        if (bq != null && !bq.isInterrupted()) bq.interrupt();
        if (forget) {
            GUIMain.currentSettings.accountManager.setUserAccount(null);
        }
        GUIMain.viewer = null;
    }

    @Override
    public void onMessage(final String channel, final String sender, final String message) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMain.onMessage(new Message(channel, sender, message, false));
            }
        });
    }

    @Override
    public void onAction(final String sender, final String channel, final String action) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMain.onMessage(new Message(channel, sender, action, true));
            }
        });
    }

    @Override
    public void onOp(String channel, String recepient) {
        //TODO if they have GUIMain.currentSettings.showModGrants, print out who gets modded as a log.
        //update the viewer list to re-organize, and put the name at the top.
    }

    @Override
    public void onJoin(String channel, String userNick) {
       /*TODO
         if they have user stats on, start to accumulate the time they're in the channel
         if they have GUIMain.currentSettings.showUserJoins, log the join message
         update the viewer list for the channel
       */
    }

    @Override
    public void onPart(String channel, String userNick) {
       /*
        TODO
        if they have user stats on, accumulate the time they were in the channel
        if they have GUIMain.currentSettings.showUserParts on, log the part message
        update the viewer list
         */
    }

    @Override
    public void onNewSubscriber(final String channel, final String newSub) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMain.onMessage(new Message(channel, newSub, Message.MessageType.SUB_NOTIFY, null));
            }
        });
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
            if (bq == null) {
                bq = new BanQueue();
                bq.start();
            }
            bq.addToMap(channel, line.split(" ")[1]);
        } else {
            GUIMain.onBan(channel, "The chat was cleared by a moderator. (Prevented by Botnak)");
        }
    }

    @Override
    public void onConnect() {
        for (String channel : GUIMain.channelSet) {
            doConnect(channel);
        }
        GUIMain.updateTitle(null);
    }
}
