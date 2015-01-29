package thread.heartbeat;

import gui.ChatPane;
import gui.GUIMain;
import util.Timer;
import util.Utils;

import java.util.Set;

/**
 * Created by Nick on 7/8/2014.
 */
public class ViewerCount implements HeartbeatThread {

    private Timer toUpdate;
    private boolean beating;

    public ViewerCount() {
        toUpdate = new Timer(3500);
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return !beating && !toUpdate.isRunning() && GUIMain.chatPanes.size() > 1;
    }

    @Override
    public void beat() {
        beating = true;
        Set<String> keys = GUIMain.chatPanes.keySet();
        for (String s : keys) {
            if (s.equalsIgnoreCase("system logs")) continue;
            ChatPane cp = GUIMain.chatPanes.get(s);
            if (cp == null) continue;
            if (Utils.isChannelLive(s)) {
                int count = Utils.countViewers(s);
                if (count >= 0) {
                    cp.setViewerCount(count);
                }
            } else {
                cp.setViewerCount(-1);
            }
        }
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
        beating = false;
    }
}
