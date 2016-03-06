package gui.forms;

import gui.ChatPane;
import gui.CombinedChatPane;
import irc.account.Oauth;
import thread.ThreadEngine;
import thread.heartbeat.FollowCheck;
import util.APIRequests;
import util.Utils;
import util.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Nick K
 */
public class GUIStreams extends JFrame {

    private Oauth getKey() {
        return (Settings.accountManager.getUserAccount() != null ? Settings.accountManager.getUserAccount().getKey() : null);
    }

    public GUIStreams() {
        initComponents();
        parseFollowed();
    }

    public void parseFollowed() {
        if (getKey() != null && getKey().canReadFollowed()) {
            CopyOnWriteArraySet<String> channels = FollowCheck.followedChannels;
            if (channels != null && channels.size() > 0) {
                setFollowedListModel(channels.toArray(new String[channels.size()]));
            } else {
                setFollowedListModel("No followed streams", " are live :(");
            }
        } else {
            setFollowedListModel("Enable \"Read followed Streams\" on", " your Oauth key!");
        }
        if (!listLabel.getText().equals("Followed Streams:")) {
            listLabel.setText("Followed Streams:");
        }
    }

    public void doneButtonActionPerformed() {
        dispose();
    }

    private void newChannelKeyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            addStreamButtonActionPerformed();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        } else {
            final String text = Utils.checkText(newChannel.getText().trim());
            if ("".equals(text) || text.length() < 3) {
                parseFollowed();
            } else if (text.length() > 2) {
                ThreadEngine.submit(() -> {
                    if (!listLabel.getText().equals("Suggested Streams:")) {
                        listLabel.setText("Suggested Streams:");
                    }
                    String[] response = APIRequests.Twitch.getUsernameSuggestions(text);
                    if (response.length > 0) {
                        setFollowedListModel(response);
                    } else {
                        setFollowedListModel("No suggested", " streamers found!");
                    }
                });
            }
        }
    }

    public void addStreamButtonActionPerformed() {
        String text = Utils.checkText(newChannel.getText());
        if (!text.isEmpty()) {
            if (text.contains(",")) {
                String[] channels = text.split(",");
                ArrayList<ChatPane> panes = new ArrayList<>();
                for (String channel : channels) {
                    channel = channel.trim().toLowerCase();
                    if (channel.equals("")) continue;
                    //create the ChatPane but do not add to the tabbed pane
                    if (GUIMain.chatPanes.containsKey(channel)) {
                        //if the pane exists just use it, no need to create multiple
                        panes.add(GUIMain.getChatPane(channel));
                        //note: since they're adding the combined tab and the tab
                        //already exists, they know full well that it does, so
                        //we're not removing/setting visible to false for the tab
                    } else {
                        ChatPane cp = ChatPane.createPane(channel);
                        //the tab will not be added to the tabbed pane and therefore invisible
                        cp.setTabVisible(false);
                        if (GUIMain.viewer != null) GUIMain.viewer.doConnect(channel);
                        if (GUIMain.bot != null) GUIMain.bot.doConnect(channel);
                        GUIMain.channelSet.add(channel);
                        GUIMain.chatPanes.put(cp.getChannel(), cp);
                        panes.add(cp);
                    }
                }
                CombinedChatPane ccp = CombinedChatPane.createCombinedChatPane(panes.toArray(new ChatPane[panes.size()]));
                GUIMain.channelPane.insertTab(ccp.getTabTitle(), null, ccp.getScrollPane(), null, GUIMain.channelPane.getTabCount() - 1);
                GUIMain.combinedChatPanes.add(ccp);
            } else {
                String channel = text.toLowerCase();
                if (!channel.isEmpty() && !channel.contains(" ") && !GUIMain.chatPanes.containsKey(channel)) {
                    ChatPane cp = ChatPane.createPane(channel);
                    if (GUIMain.viewer != null) GUIMain.viewer.doConnect(channel);
                    if (GUIMain.bot != null) GUIMain.bot.doConnect(channel);
                    GUIMain.chatPanes.put(cp.getChannel(), cp);
                    GUIMain.channelSet.add(channel);
                    GUIMain.channelPane.insertTab(cp.getChannel(), null, cp.getScrollPane(), null, cp.getIndex());
                }
            }
        }
        GUIMain.channelPane.updateIndexes();
        newChannel.setText("");
        parseFollowed();
    }


    private void setFollowedListModel(String... values) {
        followedList.setModel(new AbstractListModel<String>() {
            @Override
            public int getSize() {
                return values.length;
            }

            @Override
            public String getElementAt(int i) {
                return values[i];
            }
        });
        followedList.repaint();
    }

    @Override
    public void setVisible(boolean b) {
        if (!newChannel.getText().isEmpty()) newChannel.setText("");
        if (b) parseFollowed();
        setAlwaysOnTop(Settings.alwaysOnTop.getValue());
        super.setVisible(b);
    }

    private void initComponents() {
        JScrollPane scrollPane1 = new JScrollPane();
        followedList = new JList<>();
        listLabel = new JLabel();
        JLabel label3 = new JLabel();
        newChannel = new JTextField();
        JSeparator separator1 = new JSeparator();
        JSeparator separator2 = new JSeparator();
        JButton addStreamButton = new JButton();
        JButton doneButton = new JButton();

        //======== this ========
        setTitle("Add a Stream");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {
            //---- followedList ----
            setFollowedListModel("Enable \"Read followed Streams\" on", " your Oauth key!");
            followedList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    String selected = followedList.getSelectedValue();
                    if (selected != null) {
                        newChannel.setText(selected);
                        newChannel.requestFocusInWindow();
                    }
                }
            });
            followedList.setFocusable(false);
            scrollPane1.setViewportView(followedList);
        }

        //---- listLabel ----
        listLabel.setText("Followed Streams:");

        //---- label3 ----
        label3.setText("Twitch Username:");

        //---- newChannel ----
        newChannel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        newChannel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                newChannelKeyReleased(e);
            }
        });

        //---- addStreamButton ----
        addStreamButton.setText("Add");
        addStreamButton.setFocusable(false);
        addStreamButton.addActionListener(e -> addStreamButtonActionPerformed());

        //---- doneButton ----
        doneButton.setText("Close");
        doneButton.setFocusable(false);
        doneButton.addActionListener(e -> doneButtonActionPerformed());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(addStreamButton, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                                                .addComponent(doneButton, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(listLabel)
                                                        .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(label3)
                                                        .addComponent(newChannel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                        .addComponent(separator1, GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                        .addComponent(separator2, GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(listLabel)
                                .addGap(6, 6, 6)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(separator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(label3)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newChannel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(separator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(addStreamButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(doneButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(3, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JList<String> followedList;
    private JLabel listLabel;
    private JTextField newChannel;
}