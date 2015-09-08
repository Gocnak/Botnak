package thread.heartbeat;

import gui.forms.GUIMain;
import gui.forms.GUIViewerList;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.ChannelManager;
import lib.pircbot.User;
import util.Timer;
import util.Utils;
import util.settings.Settings;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.URL;
import java.util.Enumeration;
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
        toUpdate = new Timer(4000);
        collectedUsers = new HashSet<>();
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return !beating && Settings.channelManager != null && !toUpdate.isRunning();
    }

    @Override
    public void beat() {
        beating = true;
        String[] channels = Settings.channelManager.getChannelNames();
        URL url;
        for (String chan : channels) {
            String chanOut = chan.substring(1);
            GUIViewerList list = GUIMain.viewerLists.get(chanOut);
            if (list != null) {
                try {
                    url = new URL("http://tmi.twitch.tv/group/user/" + chan.substring(1) + "/chatters");
                    String line = Utils.createAndParseBufferedReader(url.openStream());
                    if (!line.isEmpty()) {
                        final Enumeration<TreePath> beforePaths = list.beforePaths();
                        final int beforeScroll = list.beforeScroll();

                        JSONObject site = new JSONObject(line);
                        JSONObject chatters = site.getJSONObject("chatters");

                        JSONArray mods = chatters.getJSONArray("moderators");
                        readAndUpdate(mods, list, GUIViewerList.ViewerType.MOD);

                        JSONArray staff = chatters.getJSONArray("staff");
                        readAndUpdate(staff, list, GUIViewerList.ViewerType.STAFF);

                        JSONArray admins = chatters.getJSONArray("admins");
                        readAndUpdate(admins, list, GUIViewerList.ViewerType.ADMIN);

                        JSONArray global_mods = chatters.getJSONArray("global_mods");
                        readAndUpdate(global_mods, list, GUIViewerList.ViewerType.GLOBAL_MOD);

                        JSONArray viewers = chatters.getJSONArray("viewers");
                        readAndUpdate(viewers, list, GUIViewerList.ViewerType.VIEWER);

                        EventQueue.invokeLater(() -> list.updateRoot(beforePaths, beforeScroll));

                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    GUIMain.log(e);
                }
            }
        }
    }

    private void readAndUpdate(JSONArray toRead, GUIViewerList list, GUIViewerList.ViewerType type) {
        for (int i = 0; i < toRead.length(); i++) {
            User u = new User(toRead.getString(i));
            switch (type) {
                case GLOBAL_MOD:
                    u.setGlobalMod(true);
                    break;
                case ADMIN:
                    u.setAdmin(true);
                    break;
                case STAFF:
                    u.setStaff(true);
                    break;
                default:
                    break;
            }
            addUser(u);
            collectedUsers.add(toRead.getString(i));
        }
        list.updateCategory(type, collectedUsers);
        collectedUsers.clear();
    }

    private void addUser(User u) {
        if (getChannelManager().getUser(u.getNick(), false) == null)
            getChannelManager().addUser(u);
    }

    private ChannelManager getChannelManager() {
        return Settings.channelManager;
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
        beating = false;
    }
}