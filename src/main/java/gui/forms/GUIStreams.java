package gui.forms;

import gui.ChatPane;
import gui.CombinedChatPane;
import irc.account.Oauth;
import util.APIRequests;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


/**
 * @author Nick K
 */
public class GUIStreams extends JFrame {

    public GUIStreams() {
        Oauth key = GUIMain.currentSettings.accountManager.getUserAccount().getKey();
        initComponents();
        if (key.canReadFollowed()) {
            String[] channels = APIRequests.Twitch.getLiveFollowedChannels(key.getKey().split(":")[1]);
            if (channels.length > 0) {
                setFollowedListModel(channels);
            } else {
                setFollowedListModel("No followed streams", " are live :(");
            }
        }
    }

    public void doneButtonActionPerformed() {
        dispose();
    }

    private void newChannelKeyReleased(KeyEvent e) {
        //TODO enter -> add the text, every other key filters the list
        //use APIRequests.Twitch.getUsernameSuggestions(newChannel.getText()) to fill the list, change the JLabel to "Suggested Streams"
    }

    public void addStreamButtonActionPerformed() {
        String text = newChannel.getText();
        if (!text.equals("") && !text.trim().equals("")) {
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
                String channel = text.trim().toLowerCase();
                if (!channel.equals("") && !channel.contains(" ") && !GUIMain.chatPanes.containsKey(channel)) {
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
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        scrollPane1 = new JScrollPane();
        followedList = new JList<>();
        label2 = new JLabel();
        label3 = new JLabel();
        newChannel = new JTextField();
        separator1 = new JSeparator();
        separator2 = new JSeparator();
        addStreamButton = new JButton();
        doneButton = new JButton();

        //======== this ========
        setTitle("Add a Stream");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- followedList ----
            setFollowedListModel("Enable \"Read followed Streams\" on", " your Oauth key!");
            followedList.setFocusable(false);
            scrollPane1.setViewportView(followedList);
        }

        //---- label2 ----
        label2.setText("Followed Streams:");

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
                                                        .addComponent(label2)
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
                                .addComponent(label2)
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
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    private JScrollPane scrollPane1;
    private JList<String> followedList;
    private JLabel label2;
    private JLabel label3;
    private JTextField newChannel;
    private JSeparator separator1;
    private JSeparator separator2;
    private JButton addStreamButton;
    private JButton doneButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}