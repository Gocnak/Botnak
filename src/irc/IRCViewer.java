package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;
import util.Settings;
import util.Utils;

import java.awt.*;


public class IRCViewer extends MessageHandler {

    String name, pass;

    BanQueue bq = null;
    private PircBot viewer;

    public PircBot getViewer() {
        return viewer;
    }

    public String getMaster() {
        return name;
    }

    public IRCViewer(String user, String password) {
        if (GUIMain.currentSettings.user == null) {//someone clicked "login"
            name = user;
            pass = password;
            GUIMain.currentSettings.user = new Settings.Account(name, pass);
            GUIMain.currentSettings.loadKeywords();//set that name keyword
        } else {// it was loaded from file
            name = GUIMain.currentSettings.user.getAccountName();
            pass = GUIMain.currentSettings.user.getAccountPass();
        }
        viewer = new PircBot(this);
        viewer.setVerbose(true);//TODO remove dis
        viewer.setNick(user);
        GUIMain.updateTitle();
        try {
            /*
            TODO
            put this in a ConnectThread, which will handle all (re)connections
            for the PircBot.

            it's changed to a boolean now, so we can rid the try{}catch block and use
            the outcome of the boolean to determine a reconnect

            also I should make a cancel listener for that
             */
            viewer.connect("irc.twitch.tv", 6667, pass);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        viewer.sendRawLineViaQueue("TWITCHCLIENT 3");
        if (GUIMain.loadedStreams()) {
            for (String s : GUIMain.channelSet) {
                doConnect(s);
            }
        }
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!GUIMain.viewerCheck.isAlive()) {
                        GUIMain.viewerCheck.start();
                    }
                }
            });
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (bq == null || !bq.isAlive()) {
                        bq = new BanQueue();
                        bq.start();
                    }
                }
            });
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        GUIMain.log("Loaded User: " + name + "!");
        GUIMain.viewer = this;
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!viewer.isConnected()) {
            try {
                //TODO see the todo above in the constructor
                viewer.connect("irc.twitch.tv", 6667, pass);
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else {
            if (!viewer.isInChannel(channelName)) {
                viewer.joinChannel(channelName);
                GUIMain.channelSet.add(channel);
                if (GUIMain.currentSettings.logChat) Utils.logChat(null, channel, 0);
            }
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
        if (viewer.isInChannel(channel)) {
            viewer.partChannel(channel);
        }
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget If true, will forget the user.
     */
    public synchronized void close(boolean forget) {
        GUIMain.log("Logging out of user: " + name);
        for (String s : viewer.getChannels()) {
            doLeave(s);
        }
        viewer.disconnect();
        viewer.dispose();
        if (GUIMain.viewerCheck != null && !GUIMain.viewerCheck.isInterrupted()) GUIMain.viewerCheck.interrupt();
        if (bq != null && !bq.isInterrupted()) bq.interrupt();
        if (forget) {
            GUIMain.currentSettings.user = null;
        }
        viewer = null;
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
                GUIMain.onMessage(new Message(channel, newSub));
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
            GUIMain.onBan(channel, "The chat was cleared by a moderator (Prevented by Botnak)");
        }
    }

}
