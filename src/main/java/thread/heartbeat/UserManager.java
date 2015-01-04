package thread.heartbeat;

import gui.GUIMain;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.org.jibble.pircbot.User;
import util.Timer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;

/**
 * Created by Nick on 8/9/2014.
 * <p>
 * Handles removing users.
 */
public class UserManager implements HeartbeatThread {

    private Timer toUpdate;
    private HashSet<User> collectedUsers;

    public UserManager() {
        toUpdate = new Timer(5000);
        collectedUsers = new HashSet<>();
    }

    @Override
    public boolean shouldBeat() {
        return GUIMain.currentSettings.channelManager != null && !toUpdate.isRunning();
    }

    @Override
    public void beat() {
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
                String line;
                while ((line = br.readLine()) != null) {
                    stanSB.append(line);
                }
                br.close();
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
        }
    }

    @Override
    public void afterBeat() {
        User[] stored = GUIMain.currentSettings.channelManager.getUsers();
        for (User u : stored) {
            boolean flag = false;
            for (User collected : collectedUsers) {
                if (u.equals(collected)) {
                    //still here
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                GUIMain.currentSettings.channelManager.removeUser(u);
            }
        }
        collectedUsers.clear();
        toUpdate.reset();
    }
}
