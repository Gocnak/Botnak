package gui;

import irc.account.Account;
import irc.account.Oauth;
import irc.account.Task;
import util.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * This GUI handles authorizing Botnak to handle your main account.
 * <p>
 * Botnak does not generate an Oauth for your bot account.
 *
 * @author Nick K
 */
public class AuthorizeAccountGUI extends JFrame {
    private TokenListener listener;

    public AuthorizeAccountGUI() {
        listener = null;
        initComponents();
        if (GUIMain.currentSettings.accountManager.getUserAccount() != null) {
            accountNameField.setText(GUIMain.currentSettings.accountManager.getUserAccount().getName());
            oAuthField.setText(GUIMain.currentSettings.accountManager.getUserAccount().getKey().getKey());
        }
    }

    private void authorizeButtonActionPerformed() {
        if (accountNameField.getText().length() > 0) {
            String URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                    "?response_type=token" +
                    "&client_id=qw8d3ve921t0n6e3if07l664f1jn1y7" +
                    "&redirect_uri=http://gocnak.github.io/Botnak/token_redirect.html" +
                    "&scope=chat_login";
            if (/*TODO implement "Read Subs" box*/true) URL += "+channel_subscriptions";
            if (boxCommercial.isSelected()) URL += "+channel_commercial";
            if (boxEditStream.isSelected()) URL += "+channel_editor";
            if (boxFollowed.isSelected()) URL += "+user_follows_edit";
            Utils.openWebPage(URL);
            if (listener == null || !listener.isAlive()) {
                listener = new TokenListener(this);
                listener.start();
            }
        }
    }

    private void closeButtonActionPerformed() {
        if (GUIMain.currentSettings.accountManager.getUserAccount() == null) {
            if (accountNameField.getText().length() > 0 && oAuthField.getPassword().length > 5) {
                GUIMain.currentSettings.accountManager.setUserAccount(
                        new Account(accountNameField.getText().toLowerCase(),
                                new Oauth("oauth:" + new String(oAuthField.getPassword()), boxEditStream.isSelected(), boxCommercial.isSelected())));
                GUIMain.currentSettings.accountManager.addTask(new Task(null, Task.Type.CREATE_VIEWER_ACCOUNT, null));
            }
        }
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        JLabel label1 = new JLabel();
        accountNameField = new JTextField();
        JLabel label2 = new JLabel();
        boxEditStream = new JCheckBox();
        boxCommercial = new JCheckBox();
        boxFollowed = new JCheckBox();
        JButton authorizeButton = new JButton();
        JLabel label3 = new JLabel();
        oAuthField = new JPasswordField();
        JLabel label4 = new JLabel();
        JButton closeButton = new JButton();

        //======== this ========
        setTitle("Authorize an Account");
        setResizable(false);
        Container contentPane = getContentPane();

        //---- label1 ----
        label1.setText("Account Name:");

        //---- label2 ----
        label2.setText("Allow Botnak to also:");

        //---- boxEditStream ----
        boxEditStream.setText("Edit Stream Title/Game");
        boxEditStream.setFocusable(false);

        //---- boxCommercial ----
        boxCommercial.setText("Play a Commercial");
        boxCommercial.setFocusable(false);

        //---- boxFollowed ----
        boxFollowed.setText("View my Followed Channels");
        boxFollowed.setFocusable(false);

        //---- authorizeButton ----
        authorizeButton.setText("Authorize");
        authorizeButton.setFocusable(false);
        authorizeButton.addActionListener(e -> authorizeButtonActionPerformed());

        //---- label3 ----
        label3.setText("OAuth Key:");

        //---- label4 ----
        label4.setText("(alternatively, you may just paste an OAuth key in the field, had you generated one yourself)");
        label4.setFont(new Font("Tahoma", Font.PLAIN, 9));

        //---- closeButton ----
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> closeButtonActionPerformed());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(label1)
                                                        .addComponent(label3))
                                                .addGap(18, 18, 18)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(accountNameField)
                                                        .addComponent(oAuthField)))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(boxFollowed)
                                                        .addComponent(boxCommercial)
                                                        .addComponent(boxEditStream)))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(authorizeButton, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)
                                                .addGap(96, 96, 96)
                                                .addComponent(closeButton))
                                        .addComponent(label2)
                                        .addComponent(label4))
                                .addContainerGap(12, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label1)
                                        .addComponent(accountNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(oAuthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label3))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(label2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(boxEditStream)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boxCommercial)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boxFollowed)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(authorizeButton)
                                        .addComponent(closeButton))
                                .addGap(4, 4, 4)
                                .addComponent(label4)
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private JTextField accountNameField;
    private JCheckBox boxEditStream;
    private JCheckBox boxCommercial;
    private JCheckBox boxFollowed;
    public JPasswordField oAuthField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}