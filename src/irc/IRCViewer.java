package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;
import util.Settings;
import util.Utils;

import java.awt.*;


public class IRCViewer extends PircBot {

    String name, pass;

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
        setName(user);
        setLogin(user);
        GUIMain.updateTitle();
        try {
            connect("irc.twitch.tv", 6667, pass);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        sendRawLineViaQueue("TWITCHCLIENT");
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
        GUIMain.log("Loaded User: " + name + "!");
        GUIMain.viewer = this;
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!isConnected()) {
            try {
                connect("irc.twitch.tv", 6667, pass);
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else {
            joinChannel(channelName);
            if (Utils.isInChannel(this, channelName)) {
                if (!GUIMain.channelSet.contains(channel)) GUIMain.channelSet.add(channel);
            }
        }
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     * @param forget  If true, will remove the channel from the channel list.
     */
    public void doLeave(String channel, boolean forget) {
        String channelName = "#" + channel;
        if (Utils.isInChannel(this, channelName)) {
            partChannel(channelName);
        }
        if (forget) {
            if (GUIMain.channelSet.contains(channel)) {
                GUIMain.channelSet.remove(channel);
            }
        }
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget If true, will forget the user.
     */
    public void close(boolean forget) {
        GUIMain.log("Logging out of user: " + name);
        for (String s : getChannels()) {
            doLeave(s.substring(1), false);
        }
        disconnect();
        dispose();
        if (forget) {
            GUIMain.currentSettings.user = null;
        }
        GUIMain.viewer = null;
    }


    public void onMessage(final String channel, final String sender, final String login, final String hostname, final String message) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMain.onMessage(channel, sender, message, false);
            }
        });
    }

    public void onAction(final String sender, final String login, final String hostname, final String target, final String action) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMain.onMessage(target, sender, action, true);
            }
        });
    }

}
