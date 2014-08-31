package gui.listeners;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

/**
 * Created by Nick on 1/18/14.
 */
public class PaneMenuListener implements ActionListener {


    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        if (source != null && source.getText() != null) {
            ChatPane pane = Utils.getChatPane(GUIMain.channelPane.getSelectedIndex());
            String text = source.getText();
            if (text.startsWith("Pop-out")) {
                if (pane != null) {
                    pane.createPopOut();
                }
            } else if (text.startsWith("Toggle Tab")) {
                if (pane != null) {
                    pane.setShouldPulse(text.contains("ON"));
                } else {
                    CombinedChatPane ccp = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                    if (ccp != null) {
                        ccp.setShouldPulse(text.contains("ON"));
                    }
                }
            } else if (text.startsWith("View viewer")) {
                //TODO create an alphabetically sorted viewer list in a JPanel or something,
                //which has clickable names that will pop out viewer stats
                if (pane != null) {

                }
            } else if (text.startsWith("Remove ")) {
                if (pane != null) {
                    if (GUIMain.viewer != null) {
                        GUIMain.viewer.doLeave(pane.getChannel());
                    }
                    if (GUIMain.bot != null) {
                        GUIMain.bot.doLeave(pane.getChannel());
                    }
                    GUIMain.channelSet.remove(pane.getChannel());
                    GUIMain.chatPanes.remove(pane.getChannel());
                    pane.deletePane();
                    GUIMain.channelPane.updateIndexes();
                }
            } else if (text.startsWith("Disband")) {
                CombinedChatPane ccp = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                if (ccp != null) {
                    ccp.disbandTab();
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
            if (source instanceof JCheckBoxMenuItem) {
                String channel = source.getText();
                if (channel != null) {
                    CombinedChatPane ccp = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                    if (ccp.getActiveChannel().equalsIgnoreCase(channel)) {
                        return;
                    }
                    ccp.setActiveChannel(channel);
                    if (channel.equalsIgnoreCase("All")) {
                        ccp.setDefaultScrollPane();
                    } else {
                        //and pulse the + tab as well
                        ccp.setActiveScrollPane(channel);
                    }
                }
            }
        }
    }
}
