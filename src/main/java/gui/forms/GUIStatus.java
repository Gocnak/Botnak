package gui.forms;

import irc.account.Oauth;
import thread.ThreadEngine;
import util.APIRequests;
import util.Response;
import util.settings.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nick K
 */
public class GUIStatus extends JFrame {

    public String getChannel() {
        return Settings.accountManager.getUserAccount() == null ? null :
                Settings.accountManager.getUserAccount().getName();
    }

    public Oauth getKey() {
        return Settings.accountManager.getUserAccount() == null ? null :
                Settings.accountManager.getUserAccount().getKey();
    }

    public GUIStatus() {
        initComponents();
    }

    private void playingGameToggleStateChanged() {
        gameText.setEnabled(!playingGameToggle.isSelected());
    }

    private void saveButtonActionPerformed() {
        if (getChannel() == null || getKey() == null) {
            setTitle("Please Login First!");
            return;
        }
        playingGameToggle.setSelected("".equals(gameText.getText()));
        Response r = APIRequests.Twitch.setStatusOfStream(getKey().getKey(), getChannel(), titleText.getText(),
                gameText.isEnabled() ? gameText.getText() : "");
        if (r.isSuccessful()) {
            setTitle("Status successfully updated!");
            if (!gameText.isEnabled()) gameText.setText("");
        } else {
            setTitle("Status failed to update!");
        }
        ThreadEngine.submit(() -> {
            try {
                Thread.sleep(5000);
                setTitle("Change Stream Status");
            } catch (InterruptedException ignored) {
            }
        });
    }

    public void updateStatusComponents() {
        if (getChannel() == null) {
            setTitle("Please Login First!");
            return;
        }
        String[] status = APIRequests.Twitch.getStatusOfStream(getChannel());
        titleText.setText("".equals(status[0]) ? "(Untitled Broadcast)" : status[0]);
        gameText.setEnabled(!"".equals(status[1]));
        playingGameToggle.setSelected(!gameText.isEnabled());
        gameText.setText(status[1]);
    }

    private void closeButtonActionPerformed() {
        dispose();
    }

    @Override
    public void setVisible(boolean b) {
        setAlwaysOnTop(Settings.alwaysOnTop.getValue());
        super.setVisible(b);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        JLabel label1 = new JLabel();
        JScrollPane scrollPane1 = new JScrollPane();
        titleText = new JTextArea();
        titleText.setLineWrap(true);
        titleText.setWrapStyleWord(true);
        Font currentFont = Settings.font.getValue();
        titleText.setFont(new Font(currentFont.getName(), currentFont.getStyle(), 11));
        gameText = new JTextField();
        JLabel label2 = new JLabel();
        playingGameToggle = new JCheckBox();
        playingGameToggle.setFocusable(false);
        saveButton = new JButton();
        saveButton.setFocusable(false);
        closeButton = new JButton();
        closeButton.setFocusable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //======== this ========
        setTitle("Change Stream Status");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //---- label1 ----
        label1.setText("Title");

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(titleText);
        }

        //---- label2 ----
        label2.setText("Game");

        //---- playingGameToggle ----
        playingGameToggle.setText("Not Playing a Game");
        playingGameToggle.addChangeListener(e -> playingGameToggleStateChanged());

        //---- saveButton ----
        saveButton.setText("Update");
        saveButton.addActionListener(e -> saveButtonActionPerformed());

        //---- closeButton ----
        closeButton.setText("Close");
        closeButton.addActionListener(e -> closeButtonActionPerformed());

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(playingGameToggle)
                                        .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(label1)
                                                .addComponent(label2)
                                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                                                .addComponent(gameText, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)))
                                .addContainerGap(25, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(140, Short.MAX_VALUE)
                                .addComponent(saveButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(closeButton)
                                .addGap(18, 18, 18))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(label2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gameText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playingGameToggle)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton)
                                        .addComponent(closeButton))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        updateStatusComponents();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private JTextArea titleText;
    private JTextField gameText;
    private JCheckBox playingGameToggle;
    private JButton saveButton;
    private JButton closeButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}