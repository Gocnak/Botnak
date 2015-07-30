package gui;

import util.Constants;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Nick on 12/22/13.
 */
public class GUIUpdate extends JFrame {

    public GUIUpdate() {
        initComponents();
    }

    public static String text = "";

    public static boolean checkForUpdate() {
        try {
            final URL url = new URL("https://raw.githubusercontent.com/Gocnak/Botnak/master/version.txt");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuilder stanSB = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                double version;
                try {
                    version = Double.parseDouble(line);
                    if (Constants.VERSION >= version) {
                        reader.close();
                        return false;
                    }
                } catch (Exception ignored) {
                }
                stanSB.append(line);
                stanSB.append("\n");
            }
            reader.close();
            text = stanSB.toString();
            return true;
        } catch (Exception e) {
            GUIMain.log(e);
        }
        return false;
    }

    public void downloadButtonActionPerformed() {
        Utils.openWebPage("https://github.com/Gocnak/Botnak/releases");
        dispose();
    }

    public void skipButtonActionPerformed() {
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        scrollPane1 = new JScrollPane();
        updateText = new JTextPane();
        updateText.setFocusable(false);
        updateText.setEditorKit(new WrapEditorKit());
        updateText.setText(text);
        updateText.setCaretPosition(0);
        downloadButton = new JButton();
        skipButton = new JButton();

        //======== this ========
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setTitle("A new version of Botnak is available!");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- updateText ----
            updateText.setFont(new Font("Arial", Font.PLAIN, 12));
            scrollPane1.setViewportView(updateText);
        }

        //---- downloadButton ----
        downloadButton.setText("Download");
        downloadButton.setFocusable(false);
        downloadButton.addActionListener(e -> downloadButtonActionPerformed());

        //---- skipButton ----
        skipButton.setText("Skip");
        skipButton.setFocusable(false);
        skipButton.addActionListener(e -> skipButtonActionPerformed());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(downloadButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
                                .addComponent(skipButton)
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 213, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(downloadButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(skipButton, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JScrollPane scrollPane1;
    public static JTextPane updateText;
    public static JButton downloadButton;
    public static JButton skipButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}