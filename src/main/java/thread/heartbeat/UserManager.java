package thread.heartbeat;

import gui.GUIMain;
import gui.GUIViewerList;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.org.jibble.pircbot.ChannelManager;
import lib.pircbot.org.jibble.pircbot.User;
import util.Timer;
import util.Utils;

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

    public static Timer toUpdate;
    private HashSet<String> collectedUsers;
    private static boolean beating;

    public UserManager() {
        toUpdate = new Timer(5000);
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
            String chanOut = chan.substring(1);
            GUIViewerList list = GUIMain.viewerLists.get(chanOut);
            if (list != null) {
                try {
                    url = new URL("http://tmi.twitch.tv/group/user/" + chan.substring(1) + "/chatters");

                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder stanSB = new StringBuilder();
                    Utils.parseBufferedReader(br, stanSB, false);
                    JSONObject site = new JSONObject(stanSB.toString());
                    JSONObject chatters = site.getJSONObject("chatters");
                    JSONArray mods = chatters.getJSONArray("moderators");
                    for (int i = 0; i < mods.length(); i++) {
                        collectedUsers.add(mods.getString(i));
                    }
                    list.updateCategory(GUIViewerList.ViewerType.MOD, collectedUsers);
                    collectedUsers.clear();
                    JSONArray staff = chatters.getJSONArray("staff");
                    for (int i = 0; i < staff.length(); i++) {
                        User u = new User(staff.getString(i));
                        u.setStaff(true);
                        addUser(u);
                        collectedUsers.add(staff.getString(i));
                    }
                    list.updateCategory(GUIViewerList.ViewerType.STAFF, collectedUsers);
                    collectedUsers.clear();
                    JSONArray admins = chatters.getJSONArray("admins");
                    for (int i = 0; i < admins.length(); i++) {
                        User u = new User(admins.getString(i));
                        u.setAdmin(true);
                        addUser(u);
                        collectedUsers.add(admins.getString(i));
                    }
                    list.updateCategory(GUIViewerList.ViewerType.ADMIN, collectedUsers);
                    collectedUsers.clear();
                    JSONArray global_mods = chatters.getJSONArray("global_mods");
                    for (int i = 0; i < global_mods.length(); i++) {
                        User u = new User(global_mods.getString(i));
                        u.setGlobalMod(true);
                        addUser(u);
                        collectedUsers.add(global_mods.getString(i));
                    }
                    list.updateCategory(GUIViewerList.ViewerType.GLOBAL_MOD, collectedUsers);
                    collectedUsers.clear();
                    JSONArray viewers = chatters.getJSONArray("viewers");
                    for (int i = 0; i < viewers.length(); i++) {
                        collectedUsers.add(viewers.getString(i));
                    }
                    list.updateCategory(GUIViewerList.ViewerType.VIEWER, collectedUsers);
                    collectedUsers.clear();
                    Thread.sleep(750);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addUser(User u) {
        if (getChannelManager().getUser(u.getNick(), false) == null)
            getChannelManager().addUser(u);
    }

    private ChannelManager getChannelManager() {
        return GUIMain.currentSettings.channelManager;
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
        beating = false;
    }
}