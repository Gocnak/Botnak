package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Credit to
 * <p>
 * https://community.oracle.com/thread/1352788
 * and
 * https://community.oracle.com/thread/1352946
 */
public class ScrollablePanel extends JPanel implements Scrollable {
    public ScrollablePanel() {
        super();
        setLayout(new BorderLayout());
        setBackground(Color.black);
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 60;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && (getParent().getHeight() > getPreferredSize().height);
    }
}