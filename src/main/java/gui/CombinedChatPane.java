package gui;

import gui.forms.GUIMain;
import irc.message.MessageWrapper;
import util.Constants;
import util.Utils;
import util.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nick on 1/4/14.
 */
public class CombinedChatPane extends ChatPane {

    private String title, activeChannel = "All";

    private boolean customTitle = false;

    private List<String> channels;

    private List<ChatPane> panes;

    private JScrollPane scrollPaneAll;

    private ChatPane activeChatPane = this;

    public String getTabTitle() {
        return title;
    }


    public void setCustomTitle(String title) {
        customTitle = true;
        setTabTitle(title);
    }

    public void setTabTitle(String title) {
        this.title = title;
        GUIMain.channelPane.setTitleAt(getIndex(), title);
    }


    public List<String> getChannels() {
        return channels;
    }

    public List<ChatPane> getPanes() {
        return panes;
    }

    public void setActiveChannel(String channel) {
        activeChannel = channel;
    }

    public String getActiveChannel() {
        return activeChannel;
    }

    public ChatPane getActiveChatPane() {
        return activeChatPane;
    }

    public void setActiveScrollPane(String channel) {
        panes.stream().filter(p -> p.getChannel().equalsIgnoreCase(channel)).forEach(chatPane -> {
            setScrollPane(chatPane.getScrollPane());
            chatPane.scrollToBottom();
            activeChatPane = chatPane;
        });

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
        panes = Arrays.asList(chatPanes);
        this.channels = Arrays.stream(chatPanes).map(ChatPane::getChannel).collect(Collectors.toList());
        scrollPaneAll = scrollPane;
        determineTitle();
        determineIndex();
    }

    private void determineTitle() {
        //if we have only 2, it'll be channel1 + channel2
        if (panes.size() == 2) {
            title = panes.get(0).getChannel() + " + " + panes.get(1).getChannel();
        } else if (panes.size() > 2) {//3 or more is separated by commas.
            title = channels.stream().collect(Collectors.joining(", "));
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

    public boolean addChatPane(List<ChatPane> newPanes) {
        if (newPanes.isEmpty())
            return false;

        //prevent adding a pane already in here/Copy the stuff
        newPanes.stream().filter(p -> !panes.contains(p)).forEach(newPane -> {
            newPane.setTabVisible(false);
            panes.add(newPane);
            channels.add(newPane.getChannel());
        });

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