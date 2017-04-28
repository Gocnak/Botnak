/*
 * Created by JFormDesigner on Fri Apr 28 01:55:26 EDT 2017
 */

package gui.forms;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * @author Nick Kerns
 */
public class GUILogViewer extends JFrame
{
    public GUILogViewer()
    {
        initComponents();
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick Kerns
        scrollPane1 = new JScrollPane();
        tree1 = new JTree();
        textField1 = new JTextField();
        scrollPane2 = new JScrollPane();
        textPane1 = new JTextPane();

        //======== this ========
        setTitle("Log Viewer");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- tree1 ----
            tree1.setModel(new DefaultTreeModel(
                    new DefaultMutableTreeNode("All Chats")
                    {
                        {
                            add(new DefaultMutableTreeNode("Channel"));
                        }
                    }));
            scrollPane1.setViewportView(tree1);
        }

        //---- textField1 ----
        textField1.setText("user:gocnak");

        //======== scrollPane2 ========
        {
            scrollPane2.setViewportView(textPane1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                                        .addComponent(textField1, GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick Kerns
    private JScrollPane scrollPane1;
    private JTree tree1;
    private JTextField textField1;
    private JScrollPane scrollPane2;
    private JTextPane textPane1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
