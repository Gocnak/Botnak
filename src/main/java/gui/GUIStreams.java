package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class GUIStreams extends JFrame {

    public GUIStreams() {
        initComponents();
    }

    public void doneButtonActionPerformed() {
        GUIMain.channelPane.setSelectedIndex(GUIMain.channelPane.getTabCount() - 2);
        dispose();
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
                        panes.add(GUIMain.chatPanes.get(channel));
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

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        doneButton = new JButton();
        newChannel = new JTextField();
        addStreamButton = new JButton();
        scrollPane2 = new JScrollPane();
        label1 = new JTextArea();

        //======== this ========
        setTitle("Add Streams");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //---- doneButton ----
        doneButton.setText("Done");
        doneButton.setFocusable(false);
        doneButton.addActionListener(e -> doneButtonActionPerformed());

        //---- newChannel ----
        newChannel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        newChannel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addStreamButtonActionPerformed();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        //---- addStreamButton ----
        addStreamButton.setText("Add");
        addStreamButton.setFocusable(false);
        addStreamButton.addActionListener(e -> addStreamButtonActionPerformed());

        //======== scrollPane2 ========
        {

            //---- label1 ----
            label1.setText("Enter the username of the Twitch user that you want to join.  \nYou may separate multiple names by commas to create a combined chat panel.\n\nEx: \n\"gocnak\" creates a tab for Gocnak's chat.\n\n\"gocnak,botnak,tduva\" creates a combined tab with the chats of \nGocnak, Botnak, and TDuva.");
            label1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            label1.setFocusable(false);
            label1.setEditable(false);
            label1.setFont(new Font("Arial", Font.PLAIN, 12));
            label1.setOpaque(false);
            scrollPane2.setViewportView(label1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(newChannel)
                                                .addGap(18, 18, 18)
                                                .addComponent(addStreamButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(doneButton))
                                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 459, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(addStreamButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(doneButton, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                                        .addComponent(newChannel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                                .addGap(28, 28, 28))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JButton doneButton;
    public static JTextField newChannel;
    public static JButton addStreamButton;
    public static JScrollPane scrollPane2;
    public static JTextArea label1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
