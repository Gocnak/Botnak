package gui;

import gui.forms.GUIMain;
import gui.listeners.PaneMenuListener;
import util.Constants;
import util.Utils;
import util.settings.Settings;

import javax.accessibility.AccessibleComponent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Nick on 1/4/14.
 * <p>
 * Credit http://stackoverflow.com/questions/60269/how-to-implement-draggable-tab-using-java-swing
 * <p>
 * Modified by me.
 */
public class DraggableTabbedPane extends JTabbedPane {

    public boolean dragging = false;
    private Image tabImage = null;
    private Point currentMouseLocation = null;
    private int draggedTabIndex = 0;

    private TabType toPlace = null;

    public DraggableTabbedPane() {
        super();
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {

                currentMouseLocation = e.getPoint();
                if (!dragging) {
                    // Gets the tab index based on the mouse position
                    int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
                    if (tabNumber == 0 || tabNumber == getTabCount() - 1) return;
                    if (tabNumber > 0) {
                        draggedTabIndex = tabNumber;
                        Rectangle bounds = getUI().getTabBounds(DraggableTabbedPane.this, tabNumber);

                        // Paint the tabbed pane to a buffer
                        Image totalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics totalGraphics = totalImage.getGraphics();
                        totalGraphics.setClip(bounds);
                        // Don't be double buffered when painting to a static image.
                        setDoubleBuffered(false);
                        paintComponent(totalGraphics);

                        // Paint just the dragged tab to the buffer
                        tabImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
                        Graphics graphics = tabImage.getGraphics();
                        graphics.drawImage(totalImage, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, DraggableTabbedPane.this);
                        dragging = true;
                        repaint();
                    }
                } else {
                    TabType tt = getTabType(e);
                    if (tt.getType() != TabTypeEnum.TAB_NEITHER) {
                        toPlace = tt;
                    } else {
                        toPlace = null;
                    }
                    // Need to repaint
                    repaint();
                }

                super.mouseDragged(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
                if (dragging) {
                    if (tabNumber > 0 && tabNumber != draggedTabIndex && tabNumber != getTabCount() - 1) {
                        int indexWillPlace = draggedTabIndex;
                        if (toPlace.getType() == TabTypeEnum.TAB_MOVE_RIGHT) {
                            if (tabNumber > draggedTabIndex) {
                                indexWillPlace = tabNumber;
                            } else {
                                indexWillPlace = tabNumber + 1;
                            }
                        } else if (toPlace.getType() == TabTypeEnum.TAB_MOVE_LEFT) {
                            if (tabNumber > draggedTabIndex) {
                                indexWillPlace = tabNumber - 1;
                            } else {
                                indexWillPlace = tabNumber;
                            }
                        } else if (toPlace.getType() == TabTypeEnum.TAB_COMBINED) {
                            indexWillPlace = tabNumber;
                        }
                        if (indexWillPlace != draggedTabIndex) {
                            if (toPlace.getType() == TabTypeEnum.TAB_COMBINED) {
                                //there's four cases to catch
                                ChatPane cp = Utils.getChatPane(draggedTabIndex);
                                if (cp != null) {
                                    ChatPane willPlace = Utils.getChatPane(indexWillPlace);
                                    if (willPlace != null) {
                                        //single -> single
                                        //creating a combined chat pane for the first time
                                        CombinedChatPane combinedChatPane = CombinedChatPane.createCombinedChatPane(cp, willPlace);
                                        if (combinedChatPane != null) {
                                            if (!e.isControlDown()) {
                                                if (draggedTabIndex > indexWillPlace) {
                                                    removeTabAt(draggedTabIndex);
                                                    removeTabAt(indexWillPlace);
                                                } else {
                                                    removeTabAt(indexWillPlace);
                                                    removeTabAt(draggedTabIndex);
                                                }
                                            }
                                            insertTab(combinedChatPane.getTabTitle(), null, combinedChatPane.getScrollPane(), null, combinedChatPane.getIndex());
                                            setSelectedIndex(combinedChatPane.getIndex());
                                            GUIMain.combinedChatPanes.add(combinedChatPane);
                                        }
                                    } else {
                                        CombinedChatPane willPlaceCombined = Utils.getCombinedChatPane(indexWillPlace);
                                        if (willPlaceCombined != null) {
                                            //single -> combined
                                            //adding to an already existing combined pane

                                            if (willPlaceCombined.addChatPane(cp)) {
                                                //successfully added to the pane
                                                removeTabAt(draggedTabIndex);
                                                setSelectedIndex(willPlaceCombined.getIndex());
                                            }
                                        }
                                    }
                                } else {
                                    CombinedChatPane ccp = Utils.getCombinedChatPane(draggedTabIndex);
                                    if (ccp != null) {
                                        ChatPane willPlace = Utils.getChatPane(indexWillPlace);
                                        if (willPlace != null) {
                                            //combined -> single
                                            //we'll convert it back to single -> combined
                                            if (ccp.addChatPane(willPlace)) {
                                                if (!e.isControlDown()) {
                                                    removeTabAt(indexWillPlace);
                                                }
                                                setSelectedIndex(ccp.getIndex());
                                            }
                                        } else {
                                            CombinedChatPane willPlaceCombined = Utils.getCombinedChatPane(indexWillPlace);
                                            if (willPlaceCombined != null) {
                                                //combined -> combined
                                                if (ccp.addChatPane(willPlaceCombined.getPanes())) {
                                                    if (e.isControlDown()) {
                                                        removeTabAt(willPlaceCombined.getIndex());
                                                        //we'll have to get rid of the other one
                                                        GUIMain.combinedChatPanes.remove(willPlaceCombined);
                                                    }
                                                    setSelectedIndex(ccp.getIndex());
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { //dragging and moving the tabs
                                Component comp = getComponentAt(draggedTabIndex);
                                String title = getTitleAt(draggedTabIndex);
                                removeTabAt(draggedTabIndex);
                                insertTab(title, null, comp, null, indexWillPlace);
                                setSelectedIndex(indexWillPlace);
                            }
                        }
                    }
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    if (tabNumber > 0) {
                        ChatPane detected = Utils.getChatPane(tabNumber);
                        CombinedChatPane detectedCombo = Utils.getCombinedChatPane(tabNumber);
                        boolean first = (detected != null);
                        boolean second = (detectedCombo != null);
                        if (first || second) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            PaneMenuListener listener = Constants.listenerPaneMenu;

                            JMenuItem menuItem = new JMenuItem("Pop-out chat");
                            menuItem.addActionListener(listener);
                            popupMenu.add(menuItem);

                            if (Settings.showTabPulses.getValue()) {
                                menuItem = new JMenuItem("Toggle Tab Pulsing " +
                                        (first ? (detected.shouldPulseLoc() ? "OFF" : "ON") :
                                                (detectedCombo.shouldPulseLoc() ? "OFF" : "ON")));
                                menuItem.addActionListener(listener);
                                popupMenu.add(menuItem);
                            }
                            if (first) {
                                menuItem = new JMenuItem("Go to " + detected.getChannel() + "'s channel");
                                menuItem.addActionListener(listener);
                                popupMenu.add(menuItem);

                                menuItem = new JMenuItem("View viewer list");
                                menuItem.addActionListener(listener);
                                popupMenu.add(menuItem);

                                menuItem = new JMenuItem("Remove tab");
                                menuItem.addActionListener(listener);
                                popupMenu.add(menuItem);
                            } else {
                                menuItem = new JMenuItem("Disband tab");
                                menuItem.addActionListener(listener);
                                popupMenu.add(menuItem);

                                menuItem = new JMenuItem("Rename tab");
                                menuItem.addActionListener(Constants.tabListener);
                                popupMenu.add(menuItem);

                                JMenu panels = new JMenu("Set Active Panel...");
                                JCheckBoxMenuItem streamCheck;
                                streamCheck = new JCheckBoxMenuItem("All");
                                streamCheck.addActionListener(listener);
                                if (detectedCombo.getActiveChannel().equalsIgnoreCase("All"))
                                    streamCheck.setState(true);
                                panels.add(streamCheck);

                                panels.add(new JPopupMenu.Separator());

                                String[] streams = detectedCombo.getChannels();
                                for (String stream : streams) {
                                    streamCheck = new JCheckBoxMenuItem(stream);
                                    streamCheck.addActionListener(listener);
                                    if (detectedCombo.getActiveChannel().equalsIgnoreCase(stream))
                                        streamCheck.setState(true);
                                    panels.add(streamCheck);
                                }
                                popupMenu.add(panels);
                            }

                            menuItem = new JMenuItem("Clear Chat");
                            menuItem.addActionListener(listener);
                            popupMenu.add(menuItem);

                            popupMenu.show((DraggableTabbedPane) e.getSource(), e.getX(), e.getY());
                        }
                    }
                }
                updateIndexes();
                tabImage = null;
                toPlace = null;
                dragging = false;
                repaint();
            }
        });
    }

    public void updateIndexes() {
        if (!GUIMain.chatPanes.isEmpty()) {
            for (int i = 0; i < getTabCount(); i++) {
                String title = getTitleAt(i);
                ChatPane toUpdate = GUIMain.chatPanes.get(title);
                if (toUpdate != null) toUpdate.setIndex(i);
            }
        }
        if (!GUIMain.combinedChatPanes.isEmpty()) {
            for (int i = 0; i < getTabCount(); i++) {
                String title = getTitleAt(i);
                for (CombinedChatPane cp : GUIMain.combinedChatPanes) {
                    if (cp.getTabTitle().equalsIgnoreCase(title)) {
                        cp.setIndex(i);
                        break;
                    }
                }
            }
        }
    }

    public void scrollDownPanes() {
        if (!GUIMain.chatPanes.isEmpty()) {
            ChatPane[] panes = GUIMain.chatPanes.values().toArray(new ChatPane[GUIMain.chatPanes.size()]);
            for (ChatPane p : panes) {
                p.scrollToBottom();
            }
        }
        if (!GUIMain.combinedChatPanes.isEmpty()) {
            GUIMain.combinedChatPanes.forEach(gui.CombinedChatPane::scrollToBottom);
        }
    }

    public AccessibleComponent getPage(int index) {
        try {
            Field pages = JTabbedPane.class.getDeclaredField("pages");
            pages.setAccessible(true);
            Object p = pages.get(this);
            return (AccessibleComponent) ((ArrayList) p).get(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setTitleAt(int index, String title) {
        CombinedChatPane pane = Utils.getCombinedChatPane(index);
        if (pane != null && !pane.getTabTitle().equalsIgnoreCase(title)) pane.setCustomTitle(title);
        super.setTitleAt(index, title);
    }

    private final RenderingHints antialiasing = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(antialiasing);
        if (toPlace != null && toPlace.getType() != TabTypeEnum.TAB_NEITHER) {
            g2.setColor(Color.orange);
            Polygon toFill = getFillShape(toPlace);
            g2.fillPolygon(toFill);
            g2.drawRect((int) toPlace.getRectangle().getX(), (int) toPlace.getRectangle().getY(), (int) toPlace.getRectangle().getWidth(),
                    (int) toPlace.getRectangle().getHeight());
        }
        // Are we dragging?
        if (dragging && currentMouseLocation != null && tabImage != null) {
            // Draw the dragged tab
            g2.drawImage(tabImage, currentMouseLocation.x, currentMouseLocation.y, this);
        }
    }

    /**
     * So we're going to apply some geometry/trig here.
     * <p>
     * Each tabType rectangle will be dynamic in size. We need
     * to create the appropriate shape for each type, heeding to
     * the dynamic size of the rectangle.
     * <p>
     * We want a filled ">" or "<" triangle to show it's moving, and a filled "+"
     * to show it's going to be combined.
     * <p>
     * For the triangles, we create an equilateral triangle around the center point,
     * easy if you inscribe the triangle on a circle with a defined radius,
     * which is half the distance from the center of the rectangle to the left/right bound.
     * ________
     * |        |
     * | x      |
     * |   *  x |
     * | x      |
     * |________|
     * <p>
     * The trig part comes in for the two points on the same x, but different y. The y will be calculated with
     * sin (60) = x/radius or radius * sin(60), with the x point being negated if it's pointing to the right.
     * <p>
     * For the "+" shape, we need to create it by doing fourth blocks.
     * _________
     * |    _    |
     * |  _| |_  |
     * | |_ * _| |
     * |   |_|   |
     * |_________|
     * <p>
     * We can calculate the points by doing (the distance from the center to left/right bound / 4)
     * as our initial x/y point, then start rotating (and applying - signs) and adding the points
     * on the outer edge.
     *
     * @param tabType The tab to determine which shape to make.
     * @return The shape.
     */
    private Polygon getFillShape(TabType tabType) {
        Polygon p = new Polygon();
        Rectangle r = tabType.getRectangle();
        int centerRectX = (int) r.getCenterX();
        int centerRectY = (int) r.getCenterY();
        if (tabType.getType() == TabTypeEnum.TAB_MOVE_RIGHT) {
            int distX = (int) (r.getX() + r.getWidth()) - centerRectX;
            int distY = (int) (r.getY() + r.getHeight()) - centerRectY;
            Point first = new Point(centerRectX + (distX), centerRectY);
            p.addPoint(first.x, first.y);
            Point second;
            int x2 = (centerRectX - (distX / 2));
            int y2 = (centerRectY + (distY / 2));
            int y3 = (centerRectY - (distY / 2));
            second = new Point(x2, y2);
            p.addPoint(second.x, second.y);
            p.addPoint(second.x, y3);
        } else if (tabType.getType() == TabTypeEnum.TAB_MOVE_LEFT) {
            int distX = (int) (r.getX() + r.getWidth()) - centerRectX;
            int distY = (int) (r.getY() + r.getHeight()) - centerRectY;
            Point first = new Point((int) r.getX(), centerRectY);
            p.addPoint(first.x, first.y);
            Point second;
            int x2 = (centerRectX + distX);
            int y2 = (centerRectY + (distY / 2));
            int y3 = (centerRectY - (distY / 2));
            second = new Point(x2, y2);
            p.addPoint(second.x, second.y);
            p.addPoint(second.x, y3);
        } else {//combine
            int distX = (int) (r.getX() + r.getWidth()) - centerRectX;
            int distY = (int) (r.getY() + r.getHeight()) - centerRectY;
            int outerX = (int) (distX / 1.25);
            int innerX = outerX / 2;
            int outerY = (int) (distY / 1.25);
            int innerY = outerY / 2;
            //bottom
            p.addPoint(centerRectX - innerX, centerRectY - innerY);
            p.addPoint(centerRectX - innerX, centerRectY - outerY);
            p.addPoint(centerRectX + innerX, centerRectY - outerY);
            p.addPoint(centerRectX + innerX, centerRectY - innerY);
            //right
            p.addPoint(centerRectX + outerX, centerRectY - innerY);
            p.addPoint(centerRectX + outerX, centerRectY + innerY);
            //top
            p.addPoint(centerRectX + innerX, centerRectY + innerY);
            p.addPoint(centerRectX + innerX, centerRectY + outerY);
            p.addPoint(centerRectX - innerX, centerRectY + outerY);
            p.addPoint(centerRectX - innerX, centerRectY + innerY);
            //left
            p.addPoint(centerRectX - outerX, centerRectY + innerY);
            p.addPoint(centerRectX - outerX, centerRectY - innerY);
        }
        return p;
    }

    /**
     * Overrides the TabbedPane in order to prevent the "+" tab from getting
     * selected and shown.
     */
    @Override
    protected void fireStateChanged() {
        ChatPane cp = Utils.getChatPane(getSelectedIndex());
        CombinedChatPane ccp = Utils.getCombinedChatPane(getSelectedIndex());
        if (cp != null) GUIMain.updateTitle(cp.getViewerCountString());
        if (ccp != null) {
            String activeChan = ccp.getActiveChannel();
            GUIMain.updateTitle(activeChan.equalsIgnoreCase("all") ? null : GUIMain.chatPanes.get(activeChan).getViewerCountString());
        }
        if (getSelectedIndex() == getTabCount() - 1) return;
        super.fireStateChanged();
    }

    /**
     * Right so.
     * We have this rectangle.
     * -------------------
     * |                 |
     * |                 |
     * -------------------
     * but how do we cut it up into thirds? By cutting it into halves, and
     * using the halves as midpoints.
     * -------------------
     * |        |        |
     * |        |        |
     * -------------------
     * now it's in half, just cut it in half again.
     * -------------------
     * |   |    |        |
     * |   |    |        |
     * -------------------
     * repeat for the right side, and when finished just cut out the middle line
     * (or make a new rectangle with those coordinates).
     * -------------------
     * |   |         |   |
     * |   |         |   |
     * -------------------
     * <p>
     * Now we can check the point of the mouse event and see if it will
     * create a new combined tab, or just move the tab to the left/right of the
     * tab.
     *
     * @param e The mouse event (gives the Point) of the mouse.
     * @return The Tab Type that the tab will create.
     */
    public TabType getTabType(MouseEvent e) {
        int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
        if (tabNumber == draggedTabIndex || tabNumber <= 0 || tabNumber == getTabCount() - 1)
            return new TabType(null, TabTypeEnum.TAB_NEITHER);
        Rectangle r = getUI().getTabBounds(DraggableTabbedPane.this, tabNumber);//base tab rectangle.
        Rectangle leftHalf = new Rectangle((int) r.getX(), (int) r.getY(), (int) (r.getWidth() / 2), (int) r.getHeight());
        Rectangle leftMove = new Rectangle((int) leftHalf.getX(), (int) leftHalf.getY(), (int) (leftHalf.getWidth() / 2), (int) leftHalf.getHeight());
        Rectangle rightHalf = new Rectangle((int) (r.getX() + leftHalf.getWidth()), (int) r.getY(), (int) leftHalf.getWidth(), (int) leftHalf.getHeight()); //same width as the other half
        Rectangle rightMove = new Rectangle((int) (rightHalf.getX() + leftMove.getWidth()), (int) rightHalf.getY(), (int) (leftMove.getWidth()), (int) leftHalf.getHeight());
        Rectangle combine = new Rectangle((int) (r.getX() + leftMove.getWidth()), (int) r.getY(), (int) leftHalf.getWidth(), (int) r.getHeight());
        Point p = e.getPoint();
        if (leftMove.contains(p)) {
            return new TabType(leftMove, TabTypeEnum.TAB_MOVE_LEFT);
        } else if (rightMove.contains(p)) {
            return new TabType(rightMove, TabTypeEnum.TAB_MOVE_RIGHT);
        } else if (combine.contains(p)) {
            return new TabType(combine, TabTypeEnum.TAB_COMBINED);
        } else
            return new TabType(null, TabTypeEnum.TAB_NEITHER);
    }

    enum TabTypeEnum {
        TAB_COMBINED,
        TAB_MOVE_LEFT,
        TAB_MOVE_RIGHT,
        TAB_NEITHER
    }

    private class TabType {

        Rectangle rectangle;
        TabTypeEnum type;

        TabType(Rectangle r, TabTypeEnum type) {
            rectangle = r;
            this.type = type;
        }

        public Rectangle getRectangle() {
            return rectangle;
        }

        public TabTypeEnum getType() {
            return type;
        }
    }
}