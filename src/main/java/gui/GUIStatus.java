package gui;

import irc.account.Oauth;
import util.APIRequests;
import util.Response;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nick K
 */
public class GUIStatus extends JFrame {

    public GUIStatus() {
        initComponents();
    }

    private void playingGameToggleStateChanged() {
        gameText.setEnabled(playingGameToggle.isEnabled());
    }

    private void saveButtonActionPerformed() {
        Oauth key = GUIMain.currentSettings.accountManager.getUserAccount().getKey();
        Response r = APIRequests.Twitch.setStatusOfStream(key.getKey(), "gocnak", gameText.isEnabled() ? gameText.getText() : "", titleText.getText());
    }

    private void closeButtonActionPerformed() {
        dispose();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        titleText = new JTextArea();
        titleText.setLineWrap(true);
        titleText.setWrapStyleWord(true);
        titleText.setFont(new Font("Tahoma", Font.PLAIN, 11));
        gameText = new JTextField();
        label2 = new JLabel();
        playingGameToggle = new JCheckBox();
        saveButton = new JButton();
        closeButton = new JButton();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //======== this ========
        setTitle("Change Stream Status");
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
        saveButton.setText("Save");
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
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    private JLabel label1;
    private JScrollPane scrollPane1;
    private JTextArea titleText;
    private JTextField gameText;
    private JLabel label2;
    private JCheckBox playingGameToggle;
    private JButton saveButton;
    private JButton closeButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
