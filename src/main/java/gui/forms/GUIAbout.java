package gui.forms;

import thread.ThreadEngine;
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
        ThreadEngine.submit(this::getUpdateInfo);
    }

    private void getUpdateInfo() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/Gocnak/Botnak/master/version.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder stanSB = new StringBuilder();
            Utils.parseBufferedReader(reader, stanSB, true);
            versionInformationArea.setText(stanSB.toString());
            versionInformationArea.setCaretPosition(0);
        } catch (Exception e) {
            GUIMain.log("Failed to download version info: ");
            GUIMain.log(e);
            if (versionInformationArea != null)
                versionInformationArea.setText("Failed to download version information!");
        }
    }

    @Override
    public void setVisible(boolean b) {
        setAlwaysOnTop(GUIMain.alwaysOnTop);
        super.setVisible(b);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        JSeparator separator1 = new JSeparator();
        JLabel label1 = new JLabel();
        JScrollPane scrollPane1 = new JScrollPane();
        versionInformationArea = new JTextArea();
        JLabel label4 = new JLabel();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
    }

    private JTextArea versionInformationArea;
}