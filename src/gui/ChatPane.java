package gui;

import gui.listeners.ListenerName;
import gui.listeners.ListenerURL;
import irc.Donator;
import irc.Message;
import lib.pircbot.org.jibble.pircbot.User;
import lib.scalr.Scalr;
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
import java.util.Date;
import java.util.Locale;

/**
 * All channels are stored in this format.
 */
public class ChatPane {

    public boolean shouldPulse() {
        return shouldPulse;
    }

    private int subCount = 0;

    private int viewerCount = 0;
    private int viewerPeak = 0;


    public int getViewerPeak() {
        return viewerPeak;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(int newCount) {
        if (newCount > viewerPeak) viewerPeak = newCount;
        viewerCount = newCount;
    }

    private boolean shouldPulse = true;

    public void setShouldPulse(boolean newBool) {
        shouldPulse = newBool;
    }

    //credit to http://stackoverflow.com/a/4047794 for the below
    public boolean isScrollBarFullyExtended(JScrollBar vScrollBar) {
        BoundedRangeModel model = vScrollBar.getModel();
        return (model.getExtent() + model.getValue()) == model.getMaximum();
    }

    public void doScrollToBottom() {
        if (textPane.isVisible()) {
            Rectangle visibleRect = textPane.getVisibleRect();
            visibleRect.y = textPane.getHeight() - visibleRect.height;
            textPane.scrollRectToVisible(visibleRect);
        } else {
            textPane.setCaretPosition(textPane.getDocument().getLength());
        }
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
                scrollToBottom();
            }
        }
    }

    public void scrollToBottom() {
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
                        doScrollToBottom();
                    }
                });
            }
        });
    }

    private String chan;

    public String getChannel() {
        return chan;
    }

    private int index;

    public void setIndex(int newIndex) {
        index = newIndex;
    }

    public int getIndex() {
        return index;
    }

    public JTextPane textPane;

    public JTextPane getTextPane() {
        return textPane;
    }

    public JScrollPane scrollPane;

    public JScrollPane getScrollPane() {
        return scrollPane;
    }


    private boolean isTabVisible = true;

    public boolean isTabVisible() {
        return isTabVisible;
    }

    public void setTabVisible(boolean newBool) {
        isTabVisible = newBool;
    }

    private int cleanupCounter = 0;

    //TODO make this be in 24 hour if they want
    final SimpleDateFormat format = new SimpleDateFormat("[h:mm a]", Locale.getDefault());

    /**
     * You initialize this class with the channel it's for and the text pane you'll be editing.
     *
     * @param channel    The channel ("name") of this chat pane. Ex: "System Logs" or "#gocnak"
     * @param scrollPane The scroll pane for the tab.
     * @param pane       The text pane that shows the messages for the given channel.
     * @param index      The index of the pane in the main GUI.
     */
    public ChatPane(String channel, JScrollPane scrollPane, JTextPane pane, int index) {
        chan = channel;
        textPane = pane;
        ((DefaultCaret) textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.index = index;
        this.scrollPane = scrollPane;
        textPane.getDocument().addDocumentListener(new ScrollingDocumentListener());
        textPane.getStyledDocument().addDocumentListener(new ScrollingDocumentListener());
    }

    public ChatPane() {

    }

    /**
     * In order to update one pane that isn't hard coded requires a bit of trickery.
     * We'll have a set of these, but the channel will be the trigger, which is
     * compared elsewhere (GUIMain). The update happens; it can be a clearing event
     * (if they're clearing the chat), a logging event, and the line is added.
     * <p/>
     *
     * @param message The message from the chat.
     */
    public void onMessage(Message message, boolean showChannel) {
        SimpleAttributeSet user = new SimpleAttributeSet();
        StyleConstants.setFontFamily(user, GUIMain.currentSettings.font.getFamily());
        StyleConstants.setFontSize(user, GUIMain.currentSettings.font.getSize());
        String sender = message.getSender().toLowerCase();
        String channel = message.getChannel();
        String mess = message.getContent();
        boolean isMe = (message.getType() == Message.MessageType.ACTION_MESSAGE);
        String time = format.format(new Date(System.currentTimeMillis()));
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "\n" + time, GUIMain.norm);
            User u = GUIMain.viewer.getViewer().getUser(channel, sender);
            if (u != null) {
                Color c;
                if (u.getColor() != null) {
                    if (GUIMain.userColMap.containsKey(sender)) {
                        c = GUIMain.userColMap.get(sender);
                    } else {
                        c = u.getColor();
                        if (!Utils.checkColor(c)) {
                            c = Utils.getColorFromHashcode(sender.hashCode());
                        }
                    }
                } else {//temporarily assign their color as randomly generated
                    c = Utils.getColorFromHashcode(sender.hashCode());
                }
                StyleConstants.setForeground(user, c);
                if (channel.substring(1).equals(sender)) {
                    insertIcon(doc, doc.getLength(), 1, null);
                }
                if (u.isOp()) {
                    if (!channel.substring(1).equals(sender) && !u.isStaff() && !u.isAdmin()) {//not the broadcaster again
                        insertIcon(doc, doc.getLength(), 0, null);
                    }
                }
                Donator d = Utils.getDonator(u.getNick());
                if (d != null) {
                    insertIcon(doc, doc.getLength(), d.getDonationStatus(), null);
                }
                if (u.isStaff()) {
                    insertIcon(doc, doc.getLength(), 3, null);
                }
                if (u.isAdmin()) {
                    insertIcon(doc, doc.getLength(), 2, null);
                }
                if (u.isSubscriber()) {
                    insertIcon(doc, doc.getLength(), 5, channel);
                }
                if (u.isTurbo()) {
                    insertIcon(doc, doc.getLength(), 4, null);
                }
            }
            int nameStart = doc.getLength() + 1;
            if (showChannel) {
                doc.insertString(doc.getLength(), " " + sender, user);
                doc.insertString(doc.getLength(), " (" + channel.substring(1) + ")" + (isMe ? " " : ": "), GUIMain.norm);
            } else {
                doc.insertString(doc.getLength(), " " + sender + (!isMe ? ": " : " "), user);
            }
            Utils.handleNames(doc, nameStart, sender, user);
            Utils.handleNameFaces(doc, nameStart, sender);
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
                    if (cleanupChat())
                        cleanupCounter = 0;
                }
            }
            if (index != 0 && isTabVisible && shouldPulse)
                GUIMain.instance.pulseTab(index);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void onBan(String message) {
        if (EventQueue.isDispatchThread()) {
            String time = format.format(new Date(System.currentTimeMillis()));
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), "\n" + time, GUIMain.norm);
                doc.insertString(doc.getLength(), " " + message, GUIMain.norm);
            } catch (Exception ignored) {
            }
            if (GUIMain.currentSettings.cleanupChat) {
                cleanupCounter++;
                if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                    /* cleanup every n messages */
                    if (cleanupChat()) {
                        cleanupCounter = 0;
                    }
                }
            }
        }
    }

    public void onSub(Message message) {
        if (EventQueue.isDispatchThread()) {
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), "\n", GUIMain.norm);
                for (int i = 0; i < 5; i++) {
                    insertIcon(doc, doc.getLength(), 5, message.getChannel());
                }
                //TODO make a setting for the color for the sub message
                doc.insertString(doc.getLength(), message.getContent() + " (" + (subCount + 1) + ")", GUIMain.norm);
                for (int i = 0; i < 5; i++) {
                    insertIcon(doc, doc.getLength(), 5, message.getChannel());
                }
            } catch (Exception ignored) {
            }
            subCount++;
            if (GUIMain.currentSettings.cleanupChat) {
                cleanupCounter++;
                if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                    /* cleanup every n messages */
                    if (cleanupChat()) {
                        cleanupCounter = 0;
                    }
                }
            }
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

    public String getText() {
        return (textPane != null && textPane.getText() != null) ? textPane.getText() : "";
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
                        if (GUIMain.currentSettings.logChat && chan != null) {
                            String[] toRemove = doc.getText(0, start).split("\\n");
                            Utils.logChat(toRemove, chan, 1);
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

    /**
     * Creates a pane of the given channel.
     *
     * @param channel The channel, also used as the key for the hashmap.
     * @return The created ChatPane.
     */
    public static ChatPane createPane(String channel) {
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
        return new ChatPane(channel, scrollPane, pane, GUIMain.channelPane.getTabCount() - 1);
    }

    /**
     * Deletes the pane and removes the tab from the tabbed pane.
     */
    public void deletePane() {
        if (GUIMain.currentSettings.logChat) {
            Utils.logChat(getText().split("\\n"), chan, 2);
        }
        GUIMain.channelPane.removeTabAt(index);
        GUIMain.channelPane.setSelectedIndex(index - 1);
    }

    public void log(String message) {
        if (EventQueue.isDispatchThread()) {
            String time = format.format(new Date(System.currentTimeMillis()));
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), "\n" + time + " SYS: " + message, GUIMain.norm);
                if (GUIMain.currentSettings.cleanupChat) {
                    cleanupCounter++;
                    if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                    /* cleanup every n messages */
                        if (GUIMain.doneWithTwitchFaces) {
                            if (cleanupChat()) {
                                cleanupCounter = 0;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
