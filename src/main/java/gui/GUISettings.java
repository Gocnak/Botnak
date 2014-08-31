package gui;

import irc.Account;
import irc.Oauth;
import irc.Task;
import lib.scalr.Scalr;
import sound.Sound;
import util.Constants;
import util.Settings;
import util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;


/**
 * @author Nick K
 */
public class GUISettings extends JFrame {

    GUISounds_2 s2;
    AuthorizeAccountGUI mainAccGUI;

    public GUISettings() {
        initComponents();
        buildTree();
        s2 = null;
        mainAccGUI = null;
    }

    // builds the sound tree
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


    void setDir(boolean isFace) {
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
                if (isFace) {
                    faceDir.setText(path);
                } else soundDir.setText(path);
            }
        }
    }

    public void faceButtonActionPerformed() {
        setDir(true);
    }

    public void soundsButtonActionPerformed() {
        setDir(false);
    }

    void save() {
        //accounts
        //handled by manager

        //directories
        GUIMain.currentSettings.defaultFaceDir = faceDir.getText();
        GUIMain.currentSettings.defaultSoundDir = soundDir.getText();

        //icons
        GUIMain.currentSettings.useMod = useCustomMod.isSelected();
        GUIMain.currentSettings.useBroad = useCustomBroad.isSelected();
        GUIMain.currentSettings.useAdmin = useCustomAdmin.isSelected();
        GUIMain.currentSettings.useStaff = useCustomStaff.isSelected();
        try {
            if (useCustomMod.isSelected()) {
                GUIMain.currentSettings.modIcon = new URL(customMod.getText());
            } else {
                GUIMain.currentSettings.modIcon = GUISettings.class.getResource("/resource/mod.png");
            }
            if (useCustomBroad.isSelected()) {
                GUIMain.currentSettings.broadIcon = new URL(customBroad.getText());
            } else {
                GUIMain.currentSettings.broadIcon = GUISettings.class.getResource("/resource/broad.png");
            }
            if (useCustomAdmin.isSelected()) {
                GUIMain.currentSettings.adminIcon = new URL(customAdminField.getText());
            } else {
                GUIMain.currentSettings.adminIcon = GUISettings.class.getResource("/resource/admin.png");
            }
            if (useCustomStaff.isSelected()) {
                GUIMain.currentSettings.staffIcon = new URL(customStaffField.getText());
            } else {
                GUIMain.currentSettings.staffIcon = GUISettings.class.getResource("/resource/staff.png");
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }

        //sounds
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) soundTree.getModel().getRoot();
        DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
        int childrenOfRoot = root.getChildCount();
        for (int i = 0; i < childrenOfRoot; i++) {// for each sound...
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(root, i);
            if (child != null && child.getUserObject() != null) {// gaben-0
                String[] split = child.getUserObject().toString().split("-");
                String command = split[0];
                int perm;
                try {
                    perm = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    continue;
                }
                int children = child.getChildCount();// gaben-0 having gaben1.wav and gaben2.wav
                ArrayList<String> list = new ArrayList<>();
                for (int in = 0; in < children; in++) {
                    if (child.getChildAt(in) != null) {
                        DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) child.getChildAt(in);//soundAti.wav
                        if (child1.getUserObject() != null) {
                            list.add(child1.getUserObject().toString());
                        }
                    }
                }
                Sound newSound = new Sound(perm, list.toArray(new String[list.size()]));
                GUIMain.soundMap.put(command, newSound);
            }
        }

        //appearance
        GUIMain.currentSettings.logChat = logChatCheck.isSelected();
        GUIMain.currentSettings.chatMax = (int) clearChatSpinner.getValue();
        GUIMain.currentSettings.cleanupChat = clearChatCheck.isSelected();
        String comm = buttonGroup.getSelection().getActionCommand();
        if (comm != null) {
            if (comm.equals("HiFi")) {
                Settings.lookAndFeel = "lib.jtattoo.com.jtattoo.plaf.hifi.HiFiLookAndFeel";
            } else {
                Settings.lookAndFeel = "lib.jtattoo.com.jtattoo.plaf.graphite.GraphiteLookAndFeel";
            }
        }
    }

    void setIcon(int type) {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(Constants.pictureFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null) {
                try {
                    switch (type) {
                        case 0:
                            customMod.setText(selectedFile.toURI().toURL().toString());
                            setIcon(customModIconNew, selectedFile.toURI().toURL());
                            break;
                        case 1:
                            customBroad.setText(selectedFile.toURI().toURL().toString());
                            setIcon(customBroadIconNew, selectedFile.toURI().toURL());
                            break;
                        case 2:
                            customAdminField.setText(selectedFile.toURI().toURL().toString());
                            setIcon(customAdminIconNew, selectedFile.toURI().toURL());
                            break;
                        case 3:
                            customStaffField.setText(selectedFile.toURI().toURL().toString());
                            setIcon(customStaffIconNew, selectedFile.toURI().toURL());
                            break;
                    }

                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            }
        }
    }

    public void saveButtonActionPerformed() {
        save();
        GUIMain.settings = null;
        if (mainAccGUI != null) mainAccGUI.dispose();
        mainAccGUI = null;
        if (s2 != null) s2.dispose();
        s2 = null;
        dispose();
    }

    public void cancelButtonActionPerformed() {
        GUIMain.settings = null;
        if (mainAccGUI != null) mainAccGUI.dispose();
        mainAccGUI = null;
        if (s2 != null) s2.dispose();
        s2 = null;
        dispose();
    }

    public void useCustomModStateChanged() {
        customMod.setEnabled(useCustomMod.isSelected());
        customModButton.setEnabled(useCustomMod.isSelected());
    }


    public void customModButtonActionPerformed() {
        setIcon(0);
    }

    public void useCustomBroadStateChanged() {
        customBroad.setEnabled(useCustomBroad.isSelected());
        customBroadButton.setEnabled(useCustomBroad.isSelected());
    }

    public void customBroadButtonActionPerformed() {
        setIcon(1);
    }

    public void useCustomAdminStateChanged() {
        customAdminButton.setEnabled(useCustomAdmin.isSelected());
        customAdminField.setEnabled(useCustomAdmin.isSelected());
    }

    public void useCustomStaffStateChanged() {
        customStaffField.setEnabled(useCustomStaff.isSelected());
        customStaffButton.setEnabled(useCustomStaff.isSelected());
    }


    public void userLogoutButtonActionPerformed() {
        if (GUIMain.viewer != null) {
            GUIMain.viewer.close(true);
            normUser.setText("");
            normPass.setText("");
            userLogoutButton.setEnabled(false);
        }
    }

    public void botLogoutButtonActionPerformed() {
        if (GUIMain.bot != null) {
            GUIMain.bot.close(true);
            botUser.setText("");
            botPass.setText("");
            botLogoutButton.setEnabled(false);
        }
    }

    public void userLoginButtonActionPerformed() {
        //TODO
        if (GUIMain.viewer == null) {
            if (mainAccGUI == null) mainAccGUI = new AuthorizeAccountGUI();
            mainAccGUI.setVisible(true);

            /*String normus = normUser.getText().toLowerCase();
            String normpass = new String(normPass.getPassword());
            if (!normus.equals("") && !normus.contains(" ")) {
                if (!normpass.equals("") && !normpass.contains(" ")) {
                    if (!normpass.contains("oauth")) {
                        JOptionPane.showMessageDialog(this,
                                "The password must be the entire oauth string!" +
                                        "\n See http://help.twitch.tv/customer/portal/articles/1302780-twitch-irc for more info.",
                                "Password Needs Oauth", JOptionPane.ERROR_MESSAGE);
                        normPass.setText("");
                    } else {//TODO make the Account GUI
                        //GUIMain.viewer = new IRCViewer(normus, normpass);
                        //GUIMain.manageAccount.setEnabled(true);
                    }
                }
            }*/
        }
    }

    public void botLoginButtonActionPerformed() {
        if (GUIMain.bot == null) {
            String botus = botUser.getText().toLowerCase();
            String botpass = new String(botPass.getPassword());
            if (!botus.equals("") && !botus.contains(" ")) {
                if (!botpass.equals("") && !botpass.contains(" ")) {
                    if (!botpass.contains("oauth")) {
                        JOptionPane.showMessageDialog(this,
                                "The password must be the entire oauth string!" +
                                        "\n See http://help.twitch.tv/customer/portal/articles/1302780-twitch-irc for more info.",
                                "Password Needs Oauth", JOptionPane.ERROR_MESSAGE);
                        botPass.setText("");
                    } else {//TODO make the account GUI
                        GUIMain.currentSettings.accountManager.setBotAccount(new Account(botus, new Oauth(botpass, false, false)));
                        GUIMain.currentSettings.accountManager.addTask(new Task(null, Task.Type.CREATE_BOT_ACCOUNT, null));
                    }
                }
            }
        }
    }

    public void soundTreeMouseReleased() {
        removeSoundButton.setEnabled(!soundTree.isSelectionEmpty());
    }

    public void searchFileActionPerformed() {
        if (s2 == null) {
            s2 = new GUISounds_2();
        }
        if (!s2.isVisible()) {
            s2.setVisible(true);
        }
    }

    public void removeSoundButtonActionPerformed() {
        if (!soundTree.isSelectionEmpty()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) soundTree.getLastSelectedPathComponent();
            if (!node.isRoot()) {//don't remove everything silly
                DefaultTreeModel model = (DefaultTreeModel) soundTree.getModel();
                if (node.getChildCount() > 0) { // parent (all the sounds)
                    GUIMain.soundMap.remove(node.getUserObject().toString().split("-")[0]);
                    node.removeAllChildren();
                    node.removeFromParent();
                    model.reload();
                } else { //one sound clip
                    if (node.getParent().getChildCount() == 1) {
                        //this is one sound clip; they decided to delete the file, so let's delete the parent.
                        DefaultMutableTreeNode node1 = ((DefaultMutableTreeNode) node.getParent());
                        GUIMain.soundMap.remove(node1.getUserObject().toString().split("-")[0]);
                        node1.removeAllChildren();
                        node1.removeFromParent();
                        model.reload();
                    } else {//they're removing just one file.
                        model.removeNodeFromParent(node);
                    }
                }
            }
        }
    }

    public void customAdminButtonActionPerformed() {
        setIcon(2);
    }

    public void customStaffButtonActionPerformed() {
        setIcon(3);
    }

    public void clearChatCheckStateChanged() {
        clearChatSpinner.setEnabled(clearChatCheck.isSelected());
    }

    public void changeFontButtonActionPerformed() {
        JFontChooser jfc = new JFontChooser(Constants.fontSizeArray);
        jfc.setSelectedFont(GUIMain.currentSettings.font);
        if (jfc.showDialog(this) == JFontChooser.OK_OPTION) {
            if (jfc.getSelectedFont() != null) GUIMain.currentSettings.font = jfc.getSelectedFont();
            StyleConstants.setFontFamily(GUIMain.norm, GUIMain.currentSettings.font.getFamily());
            StyleConstants.setFontSize(GUIMain.norm, GUIMain.currentSettings.font.getSize());
            currentFontLabel.setText(Utils.fontToString(GUIMain.currentSettings.font));
            currentFontLabel.setFont(GUIMain.currentSettings.font);
        }
    }

    public void setIcon(JLabel label, URL image) {
        try {
            BufferedImage img = ImageIO.read(image);
            if (img.getWidth() > 40) {
                img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, 40, 14);
            } else {
                img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 14);
            }
            ImageIcon icon = new ImageIcon(img);
            icon.getImage().flush();
            label.setIcon(icon);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        tabbedPane1 = new JTabbedPane();
        accountsPanel = new JPanel();
        label9 = new JLabel();
        label10 = new JLabel();
        normUser = new JTextField();
        normPass = new JPasswordField();
        normUsername = new JLabel();
        label6 = new JLabel();
        userLogoutButton = new JButton();
        userLoginButton = new JButton();
        label7 = new JLabel();
        label8 = new JLabel();
        botUser = new JTextField();
        botPass = new JPasswordField();
        separator3 = new JSeparator();
        rememberBotLogin = new JCheckBox();
        rememberNormLogin = new JCheckBox();
        autoLoginCheck = new JCheckBox();
        label11 = new JLabel();
        botLoginButton = new JButton();
        botLogoutButton = new JButton();
        customDirPanel = new JPanel();
        separator2 = new JSeparator();
        label1 = new JLabel();
        label14 = new JLabel();
        faceButton = new JButton();
        faceDir = new JTextField();
        textArea1 = new JTextArea();
        textArea2 = new JTextArea();
        label12 = new JLabel();
        soundDir = new JTextField();
        label2 = new JLabel();
        soundsButton = new JButton();
        customIconPanel = new JPanel();
        useCustomMod = new JCheckBox();
        label3 = new JLabel();
        customMod = new JTextField();
        customModButton = new JButton();
        useCustomBroad = new JCheckBox();
        label4 = new JLabel();
        customBroad = new JTextField();
        customBroadButton = new JButton();
        useCustomAdmin = new JCheckBox();
        customAdminButton = new JButton();
        useCustomStaff = new JCheckBox();
        label13 = new JLabel();
        customAdminField = new JTextField();
        customStaffButton = new JButton();
        label15 = new JLabel();
        customStaffField = new JTextField();
        textArea3 = new JTextArea();
        label16 = new JLabel();
        label17 = new JLabel();
        label18 = new JLabel();
        label19 = new JLabel();
        customStaffIconCurrent = new JLabel();
        label21 = new JLabel();
        label22 = new JLabel();
        label23 = new JLabel();
        label24 = new JLabel();
        customAdminIconCurrent = new JLabel();
        customBroadIconCurrent = new JLabel();
        customBroadIconNew = new JLabel();
        customAdminIconNew = new JLabel();
        customStaffIconNew = new JLabel();
        customModIconCurrent = new JLabel();
        customModIconNew = new JLabel();
        soundsPanel = new JPanel();
        scrollPane1 = new JScrollPane();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Sounds");
        soundTree = new JTree(node);
        soundTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        searchFile = new JButton();
        removeSoundButton = new JButton();
        chatSettingsPanel = new JPanel();
        label25 = new JLabel();
        clearChatCheck = new JCheckBox();
        label5 = new JLabel();
        label20 = new JLabel();
        clearChatSpinner = new JSpinner();
        logChatCheck = new JCheckBox();
        label26 = new JLabel();
        label27 = new JLabel();
        currentFontLabel = new JLabel();
        changeFontButton = new JButton();
        separator1 = new JSeparator();
        label28 = new JLabel();
        label29 = new JLabel();
        graphiteButton = new JRadioButton();
        label31 = new JLabel();
        label30 = new JLabel();
        hifiButton = new JRadioButton();
        saveButton = new JButton();
        cancelButton = new JButton();

        faceDir.setFocusable(false);
        soundDir.setFocusable(false);
        customMod.setFocusable(false);
        customBroad.setFocusable(false);
        customAdminField.setFocusable(false);
        customStaffField.setFocusable(false);
        //======== this ========
        setTitle("Settings");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
        Container contentPane = getContentPane();

        //======== tabbedPane1 ========
        {
            tabbedPane1.setFocusable(false);

            //======== accountsPanel ========
            {

                //---- label9 ----
                label9.setText("User");
                label9.setFont(new Font("Tahoma", Font.BOLD, 14));

                //---- label10 ----
                label10.setText("Bot");
                label10.setFont(new Font("Tahoma", Font.BOLD, 14));

                //---- normUsername ----
                normUsername.setText("Username:");
                normUsername.setToolTipText("This is for your normal account for chatting in the client.");

                //---- label6 ----
                label6.setText("OAuth Key:");
                label6.setToolTipText("This is for your normal account for chatting in the client.");

                //---- userLogoutButton ----
                userLogoutButton.setText("Logout");
                userLogoutButton.setToolTipText("Logs out of the current Normal account.");
                userLogoutButton.setFocusable(false);
                userLogoutButton.addActionListener(e -> userLogoutButtonActionPerformed());

                //---- userLoginButton ----
                userLoginButton.setText("Setup User Account");
                userLoginButton.setFocusable(false);
                userLoginButton.addActionListener(e -> userLoginButtonActionPerformed());

                //---- label7 ----
                label7.setText("Username:");

                //---- label8 ----
                label8.setText("OAuth Key:");

                //---- separator3 ----
                separator3.setOrientation(SwingConstants.VERTICAL);

                //---- rememberBotLogin ----
                rememberBotLogin.setText("Remember Me");
                rememberBotLogin.setFocusable(false);
                rememberBotLogin.setEnabled(false);

                //---- rememberNormLogin ----
                rememberNormLogin.setText("Remember Me");
                rememberNormLogin.setFocusable(false);
                rememberNormLogin.setEnabled(false);

                //---- autoLoginCheck ----
                autoLoginCheck.setText("To be updated!");
                autoLoginCheck.setFocusable(false);
                autoLoginCheck.setEnabled(false);

                //---- label11 ----
                label11.setText("(To be updated!)");

                //---- botLoginButton ----
                botLoginButton.setText("Login");
                botLoginButton.setFocusable(false);
                botLoginButton.addActionListener(e -> botLoginButtonActionPerformed());

                //---- botLogoutButton ----
                botLogoutButton.setText("Logout");
                botLogoutButton.setFocusable(false);
                botLogoutButton.setToolTipText("Logs out of the current Bot account.");
                botLogoutButton.addActionListener(e -> botLogoutButtonActionPerformed());
                normUser.setEnabled(false);
                normPass.setEnabled(false);
                if (GUIMain.loadedSettingsUser()) {
                    normUser.setText(GUIMain.currentSettings.accountManager.getUserAccount().getName());
                    normPass.setText(GUIMain.currentSettings.accountManager.getUserAccount().getKey().getKey());
                    userLogoutButton.setEnabled(true);
                }
                if (GUIMain.loadedSettingsBot()) {
                    botUser.setText(GUIMain.currentSettings.accountManager.getBotAccount().getName());
                    botPass.setText(GUIMain.currentSettings.accountManager.getBotAccount().getKey().getKey());
                    botLogoutButton.setEnabled(true);
                }

                GroupLayout accountsPanelLayout = new GroupLayout(accountsPanel);
                accountsPanel.setLayout(accountsPanelLayout);
                accountsPanelLayout.setHorizontalGroup(
                        accountsPanelLayout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createSequentialGroup()
                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                                                .addComponent(label6)
                                                                                                .addComponent(normUsername))
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                                                .addComponent(normPass)
                                                                                                .addComponent(normUser)))
                                                                                .addComponent(rememberNormLogin)))
                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                        .addGap(145, 145, 145)
                                                                        .addComponent(label9)
                                                                        .addGap(0, 0, Short.MAX_VALUE)))
                                                        .addGap(20, 20, 20))
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(userLoginButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(userLogoutButton)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                .addComponent(autoLoginCheck)
                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                        .addGap(10, 10, 10)
                                                                        .addComponent(label11)))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(separator3, GroupLayout.PREFERRED_SIZE, 5, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addGap(145, 145, 145)
                                                        .addComponent(label10))
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                        .addComponent(label7)
                                                                        .addGap(22, 22, 22)
                                                                        .addComponent(botUser, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                                        .addComponent(label8)
                                                                        .addGap(22, 22, 22)
                                                                        .addComponent(botPass, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE))))
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(rememberBotLogin))
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(botLoginButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(botLogoutButton)))
                                        .addGap(10, 10, 10))
                );
                accountsPanelLayout.setVerticalGroup(
                        accountsPanelLayout.createParallelGroup()
                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                .addComponent(label10)
                                                                .addComponent(label9))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(normUsername)
                                                                .addComponent(normUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addGap(35, 35, 35)
                                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(label6)
                                                                .addComponent(normPass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createSequentialGroup()
                                                        .addGap(36, 36, 36)
                                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(label7)
                                                                .addComponent(botUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addGap(34, 34, 34)
                                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(label8)
                                                                .addComponent(botPass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addGroup(GroupLayout.Alignment.LEADING, accountsPanelLayout.createSequentialGroup()
                                                        .addGap(28, 28, 28)
                                                        .addComponent(rememberBotLogin)
                                                        .addContainerGap())
                                                .addGroup(accountsPanelLayout.createSequentialGroup()
                                                        .addGap(26, 26, 26)
                                                        .addComponent(rememberNormLogin)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createSequentialGroup()
                                                                        .addGroup(accountsPanelLayout.createParallelGroup()
                                                                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createSequentialGroup()
                                                                                        .addComponent(autoLoginCheck)
                                                                                        .addGap(7, 7, 7)
                                                                                        .addComponent(label11))
                                                                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(userLoginButton)
                                                                                        .addComponent(userLogoutButton)))
                                                                        .addGap(24, 24, 24))
                                                                .addGroup(GroupLayout.Alignment.TRAILING, accountsPanelLayout.createSequentialGroup()
                                                                        .addGroup(accountsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(botLoginButton)
                                                                                .addComponent(botLogoutButton))
                                                                        .addGap(25, 25, 25))))))
                                .addComponent(separator3)
                );
            }
            tabbedPane1.addTab("Accounts", accountsPanel);

            //======== customDirPanel ========
            {

                //---- label1 ----
                label1.setText("Custom Face Directory:");

                //---- label14 ----
                label14.setText("Custom Faces");
                label14.setFont(new Font("Tahoma", Font.BOLD, 26));

                //---- faceButton ----
                faceButton.setText("Browse...");
                faceButton.setFocusable(false);
                faceButton.addActionListener(e -> faceButtonActionPerformed());
                faceDir.setText(GUIMain.currentSettings.defaultFaceDir);

                //---- textArea1 ----
                textArea1.setText("This should be a seperate folder from the one found in the Botnak folder. \nFiles in this directory can be read whenever \"!addface\" or \"!changeface\" is called from the chat. \nIt is recommended that you set this to a Dropbox (shared) folder so that people can add faces locally.");
                textArea1.setFont(new Font("Tahoma", Font.PLAIN, 11));
                textArea1.setFocusable(false);
                textArea1.setEditable(false);

                //---- textArea2 ----
                textArea2.setText("This is a folder where Botnak reads most sounds from. \nFiles in this directory can be read whenever \"!addsound\" or \"!changesound\" is called from the chat. \nIt is recommended that you set this to a Dropbox (shared) folder so that other people may add sounds.");
                textArea2.setFont(new Font("Tahoma", Font.PLAIN, 11));
                textArea2.setFocusable(false);
                textArea2.setEditable(false);

                //---- label12 ----
                label12.setText("Custom Sounds");
                label12.setFont(new Font("Tahoma", Font.BOLD, 26));

                //---- label2 ----
                label2.setText("Custom Sound Directory:");

                //---- soundsButton ----
                soundsButton.setText("Browse...");
                soundsButton.setFocusable(false);
                soundsButton.addActionListener(e -> soundsButtonActionPerformed());
                soundDir.setText(GUIMain.currentSettings.defaultSoundDir);

                GroupLayout customDirPanelLayout = new GroupLayout(customDirPanel);
                customDirPanel.setLayout(customDirPanelLayout);
                customDirPanelLayout.setHorizontalGroup(
                        customDirPanelLayout.createParallelGroup()
                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(customDirPanelLayout.createParallelGroup()
                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                        .addComponent(label14)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                                                        .addComponent(textArea1, GroupLayout.PREFERRED_SIZE, 498, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(separator2, GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                        .addComponent(label12)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                                                        .addComponent(textArea2, GroupLayout.PREFERRED_SIZE, 505, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                        .addGroup(customDirPanelLayout.createParallelGroup()
                                                                .addGroup(GroupLayout.Alignment.TRAILING, customDirPanelLayout.createSequentialGroup()
                                                                        .addComponent(label2)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                                                        .addComponent(soundDir, GroupLayout.PREFERRED_SIZE, 604, GroupLayout.PREFERRED_SIZE))
                                                                .addComponent(soundsButton)
                                                                .addComponent(faceButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                                        .addComponent(label1)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(faceDir, GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)))
                                                        .addContainerGap())))
                );
                customDirPanelLayout.setVerticalGroup(
                        customDirPanelLayout.createParallelGroup()
                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                        .addGroup(customDirPanelLayout.createParallelGroup()
                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(label14))
                                                .addComponent(textArea1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(customDirPanelLayout.createParallelGroup()
                                                .addGroup(customDirPanelLayout.createSequentialGroup()
                                                        .addGap(21, 21, 21)
                                                        .addComponent(faceButton)
                                                        .addGap(61, 61, 61))
                                                .addGroup(GroupLayout.Alignment.TRAILING, customDirPanelLayout.createSequentialGroup()
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(customDirPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(faceDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(label1))
                                                        .addGap(18, 18, 18)))
                                        .addComponent(separator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(customDirPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(textArea2, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label12))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                                        .addComponent(soundsButton)
                                        .addGap(18, 18, 18)
                                        .addGroup(customDirPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(soundDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label2))
                                        .addContainerGap())
                );
            }
            tabbedPane1.addTab("Custom Directories", customDirPanel);

            //======== customIconPanel ========
            {

                //---- useCustomMod ----
                useCustomMod.setText("Use Custom Mod Icon");
                useCustomMod.setFocusable(false);
                useCustomMod.addChangeListener(e -> useCustomModStateChanged());
                useCustomMod.setSelected(GUIMain.currentSettings.useMod);
                customMod.setText(GUIMain.currentSettings.modIcon.toString());
                setIcon(customModIconCurrent, GUIMain.currentSettings.modIcon);

                //---- label3 ----
                label3.setText("Custom Mod Icon:");

                //---- customMod ----
                customMod.setEnabled(useCustomMod.isSelected());

                //---- customModButton ----
                customModButton.setText("Browse...");
                customModButton.setFocusable(false);
                customModButton.addActionListener(e -> customModButtonActionPerformed());

                //---- useCustomBroad ----
                useCustomBroad.setText("Use Custom Broadcaster Icon");
                useCustomBroad.setFocusable(false);
                useCustomBroad.addChangeListener(e -> useCustomBroadStateChanged());
                useCustomBroad.setSelected(GUIMain.currentSettings.useBroad);
                customBroad.setText(GUIMain.currentSettings.broadIcon.toString());
                setIcon(customBroadIconCurrent, GUIMain.currentSettings.broadIcon);

                //---- label4 ----
                label4.setText("Custom Broadcaster Icon:");

                //---- customBroad ----
                customBroad.setEnabled(useCustomBroad.isSelected());

                //---- customBroadButton ----
                customBroadButton.setText("Browse...");
                customBroadButton.setFocusable(false);
                customBroadButton.addActionListener(e -> customBroadButtonActionPerformed());

                //---- useCustomAdmin ----
                useCustomAdmin.setText("Use Custom Admin Icon");
                useCustomAdmin.setFocusable(false);
                useCustomAdmin.addChangeListener(e -> useCustomAdminStateChanged());
                useCustomAdmin.setSelected(GUIMain.currentSettings.useAdmin);
                customAdminField.setText(GUIMain.currentSettings.adminIcon.toString());
                setIcon(customAdminIconCurrent, GUIMain.currentSettings.adminIcon);

                //---- customAdminButton ----
                customAdminButton.setText("Browse...");
                customAdminButton.setFocusable(false);
                customAdminButton.addActionListener(e -> customAdminButtonActionPerformed());

                //---- useCustomStaff ----
                useCustomStaff.setText("Use Custom Staff Icon");
                useCustomStaff.setFocusable(false);
                useCustomStaff.addChangeListener(e -> useCustomStaffStateChanged());
                useCustomStaff.setSelected(GUIMain.currentSettings.useStaff);
                customStaffField.setText(GUIMain.currentSettings.staffIcon.toString());
                setIcon(customStaffIconCurrent, GUIMain.currentSettings.staffIcon);


                //---- label13 ----
                label13.setText("Custom Admin Icon:");

                //---- customAdminField ----
                customAdminField.setEnabled(useCustomAdmin.isSelected());

                //---- customStaffButton ----
                customStaffButton.setText("Browse...");
                customStaffButton.setFocusable(false);
                customStaffButton.addActionListener(e -> customStaffButtonActionPerformed());

                //---- label15 ----
                label15.setText("Custom Staff Icon:");

                //---- customStaffField ----
                customStaffField.setEnabled(useCustomStaff.isSelected());

                //---- textArea3 ----
                textArea3.setEditable(false);
                textArea3.setText("Icons should be about the same height as your text.\nFor example, at size 18 font, an ideal icon is 18x18.");
                textArea3.setFont(new Font("Tahoma", Font.PLAIN, 11));

                //---- label16 ----
                label16.setText("Current:");

                //---- label17 ----
                label17.setText("Current:");

                //---- label18 ----
                label18.setText("Current:");

                //---- label19 ----
                label19.setText("Current:");

                //---- customStaffIconCurrent ----
                customStaffIconCurrent.setText("    ");

                //---- label21 ----
                label21.setText("New:");

                //---- label22 ----
                label22.setText("New:");

                //---- label23 ----
                label23.setText("New:");

                //---- label24 ----
                label24.setText("New:");

                //---- customAdminIconCurrent ----
                customAdminIconCurrent.setText("   ");

                //---- customBroadIconCurrent ----
                customBroadIconCurrent.setText("  ");

                //---- customBroadIconNew ----
                customBroadIconNew.setText("  ");

                //---- customAdminIconNew ----
                customAdminIconNew.setText("     ");

                //---- customStaffIconNew ----
                customStaffIconNew.setText("    ");

                //---- customModIconCurrent ----
                customModIconCurrent.setText("  ");

                //---- customModIconNew ----
                customModIconNew.setText("   ");

                GroupLayout customIconPanelLayout = new GroupLayout(customIconPanel);
                customIconPanel.setLayout(customIconPanelLayout);
                customIconPanelLayout.setHorizontalGroup(
                        customIconPanelLayout.createParallelGroup()
                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(customIconPanelLayout.createParallelGroup()
                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                        .addGroup(customIconPanelLayout.createParallelGroup()
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(label3)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(customMod))
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(label4)
                                                                        .addGap(10, 10, 10)
                                                                        .addComponent(customBroad))
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(label13)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customAdminField))
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(label15)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customStaffField)))
                                                        .addContainerGap())
                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                        .addGroup(customIconPanelLayout.createParallelGroup()
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(useCustomAdmin)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customAdminButton)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(label18)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customAdminIconCurrent)
                                                                        .addGap(12, 12, 12)
                                                                        .addComponent(label23)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(customAdminIconNew))
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(useCustomBroad)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customBroadButton)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(label17)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(customBroadIconCurrent, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(26, 26, 26)
                                                                        .addComponent(label22)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(customBroadIconNew))
                                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                                        .addComponent(useCustomStaff)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(customStaffButton)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(label19)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(customStaffIconCurrent)
                                                                        .addGap(22, 22, 22)
                                                                        .addComponent(label24)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(customStaffIconNew)))
                                                        .addGap(0, 252, Short.MAX_VALUE))
                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                        .addComponent(useCustomMod)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(customModButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(label16)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(customModIconCurrent)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(label21)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(customModIconNew)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                                                        .addComponent(textArea3, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE))))
                );
                customIconPanelLayout.setVerticalGroup(
                        customIconPanelLayout.createParallelGroup()
                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                        .addGroup(customIconPanelLayout.createParallelGroup()
                                                .addGroup(customIconPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(useCustomMod)
                                                                .addComponent(customModButton)
                                                                .addComponent(label16)
                                                                .addComponent(customModIconCurrent)
                                                                .addComponent(label21)
                                                                .addComponent(customModIconNew)))
                                                .addComponent(textArea3, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                                        .addGap(11, 11, 11)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label3)
                                                .addComponent(customMod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(useCustomBroad)
                                                .addComponent(customBroadButton)
                                                .addComponent(label17)
                                                .addComponent(label22)
                                                .addComponent(customBroadIconNew)
                                                .addComponent(customBroadIconCurrent))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(customBroad, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label4))
                                        .addGap(18, 18, 18)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(useCustomAdmin)
                                                        .addComponent(customAdminButton))
                                                .addGroup(GroupLayout.Alignment.TRAILING, customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(label18)
                                                        .addComponent(label23)
                                                        .addComponent(customAdminIconCurrent, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addComponent(customAdminIconNew, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label13)
                                                .addComponent(customAdminField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(useCustomStaff)
                                                        .addComponent(customStaffButton))
                                                .addGroup(GroupLayout.Alignment.TRAILING, customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(label19)
                                                        .addComponent(label24)
                                                        .addComponent(customStaffIconNew)
                                                        .addComponent(customStaffIconCurrent, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGap(18, 18, 18)
                                        .addGroup(customIconPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label15)
                                                .addComponent(customStaffField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(15, 15, 15))
                );
            }
            tabbedPane1.addTab("Custom Icons", customIconPanel);

            //======== soundsPanel ========
            {

                //======== scrollPane1 ========
                {

                    //---- soundTree ----
                    soundTree.setFocusable(false);
                    soundTree.setShowsRootHandles(true);
                    soundTree.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            soundTreeMouseReleased();
                        }
                    });
                    scrollPane1.setViewportView(soundTree);
                }

                //---- searchFile ----
                searchFile.setText("Add Sound(s)");
                searchFile.setFocusable(false);
                searchFile.setToolTipText("Add sound(s) to the sound tree.");
                searchFile.addActionListener(e -> searchFileActionPerformed());

                //---- removeSoundButton ----
                removeSoundButton.setText("Remove Sound");
                removeSoundButton.setFocusable(false);
                removeSoundButton.addActionListener(e -> removeSoundButtonActionPerformed());

                GroupLayout soundsPanelLayout = new GroupLayout(soundsPanel);
                soundsPanel.setLayout(soundsPanelLayout);
                soundsPanelLayout.setHorizontalGroup(
                        soundsPanelLayout.createParallelGroup()
                                .addGroup(soundsPanelLayout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addComponent(searchFile, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(removeSoundButton)
                                        .addContainerGap(497, Short.MAX_VALUE))
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 754, Short.MAX_VALUE)
                );
                soundsPanelLayout.setVerticalGroup(
                        soundsPanelLayout.createParallelGroup()
                                .addGroup(soundsPanelLayout.createSequentialGroup()
                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(soundsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(searchFile)
                                                .addComponent(removeSoundButton))
                                        .addContainerGap())
                );
            }
            tabbedPane1.addTab("Sound Manager", soundsPanel);

            //======== chatSettingsPanel ========
            {

                //---- label25 ----
                label25.setText("Logging & Cleanup");
                label25.setFont(new Font("Tahoma", Font.BOLD, 15));

                //---- clearChatCheck ----
                clearChatCheck.setText("Clear The Chat");
                clearChatCheck.setSelected(GUIMain.currentSettings.cleanupChat);
                clearChatCheck.setFocusable(false);
                clearChatCheck.addChangeListener(e -> clearChatCheckStateChanged());

                //---- label5 ----
                label5.setText("After");

                //---- label20 ----
                label20.setText("lines");

                //---- clearChatSpinner ----
                if (GUIMain.currentSettings.chatMax < 40) GUIMain.currentSettings.chatMax = 40;
                clearChatSpinner.setModel(new SpinnerNumberModel(GUIMain.currentSettings.chatMax, 40, null, 1));
                clearChatSpinner.setFocusable(false);
                clearChatSpinner.setEnabled(GUIMain.currentSettings.cleanupChat);

                //---- logChatCheck ----
                logChatCheck.setText("Log The Chat To File");
                logChatCheck.setFocusable(false);
                logChatCheck.setSelected(GUIMain.currentSettings.logChat);

                //---- label26 ----
                label26.setText("Appearance");
                label26.setFont(new Font("Tahoma", Font.BOLD, 15));

                //---- label27 ----
                label27.setText("Current Font:");

                //---- currentFontLabel ----
                currentFontLabel.setText(Utils.fontToString(GUIMain.currentSettings.font));
                currentFontLabel.setFont(GUIMain.currentSettings.font);

                //---- changeFontButton ----
                changeFontButton.setText("Change Font...");
                changeFontButton.setFocusable(false);
                changeFontButton.addActionListener(e -> changeFontButtonActionPerformed());

                //---- separator1 ----
                separator1.setOrientation(SwingConstants.VERTICAL);

                //---- label28 ----
                label28.setText("Look and Feel");
                label28.setFont(new Font("Tahoma", Font.BOLD, 16));

                //---- label29 ----
                label29.setText("(changes to LaF will be made next time you boot Botnak)");

                //---- graphiteButton ----
                graphiteButton.setText("Graphite");
                graphiteButton.setFocusable(false);
                graphiteButton.setSelected(Settings.lookAndFeel.contains("Graphite"));
                graphiteButton.setActionCommand("Graphite");

                //---- label31 ----
                label31.setIcon(new ImageIcon(getClass().getResource("/resource/graphite.png")));

                //---- label30 ----
                label30.setIcon(new ImageIcon(getClass().getResource("/resource/hifi.png")));

                //---- hifiButton ----
                hifiButton.setText("HiFi");
                hifiButton.setFocusable(false);
                hifiButton.setSelected(Settings.lookAndFeel.contains("HiFi"));
                hifiButton.setActionCommand("HiFi");

                GroupLayout chatSettingsPanelLayout = new GroupLayout(chatSettingsPanel);
                chatSettingsPanel.setLayout(chatSettingsPanelLayout);
                chatSettingsPanelLayout.setHorizontalGroup(
                        chatSettingsPanelLayout.createParallelGroup()
                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                                .addComponent(label25)
                                                                .addComponent(currentFontLabel, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(clearChatCheck)
                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                        .addGap(33, 33, 33)
                                                                        .addComponent(label5)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(clearChatSpinner, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(label20))))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(19, 19, 19)
                                                        .addComponent(label27))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(label26))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(logChatCheck))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(changeFontButton)))
                                        .addGap(18, 18, 18)
                                        .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(96, 96, 96)
                                                        .addComponent(label29)
                                                        .addGap(0, 137, Short.MAX_VALUE))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                                        .addComponent(label31)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE))
                                                                                .addGroup(GroupLayout.Alignment.TRAILING, chatSettingsPanelLayout.createSequentialGroup()
                                                                                        .addGap(0, 70, Short.MAX_VALUE)
                                                                                        .addComponent(graphiteButton)
                                                                                        .addGap(165, 165, 165)))
                                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                                                .addGroup(GroupLayout.Alignment.TRAILING, chatSettingsPanelLayout.createSequentialGroup()
                                                                                        .addComponent(label30)
                                                                                        .addGap(44, 44, 44))
                                                                                .addGroup(GroupLayout.Alignment.TRAILING, chatSettingsPanelLayout.createSequentialGroup()
                                                                                        .addComponent(hifiButton)
                                                                                        .addGap(107, 107, 107))))
                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                        .addGap(176, 176, 176)
                                                                        .addComponent(label28)
                                                                        .addContainerGap(212, Short.MAX_VALUE))))))
                );
                chatSettingsPanelLayout.setVerticalGroup(
                        chatSettingsPanelLayout.createParallelGroup()
                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 310, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(chatSettingsPanelLayout.createParallelGroup()
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addComponent(label25)
                                                        .addGap(25, 25, 25)
                                                        .addComponent(clearChatCheck)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(label20)
                                                                .addComponent(clearChatSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(label5))
                                                        .addGap(18, 18, 18)
                                                        .addComponent(logChatCheck)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(label26)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(label27)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(currentFontLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                                                        .addComponent(changeFontButton)
                                                        .addGap(25, 25, 25))
                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                        .addComponent(label28)
                                                        .addGap(32, 32, 32)
                                                        .addGroup(chatSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                        .addComponent(graphiteButton)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(label31))
                                                                .addGroup(chatSettingsPanelLayout.createSequentialGroup()
                                                                        .addComponent(hifiButton)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(label30)))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                                                        .addComponent(label29)
                                                        .addGap(50, 50, 50))))
                );
            }
            tabbedPane1.addTab("Chat Settings", chatSettingsPanel);
        }

        //---- saveButton ----
        saveButton.setText("Save");
        saveButton.setFocusable(false);
        saveButton.addActionListener(e -> saveButtonActionPerformed());

        //---- cancelButton ----
        cancelButton.setText("Close");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(e -> cancelButtonActionPerformed());

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(saveButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cancelButton)
                                .addGap(6, 6, 6))
                        .addComponent(tabbedPane1)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(tabbedPane1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        buttonGroup = new ButtonGroup();
        buttonGroup.add(graphiteButton);
        buttonGroup.add(hifiButton);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static ButtonGroup buttonGroup;
    public static JTabbedPane tabbedPane1;
    public static JPanel accountsPanel;
    public static JLabel label9;
    public static JLabel label10;
    public static JTextField normUser;
    public static JPasswordField normPass;
    public static JLabel normUsername;
    public static JLabel label6;
    public static JButton userLogoutButton;
    public static JButton userLoginButton;
    public static JLabel label7;
    public static JLabel label8;
    public static JTextField botUser;
    public static JPasswordField botPass;
    public static JSeparator separator3;
    public static JCheckBox rememberBotLogin;
    public static JCheckBox rememberNormLogin;
    public static JCheckBox autoLoginCheck;
    public static JLabel label11;
    public static JButton botLoginButton;
    public static JButton botLogoutButton;
    public static JPanel customDirPanel;
    public static JSeparator separator2;
    public static JLabel label1;
    public static JLabel label14;
    public static JButton faceButton;
    public static JTextField faceDir;
    public static JTextArea textArea1;
    public static JTextArea textArea2;
    public static JLabel label12;
    public static JTextField soundDir;
    public static JLabel label2;
    public static JButton soundsButton;
    public static JPanel customIconPanel;
    public static JCheckBox useCustomMod;
    public static JLabel label3;
    public static JTextField customMod;
    public static JButton customModButton;
    public static JCheckBox useCustomBroad;
    public static JLabel label4;
    public static JTextField customBroad;
    public static JButton customBroadButton;
    public static JCheckBox useCustomAdmin;
    public static JButton customAdminButton;
    public static JCheckBox useCustomStaff;
    public static JLabel label13;
    public static JTextField customAdminField;
    public static JButton customStaffButton;
    public static JLabel label15;
    public static JTextField customStaffField;
    public static JTextArea textArea3;
    public static JLabel label16;
    public static JLabel label17;
    public static JLabel label18;
    public static JLabel label19;
    public static JLabel customStaffIconCurrent;
    public static JLabel label21;
    public static JLabel label22;
    public static JLabel label23;
    public static JLabel label24;
    public static JLabel customAdminIconCurrent;
    public static JLabel customBroadIconCurrent;
    public static JLabel customBroadIconNew;
    public static JLabel customAdminIconNew;
    public static JLabel customStaffIconNew;
    public static JLabel customModIconCurrent;
    public static JLabel customModIconNew;
    public static JPanel soundsPanel;
    public static JScrollPane scrollPane1;
    public static JTree soundTree;
    public static JButton searchFile;
    public static JButton removeSoundButton;
    public static JPanel chatSettingsPanel;
    public static JLabel label25;
    public static JCheckBox clearChatCheck;
    public static JLabel label5;
    public static JLabel label20;
    public static JSpinner clearChatSpinner;
    public static JCheckBox logChatCheck;
    public static JLabel label26;
    public static JLabel label27;
    public static JLabel currentFontLabel;
    public static JButton changeFontButton;
    public static JSeparator separator1;
    public static JLabel label28;
    public static JLabel label29;
    public static JRadioButton graphiteButton;
    public static JLabel label31;
    public static JLabel label30;
    public static JRadioButton hifiButton;
    public static JButton saveButton;
    public static JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static String[] filePaths;

    class GUISounds_2 extends JFrame {
        public GUISounds_2() {
            initComponents();
        }

        public void browseButtonActionPerformed() {
            JFileChooser jfc = new JFileChooser();
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.addChoosableFileFilter(Constants.wavfiles);
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

        public void saveButtonActionPerformed() {
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
            s2 = null;
            dispose();
        }

        public void cancelButtonActionPerformed() {
            s2 = null;
            dispose();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner Evaluation license - Nick K
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
            setTitle("Add Sound(s)");
            setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
            Container contentPane = getContentPane();

            //---- label2 ----
            label2.setText("Command Name:");

            //---- filesSelectedLabel ----
            filesSelectedLabel.setText("No files selected.");
            filesSelectedLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));

            //---- browseButton ----
            browseButton.setText("Browse...");
            browseButton.setFocusable(false);
            browseButton.addActionListener(e -> browseButtonActionPerformed());

            //---- saveButton ----
            saveButton.setText("Save");
            saveButton.setFocusable(false);
            saveButton.addActionListener(e -> saveButtonActionPerformed());

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.addActionListener(e -> cancelButtonActionPerformed());

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
                                                    .addGroup(contentPaneLayout.createParallelGroup()
                                                            .addComponent(label2)
                                                            .addComponent(label3, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(18, 18, 18)
                                                    .addGroup(contentPaneLayout.createParallelGroup()
                                                            .addComponent(commandField)
                                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                                    .addComponent(permissionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(0, 68, Short.MAX_VALUE))))
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addComponent(browseButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                                                    .addComponent(saveButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(cancelButton))
                                            .addGroup(contentPaneLayout.createSequentialGroup()
                                                    .addComponent(filesSelectedLabel)
                                                    .addGap(0, 0, Short.MAX_VALUE)))
                                    .addContainerGap())
            );
            contentPaneLayout.setVerticalGroup(
                    contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label2)
                                            .addComponent(commandField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(permissionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label3, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
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
        // Generated using JFormDesigner Evaluation license - Nick K
        public JLabel label2;
        public JLabel filesSelectedLabel;
        public JButton browseButton;
        public JTextField commandField;
        public JButton saveButton;
        public JButton cancelButton;
        public JLabel label3;
        public JComboBox<String> permissionBox;
    }

}

