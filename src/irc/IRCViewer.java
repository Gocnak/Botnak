package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;
import util.Utils;

import java.awt.*;


public class IRCViewer extends PircBot {


    public static String name, pass;

    public String getMaster() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public IRCViewer(String user, String password) {
        name = user;
        pass = password;
        setName(name);
        setLogin(name);
        GUIMain.normUser.setText(user);
        try {
            connect("irc.twitch.tv", 6667, pass);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        if (GUIMain.loadedStreams()) {
            for (String s : GUIMain.channelMap) {
                doConnect(s);
            }
        }
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    GUIMain.viewerCheck.start();
                }
            });
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        GUIMain.log("LOADED USER: " + name);
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
                if (!GUIMain.channelMap.contains(channel)) GUIMain.channelMap.add(channel);
            }
        }
    }

    public void doLeave(String channel, boolean forget) {
        String channelName = "#" + channel;
        if (Utils.isInChannel(this, channelName)) {
            partChannel(channelName);
        }
        if (forget) {
            if (GUIMain.channelMap.contains(channel)) {
                GUIMain.channelMap.remove(channel);
            }
        }
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
