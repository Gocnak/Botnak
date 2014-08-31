package gui.listeners;

import gui.DraggableTabbedPane;
import gui.GUIMain;
import gui.GUIStreams;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Nick on 1/19/14.
 */
public class NewTabListener extends MouseAdapter {

    @Override
    public void mouseReleased(MouseEvent e) {
        //ensure it's the add new stream tab
        DraggableTabbedPane pane = (DraggableTabbedPane) e.getSource();
        int idx = pane.getUI().tabForCoordinate(pane, e.getX(), e.getY());
        if (idx != -1 && !pane.dragging && pane.getTitleAt(idx).equals("+")) {
            if (GUIMain.streams == null) {
                GUIMain.streams = new GUIStreams();
            }
            GUIMain.streams.setVisible(true);
        }
        super.mouseReleased(e);
    }

}
