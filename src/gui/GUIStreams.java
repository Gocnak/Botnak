package gui;

import util.ChatPane;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class GUIStreams extends JFrame {

    GUIStreams_2 s2 = null;

    public GUIStreams() {
        initComponents();
        makeList();
        s2 = new GUIStreams_2();
    }

    public static void makeList() {
        String[] channels = null;
        DefaultListModel<String> defaultListModel = new DefaultListModel<>();
        if (GUIMain.loadedStreams()) {
            rememberStreams.setSelected(true);
            channels = GUIMain.channelSet.toArray(new String[GUIMain.channelSet.size()]);
        }
        if (channels != null) {
            for (String s : channels) {
                if (s != null) {
                    defaultListModel.addElement(s);
                }
            }
        }
        if (!defaultListModel.isEmpty()) {
            streamList.setModel(defaultListModel);
        }
    }

    public void addStreamActionPerformed() {
        if (s2 == null) {
            s2 = new GUIStreams_2();
        }
        if (!s2.isVisible()) {
            s2.setVisible(true);
        }
    }

    public void removeStreamActionPerformed() {
        String[] channels = readList(streamList);
        DefaultListModel<String> listModel = new DefaultListModel<>();
        String channelToLeave = streamList.getSelectedValue();
        if (channelToLeave != null) {
            if (channels != null && channels.length > 0) {
                for (String user : channels) {
                    if (user.equals(channelToLeave)) continue;
                    listModel.addElement(user);
                }
            }
            if (GUIMain.viewer != null) {
                GUIMain.viewer.doLeave(channelToLeave, true);
            }
            if (GUIMain.bot != null) {
                GUIMain.bot.doLeave(channelToLeave, true);
            }
            GUIMain.chatPanes.get(channelToLeave).deletePane();
            GUIMain.chatPanes.remove(channelToLeave);
        }
        streamList.setModel(listModel);
        removeStream.setEnabled(false);
    }

    public void cancelButtonActionPerformed() {
        GUIMain.streams = null;
        if (s2 != null) s2.dispose();
        s2 = null;
        dispose();
    }

    public void saveButtonActionPerformed() {
        String[] channels = readList(streamList);
        if (channels != null) {
            handleList(channels);
            if (rememberStreams.isSelected()) {
                GUIMain.currentSettings.saveStreams();
            }
            for (String s : channels) {
                if (GUIMain.viewer != null && !Utils.isInChannel(GUIMain.viewer, "#" + s)) {
                    GUIMain.viewer.doConnect(s);
                }
                if (GUIMain.bot != null && !Utils.isInChannel(GUIMain.bot, "#" + s)) {
                    GUIMain.bot.doConnect(s);
                }
            }
            for (String s : channels) {
                if (!GUIMain.chatPanes.containsKey(s)) {
                    ChatPane.createPane(s);
                }
            }
        }
        GUIMain.streams = null;
        dispose();
    }

    public String[] readList(JList<String> list) {
        ArrayList<String> things = new ArrayList<>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            String o = list.getModel().getElementAt(i);
            if (o != null) {
                things.add(o.toLowerCase());
            }
        }
        return things.toArray(new String[things.size()]);
    }

    public void handleList(String[] toAdd) {
        if (GUIMain.channelSet != null && toAdd != null && toAdd.length > 0) {
            for (String s : toAdd) {
                if (GUIMain.channelSet.contains(s)) continue;
                GUIMain.channelSet.add(s);
            }
        }
    }

    public static void streamListMouseClicked() {
        removeStream.setEnabled(streamList.getSelectedValue() != null);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        scrollPane1 = new JScrollPane();
        streamList = new JList<>();
        addStream = new JButton();
        removeStream = new JButton();
        rememberStreams = new JCheckBox();
        cancelButton = new JButton();
        saveButton = new JButton();

        //======== this ========
        setTitle("Stream List");
        setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- streamList ----
            streamList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    streamListMouseClicked();
                }
            });
            streamList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            scrollPane1.setViewportView(streamList);
        }

        //---- addStream ----
        addStream.setText("Add a Stream");
        addStream.setFocusable(false);
        addStream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStreamActionPerformed();
            }
        });

        //---- removeStream ----
        removeStream.setText("Remove Selected Stream");
        removeStream.setFocusable(false);
        removeStream.setEnabled(streamList.getSelectedValue() != null);
        removeStream.setActionCommand("Remove Selected Stream");
        removeStream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeStreamActionPerformed();
            }
        });

        //---- rememberStreams ----
        rememberStreams.setText("Remember these streams");
        rememberStreams.setFocusable(false);

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });

        //---- saveButton ----
        saveButton.setText("Save");
        saveButton.setFocusable(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButtonActionPerformed();
            }
        });

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(rememberStreams)
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addComponent(addStream)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(removeStream)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(5, 5, 5)
                                                                .addComponent(saveButton))
                                                        .addComponent(cancelButton))))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(removeStream)
                                                        .addComponent(addStream))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(rememberStreams))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(saveButton)
                                                .addGap(7, 7, 7)
                                                .addComponent(cancelButton)))
                                .addContainerGap(5, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JScrollPane scrollPane1;
    public static JList<String> streamList;
    public static JButton addStream;
    public static JButton removeStream;
    public static JCheckBox rememberStreams;
    public static JButton cancelButton;
    public static JButton saveButton;


    /**
     * *\
     * ======================================= GUI STREAMS SUB GUI=================================================== *
     */

    class GUIStreams_2 extends JFrame {

        public GUIStreams_2() {
            initComponents();
        }

        public void addButtonActionPerformed() {
            if (userField.getText() == null || userField.getText().equals("") || userField.getText().contains(" "))
                return;
            String userToAdd = userField.getText().toLowerCase();
            ArrayList<String> things = new ArrayList<>();
            if (streamList.getModel().getSize() > 0) {
                for (int i = 0; i < streamList.getModel().getSize(); i++) {
                    Object o = streamList.getModel().getElementAt(i);
                    if (o != null) {
                        things.add(o.toString());
                    }
                }
            } else {
                things.add(userToAdd);
            }
            DefaultListModel<String> listModel = new DefaultListModel<>();
            if (!things.isEmpty()) {
                for (String user : things) {
                    listModel.addElement(user);
                }
                if (!listModel.contains(userToAdd)) {
                    listModel.addElement(userToAdd);
                }
            }
            streamList.setModel(listModel);
            userField.setText("");
        }

        public void cancelButtonActionPerformed() {
            s2 = null;
            dispose();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner Evaluation license - Nick K
            label1 = new JLabel();
            userField = new JTextField();
            addButton = new JButton();
            cancelButton = new JButton();

            //======== this ========
            setTitle("Add a Stream");
            setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
            setResizable(false);
            Container contentPane = getContentPane();

            //---- label1 ----
            label1.setText("Streamer Username:");
            label1.setToolTipText("The name of the desired Twitch user.");

            //---- addButton ----
            addButton.setText("Add");
            addButton.setFocusable(false);
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addButtonActionPerformed();
                }
            });

            //---- cancelButton ----
            cancelButton.setText("Done");
            cancelButton.setFocusable(false);
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelButtonActionPerformed();
                }
            });

            GroupLayout contentPaneLayout = new GroupLayout(contentPane);
            contentPane.setLayout(contentPaneLayout);
            contentPaneLayout.setHorizontalGroup(
                    contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addGroup(contentPaneLayout.createParallelGroup()
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(label1)
                                                    .addGap(10, 10, 10)
                                                    .addComponent(userField, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addGap(74, 74, 74)
                                                    .addComponent(addButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(cancelButton)
                                                    .addGap(0, 26, Short.MAX_VALUE)))
                                    .addContainerGap())
            );
            contentPaneLayout.setVerticalGroup(
                    contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label1)
                                            .addComponent(userField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(cancelButton)
                                            .addComponent(addButton))
                                    .addContainerGap(12, Short.MAX_VALUE))
            );
            pack();
            setLocationRelativeTo(getOwner());
            // JFormDesigner - End of component initialization  //GEN-END:initComponents
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
        // Generated using JFormDesigner Evaluation license - Nick K
        public JLabel label1;
        public JTextField userField;
        public JButton addButton;
        public JButton cancelButton;
        // JFormDesigner - End of variables declaration  //GEN-END:variables
    }

}
