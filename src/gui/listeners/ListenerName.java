package gui.listeners;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.User;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * Created by Nick on 12/31/13.
 */
public class ListenerName extends MouseAdapter implements ActionListener {

    public ListenerName() {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        JTextPane textPane = (JTextPane) e.getSource();
        Point pt = new Point(e.getX(), e.getY());
        int pos = textPane.viewToModel(pt);
        if (pos >= 0) {
            Document doc = textPane.getDocument();
            if (doc instanceof DefaultStyledDocument) {
                String channel = GUIMain.channelPane.getTitleAt(GUIMain.channelPane.getSelectedIndex());
                DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                Element el = hdoc.getCharacterElement(pos);
                AttributeSet a = el.getAttributes();
                String name = (String) a.getAttribute(HTML.Attribute.NAME);
                if (name != null) {
                    if (GUIMain.viewer != null) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        if (!channel.equalsIgnoreCase("all chats")) {
                            User u = GUIMain.viewer.getUser("#" + channel, name);//get the user in question
                            User main = GUIMain.viewer.getUser("#" + channel, GUIMain.viewer.getMaster());//get yourself
                            if ((main != null && u != null) && !(u.isAdmin() || u.isStaff() || u.getNick().equalsIgnoreCase(channel))) {
                                //can't ban broadcaster or admin/staff
                                if (channel.equalsIgnoreCase(main.getNick()) || (main.isOp() && !u.isOp())) {
                                    //it's your channel, ops and everyone else can be banned
                                    //OR you're op and the other user isn't.
                                    JMenuItem menuItem;
                                    menuItem = new JMenuItem("Ban " + name);
                                    menuItem.addActionListener(this);
                                    popupMenu.add(menuItem);
                                    menuItem = new JMenuItem("Timeout " + name);
                                    menuItem.addActionListener(this);
                                    popupMenu.add(menuItem);
                                    if (channel.equalsIgnoreCase(main.getNick())) {
                                        //you can only (un)mod people in your chat that are(n't) mods
                                        menuItem = new JMenuItem((u.isOp() ? "Un-mod " : "Mod ") + name);
                                        menuItem.addActionListener(this);
                                        popupMenu.add(menuItem);
                                    }
                                }
                            }
                        }
                        JMenuItem menuItem = new JMenuItem("Go to " + name + "'s channel");
                        menuItem.addActionListener(this);
                        popupMenu.add(menuItem);
                        popupMenu.show(textPane, e.getX(), e.getY());
                    }
                }
            }
        }
        super.mouseReleased(e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        if (source != null && source.getText() != null) {
            String text = source.getText();
            String user = text.split(" ")[1];
            String channel = GUIMain.channelPane.getTitleAt(GUIMain.channelPane.getSelectedIndex());
            if (text.startsWith("Ban")) {
                if (GUIMain.viewer != null && !channel.equalsIgnoreCase("all chats")) {
                    GUIMain.viewer.sendMessage(channel, ".ban " + user);
                }
            } else if (text.startsWith("Timeout")) {
                if (GUIMain.viewer != null && !channel.equalsIgnoreCase("all chats")) {
                    GUIMain.viewer.sendMessage(channel, ".timeout " + user);
                }
            } else if (text.startsWith("Mod")) {
                if (GUIMain.viewer != null && !channel.equalsIgnoreCase("all chats")) {
                    GUIMain.viewer.sendMessage(channel, ".mod " + user);
                }
            } else if (text.startsWith("Un-mod")) {
                if (GUIMain.viewer != null && !channel.equalsIgnoreCase("all chats")) {
                    GUIMain.viewer.sendMessage(channel, ".unmod " + user);
                }
            } else if (text.startsWith("Go to")) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    //ex: Go to gocnak's channel
                    //    0123456     ^ substring
                    String name = text.substring(6, text.indexOf("'"));
                    URI uri = new URI("http://twitch.tv/" + name);
                    desktop.browse(uri);
                } catch (Exception ev) {
                    GUIMain.log((ev.getMessage()));
                }
            }
        }
    }
}
