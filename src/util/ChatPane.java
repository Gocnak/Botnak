package util;

import gui.GUIMain;
import gui.WrapEditorKit;
import lib.pircbot.org.jibble.pircbot.User;
import lib.scalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * All channels other than All Chats is stored in this format.
 */
public class ChatPane {


    private String chan;
    private JTextPane textPane;
    private int cleanupCounter;
    SimpleDateFormat format = new SimpleDateFormat("[h:mm a]", Locale.getDefault());

    /**
     * You initialize this class with the channel it's for, a randomly-made scroll pane,
     * and the text pane you'll be editing.
     *
     * @param channel The channel ("name") of this chat pane. Ex: "All Chats" or "#gocnak"
     * @param pane    The text pane that shows the messages for the given channel.
     */
    public ChatPane(String channel, JTextPane pane) {
        chan = channel;
        textPane = pane;
        cleanupCounter = 0;
    }

    /**
     * In order to update one pane that isn't hard coded requires a bit of trickery.
     * We'll have a set of these, but the channel will be the trigger, which is
     * compared elsewhere (GUIMain). The update happens; it can be a clearing event
     * (if they're clearing the chat), a logging event, and the line is added.
     * <p/>
     *
     * @param channel The channel the message was sent in.
     * @param sender  The sender of the message.
     * @param message The message from the chat.
     * @param isMe    Is a /me message.
     */
    public void onMessage(String channel, String sender, String message, boolean isMe) {
        if (GUIMain.currentSettings.cleanupChat) {
            cleanupCounter++;
            if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                /* cleanup every n messages */
                cleanupChat();
                cleanupCounter = 0;
            }
        }
        sender = sender.toLowerCase();
        String time = format.format(new Date(System.currentTimeMillis()));
        StyledDocument doc = textPane.getStyledDocument();
        Color c;
        if (GUIMain.userColMap.containsKey(sender)) {
            c = GUIMain.userColMap.get(sender);
        } else {
            c = Utils.getColor(sender.hashCode());
        }
        StyleConstants.setForeground(GUIMain.user, c);
        try {
            textPane.setCaretPosition(doc.getLength());
            doc.insertString(textPane.getCaretPosition(), time, GUIMain.norm);
            User u = Utils.getUser(GUIMain.viewer, channel, sender);
            if (u != null) {
                if (channel.substring(1).equals(sender)) {
                    insertIcon(doc, textPane.getCaretPosition(), 1);
                }
                if (u.isStaff()) {
                    insertIcon(doc, textPane.getCaretPosition(), 3);
                }
                if (u.isAdmin()) {
                    insertIcon(doc, textPane.getCaretPosition(), 2);
                }
                if (u.isTurbo()) {
                    insertIcon(doc, textPane.getCaretPosition(), 4);
                }
                if (u.isOp()) {
                    if (!channel.substring(1).equals(sender) && !u.isStaff() && !u.isAdmin()) {//not the broadcaster again
                        insertIcon(doc, textPane.getCaretPosition(), 0);
                    }
                }
            }
            int nameStart = textPane.getCaretPosition() + 1;
            if (chan.equalsIgnoreCase("all chats")) {
                doc.insertString(textPane.getCaretPosition(), " " + sender, GUIMain.user);
                doc.insertString(textPane.getCaretPosition(), " (" + channel.substring(1) + "): ", GUIMain.norm);
            } else {
                doc.insertString(textPane.getCaretPosition(), " " + sender + ": ", GUIMain.user);
            }
            Utils.handleFaces(doc, nameStart, sender);//if the sender has a custom face that they want instead
            int messStart = textPane.getCaretPosition();
            SimpleAttributeSet set;
            if (isMe) {
                if (Utils.mentionsKeyword(message)) {
                    set = Utils.getSetForKeyword(message);
                } else {
                    set = GUIMain.user;
                }
                doc.insertString(textPane.getCaretPosition(), message + "\n", set);
            } else {
                if (Utils.mentionsKeyword(message)) {
                    set = Utils.getSetForKeyword(message);
                } else {
                    set = GUIMain.norm;
                }
                doc.insertString(textPane.getCaretPosition(), message + "\n", set);
            }
            Utils.handleFaces(doc, messStart, message);
            Utils.handleTwitchFaces(doc, messStart, message);
            Utils.handleURLs(doc, messStart, message);
            textPane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    private ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
            BufferedImage img = ImageIO.read(image);
            int size = GUIMain.currentSettings.font.getSize();
            img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size, Scalr.OP_ANTIALIAS);
            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e) {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public void insertIcon(StyledDocument doc, int pos, int type) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        ImageIcon icon;
        String kind;
        switch (type) {
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
    private void cleanupChat() {
        if (textPane == null || textPane.getParent() == null) return;
        if (!(textPane.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = ((JViewport) textPane.getParent());
        Point startPoint = viewport.getViewPosition();
        // we are not deleting right before the visible area, but one screen behind
        // for convenience, otherwise flickering.
        int start = textPane.viewToModel(startPoint);
        if (start > 0) // not equal zero, because then we don't have to delete anything
        {
            Document doc = textPane.getDocument();
            try {
                if (GUIMain.currentSettings.cleanupChat) {
                    if (GUIMain.currentSettings.logChat) {
                        String[] toremove = doc.getText(0, start).split("\\n");
                        Utils.logChat(toremove, chan);
                    }
                    doc.remove(0, start);
                    textPane.setCaretPosition(doc.getLength());
                }
            } catch (BadLocationException e) {
                // we cannot do anything here
                GUIMain.log(e.getMessage());
            }
        }
    }

    /**
     * Creates a pane and adds it to a tab to the main GUI.
     *
     * @param channel The channel, also used as the key for the hashmap.
     */
    public static void createPane(String channel) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JTextPane pane = new JTextPane();
        pane.setEditorKit(new WrapEditorKit());
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setMargin(new Insets(0, 0, 0, 0));
        pane.setBackground(Color.black);
        pane.setFont(GUIMain.currentSettings.font);
        pane.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            //credit to Fenerista from
            //http://www.daniweb.com/software-development/java/threads/331500/how-can-i-add-a-clickable-url-in-a-jtextpane#post1422477
            @Override
            public void mouseReleased(MouseEvent e) {
                JTextPane editor = (JTextPane) e.getSource();
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.viewToModel(pt);
                if (pos >= 0) {
                    Document doc = editor.getDocument();
                    if (doc instanceof DefaultStyledDocument) {
                        DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                        Element el = hdoc.getCharacterElement(pos);
                        AttributeSet a = el.getAttributes();
                        String href = (String) a.getAttribute(HTML.Attribute.HREF);
                        if (href != null) {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                URI uri = new URI(href);
                                desktop.browse(uri);
                            } catch (Exception ev) {
                                GUIMain.log((ev.getMessage()));
                            }
                        }
                    }
                }
            }
        });
        scrollPane.setViewportView(pane);
        GUIMain.channelPane.addTab(channel, scrollPane);
        GUIMain.chatPanes.put(channel, new ChatPane(channel, pane));
    }

    public void deletePane() {
        for (int i = 0; i < GUIMain.channelPane.getTabCount(); i++) {
            String name = GUIMain.channelPane.getTitleAt(i);
            if (name != null) {
                if (name.equalsIgnoreCase("all chats")) continue;//this will most likely be 0 every time
                if (name.equalsIgnoreCase(chan)) {
                    if (GUIMain.currentSettings.logChat) {
                        Utils.logChat(getText().split("\\n"), chan);
                    }
                    GUIMain.channelPane.removeTabAt(i);
                }
            }
        }
    }

    public void log(String message) {
        String time = format.format(new Date(System.currentTimeMillis()));
        StyledDocument doc = textPane.getStyledDocument();
        try {
            textPane.setCaretPosition(doc.getLength());
            doc.insertString(textPane.getCaretPosition(), time + " SYS: " + message + "\n", GUIMain.norm);
            textPane.setCaretPosition(doc.getLength());
            if (GUIMain.currentSettings.cleanupChat) {
                cleanupCounter++;
                if (cleanupCounter > GUIMain.currentSettings.chatMax) {
                /* cleanup every n messages */
                    if (GUIMain.doneWithFaces) {
                        cleanupChat();
                        cleanupCounter = 0;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


}
