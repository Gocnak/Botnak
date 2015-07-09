package gui.listeners;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import gui.GUIViewerList;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
                if (pane != null) {
                    GUIViewerList newVL = new GUIViewerList(pane.getChannel());
                    newVL.setVisible(true);
                    GUIMain.viewerLists.put(pane.getChannel(), newVL);
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
                //ex: Go to gocnak's channel
                //    0123456     ^ substring
                String name = text.substring(6, text.indexOf("'"));
                Utils.openWebPage("http://twitch.tv/" + name);
            } else if (text.startsWith("Clear ")) {
                if (pane == null) pane = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                if (pane != null) { //The combined could return null, still have to check
                    if (pane instanceof CombinedChatPane) pane = ((CombinedChatPane) pane).getActiveChatPane();
                    pane.resetCleanupCounter();
                    final ChatPane pane1 = pane;
                    EventQueue.invokeLater(() -> {//this should be fine, no need for message queue since clearing would be situational anyways
                        if (GUIMain.currentSettings.logChat) {
                            String[] toPrint = pane1.getText().split("\\n");
                            Utils.logChat(toPrint, pane1.getChannel(), 1);
                        }
                        pane1.getTextPane().setText(null);
                    });
                }
            }
            if (source instanceof JCheckBoxMenuItem) {
                String channel = source.getText();
                if (channel != null) {
                    CombinedChatPane ccp = Utils.getCombinedChatPane(GUIMain.channelPane.getSelectedIndex());
                    if (ccp == null || ccp.getActiveChannel().equalsIgnoreCase(channel)) {
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