package irc;

import face.FaceManager;
import gui.forms.GUIMain;
import irc.account.Task;
import irc.message.Message;
import irc.message.MessageHandler;
import irc.message.MessageQueue;
import lib.pircbot.PircBot;
import lib.pircbot.User;
import util.Utils;
import util.settings.Settings;

import java.util.HashMap;
import java.util.Optional;


public class IRCViewer extends MessageHandler {

    public PircBot getViewer() {
        return Settings.accountManager.getViewer();
    }

    public void doConnect(String channel) {
        channel = channel.startsWith("#") ? channel : "#" + channel;
        Settings.accountManager.addTask(new Task(getViewer(), Task.Type.JOIN_CHANNEL, channel));
        if (Settings.logChat.getValue()) Utils.logChat(null, channel, 0);
        if (!GUIMain.channelSet.contains(channel)) GUIMain.channelSet.add(channel);
        if (Settings.ffzFacesEnable.getValue()) {
            if (FaceManager.doneWithFrankerFaces)
                FaceManager.handleFFZChannel(channel.substring(1));
        }
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     */
    public void doLeave(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        Settings.accountManager.addTask(new Task(getViewer(), Task.Type.LEAVE_CHANNEL, channel));
        GUIMain.channelSet.remove(channel);
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget If true, will forget the user.
     */
    public synchronized void close(boolean forget) {
        GUIMain.log("Logging out of user: " + Settings.accountManager.getUserAccount().getName());
        Settings.accountManager.addTask(new Task(getViewer(), Task.Type.DISCONNECT, null));
        if (forget) {
            Settings.accountManager.setUserAccount(null);
        }
        GUIMain.viewer = null;
        Settings.channelManager.dispose();
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
        MessageQueue.addMessage(new Message(line, Message.MessageType.HOSTED_NOTIFY)
                .setChannel(Settings.accountManager.getUserAccount().getName()));
    }

    @Override
    public void onHosting(final String channel, final String target, String viewers) {
        Message m = new Message().setChannel(channel).setType(Message.MessageType.HOSTING_NOTIFY);
        if ("-".equals(target)) m.setContent("Exited host mode.");
        else {
            String content = channel + " is now hosting " + target;
            String viewCount;
            if ("-".equals(viewers)) {
                viewCount = ".";
            } else {
                viewCount = " for " + viewers + " viewer" + (viewers.compareTo("1") > 0 ? "s." : ".");
            }
            m.setContent(content + viewCount);
        }
        MessageQueue.addMessage(m);
    }

    @Override
    public void onNewSubscriber(String channel, String line, String newSub) {
        Message m = new Message(channel, line, Message.MessageType.SUB_NOTIFY);
        if (Utils.isMainChannel(channel) && line.endsWith("subscribed!"))
        {
            if (Settings.subscriberManager.addNewSubscriber(newSub, channel)) return;
        } //else it's someone else's channel, just print the message
        MessageQueue.addMessage(m);
    }

    @Override
    public void onResubscribe(String channel, String newSub, String msg)
    {
        Message m = new Message(channel, msg, Message.MessageType.SUB_NOTIFY);
        if (Utils.isMainChannel(channel))
        {
            //it's the (blah blah has subbed for more than X month(s)!)
            //Botnak already handles this, so we can construct this message again since the user feels entitled
            //to tell us they've remained subbed... again
            //the catch is the message they send isn't automatic, so there's a chance it won't be sent (ex: on an IRC client, shy, etc)
            //HOWEVER, we will make sure Botnak does not increment the sub counter for this
            Optional<Subscriber> s = Settings.subscriberManager.getSubscriber(newSub);
            if (s.isPresent() && !s.get().isActive())
            {
                s.get().setActive(true);//fixes issue #87 (I hope)
            }
            m = m.setExtra(false);//anything other than "null" works
            Settings.subscriberManager.notifyTrayIcon(m.getContent(), true);
        }
        MessageQueue.addMessage(m);
    }

    public void onDisconnect() {
        if (!GUIMain.shutDown && getViewer() != null) {
            GUIMain.logCurrent("Detected a disconnection for the account: " + getViewer().getNick());
            if (Settings.autoReconnectAccounts.getValue())
                Settings.accountManager.createReconnectThread(getViewer().getConnection());
            else {
                GUIMain.logCurrent("Auto-reconnects disabled, please check Preferences -> Auto-Reconnect!");
            }
        }
    }

    @Override
    public void onCheer(String channel, String sender, int amount, String message)
    {
        MessageQueue.addMessage(new Message(channel, message, Message.MessageType.CHEER_MESSAGE).setSender(sender).setExtra(amount));
    }

    @Override
    public void onUserPermaBanned(String channel, String user, String reason)
    {
        MessageQueue.addMessage(new Message.PermaBanMessage(channel, user, reason));
    }

    @Override
    public void onUserTimedOut(String channel, String user, int duration, String reason)
    {
        MessageQueue.addMessage(new Message.TimeoutMessage(channel, user, reason, duration));
    }

    public void onClearChat(String channel)
    {
        if (Settings.actuallyClearChat.getValue())
            GUIMain.getChatPane(channel).cleanupChat();
        MessageQueue.addMessage(new Message.ClearChatMessage(channel));
    }

    @Override
    public void onJTVMessage(String channel, String line, String tags) {
        MessageQueue.addMessage(new Message(channel, line, Message.MessageType.JTV_NOTIFY));
    }

    @Override
    public void onWhisper(String user, String receiver, String contents) {
        MessageQueue.addMessage(new Message(contents, Message.MessageType.WHISPER_MESSAGE).setSender(user).setExtra(receiver));
    }

    @Override
    public void onRoomstate(String channel, String tags) {
        if (Utils.isMainChannel(channel)) {
            HashMap<String, String> tagsMap = Utils.parseTagsToMap(tags);
            if (!tagsMap.isEmpty())
            {
                if (tagsMap.containsKey("subs-only"))
                {
                    GUIMain.instance.updateSubsOnly(tagsMap.get("subs-only"));
                }
                if (tagsMap.containsKey("slow"))
                {
                    GUIMain.instance.updateSlowMode(tagsMap.get("slow"));
                }
            }
        }
    }

    @Override
    public void onConnect() {
        Settings.channelManager.addUser(new User(getViewer().getNick()));
        GUIMain.channelSet.forEach(this::doConnect);
        GUIMain.updateTitle(null);
    }
}