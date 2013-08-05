package gui;

import util.Sound;
import util.StringArray;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class GUISounds extends JFrame {

    public static String[] filePaths;

    public GUISounds() {
        initComponents();
        buildTree();
    }

    FileFilter wavfiles = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) return true;
                String ext = Utils.getExtension(f);
                if (ext != null) {
                    if (ext.equals("wav")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return ".wav files";
        }
    };

    public void cancelButtonActionPerformed() {
        dispose();
    }

    public void searchFileActionPerformed() {
        GUISounds_2 g = new GUISounds_2();
        g.setVisible(true);
    }

    public void saveButton1ActionPerformed() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) soundTree.getModel().getRoot();
        DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
        int childrenOfRoot = root.getChildCount();
        for (int i = 0; i < childrenOfRoot; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(root, i);
            if (child != null && child.getUserObject() != null) {
                String[] split = child.getUserObject().toString().split("-");
                String command = split[0];
                int perm = 0;
                try {
                    perm = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    continue;
                }
                int children = child.getChildCount();
                ArrayList<String> list = new ArrayList<>();
                for (int in = 0; in < children; in++) {
                    if (child.getChildAt(in) != null) {
                        DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) child.getChildAt(in);
                        if (child1.getUserObject() != null) {
                            list.add(child1.getUserObject().toString());
                        }
                    }
                }
                Sound newSound = new Sound(perm, list.toArray(new String[list.size()]));
                GUIMain.soundMap.put(command, newSound);
            }
        }
        dispose();
    }

    public static void removeSoundButtonActionPerformed() {
        if (!soundTree.isSelectionEmpty()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) soundTree.getLastSelectedPathComponent();
            if (!node.isRoot()) {
                DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
                if (GUIMain.soundMap.containsKey(node.getUserObject().toString().split("-")[0])) {
                    GUIMain.soundMap.remove(node.getUserObject().toString().split("-")[0]);
                }
                if (node.getChildCount() > 0) {
                    node.removeAllChildren();
                    node.removeFromParent();
                    model.reload();
                } else {
                    model.removeNodeFromParent(node);
                }

            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K.
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Sounds");
        soundTree = new JTree(node);
        soundTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        saveButton = new JButton();
        cancelButton = new JButton();
        searchFile = new JButton();
        removeSoundButton = new JButton();

        //======== this ========
        setTitle("Sound Manager");
        setResizable(false);
        Container contentPane = getContentPane();

        //---- label1 ----
        label1.setText("Sound Manager");
        label1.setFont(new Font("Arial", Font.BOLD, 18));

        //======== scrollPane1 ========
        {

            //---- soundTree ----
            soundTree.setFocusable(false);
            soundTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                            soundTree.getLastSelectedPathComponent();
                    removeSoundButton.setEnabled(node != null);
                }
            });
            scrollPane1.setViewportView(soundTree);
        }

        //---- saveButton ----
        saveButton.setText("Save");
        saveButton.setFocusable(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButton1ActionPerformed();
            }
        });

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });

        //---- searchFile ----
        searchFile.setText("Add Sound(s)");
        searchFile.setFocusable(false);
        searchFile.setToolTipText("Add sound(s) to the sound tree.");
        searchFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchFileActionPerformed();
            }
        });

        //---- removeSoundButton ----
        removeSoundButton.setText("Remove");
        removeSoundButton.setFocusable(false);
        removeSoundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSoundButtonActionPerformed();
            }
        });
        removeSoundButton.setEnabled(false);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(searchFile, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(removeSoundButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 181, Short.MAX_VALUE)
                                                .addComponent(saveButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(207, 207, 207)
                                                .addComponent(label1)
                                                .addGap(0, 201, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 339, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(cancelButton)
                                                .addComponent(saveButton))
                                        .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(searchFile)
                                                .addComponent(removeSoundButton)))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K.
    public static JLabel label1;
    public static JScrollPane scrollPane1;
    public static JTree soundTree;
    public static JButton saveButton;
    public static JButton cancelButton;
    public static JButton searchFile;
    public static JButton removeSoundButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public void buildTree() {
        if (!GUIMain.soundMap.isEmpty()) {
            DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            String[] keys = GUIMain.soundMap.keySet().toArray(new String[GUIMain.soundMap.keySet().size()]);
            for (String name : keys) {
                Sound snd = GUIMain.soundMap.get(name);
                int perm = snd.getPermission();
                String[] files = snd.getSounds().data;
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(name + "-" + perm);
                for (String file : files) {
                    node.add(new DefaultMutableTreeNode(file));
                }
                model.insertNodeInto(node, root, root.getChildCount());
            }
        }
    }

    class GUISounds_2 extends JFrame {
        public GUISounds_2() {
            initComponents();
        }

        public void browseButtonActionPerformed() {
            JFileChooser jfc = new JFileChooser();
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.addChoosableFileFilter(wavfiles);
            jfc.setMultiSelectionEnabled(true);
            if (!GUIMain.lastSoundDir.equals("")) jfc.setCurrentDirectory(new File(GUIMain.lastSoundDir));
            int returnVal = jfc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = jfc.getSelectedFiles();
                if (selectedFiles.length > 0) {
                    GUIMain.lastSoundDir = selectedFiles[0].getParent();
                    ArrayList<String> list = new ArrayList<>();
                    for (File f : selectedFiles) {
                        list.add(f.getAbsolutePath());
                    }
                    filePaths = list.toArray(new String[list.size()]);
                    filesSelectedLabel.setText(filePaths.length == 1 ? "One file chosen." : filePaths.length + " files chosen.");
                }
            }
        }

        void saveButtonActionPerformed() {
            if (!Utils.checkText(commandField.getText()).equals("")) {
                if (filePaths.length > 0) {
                    String command = commandField.getText();
                    //update the tree
                    DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                    int permission = permissionBox.getSelectedIndex();
                    DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode(command + "-" + permission);
                    for (String s : filePaths) {
                        commandNode.add(new DefaultMutableTreeNode(s));
                    }
                    model.insertNodeInto(commandNode, root, root.getChildCount());
                }
            }
            dispose();
        }

        public void cancelButtonActionPerformed() {
            dispose();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner Evaluation license - Nick K.
            label1 = new JLabel();
            label2 = new JLabel();
            filesSelectedLabel = new JLabel();
            browseButton = new JButton();
            commandField = new JTextField();
            saveButton = new JButton();
            cancelButton = new JButton();
            label3 = new JLabel();
            permissionBox = new JComboBox<>();

            //======== this ========
            setResizable(false);
            Container contentPane = getContentPane();

            //---- label1 ----
            label1.setText("Add Sound(s)");
            label1.setFont(new Font("Tahoma", Font.BOLD, 16));

            //---- label2 ----
            label2.setText("Command Name:");

            //---- filesSelectedLabel ----
            filesSelectedLabel.setText("No files selected.");
            filesSelectedLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));

            //---- browseButton ----
            browseButton.setText("Browse...");
            browseButton.setFocusable(false);
            browseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    browseButtonActionPerformed();
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

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelButtonActionPerformed();
                }
            });

            //---- label3 ----
            label3.setText("Permission:");

            //---- permissionBox ----
            permissionBox.setModel(new DefaultComboBoxModel<>(new String[]{
                    "Everyone",
                    "Mods/Broadcaster",
                    "Broadcaster Only"
            }));
            permissionBox.setFocusable(false);

            GroupLayout contentPaneLayout = new GroupLayout(contentPane);
            contentPane.setLayout(contentPaneLayout);
            contentPaneLayout.setHorizontalGroup(
                    contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(contentPaneLayout.createParallelGroup()
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addComponent(browseButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                                                    .addComponent(saveButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(cancelButton))
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addComponent(filesSelectedLabel)
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(label2)
                                                            .addComponent(label3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                    .addGap(18, 18, 18)
                                                    .addGroup(contentPaneLayout.createParallelGroup()
                                                            .addComponent(commandField)
                                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                                    .addGroup(contentPaneLayout.createParallelGroup()
                                                                            .addComponent(label1)
                                                                            .addComponent(permissionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                                    .addGap(0, 68, Short.MAX_VALUE)))))
                                    .addContainerGap())
            );
            contentPaneLayout.setVerticalGroup(
                    contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addGap(13, 13, 13)
                                    .addComponent(label1)
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label2)
                                            .addComponent(commandField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(permissionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label3, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                                    .addGap(23, 23, 23)
                                    .addComponent(filesSelectedLabel)
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(browseButton)
                                            .addComponent(cancelButton)
                                            .addComponent(saveButton))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            pack();
            setLocationRelativeTo(getOwner());
            // JFormDesigner - End of component initialization  //GEN-END:initComponents
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
        // Generated using JFormDesigner Evaluation license - Nick K.
        public JLabel label1;
        public JLabel label2;
        public JLabel filesSelectedLabel;
        public JButton browseButton;
        public JTextField commandField;
        public JButton saveButton;
        public JButton cancelButton;
        public JLabel label3;
        public JComboBox<String> permissionBox;
        // JFormDesigner - End of variables declaration  //GEN-END:variables
    }


}
