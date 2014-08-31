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
public class UserManager extends HeartbeatThread {


    Timer toUpdate;
    HashSet<User> collectedusers;

    public UserManager() {
        toUpdate = new Timer(5000);
        collectedusers = new HashSet<>();
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
                    collectedusers.add(new User(mods.getString(i)));
                }
                JSONArray staff = chatters.getJSONArray("staff");
                for (int i = 0; i < staff.length(); i++) {
                    collectedusers.add(new User(staff.getString(i)));
                }
                JSONArray admins = chatters.getJSONArray("admins");
                for (int i = 0; i < admins.length(); i++) {
                    collectedusers.add(new User(admins.getString(i)));
                }
                JSONArray viewers = chatters.getJSONArray("viewers");
                for (int i = 0; i < viewers.length(); i++) {
                    collectedusers.add(new User(viewers.getString(i)));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void afterBeat() {
        User[] stored = GUIMain.currentSettings.channelManager.getUsers();
        for (User u : stored) {
            if (!collectedusers.contains(u)) {
                GUIMain.currentSettings.channelManager.removeUser(u);//remove people not in any channels
            }
        }
        collectedusers.clear();
        toUpdate.reset();
    }
}
