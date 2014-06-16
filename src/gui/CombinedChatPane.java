package gui;

import gui.listeners.ListenerName;
import gui.listeners.ListenerURL;
import irc.Donator;
import irc.Message;
import lib.pircbot.org.jibble.pircbot.User;
import lib.scalr.Scalr;
import util.TabPulse;
import util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Nick on 1/4/14.
 */
public class CombinedChatPane {

    public boolean shouldPulse() {
        return shouldPulse;
    }

    private boolean shouldPulse = true;

    public void setShouldPulse(boolean newBool) {
        shouldPulse = newBool;
    }

    private String title;

    public String getTabTitle() {
        return title;
    }

    private boolean customTitle = false;

    public void setCustomTitle(String title) {
        customTitle = true;
        setTabTitle(title);
    }

    public void setTabTitle(String title) {
        this.title = title;
        GUIMain.channelPane.setTitleAt(getIndex(), title);
    }

    private String[] channels;

    public String[] getChannels() {
        return channels;
    }

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int newIndex) {
        index = newIndex;
    }

    private ChatPane[] panes;

    public ChatPane[] getPanes() {
        return panes;
    }

    private int cleanupCounter;
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JScrollPane scrollPaneAll;

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    private String activeChannel = "All";

    public void setActiveChannel(String channel) {
        activeChannel = channel;
    }

    public String getActiveChannel() {
        return activeChannel;
    }

    public void setActiveScrollPane(String channel) {
        for (ChatPane p : panes) {
            if (p.getChannel().equalsIgnoreCase(channel)) {
                scrollPane = p.getScrollPane();
                p.getTextPane().setCaretPosition(p.getTextPane().getDocument().getLength());
                break;
            }
        }
        GUIMain.channelPane.setComponentAt(getIndex(), scrollPane);
        GUIMain.channelPane.repaint();
    }

    public void setDefaultScrollPane() {
        scrollPane = scrollPaneAll;
        textPane.setCaretPosition(textPane.getDocument().getLength());
        GUIMain.channelPane.setComponentAt(getIndex(), scrollPane);
        GUIMain.channelPane.repaint();
    }

    final SimpleDateFormat format = new SimpleDateFormat("[h:mm a]", Locale.getDefault());


    //credit to http://stackoverflow.com/a/4047794 for the below
    public boolean isScrollBarFullyExtended(JScrollBar vScrollBar) {
        BoundedRangeModel model = vScrollBar.getModel();
        return (model.getExtent() + model.getValue()) == model.getMaximum();
    }

    public void scrollToBottom() {
        Rectangle visibleRect = textPane.getVisibleRect();
        visibleRect.y = textPane.getHeight() - visibleRect.height;
        textPane.scrollRectToVisible(visibleRect);
    }

    // ScrollingDocumentListener takes care of re-scrolling when appropriate
    class ScrollingDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            maybeScrollToBottom();
        }

        public void insertUpdate(DocumentEvent e) {
            maybeScrollToBottom();
        }

        public void removeUpdate(DocumentEvent e) {
            maybeScrollToBottom();
        }

        private void maybeScrollToBottom() {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            boolean scrollBarAtBottom = isScrollBarFullyExtended(scrollBar);
            if (scrollBarAtBottom) {
                // Push the call to "scrollToBottom" back TWO PLACES on the
                // AWT-EDT queue so that it runs *after* Swing has had an
                // opportunity to "react" to the appending of new text:
                // this ensures that we "scrollToBottom" only after a new
                // bottom has been recalculated during the natural
                // revalidation of the GUI that occurs after having
                // appending new text to the JTextArea.
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                scrollToBottom();
                            }
                        });
                    }
                });
            }
        }
    }

    public CombinedChatPane(JScrollPane scrollPane, JTextPane pane, ChatPane... chatPanes) {
        panes = chatPanes;
        ArrayList<String> channels = new ArrayList<>();
        for (ChatPane cp : chatPanes) {
            channels.add(cp.getChannel());
        }
        this.channels = channels.toArray(new String[channels.size()]);
        textPane = pane;
        textPane.getStyledDocument().addDocumentListener(new ScrollingDocumentListener());
        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.scrollPane = scrollPane;
        scrollPaneAll = scrollPane;
        cleanupCounter = 0;
        determineTitle();
        determineIndex();
    }

    private void determineTitle() {
        //if we have only 2, it'll be channel1 + channel2
        if (panes.length == 2) {
            title = panes[0].getChannel() + " + " + panes[1].getChannel();
        } else if (panes.length > 2) {//3 or more is separated by commas.
            StringBuilder stanSB = new StringBuilder();
            for (int i = 0; i < panes.length; i++) {
                stanSB.append(panes[i].getChannel());
                if (i != panes.length - 1) stanSB.append(", ");
            }
            title = stanSB.toString();
        }
    }

    /**
     * Gets the lowest index of the non-visible tab.
     */
    private void determineIndex() {
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (ChatPane p : panes) {
            if (p.getIndex() < lowest && !p.isTabVisible()) lowest = p.getIndex();
            if (p.getIndex() > highest) highest = p.getIndex();
        }
        if (lowest == Integer.MAX_VALUE) {
            //this should only catch if control is held down for the tabs, so the tabs
            //are left being visible, so insert at highest + 1
            index = highest + 1;
        } else {
            index = lowest;
        }
    }


    public void onMessage(Message message) {
        SimpleAttributeSet user = new SimpleAttributeSet();
        StyleConstants.setFontFamily(user, GUIMain.currentSettings.font.getFamily());
        StyleConstants.setFontSize(user, GUIMain.currentSettings.font.getSize());
        String sender = message.getSender().toLowerCase();
        String channel = message.getChannel();
        boolean isMe = (message.getType() == Message.MessageType.ACTION_MESSAGE);
        String mess = message.getContent();
        String time = format.format(new Date(System.currentTimeMillis()));
        StyledDocument doc = textPane.getStyledDocument();
        Color c;
        if (GUIMain.userColMap.containsKey(sender)) {
            c = GUIMain.userColMap.get(sender);
        } else {
            c = Utils.getColorFromHashcode(sender.hashCode());
        }
        StyleConstants.setForeground(user, c);
        try {
            doc.insertString(doc.getLength(), "\n" + time, GUIMain.norm);
            User u = GUIMain.viewer.getViewer().getUser(channel, sender);
            if (u != null) {
                if (u.isOp()) {
                    if (!channel.substring(1).equals(sender) && !u.isStaff() && !u.isAdmin()) {//not the broadcaster again
                        insertIcon(doc, doc.getLength(), 0, null);
                    }
                }
                if (channel.substring(1).equals(sender)) {
                    insertIcon(doc, doc.getLength(), 1, null);
                }
                Donator d = Utils.getDonator(u.getNick());
                if (d != null) {
                    insertIcon(doc, doc.getLength(), d.getDonationStatus(), null);
                }
                if (u.isSubscriber()) {
                    insertIcon(doc, doc.getLength(), 5, channel);
                }
                if (u.isStaff()) {
                    insertIcon(doc, doc.getLength(), 3, null);
                }
                if (u.isAdmin()) {
                    insertIcon(doc, doc.getLength(), 2, null);
                }
                if (u.isTurbo()) {
                    insertIcon(doc, doc.getLength(), 4, null);
                }
            }
            int nameStart = doc.getLength() + 1;
            //we're always going to have the channel specified
            doc.insertString(doc.getLength(), " " + sender, user);
            doc.insertString(doc.getLength(), " (" + channel.substring(1) + ")" + (!isMe ? ": " : " "), (isMe ? user : GUIMain.norm));
            Utils.handleNames(doc, nameStart, sender, user);
            Utils.handleFaces(doc, nameStart, sender);//if the sender has a custom face that they want instead
            int messStart = doc.getLength();
            SimpleAttributeSet set;
            if (Utils.mentionsKeyword(mess)) {
                set = Utils.getSetForKeyword(mess);
            } else {
                set = (isMe ? user : GUIMain.norm);
            }
            doc.insertString(doc.getLength(), mess, set);
            Utils.handleFaces(doc, messStart, mess);
            Utils.handleTwitchFaces(doc, messStart, mess);
            Utils.handleURLs(doc, messStart, mess);
            if (GUIMain.currentSettings.cleanupChat) {
                cleanupCounter++;
                if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                /* cleanup every n messages */
                    if (cleanupChat()) {
                        cleanupCounter = 0;
                    }
                }
            }
            //TODO have a setting for pulsing tabs
            if (/*GUIMain.currentsettings.tabPulsing &&*/ shouldPulse) {
                GUIMain.instance.pulseTab(index);
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    private ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
            BufferedImage img = ImageIO.read(image);
            int size = GUIMain.currentSettings.font.getSize();
            img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size);
            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e) {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public void insertIcon(StyledDocument doc, int pos, int type, String channel) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        ImageIcon icon;
        String kind;
        switch (type) {
            case -1:
                return;
            case 0:
                icon = sizeIcon(GUIMain.currentSettings.modIcon);
                kind = "Mod";
                break;
            case 1:
                icon = sizeIcon(GUIMain.currentSettings.broadIcon);
                kind = "Broadcaster";
                break;
            case 2:
                icon = sizeIcon(GUIMain.currentSettings.adminIcon);
                kind = "Admin";
                break;
            case 3:
                icon = sizeIcon(GUIMain.currentSettings.staffIcon);
                kind = "Staff";
                break;
            case 4:
                icon = sizeIcon(GUIMain.currentSettings.turboIcon);
                kind = "Turbo";
                break;
            case 5:
                URL subIcon = Utils.getSubIcon(channel);
                if (subIcon == null) return;
                icon = sizeIcon(subIcon);
                kind = "Subscriber";
                break;
            case 6://donation normal
                icon = sizeIcon(ChatPane.class.getResource("/resource/green.png"));
                kind = "Donator";
                break;
            case 7:
                icon = sizeIcon(ChatPane.class.getResource("/resource/bronze.png"));
                kind = "Donator";
                break;
            case 8:
                icon = sizeIcon(ChatPane.class.getResource("/resource/silver.png"));
                kind = "Donator";
                break;
            case 9:
                icon = sizeIcon(ChatPane.class.getResource("/resource/gold.png"));
                kind = "Donator";
                break;
            case 10:
                icon = sizeIcon(ChatPane.class.getResource("/resource/diamond.png"));
                kind = "Donator";
                break;
            default:
                icon = sizeIcon(GUIMain.currentSettings.modIcon);
                kind = "Mod";
                break;
        }
        StyleConstants.setIcon(attrs, icon);
        try {
            doc.insertString(pos, " ", null);
            doc.insertString(pos + 1, kind, attrs);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    // Source: http://stackoverflow.com/a/4628879
    // by http://stackoverflow.com/users/131872/camickr & Community
    private boolean cleanupChat() {
        if (EventQueue.isDispatchThread()) {
            if (textPane == null || textPane.getParent() == null) return false;
            if (!(textPane.getParent() instanceof JViewport)) {
                return false;
            }
            JViewport viewport = ((JViewport) textPane.getParent());
            Point startPoint = viewport.getViewPosition();
            // we are not deleting right before the visible area, but one screen behind
            // for convenience, otherwise flickering.
            if (startPoint == null) return false;
            int start = textPane.viewToModel(startPoint);
            if (start > 0) // not equal zero, because then we don't have to delete anything
            {
                StyledDocument doc = textPane.getStyledDocument();
                try {
                    if (GUIMain.currentSettings.cleanupChat) {
                        if (GUIMain.currentSettings.logChat) {
                            String[] toRemove = doc.getText(0, start).split("\\n");
                            Utils.logChat(toRemove, getTabTitle(), 1);
                        }
                        doc.remove(0, start);
                        return true;
                    }
                } catch (BadLocationException e) {
                    // we cannot do anything here
                    GUIMain.log(e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    private boolean panesContains(ChatPane[] panesToCheck, ChatPane toCheck) {
        for (ChatPane p : panesToCheck) {
            if (p.getChannel().equalsIgnoreCase(toCheck.getChannel())) {
                return true;
            }
        }
        return false;
    }

    public boolean addChatPane(ChatPane... newPanes) {
        //null check
        if (newPanes == null) return false;

        ArrayList<String> channelsTemp = new ArrayList<>();
        Collections.addAll(channelsTemp, channels);
        ArrayList<ChatPane> panesTemp = new ArrayList<>();
        Collections.addAll(panesTemp, panes);

        //prevent adding a pane already in here/Copy the stuff
        for (ChatPane p : newPanes) {
            if (panesContains(panes, p)) {
                return false;
            } else {
                p.setTabVisible(false);
                panesTemp.add(p);
                channelsTemp.add(p.getChannel());
            }
        }


        channels = channelsTemp.toArray(new String[channelsTemp.size()]);
        panes = panesTemp.toArray(new ChatPane[panesTemp.size()]);

        //redetermine the index and title
        if (!customTitle) {
            determineTitle();
            setTabTitle(title);
        }
        determineIndex();
        return true;
    }


    /**
     * Creates a CombinedChatPane consisting of the specified panes.
     *
     * @param panes The panes to make into combined.
     * @return The created CombinedChatPane.
     */
    public static CombinedChatPane createCombinedChatPane(ChatPane... panes) {
        if (panes == null) return null;
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JTextPane pane = new JTextPane();
        pane.setEditorKit(new WrapEditorKit());
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setMargin(new Insets(0, 0, 0, 0));
        pane.setBackground(Color.black);
        pane.setFont(GUIMain.currentSettings.font);
        pane.addMouseListener(new ListenerURL());
        pane.addMouseListener(new ListenerName());
        scrollPane.setViewportView(pane);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.black));
        panel.add(scrollPane);
        //TODO put the scrollpane in a JPanel and set the border for the JPanel
        scrollPane.setViewportBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.black));

        for (ChatPane cp : panes) {
            for (TabPulse tp : GUIMain.tabPulses) {
                if (tp.getIndex() == cp.getIndex()) {
                    tp.interrupt();
                }
            }
            cp.setTabVisible(false);
        }
        //Tab adding is handled at the DraggableTabbedPane
        return new CombinedChatPane(scrollPane, pane, panes);
    }


    public void disbandTab() {
        setDefaultScrollPane();
        //we're going to add it all to the right of it and then remove the combined tab
        int index = getIndex() + 1;
        for (ChatPane cp : panes) {
            if (!Utils.isTabVisible(cp.getChannel())) {
                cp.setTabVisible(true);
                GUIMain.channelPane.insertTab(cp.getChannel(), null, cp.getScrollPane(), null, index);
                index++;
            }
        }
        //check for a pulse
        for (TabPulse tp : GUIMain.tabPulses) {
            if (tp.getIndex() == getIndex()) {
                tp.interrupt();
                GUIMain.tabPulses.remove(tp);
                break;
            }
        }

        GUIMain.channelPane.removeTabAt(getIndex());
        GUIMain.channelPane.updateIndexes();
        GUIMain.combinedChatPanes.remove(this);
    }


}
