package gui;

import util.Constants;
import util.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

public class GUIDefaults extends JFrame {
    public GUIDefaults() {
        initComponents();
        setSize(getWidth() + 120, getHeight());
        setMinimumSize(getSize());
        if (GUIMain.defaultFaceDir != null && !GUIMain.defaultFaceDir.equals("")) {
            faceDir.setText(GUIMain.defaultFaceDir);
        }
        if (GUIMain.defaultSoundDir != null && !GUIMain.defaultSoundDir.equals("")) {
            soundDir.setText(GUIMain.defaultSoundDir);
        }
        if (GUIMain.useBroad) {
            useCustomBroad.setSelected(true);
            customBroad.setEnabled(true);
            customBroadButton.setEnabled(true);
            customBroad.setText(GUIMain.broadIcon.toString());
        }
        if (GUIMain.useMod) {
            useCustomMod.setSelected(true);
            customMod.setEnabled(true);
            customModButton.setEnabled(true);
            customMod.setText(GUIMain.modIcon.toString());
        }
    }


    public void faceButtonActionPerformed() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(Constants.folderFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                faceDir.setText(path);
            }
        }
    }

    public void soundsButtonActionPerformed() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(Constants.folderFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                soundDir.setText(path);
            }
        }
    }

    public void saveButtonActionPerformed() {
        String text1 = faceDir.getText();
        String text2 = soundDir.getText();
        URL text3;
        URL text4;
        try {
            text3 = new URL(customMod.getText());
            text4 = new URL(customBroad.getText());
        } catch (Exception e) {
            text3 = getClass().getResource("/resource/mod.png");
            text4 = getClass().getResource("/resource/broad.png");
        }
        GUIMain.useMod = useCustomMod.isSelected();
        GUIMain.useBroad = useCustomBroad.isSelected();
        if (text1 == null || text1.equals("")) text1 = null;
        if (text2 == null || text2.equals("")) text2 = null;
        if (text3 == null || text3.toString().equals("")) text3 = getClass().getResource("/resource/mod.png");
        if (text4 == null || text4.toString().equals("")) text4 = getClass().getResource("/resource/broad.png");
        if (!useCustomMod.isSelected()) {//just incase they put a path in, but not have the box ticked when hitting "save"
            text3 = getClass().getResource("/resource/mod.png");
        }
        if (!useCustomBroad.isSelected()) {
            text4 = getClass().getResource("/resource/broad.png");
        }
        if (text1 != null) GUIMain.defaultFaceDir = text1;
        if (text2 != null) GUIMain.defaultSoundDir = text2;
        GUIMain.modIcon = text3;
        GUIMain.broadIcon = text4;
        Utils.saveDefaults(text1, text2, useCustomMod.isSelected(), useCustomBroad.isSelected(), text3.toString(), text4.toString());
        dispose();
    }

    public void cancelButtonActionPerformed() {
        dispose();
    }

    public void useCustomModStateChanged() {
        customMod.setEnabled(useCustomMod.isSelected());
        customModButton.setEnabled(useCustomMod.isSelected());
    }

    public void customModButtonActionPerformed() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(Constants.pictureFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null) {
                try {
                    customMod.setText(selectedFile.toURI().toURL().toString());
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            }
        }
    }

    public void useCustomBroadStateChanged() {
        customBroad.setEnabled(useCustomBroad.isSelected());
        customBroadButton.setEnabled(useCustomBroad.isSelected());
    }

    public void customBroadButtonActionPerformed() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(Constants.pictureFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null) {
                try {
                    customBroad.setText(selectedFile.toURI().toURL().toString());
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        faceDir = new JTextField();
        faceButton = new JButton();
        label2 = new JLabel();
        soundDir = new JTextField();
        soundsButton = new JButton();
        saveButton = new JButton();
        cancelButton = new JButton();
        useCustomMod = new JCheckBox();
        label3 = new JLabel();
        customMod = new JTextField();
        customModButton = new JButton();
        useCustomBroad = new JCheckBox();
        label4 = new JLabel();
        customBroad = new JTextField();
        customBroadButton = new JButton();

        //======== this ========
        setTitle("Defaults for Botnak");
        Container contentPane = getContentPane();

        faceDir.setFocusable(false);
        soundDir.setFocusable(false);
        customMod.setFocusable(false);
        customBroad.setFocusable(false);
        //---- label1 ----
        label1.setText("Default Face Directory:");

        //---- faceButton ----
        faceButton.setText("Browse...");
        faceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                faceButtonActionPerformed();
            }
        });
        faceButton.setFocusable(false);

        //---- label2 ----
        label2.setText("Default Sound Directory:");

        //---- soundsButton ----
        soundsButton.setText("Browse...");
        soundsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                soundsButtonActionPerformed();
            }
        });
        soundsButton.setFocusable(false);

        //---- saveButton ----
        saveButton.setText("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButtonActionPerformed();
            }
        });
        saveButton.setFocusable(false);

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        cancelButton.setFocusable(false);

        //---- useCustomMod ----
        useCustomMod.setText("Use Custom Mod Icon");
        useCustomMod.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                useCustomModStateChanged();
            }
        });
        useCustomMod.setFocusable(false);

        //---- label3 ----
        label3.setText("Custom Mod Icon:");

        //---- customModButton ----
        customModButton.setText("Browse...");
        customModButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customModButtonActionPerformed();
            }
        });
        customModButton.setFocusable(false);
        customModButton.setEnabled(false);
        customMod.setEnabled(false);

        //---- useCustomBroad ----
        useCustomBroad.setText("Use Custom Broadcaster Icon");
        useCustomBroad.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                useCustomBroadStateChanged();
            }
        });
        useCustomBroad.setFocusable(false);

        //---- label4 ----
        label4.setText("Custom Broadcaster Icon:");

        //---- customBroadButton ----
        customBroadButton.setText("Browse...");
        customBroadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customBroadButtonActionPerformed();
            }
        });
        customBroadButton.setFocusable(false);
        customBroadButton.setEnabled(false);
        customBroad.setEnabled(false);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                                .addGap(0, 351, Short.MAX_VALUE)
                                                .addComponent(saveButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(useCustomMod)
                                                        .addComponent(useCustomBroad)
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(label4)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(customBroad))
                                                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(label3)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(customMod))
                                                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(label1)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(faceDir, GroupLayout.PREFERRED_SIZE, 255, GroupLayout.PREFERRED_SIZE))
                                                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(label2)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(soundDir)))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(faceButton)
                                                                        .addComponent(soundsButton)
                                                                        .addComponent(customModButton)
                                                                        .addComponent(customBroadButton))))
                                                .addGap(0, 13, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label1)
                                        .addComponent(faceDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(faceButton))
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(soundDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(soundsButton)
                                        .addComponent(label2))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(useCustomMod)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label3)
                                        .addComponent(customMod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(customModButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(useCustomBroad)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label4)
                                        .addComponent(customBroad, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(customBroadButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(saveButton))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JLabel label1;
    public static JTextField faceDir;
    public static JButton faceButton;
    public static JLabel label2;
    public static JTextField soundDir;
    public static JButton soundsButton;
    public static JButton saveButton;
    public static JButton cancelButton;
    public static JCheckBox useCustomMod;
    public static JLabel label3;
    public static JTextField customMod;
    public static JButton customModButton;
    public static JCheckBox useCustomBroad;
    public static JLabel label4;
    public static JTextField customBroad;
    public static JButton customBroadButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
