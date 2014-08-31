package thread.heartbeat;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import util.Timer;
import util.Utils;

import java.util.Set;

/**
 * Created by Nick on 7/8/2014.
 */
public class ViewerCount extends HeartbeatThread {

    private Timer toUpdate;

    public ViewerCount() {
        toUpdate = new Timer(3500);
    }

    @Override
    public boolean shouldBeat() {
        if (!toUpdate.isRunning()) {
            int index = GUIMain.channelPane.getSelectedIndex();
            ChatPane cp = Utils.getChatPane(index);
            CombinedChatPane ccp = Utils.getCombinedChatPane(index);
            if (index != 0) {
                if (ccp != null && !ccp.getActiveChannel().equalsIgnoreCase("all")) {
                    return true;
                } else if (cp != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void beat() {
        Set<String> keys = GUIMain.chatPanes.keySet();
        for (String s : keys) {
            if (s.equalsIgnoreCase("system logs")) continue;
            if (Utils.isChannelLive(s)) {
                int count = Utils.countViewers(s);
                if (count >= 0) {
                    ChatPane cp = GUIMain.chatPanes.get(s);
                    cp.setViewerCount(count);
                }
            }
        }
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
    }
}
