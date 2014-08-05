package thread.heartbeat;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import util.Timer;
import util.Utils;

/**
 * Created by Nick on 7/8/2014.
 */
public class ViewerCount extends HeartbeatThread {

    private Timer toUpdate;
    private String newTitle = null;

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
        StringBuilder stanSB = new StringBuilder();
        //get the Chat Pane that's currently selected
        int index = GUIMain.channelPane.getSelectedIndex();
        if (index == 0) {
            newTitle = null;
            return;
        }
        ChatPane cp = Utils.getChatPane(index);
        CombinedChatPane ccp = Utils.getCombinedChatPane(index);
        //can't be "All Chats" or a combined pane with "all" selected as current
        ChatPane toCheck = (cp != null ? cp : (ccp != null ? (ccp.getActiveChannel().equalsIgnoreCase("all") ? null : ccp) : null));
        if (toCheck == null) {
            newTitle = null;
            return;
        }
        //count the viewers
        int count = -1;
        if (toCheck instanceof CombinedChatPane) {
            if (Utils.isChannelLive(((CombinedChatPane) toCheck).getActiveChannel())) {
                count = Utils.countViewers(((CombinedChatPane) toCheck).getActiveChannel());
            }
        } else {
            if (Utils.isChannelLive(cp.getChannel())) {
                count = Utils.countViewers(cp.getChannel());
            }
        }
        stanSB.append("| Viewer count: ");
        //actually live?
        if (count >= 0) {
            toCheck.setViewerCount(count);

            stanSB.append(toCheck.getViewerCount());
            stanSB.append(" (");
            stanSB.append(toCheck.getViewerPeak());
            stanSB.append(") ");
        } else {
            stanSB.append("Offline ");
        }
        newTitle = stanSB.toString();
    }

    @Override
    public void afterBeat() {
        GUIMain.updateTitle(newTitle);
        toUpdate.reset();
    }
}
