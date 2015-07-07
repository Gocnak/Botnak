package com.gocnak.thread.heartbeat;

import com.gocnak.gui.GUIMain;
import com.gocnak.lib.JSON.JSONArray;
import com.gocnak.lib.JSON.JSONObject;
import com.gocnak.util.Timer;
import com.gocnak.util.Utils;
import org.jibble.pircbot.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;

/**
 * Created by Nick on 8/9/2014.
 * <p>
 * Handles assigning the userlist for a channel.
 */
public class UserManager implements HeartbeatThread {

    private Timer toUpdate;
    private HashSet<User> collectedUsers;
    private boolean beating;

    public UserManager() {
        toUpdate = new Timer(10000);
        collectedUsers = new HashSet<>();
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return !beating && GUIMain.currentSettings.channelManager != null && !toUpdate.isRunning();
    }

    @Override
    public void beat() {
        beating = true;
        String[] channels = GUIMain.currentSettings.channelManager.getChannelNames();
        URL url;
        for (String chan : channels) {
            try {
                url = new URL("http://tmi.twitch.tv/group/user/" + chan.substring(1) + "/chatters");
                /**
                 * TODO GUIMain.viewerLists.exists(chan.substring(1)) GUIMain.get(chan).updateViewers
                 * create a local hashset, send the collected users of this channel to that viewer list
                 */

                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder stanSB = new StringBuilder();
                Utils.parseBufferedReader(br, stanSB);
                JSONObject site = new JSONObject(stanSB.toString());
                JSONObject chatters = site.getJSONObject("chatters");
                JSONArray mods = chatters.getJSONArray("moderators");
                for (int i = 0; i < mods.length(); i++) {
                    collectedUsers.add(new User(mods.getString(i)));
                }
                JSONArray staff = chatters.getJSONArray("staff");
                for (int i = 0; i < staff.length(); i++) {
                    collectedUsers.add(new User(staff.getString(i)));
                }
                JSONArray admins = chatters.getJSONArray("admins");
                for (int i = 0; i < admins.length(); i++) {
                    collectedUsers.add(new User(admins.getString(i)));
                }
                JSONArray viewers = chatters.getJSONArray("viewers");
                for (int i = 0; i < viewers.length(); i++) {
                    collectedUsers.add(new User(viewers.getString(i)));
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(750);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void afterBeat() {
        if (!collectedUsers.isEmpty()) {
            collectedUsers.stream().forEach(GUIMain.currentSettings.channelManager::addUser);
            collectedUsers.clear();
            toUpdate.reset();
        }
        beating = false;
    }
}