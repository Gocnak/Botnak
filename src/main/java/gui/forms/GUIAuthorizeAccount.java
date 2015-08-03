package gui.forms;

import gui.TokenListener;
import irc.account.Account;
import irc.account.Oauth;
import irc.account.Task;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Nick K
 */
public class GUIAuthorizeAccount extends JFrame {

    private TokenListener listener;

    public GUIAuthorizeAccount() {
        listener = null;
        initComponents();
        if (GUIMain.currentSettings.accountManager.getUserAccount() != null) {
            Oauth key = GUIMain.currentSettings.accountManager.getUserAccount().getKey();
            statusPane.setText("Here is the current OAuth key's permission. Logout of the account if you wish to change it!");
            accountNameField.setText(GUIMain.currentSettings.accountManager.getUserAccount().getName());
            oauthField.setText(GUIMain.currentSettings.accountManager.getUserAccount().getKey().getKey());
            boxEditStatus.setSelected(key.canSetTitle());
            boxEditStatus.setEnabled(false);
            boxCommercial.setSelected(key.canPlayAd());
            boxCommercial.setEnabled(false);
            boxFollowed.setSelected(key.canReadFollowed());
            boxFollowed.setEnabled(false);
            boxReadSubs.setSelected(key.canReadSubscribers());
            boxReadSubs.setEnabled(false);
            authorizeButton.setEnabled(false);
        }
    }

    private void authorizeButtonActionPerformed(ActionEvent e) {
        String accountName = this.accountNameField.getText().trim();
        if (accountName.length() > 0 && !accountName.contains(" ")) {
            String URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                    "?response_type=token" +
                    "&client_id=qw8d3ve921t0n6e3if07l664f1jn1y7" +
                    "&redirect_uri=http://gocnak.github.io/Botnak/token_redirect.html" +
                    "&scope=chat_login";
            if (boxReadSubs.isSelected()) URL += "+channel_subscriptions";
            if (boxCommercial.isSelected()) URL += "+channel_commercial";
            if (boxEditStatus.isSelected()) URL += "+channel_editor";
            if (boxFollowed.isSelected()) URL += "+user_read";
            Utils.openWebPage(URL);
            if (listener == null || !listener.isAlive()) {
                listener = new TokenListener(this);
                listener.start();
            }
        } else {
            statusPane.setText("Failed to authorize account, invalid account name!");
        }
    }

    private void doneButtonActionPerformed(ActionEvent e) {
        if (GUIMain.currentSettings.accountManager.getUserAccount() == null) {
            if (accountNameField.getText().length() > 0 && oauthField.getPassword().length > 5) {
                GUIMain.currentSettings.accountManager.setUserAccount(
                        new Account(accountNameField.getText().toLowerCase(),
                                new Oauth("oauth:" + new String(oauthField.getPassword()), boxEditStatus.isSelected(),
                                        boxCommercial.isSelected(), boxReadSubs.isSelected(), boxFollowed.isSelected())));
                GUIMain.currentSettings.accountManager.addTask(new Task(null, Task.Type.CREATE_VIEWER_ACCOUNT, null));
            }
        }
        dispose();
    }

    private void initComponents() {
        scrollPane1 = new JScrollPane();
        statusPane = new JTextPane();
        boxCommercial = new JCheckBox();
        boxReadSubs = new JCheckBox();
        accountNameField = new JTextField();
        label1 = new JLabel();
        authorizeButton = new JButton();
        separator1 = new JSeparator();
        boxEditStatus = new JCheckBox();
        boxFollowed = new JCheckBox();
        label2 = new JLabel();
        oauthField = new JPasswordField();
        closeButton = new JButton();
        setResizable(false);
        //======== this ========
        setTitle("Authorize Botnak");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- statusPane ----
            statusPane.setEditable(false);
            statusPane.setText("Enter your username, tick the boxes you want Botnak to be able to do, and click Authorize.");
            scrollPane1.setViewportView(statusPane);
        }

        //---- boxCommercial ----
        boxCommercial.setText("Play Commercials (Partnered Channels Only)");
        boxCommercial.setFocusable(false);

        //---- boxReadSubs ----
        boxReadSubs.setText("Read Subscribers (Partnered Channels Only)");
        boxReadSubs.setFocusable(false);

        //---- label1 ----
        label1.setText("Username:");

        //---- authorizeButton ----
        authorizeButton.setText("Authorize");
        authorizeButton.addActionListener(this::authorizeButtonActionPerformed);
        authorizeButton.setFocusable(false);

        //---- boxEditStatus ----
        boxEditStatus.setText("Edit Title and Game");
        boxEditStatus.setFocusable(false);

        //---- boxFollowed ----
        boxFollowed.setText("Read Followed Streams");
        boxFollowed.setFocusable(false);

        //---- label2 ----
        label2.setText("OAuth key:");

        //---- closeButton ----
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(this::doneButtonActionPerformed);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                        .addComponent(separator1, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(boxCommercial)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(accountNameField, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(authorizeButton)
                                                .addGap(35, 35, 35))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addComponent(boxEditStatus)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(boxFollowed, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(boxReadSubs)
                                                        .addComponent(label1))
                                                .addGap(0, 64, Short.MAX_VALUE))))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(label2)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(oauthField, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                                .addComponent(closeButton)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(label1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(authorizeButton)
                                        .addComponent(accountNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(11, 11, 11)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(boxEditStatus)
                                        .addComponent(boxFollowed))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boxCommercial)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boxReadSubs)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(label2)
                                .addGap(5, 5, 5)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(oauthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(closeButton))
                                .addGap(0, 15, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JScrollPane scrollPane1;
    public JTextPane statusPane;
    private JCheckBox boxCommercial;
    private JCheckBox boxReadSubs;
    private JTextField accountNameField;
    private JLabel label1;
    private JButton authorizeButton;
    private JSeparator separator1;
    private JCheckBox boxEditStatus;
    private JCheckBox boxFollowed;
    private JLabel label2;
    public JPasswordField oauthField;
    private JButton closeButton;
}