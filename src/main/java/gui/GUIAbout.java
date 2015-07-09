package gui;

import util.Constants;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Nick K
 */
public class GUIAbout extends JFrame {

    public GUIAbout() {
        initComponents();
        getUpdateInfo();
    }

    private void getUpdateInfo() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/Gocnak/Botnak/master/version.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder stanSB = new StringBuilder();
            Utils.parseBufferedReader(reader, stanSB, true);
            versionInformationArea.setText(stanSB.toString());
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
            versionInformationArea.setText("Failed to download version information!");
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        label2 = new JLabel();
        label3 = new JLabel();
        separator1 = new JSeparator();
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        versionInformationArea = new JTextArea();
        label4 = new JLabel();

        //======== this ========
        setTitle("About Botnak v" + Constants.VERSION);
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //---- label2 ----
        label2.setText("Made with love by Gocnak");
        label2.setFont(new Font("Calibri", Font.BOLD, 19));

        //---- label3 ----
        label3.setText("With help from: Dr Kegel, YaLTeR, Jbzdarkid, Chrisazy");
        label3.setFont(new Font("Calibri", Font.BOLD, 12));

        //---- label1 ----
        label1.setText("Version Information");
        label1.setLabelFor(versionInformationArea);

        //======== scrollPane1 ========
        {

            //---- versionInformationArea ----
            versionInformationArea.setEditable(false);
            scrollPane1.setViewportView(versionInformationArea);
        }

        //---- label4 ----
        label4.setIcon(new ImageIcon(getClass().getResource("/image/icon70.png")));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(scrollPane1)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(label1)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(label2)
                                                        .addComponent(label3))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                                                .addComponent(label4)))
                                .addContainerGap())
                        .addComponent(separator1)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(23, 23, 23)
                                                .addComponent(label2)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(label3))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(label4)))
                                .addGap(16, 16, 16)
                                .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(label1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    private JLabel label2;
    private JLabel label3;
    private JSeparator separator1;
    private JLabel label1;
    private JScrollPane scrollPane1;
    private JTextArea versionInformationArea;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}