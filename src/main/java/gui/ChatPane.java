package gui;

import face.FaceManager;
import gui.listeners.ListenerName;
import gui.listeners.ListenerURL;
import irc.Donor;
import irc.message.Message;
import irc.message.MessageWrapper;
import lib.pircbot.org.jibble.pircbot.User;
import lib.scalr.Scalr;
import util.Constants;
import util.Utils;
import util.misc.Donation;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * All channels are stored in this format.
 */
public class ChatPane implements DocumentListener {

    private JFrame poppedOutPane = null;

    public void setPoppedOutPane(JFrame pane) {
        poppedOutPane = pane;
    }

    public JFrame getPoppedOutPane() {
        return poppedOutPane;
    }

    public void createPopOut() {
        if (poppedOutPane == null) {
            JFrame frame = new JFrame(chan);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    getScrollPane().setViewportView(getTextPane());
                    scrollToBottom();
                    setPoppedOutPane(null);
                }
            });
            JScrollPane pane = new JScrollPane();
            frame.setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
            pane.setViewportView(getTextPane());
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            pane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
            frame.add(pane);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setSize(750, 420);
            frame.setVisible(true);
            setPoppedOutPane(frame);
        }
    }

    /**
     * Keeps track of how many subs this channel gets.
     * TODO: make this a statistic that the user can output to a file ("yesterday sub #")
     */
    private int subCount = 0;

    private int viewerCount = -1;
    private int viewerPeak = 0;

    public void setViewerCount(int newCount) {
        if (newCount > viewerPeak) viewerPeak = newCount;
        viewerCount = newCount;
        if (getPoppedOutPane() != null) poppedOutPane.setTitle(getPoppedOutTitle());
        if (GUIMain.channelPane.getSelectedIndex() == index) GUIMain.updateTitle(getViewerCountString());
    }

    public String getPoppedOutTitle() {
        return chan + " | " + getViewerCountString();
    }

    public String getViewerCountString() {
        if (chan.equalsIgnoreCase("system logs")) return null;
        if (viewerCount == -1) return "Viewer count: Offline";
        return String.format("Viewer count: %d (%d)", viewerCount, viewerPeak);
    }

    /**
     * This is the main boolean to check to see if this tab should pulse.
     * <p>
     * This boolean checks to see if the tab wasn't toggled, if it's visible (not in a combined tab),
     * and if it's not selected. TODO check for global setting of pulsing tabs
     *
     * @return True if this tab should pulse, else false.
     */
    public boolean shouldPulse() {
        boolean shouldPulseLocal = (this instanceof CombinedChatPane) ?
                ((CombinedChatPane) this).getActiveChatPane().shouldPulseLoc() : shouldPulseLoc;
        return shouldPulseLocal && isTabVisible() && GUIMain.channelPane.getSelectedIndex() != index && index != 0;
    }

    private boolean shouldPulseLoc = true;

    /**
     * Determines if this tab should pulse.
     *
     * @return True if this tab is not toggled off, else false. ("Tab Pulsing OFF")
     */
    public boolean shouldPulseLoc() {
        return shouldPulseLoc;
    }

    /**
     * Sets the value for if this tab should pulse or not.
     *
     * @param newBool True (default) if tab pulsing should happen, else false if you wish to
     *                toggle tab pulsing off.
     */
    public void setShouldPulse(boolean newBool) {
        shouldPulseLoc = newBool;
    }


    /**
     * Sets the pulsing boolean if this tab is starting to pulse.
     * <p>
     * Used by the TabPulse class.
     *
     * @param isPulsing True if the tab is starting to pulse, else false to stop pulsing.
     */
    public void setPulsing(boolean isPulsing) {
        this.isPulsing = isPulsing;
    }

    /**
     * Used by the TabPulse class.
     *
     * @return true if the chat pane is currently pulsing, else false.
     */
    public boolean isPulsing() {
        return isPulsing;
    }

    private boolean isPulsing = false;

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

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (GUIMain.currentSettings.cleanupChat) {
            try {
                if (e.getDocument().getText(e.getOffset(), e.getLength()).contains("\n")) {
                    cleanupCounter++;
                }
            } catch (Exception ignored) {
            }
            if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                    /* cleanup every n messages */
                if (cleanupChat()) {
                    resetCleanupCounter();
                }
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
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
        EventQueue.invokeLater(() -> EventQueue.invokeLater(this::doScrollToBottom));
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

    private JTextPane textPane;

    public JTextPane getTextPane() {
        return textPane;
    }

    private JScrollPane scrollPane;

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(JScrollPane pane) {
        scrollPane = pane;
    }

    private boolean isTabVisible = true;

    public boolean isTabVisible() {
        return isTabVisible;
    }

    public void setTabVisible(boolean newBool) {
        isTabVisible = newBool;
    }

    private int cleanupCounter = 0;

    public void resetCleanupCounter() {
        cleanupCounter = 0;
    }

    //TODO make this be in 24 hour if they want
    final SimpleDateFormat format = new SimpleDateFormat("[h:mm a]", Locale.getDefault());

    public String getTime() {
        return format.format(new Date(System.currentTimeMillis()));
    }

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
        textPane.getDocument().addDocumentListener(this);
        textPane.getDocument().addDocumentListener(new ScrollingDocumentListener());
    }

    public ChatPane() {
        //Used by the CombinedChatPane class, which calls its super anyways.
    }

    /**
     * This is the main message method when somebody sends a message to the channel.
     *
     * @param m The message from the chat.
     */
    public void onMessage(MessageWrapper m, boolean showChannel) {
        Message message = m.getLocal();
        SimpleAttributeSet user = new SimpleAttributeSet();
        StyleConstants.setFontFamily(user, GUIMain.currentSettings.font.getFamily());
        StyleConstants.setFontSize(user, GUIMain.currentSettings.font.getSize());
        String sender = message.getSender().toLowerCase();
        String channel = message.getChannel();
        String mess = message.getContent();
        boolean isMe = (message.getType() == Message.MessageType.ACTION_MESSAGE);
        try {
            print(m, "\n" + getTime(), GUIMain.norm);
            User u = GUIMain.currentSettings.channelManager.getUser(sender, true);
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
                insertIcon(m, 1, null);
            }
            if (u.isOp(channel)) {
                if (!channel.substring(1).equals(sender) && !u.isStaff() && !u.isAdmin()) {//not the broadcaster again
                    insertIcon(m, 0, null);
                }
            }
            if (u.isDonor()) {
                insertIcon(m, u.getDonationStatus(), null);
            }
            if (u.isStaff()) {
                insertIcon(m, 3, null);
            }
            if (u.isAdmin()) {
                insertIcon(m, 2, null);
            }
            boolean isSubsciber = u.isSubscriber(channel);
            if (isSubsciber) {
                insertIcon(m, 5, channel);
            }
            if (u.isTurbo()) {
                insertIcon(m, 4, null);
            }
            //name stuff
            user.addAttribute(HTML.Attribute.NAME, sender);
            SimpleAttributeSet userColor = new SimpleAttributeSet(user);
            FaceManager.handleNameFaces(sender, user);
            if (showChannel) {
                print(m, " " + sender, user);
                print(m, " (" + channel.substring(1) + ")" + (isMe ? " " : ": "), GUIMain.norm);
            } else {
                print(m, " " + sender, user);
                print(m, (!isMe ? ": " : " "), userColor);
            }
            //keyword?
            SimpleAttributeSet set;
            if (Utils.mentionsKeyword(mess)) {
                set = Utils.getSetForKeyword(mess);
            } else {
                set = (isMe ? userColor : GUIMain.norm);
            }
            //URL, Faces, rest of message
            printMessage(m, mess, set);

            if (channel.substring(1).equalsIgnoreCase(GUIMain.currentSettings.accountManager.getUserAccount().getName()))
                //check status of the sub, has it been a month?
                GUIMain.currentSettings.subscriberManager.updateSubscriber(u, channel, isSubsciber);
            if (shouldPulse())
                GUIMain.instance.pulseTab(this);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Credit: TDuva
     *
     * Cycles through message data, tagging things like Faces and URLs.
     *
     * @param text The message
     * @param style The default message style to use.
     */
    protected void printMessage(MessageWrapper m, String text, SimpleAttributeSet style) {
        // Where stuff was found
        TreeMap<Integer, Integer> ranges = new TreeMap<>();
        // The style of the stuff (basicially metadata)
        HashMap<Integer, SimpleAttributeSet> rangesStyle = new HashMap<>();

        findLinks(text, ranges, rangesStyle);

        findEmoticons(text, ranges, rangesStyle);

        // Actually print everything
        int lastPrintedPos = 0;
        Iterator<Map.Entry<Integer, Integer>> rangesIt = ranges.entrySet().iterator();
        while (rangesIt.hasNext()) {
            Map.Entry<Integer, Integer> range = rangesIt.next();
            int start = range.getKey();
            int end = range.getValue();
            if (start > lastPrintedPos) {
                // If there is anything between the special stuff, print that
                // first as regular text
                print(m, text.substring(lastPrintedPos, start), style);
            }
            print(m, text.substring(start, end + 1), rangesStyle.get(start));
            lastPrintedPos = end + 1;
        }
        // If anything is left, print that as well as regular text
        if (lastPrintedPos < text.length()) {
            print(m, text.substring(lastPrintedPos), style);
        }

    }

    private void findLinks(String text, Map<Integer, Integer> ranges, Map<Integer, SimpleAttributeSet> rangesStyle) {
        // Find links
        Constants.urlMatcher.reset(text);
        while (Constants.urlMatcher.find()) {
            int start = Constants.urlMatcher.start();
            int end = Constants.urlMatcher.end() - 1;
            if (!Utils.inRanges(start, ranges) && !Utils.inRanges(end, ranges)) {
                String foundUrl = Constants.urlMatcher.group();
                if (Utils.checkURL(foundUrl)) {
                    ranges.put(start, end);
                    rangesStyle.put(start, Utils.URLStyle(foundUrl));
                }
            }
        }
    }


    private void findEmoticons(String text, Map<Integer, Integer> ranges, Map<Integer, SimpleAttributeSet> rangesStyle) {
        FaceManager.handleFaces(ranges, rangesStyle, text, FaceManager.FACE_TYPE.NORMAL_FACE);
        FaceManager.handleFaces(ranges, rangesStyle, text, FaceManager.FACE_TYPE.TWITCH_FACE);
    }

    protected void print(MessageWrapper wrapper, String string, SimpleAttributeSet set) {
        Runnable r = () -> {
            try {
                textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), string, set);
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        };
        wrapper.addPrint(r);
    }

    /**
     * Handles inserting icons before and after the message.
     *
     * @param m       The message itself.
     * @param status  5 for sub message, else pass Donor#getDonationStatus(d#getAmount())
     */
    public void onIconMessage(MessageWrapper m, int status) {
        try {
            Message message = m.getLocal();
            print(m, "\n", GUIMain.norm);
            for (int i = 0; i < 5; i++) {
                insertIcon(m, status, (status == 5 ? message.getChannel() : null));
            }
            print(m, " " + message.getContent() + (status == 5 ? (" (" + (subCount + 1) + ") ") : " "), GUIMain.norm);
            for (int i = 0; i < 5; i++) {
                insertIcon(m, status, (status == 5 ? message.getChannel() : null));
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        if (status == 5) subCount++;
    }

    public void onSub(MessageWrapper m) {
        onIconMessage(m, 5);
    }

    public void onDonation(MessageWrapper m) {
        Donation d = (Donation) m.getLocal().getExtra();
        onIconMessage(m, Donor.getDonationStatus(d.getAmount()));
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

    public void insertIcon(MessageWrapper m, int type, String channel) {
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
                URL subIcon = FaceManager.getSubIcon(channel);
                if (subIcon == null) return;
                icon = sizeIcon(subIcon);
                kind = "Subscriber";
                break;
            case 6://donation normal
                icon = sizeIcon(ChatPane.class.getResource("/image/green.png"));
                kind = "Donator";
                break;
            case 7:
                icon = sizeIcon(ChatPane.class.getResource("/image/bronze.png"));
                kind = "Donator";
                break;
            case 8:
                icon = sizeIcon(ChatPane.class.getResource("/image/silver.png"));
                kind = "Donator";
                break;
            case 9:
                icon = sizeIcon(ChatPane.class.getResource("/image/gold.png"));
                kind = "Donator";
                break;
            case 10:
                icon = sizeIcon(ChatPane.class.getResource("/image/diamond.png"));
                kind = "Donator";
                break;
            default:
                icon = sizeIcon(GUIMain.currentSettings.modIcon);
                kind = "Mod";
                break;
        }
        StyleConstants.setIcon(attrs, icon);
        try {
            print(m, " ", null);
            print(m, kind, attrs);
        } catch (Exception e) {
            GUIMain.log("INSERT ICON " + e.getMessage());
        }
    }

    public String getText() {
        return (textPane != null && textPane.getText() != null) ? textPane.getText() : "";
    }

    // Source: http://stackoverflow.com/a/4628879
    // by http://stackoverflow.com/users/131872/camickr & Community
    public boolean cleanupChat() {
        if (textPane == null || textPane.getParent() == null) return false;
        if (!(textPane.getParent() instanceof JViewport)) {
            return false;
        }
        JViewport viewport = ((JViewport) textPane.getParent());
        Point startPoint = viewport.getViewPosition();
        // we are not deleting right before the visible area, but one screen behind
        // for convenience, otherwise flickering.
        if (startPoint == null) return false;
        final int start = textPane.viewToModel(startPoint);
        if (start > 0) // not equal zero, because then we don't have to delete anything
        {
            final StyledDocument doc = textPane.getStyledDocument();
            try {
                if (GUIMain.currentSettings.cleanupChat) {
                    if (GUIMain.currentSettings.logChat && chan != null) {
                        String[] toRemove = doc.getText(0, start).split("\\n");
                        Utils.logChat(toRemove, chan, 1);
                    }
                    EventQueue.invokeLater(() -> {
                        try {
                            doc.remove(0, start);
                        } catch (Exception ignored) {
                        }
                    });
                    return true;
                }
            } catch (BadLocationException e) {
                // we cannot do anything here
                GUIMain.log("CLEANUP CHAT " + e.getMessage());
                return false;
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
        //TODO ensure the viewer list & other popped out GUIs are deleted
        if (getPoppedOutPane() != null) {
            getPoppedOutPane().dispose();
        }
        GUIMain.channelPane.removeTabAt(index);
        GUIMain.channelPane.setSelectedIndex(index - 1);
    }

    /**
     * Logs a message to this chat pane.
     *
     * @param message  The message itself.
     * @param isSystem Whether the message is a system log message or not.
     */
    public void log(String message, boolean isSystem) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "\n" + getTime(), GUIMain.norm);
            doc.insertString(doc.getLength(), " " + (isSystem ? "SYS: " : "") + message, GUIMain.norm);
        } catch (Exception ignored) {
        }
    }
}
