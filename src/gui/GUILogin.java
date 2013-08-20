package gui;

import irc.IRCBot;
import irc.IRCViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GUILogin extends JFrame {

    public GUILogin() {
        initComponents();
        if (GUIMain.viewer != null) {
            rememberNormLogin.setSelected(GUIMain.rememberNorm);
            normUser.setText(GUIMain.userNorm);
            normPass.setText(GUIMain.userNormPass);
            autoLoginBox.setSelected(GUIMain.autoLog);
            userLogoutButton.setEnabled(true);
        }
        if (GUIMain.bot != null) {
            rememberBotLogin.setSelected(GUIMain.rememberBot);
            botUser.setText(GUIMain.userBot);
            botPass.setText(GUIMain.userBotPass);
            botLogoutButton.setEnabled(true);
        }
    }

    public void userLogoutButtonActionPerformed() {
        if (GUIMain.viewer != null) {
            GUIMain.viewer.close(true);
            normUser.setText("");
            normPass.setText("");
            rememberNormLogin.setSelected(false);
            userLogoutButton.setEnabled(false);
        }
    }

    public void botLogoutButtonActionPerformed() {
        if (GUIMain.bot != null) {
            GUIMain.bot.close(true);
            botUser.setText("");
            botPass.setText("");
            rememberBotLogin.setSelected(false);
            botLogoutButton.setEnabled(false);
        }
    }

    public void cancelButtonActionPerformed() {
        dispose();
    }

    public void loginButtonActionPerformed() {
        try {
            String normus = normUser.getText().toLowerCase();
            String normpass = new String(normPass.getPassword());
            String botus = botUser.getText().toLowerCase();
            String botpass = new String(botPass.getPassword());
            if (normus != null && !normus.equals("") && !normus.contains(" ")) {
                if (!normpass.equals("") && !normpass.contains(" ")) {
                    GUIMain.viewer = new IRCViewer(normus, normpass);
                    GUIMain.rememberNorm = rememberNormLogin.isSelected();
                    GUIMain.addStream.setEnabled(true);
                }
            }
            if (botus != null && !botus.equals("") && !botus.contains(" ")) {
                if (!botpass.equals("") && !botpass.contains(" ")) {
                    GUIMain.bot = new IRCBot(botus, botpass);
                    GUIMain.rememberBot = rememberBotLogin.isSelected();
                }
            }
            GUIMain.autoLog = autoLoginBox.isSelected();
            dispose();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K.
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
        userLogoutButton = new JButton();
        botLogoutButton = new JButton();
        cancelButton = new JButton();
        userLogoutButton.setEnabled(false);
        botLogoutButton.setEnabled(false);

        //======== this ========
        setTitle("Login Settings");
        setResizable(false);
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

        //---- userLogoutButton ----
        userLogoutButton.setText("Logout");
        userLogoutButton.setToolTipText("Logs out of the current Normal account.");
        userLogoutButton.setFocusable(false);
        userLogoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userLogoutButtonActionPerformed();
            }
        });

        //---- botLogoutButton ----
        botLogoutButton.setText("Logout");
        botLogoutButton.setFocusable(false);
        botLogoutButton.setToolTipText("Logs out of the current Bot account.");
        botLogoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botLogoutButtonActionPerformed();
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

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(normUsername)
                                                                        .addComponent(label2)
                                                                        .addComponent(userLogoutButton))
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
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addComponent(botLogoutButton)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(rememberBotLogin)))
                                                .addContainerGap(21, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                                .addGap(0, 71, Short.MAX_VALUE)
                                                .addComponent(label1)
                                                .addGap(72, 72, 72))))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(49, 49, 49)
                                                .addComponent(loginButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cancelButton))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(38, 38, 38)
                                                .addComponent(autoLoginBox)))
                                .addGap(0, 43, Short.MAX_VALUE))
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
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(rememberNormLogin)
                                        .addComponent(userLogoutButton))
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label3)
                                        .addComponent(botUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label4)
                                        .addComponent(botPass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(rememberBotLogin)
                                        .addComponent(botLogoutButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                                .addComponent(autoLoginBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(loginButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K.
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
    public static JButton userLogoutButton;
    public static JButton botLogoutButton;
    public static JButton cancelButton;


}
