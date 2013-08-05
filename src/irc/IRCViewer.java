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
            e.printStackTrace();
        }
        GUIMain.viewerCheck.start();
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!isConnected()) {
            try {
                connect("irc.twitch.tv", 6667, pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                joinChannel(channelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Utils.isInChannel(this, channel)) {
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
                GUIMain.mainGUI.onMessage(channel, sender, message);
            }
        });
    }


}
