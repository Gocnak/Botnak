package gui;

import irc.IRCBot;
import irc.IRCViewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;


public class GUILogin extends JFrame {

    public GUILogin() {
        initComponents();
        if (GUIMain.loadedSettingsUser()) {
            rememberNormLogin.setSelected(GUIMain.rememberNorm);
            normUser.setText(GUIMain.userNorm);
            normPass.setText(GUIMain.userNormPass);
            autoLoginBox.setSelected(GUIMain.autoLog);
        }
        if (GUIMain.loadedSettingsBot()) {
            rememberBotLogin.setSelected(GUIMain.rememberBot);
            botUser.setText(GUIMain.userBot);
            botPass.setText(GUIMain.userBotPass);
        }
    }

    public static String user, userpass, botuser, bpass;


    public void loginButtonActionPerformed() {
        String normus = normUser.getText();
        String normpass = new String(normPass.getPassword());
        String botus = botUser.getText();
        String botpass = new String(botPass.getPassword());
        if (normus != null && !normus.equals("") && !normus.contains(" ")) {
            if (!normpass.equals("") && !normpass.contains(" ")) {
                user = normus;
                userpass = normpass;
                GUIMain.viewer = new IRCViewer(user, userpass);
                GUIMain.rememberNorm = rememberNormLogin.isSelected();
                GUIMain.addStream.setEnabled(true);
            }
        }
        if (botus != null && !botus.equals("") && !botus.contains(" ")) {
            if (!botpass.equals("") && !botpass.contains(" ")) {
                botuser = botus;
                bpass = botpass;
                GUIMain.bot = new IRCBot(botuser, bpass);
                GUIMain.rememberBot = rememberBotLogin.isSelected();
            }
        }
        GUIMain.autoLog = autoLoginBox.isSelected();
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        normUser = new JTextField();
        normUsername = new JLabel();
        label1 = new JLabel();
        normPass = new JPasswordField();
        label2 = new JLabel();
        rememberNormLogin = new JCheckBox();
        label3 = new JLabel();
        label4 = new JLabel();
        botUser = new JTextField();
        botPass = new JPasswordField();
        rememberBotLogin = new JCheckBox();
        autoLoginBox = new JCheckBox();
        loginButton = new JButton();

        //======== this ========
        setTitle("Login Settings");
        Container contentPane = getContentPane();

        //---- normUsername ----
        normUsername.setText("Twitch Username:");
        normUsername.setToolTipText("This is for your normal account for chatting in the client.");

        //---- label1 ----
        label1.setText("Login Settings");
        label1.setFont(new Font("Arial", Font.BOLD, 14));

        //---- label2 ----
        label2.setText("Twitch Password:");
        label2.setToolTipText("This is for your normal account for chatting in the client.");

        //---- rememberNormLogin ----
        rememberNormLogin.setText("Remember My Login");
        rememberNormLogin.setFocusable(false);

        //---- label3 ----
        label3.setText("Twitch Bot Username:");

        //---- label4 ----
        label4.setText("Twitch Bot Password:");

        //---- rememberBotLogin ----
        rememberBotLogin.setText("Remember My Login");
        rememberBotLogin.setFocusable(false);

        //---- autoLoginBox ----
        autoLoginBox.setText("Log In Automatically (on Start)");
        autoLoginBox.setFocusable(false);

        //---- loginButton ----
        loginButton.setText("Login");
        loginButton.setFocusable(false);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginButtonActionPerformed();
            }
        });

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(normUsername)
                                                        .addComponent(label2))
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(rememberNormLogin))
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(20, 20, 20)
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(normUser)
                                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                                .addComponent(normPass, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(0, 0, Short.MAX_VALUE))))))
                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(label3)
                                                        .addComponent(label4))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(botPass)
                                                        .addComponent(botUser)))
                                        .addComponent(rememberBotLogin)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(autoLoginBox)
                                                .addGap(22, 22, 22)))
                                .addContainerGap(16, Short.MAX_VALUE))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(98, 98, 98)
                                .addComponent(loginButton)
                                .addGap(0, 94, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(76, Short.MAX_VALUE)
                                .addComponent(label1)
                                .addGap(72, 72, 72))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label1)
                                .addGap(24, 24, 24)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(normUsername)
                                        .addComponent(normUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label2)
                                        .addComponent(normPass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rememberNormLogin)
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label3)
                                        .addComponent(botUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label4)
                                        .addComponent(botPass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rememberBotLogin)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(autoLoginBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(loginButton)
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JTextField normUser;
    public static JLabel normUsername;
    public static JLabel label1;
    public static JPasswordField normPass;
    public static JLabel label2;
    public static JCheckBox rememberNormLogin;
    public static JLabel label3;
    public static JLabel label4;
    public static JTextField botUser;
    public static JPasswordField botPass;
    public static JCheckBox rememberBotLogin;
    public static JCheckBox autoLoginBox;
    public static JButton loginButton;


}
