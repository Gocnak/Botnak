package com.gocnak.gui.listeners;

import com.gocnak.gui.ChatPane;
import com.gocnak.gui.CombinedChatPane;
import com.gocnak.gui.GUIMain;
import com.gocnak.util.Utils;
import org.jibble.pircbot.User;

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

    @Override
    public void mouseReleased(MouseEvent e) {
        JTextPane textPane = (JTextPane) e.getSource();
        Point pt = new Point(e.getX(), e.getY());
        int pos = textPane.viewToModel(pt);
        if (pos >= 0) {
            Document doc = textPane.getDocument();
            if (doc instanceof DefaultStyledDocument) {
                ChatPane cp = Utils.getChatPane(GUIMain.channelPane.getSelectedIndex());
                CombinedChatPane ccp = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                String channel = (cp != null ? cp.getChannel() : (ccp != null ? ccp.getActiveChannel() : ""));
                DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                Element el = hdoc.getCharacterElement(pos);
                AttributeSet a = el.getAttributes();
                String name = (String) a.getAttribute(HTML.Attribute.NAME);
                if (name != null) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    if (!(channel.equals("") || channel.equalsIgnoreCase("all"))) {
                        if (GUIMain.viewer != null) {
                            User u = GUIMain.currentSettings.channelManager.getUser(name, false);//get the user in question
                            User main = GUIMain.currentSettings.channelManager.getUser(GUIMain.currentSettings.accountManager.getUserAccount().getName(), false);//get yourself
                            if (main != null) {
                                int count = 0; //don't worry about it
                                //can't ban broadcaster or admin/staff/global mod
                                if (u != null && (u.isAdmin() || u.isGlobalMod() || u.isStaff() || name.equalsIgnoreCase(channel.replace("#", ""))))
                                    count++;

                                //can't ban other mods if you aren't the broadcaster
                                if (u != null && (!main.getNick().equalsIgnoreCase(channel.replace("#", "")) && (main.isOp(channel) && u.isOp(channel))))
                                    count++;

                                //Feature added: the ability to purge spammers' messages, even if they leave
                                if (count == 0 && (channel.replace("#", "").equalsIgnoreCase(main.getNick()) || main.isOp(channel))) {
                                    //it's your channel OR you're op and the other user isn't.
                                    JMenuItem menuItem;
                                    menuItem = new JMenuItem("Purge " + name);
                                    menuItem.addActionListener(this);
                                    popupMenu.add(menuItem);
                                    menuItem = new JMenuItem("Timeout " + name);
                                    menuItem.addActionListener(this);
                                    popupMenu.add(menuItem);
                                    menuItem = new JMenuItem("Ban " + name);
                                    menuItem.addActionListener(this);
                                    popupMenu.add(menuItem);
                                    if (channel.replace("#", "").equalsIgnoreCase(main.getNick()) && u != null) {
                                        //you can only (un)mod people in your chat that are(n't) mods
                                        menuItem = new JMenuItem((u.isOp(channel) ? "Un-mod " : "Mod ") + name);
                                        menuItem.addActionListener(this);
                                        popupMenu.add(menuItem);
                                    }
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

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        if (source != null && source.getText() != null) {
            String text = source.getText();
            String user = text.split(" ")[1];
            String channel = "#" + GUIMain.channelPane.getTitleAt(GUIMain.channelPane.getSelectedIndex());
            if (text.startsWith("Ban")) {
                if (GUIMain.viewer != null) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".ban " + user);
                }
            } else if (text.startsWith("Purge")) {
                if (GUIMain.viewer != null) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".timeout " + user + " 1");
                }
            } else if (text.startsWith("Timeout")) {
                if (GUIMain.viewer != null) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".timeout " + user);
                }
            } else if (text.startsWith("Mod")) {
                if (GUIMain.viewer != null) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".mod " + user);
                }
            } else if (text.startsWith("Un-mod")) {
                if (GUIMain.viewer != null) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".unmod " + user);
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