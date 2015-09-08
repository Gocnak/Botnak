package gui;

import gui.forms.GUIMain;
import irc.message.MessageWrapper;
import util.Constants;
import util.Utils;
import util.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nick on 1/4/14.
 */
public class CombinedChatPane extends ChatPane {

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


    private ChatPane[] panes;

    public ChatPane[] getPanes() {
        return panes;
    }

    private JScrollPane scrollPaneAll;

    private String activeChannel = "All";

    public void setActiveChannel(String channel) {
        activeChannel = channel;
    }

    public String getActiveChannel() {
        return activeChannel;
    }

    private ChatPane activeChatPane = this;

    public ChatPane getActiveChatPane() {
        return activeChatPane;
    }

    public void setActiveScrollPane(String channel) {
        for (ChatPane p : panes) {
            if (p.getChannel().equalsIgnoreCase(channel)) {
                setScrollPane(p.getScrollPane());
                p.scrollToBottom();
                activeChatPane = p;
                break;
            }
        }
        GUIMain.channelPane.setComponentAt(getIndex(), getScrollPane());
        GUIMain.channelPane.fireStateChanged();
        GUIMain.channelPane.repaint();
    }

    public void setDefaultScrollPane() {
        activeChannel = "All";
        activeChatPane = this;
        setScrollPane(scrollPaneAll);
        GUIMain.channelPane.setComponentAt(getIndex(), getScrollPane());
        GUIMain.channelPane.fireStateChanged();
        GUIMain.channelPane.repaint();
        scrollToBottom();
    }


    public CombinedChatPane(JScrollPane scrollPane, JTextPane pane, ScrollablePanel panel, ChatPane... chatPanes) {
        super(null, scrollPane, pane, panel, -1);
        panes = chatPanes;
        ArrayList<String> channels = new ArrayList<>();
        for (ChatPane cp : chatPanes) {
            channels.add(cp.getChannel());
        }
        this.channels = channels.toArray(new String[channels.size()]);
        scrollPaneAll = scrollPane;
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
            setIndex(highest + 1);
        } else {
            setIndex(lowest);
        }
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
        pane.setEditorKit(Constants.wrapEditorKit);
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setMargin(new Insets(0, 0, 0, 0));
        pane.setBackground(Color.black);
        pane.setFont(Settings.font.getValue());
        pane.addMouseListener(Constants.listenerURL);
        pane.addMouseListener(Constants.listenerName);
        pane.addMouseListener(Constants.listenerFace);
        scrollPane.setViewportView(pane);
        for (ChatPane cp : panes) {
            cp.setTabVisible(false);
        }
        ScrollablePanel sp = new ScrollablePanel();
        sp.add(pane, BorderLayout.SOUTH);
        scrollPane.setViewportView(sp);
        //Tab adding is handled at the DraggableTabbedPane
        return new CombinedChatPane(scrollPane, pane, sp, panes);
    }

    /**
     * Disbands the combined tab and places
     */
    public void disbandTab() {
        setDefaultScrollPane();
        //we're going to add it all to the right of it and then remove the combined tab
        int index = getIndex() + 1;
        for (ChatPane cp : panes) {
            if (!Utils.isTabVisible(cp.getChannel())) {
                cp.setTabVisible(true);
                cp.scrollToBottom();
                GUIMain.channelPane.insertTab(cp.getChannel(), null, cp.getScrollPane(), null, index);
                index++;
            }
        }

        GUIMain.channelPane.removeTabAt(getIndex());
        GUIMain.channelPane.updateIndexes();
        GUIMain.channelPane.fireStateChanged();
        GUIMain.combinedChatPanes.remove(this);
    }

    @Override
    public void log(MessageWrapper message, boolean isSystem) {
        if ("all".equalsIgnoreCase(activeChannel)) {
            super.log(message, isSystem);
        } else
            getActiveChatPane().log(message, isSystem);
    }
}