/*
 * Created by JFormDesigner on Thu Apr 27 17:58:12 EDT 2017
 */

package gui.forms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * @author Nick Kerns
 */
public class GUIVote extends JFrame
{
    public GUIVote()
    {
        initComponents();
    }

    private void newVoteButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void closeButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void cancelVoteButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void addOptionButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void removeOptionButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void startVoteButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void cancelButtonActionPerformed()
    {
        // TODO add your code here
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick Kerns
        option1Label = new JLabel();
        pieChartPanel = new JPanel();
        option2Label = new JLabel();
        option3Label = new JLabel();
        option4Label = new JLabel();
        option5Label = new JLabel();
        option6Label = new JLabel();
        option7Label = new JLabel();
        option8Label = new JLabel();
        option9Label = new JLabel();
        option10Label = new JLabel();
        separator2 = new JSeparator();
        newVoteButton = new JButton();
        closeButton = new JButton();
        cancelVoteButton = new JButton();
        GUICreateVote = new JFrame();
        scrollPane1 = new JScrollPane();
        optionTable = new JTable();
        optionField = new JTextField();
        addOptionButton = new JButton();
        removeOptionButton = new JButton();
        separator1 = new JSeparator();
        startVoteButton = new JButton();
        cancelButton = new JButton();
        label1 = new JLabel();
        durationComboBox = new JComboBox<>();

        //======== this ========
        setTitle("User Votes");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        Container contentPane = getContentPane();

        //---- option1Label ----
        option1Label.setText("text");
        option1Label.setForeground(Color.red);

        //======== pieChartPanel ========
        {

            // JFormDesigner evaluation mark
            pieChartPanel.setBorder(new javax.swing.border.CompoundBorder(
                    new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                            "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                            javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            java.awt.Color.red), pieChartPanel.getBorder()));
            pieChartPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener()
            {
                public void propertyChange(java.beans.PropertyChangeEvent e)
                {
                    if ("border".equals(e.getPropertyName())) throw new RuntimeException();
                }
            });


            GroupLayout pieChartPanelLayout = new GroupLayout(pieChartPanel);
            pieChartPanel.setLayout(pieChartPanelLayout);
            pieChartPanelLayout.setHorizontalGroup(
                    pieChartPanelLayout.createParallelGroup()
                            .addGap(0, 230, Short.MAX_VALUE)
            );
            pieChartPanelLayout.setVerticalGroup(
                    pieChartPanelLayout.createParallelGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
            );
        }

        //---- option2Label ----
        option2Label.setText("text");
        option2Label.setForeground(new Color(153, 102, 0));

        //---- option3Label ----
        option3Label.setText("text");
        option3Label.setForeground(Color.blue);

        //---- option4Label ----
        option4Label.setText("text");
        option4Label.setForeground(Color.lightGray);
        option4Label.setFont(option4Label.getFont().deriveFont(option4Label.getFont().getStyle() & ~Font.BOLD));

        //---- option5Label ----
        option5Label.setText("text");
        option5Label.setForeground(Color.magenta);

        //---- option6Label ----
        option6Label.setText("text");
        option6Label.setForeground(Color.pink);

        //---- option7Label ----
        option7Label.setText("text");
        option7Label.setForeground(Color.orange);

        //---- option8Label ----
        option8Label.setText("text");
        option8Label.setForeground(Color.yellow);

        //---- option9Label ----
        option9Label.setText("text");
        option9Label.setForeground(Color.cyan);

        //---- option10Label ----
        option10Label.setText("text");
        option10Label.setForeground(Color.green);

        //---- newVoteButton ----
        newVoteButton.setText("Create New Vote...");
        newVoteButton.setFocusable(false);
        newVoteButton.addActionListener(e -> newVoteButtonActionPerformed());

        //---- closeButton ----
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> closeButtonActionPerformed());

        //---- cancelVoteButton ----
        cancelVoteButton.setText("Cancel Current Vote");
        cancelVoteButton.setFocusable(false);
        cancelVoteButton.addActionListener(e -> cancelVoteButtonActionPerformed());

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(separator2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(option10Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option9Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option7Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option8Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option6Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option3Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option4Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option5Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option2Label, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                                        .addComponent(option1Label, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(pieChartPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(newVoteButton)
                                                .addGap(64, 64, 64)
                                                .addComponent(cancelVoteButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                                                .addComponent(closeButton)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(pieChartPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(option1Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option2Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option3Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option4Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option5Label, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option6Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option7Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option8Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option9Label)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(option10Label)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(closeButton)
                                        .addComponent(newVoteButton)
                                        .addComponent(cancelVoteButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //======== GUICreateVote ========
        {
            GUICreateVote.setTitle("Create New Vote");
            GUICreateVote.setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
            GUICreateVote.setResizable(false);
            Container GUICreateVoteContentPane = GUICreateVote.getContentPane();

            //======== scrollPane1 ========
            {

                //---- optionTable ----
                optionTable.setModel(new DefaultTableModel(
                        new Object[][]{
                        },
                        new String[]{
                                "Option", "Value"
                        }
                )
                {
                    Class<?>[] columnTypes = new Class<?>[]{
                            Integer.class, String.class
                    };
                    boolean[] columnEditable = new boolean[]{
                            false, true
                    };

                    @Override
                    public Class<?> getColumnClass(int columnIndex)
                    {
                        return columnTypes[columnIndex];
                    }

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex)
                    {
                        return columnEditable[columnIndex];
                    }
                });
                {
                    TableColumnModel cm = optionTable.getColumnModel();
                    cm.getColumn(0).setResizable(false);
                    cm.getColumn(0).setPreferredWidth(50);
                    cm.getColumn(1).setPreferredWidth(165);
                }
                scrollPane1.setViewportView(optionTable);
            }

            //---- addOptionButton ----
            addOptionButton.setText("Add Option to List");
            addOptionButton.setFocusable(false);
            addOptionButton.addActionListener(e -> addOptionButtonActionPerformed());

            //---- removeOptionButton ----
            removeOptionButton.setText("Remove");
            removeOptionButton.setFocusable(false);
            removeOptionButton.addActionListener(e -> removeOptionButtonActionPerformed());

            //---- startVoteButton ----
            startVoteButton.setText("Start Vote");
            startVoteButton.setFocusable(false);
            startVoteButton.addActionListener(e -> startVoteButtonActionPerformed());

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.addActionListener(e -> cancelButtonActionPerformed());

            //---- label1 ----
            label1.setText("Duration of Vote:");

            //---- durationComboBox ----
            durationComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                    "30 seconds",
                    "1 minute",
                    "2 minutes",
                    "5 minutes"
            }));
            durationComboBox.setFocusable(false);

            GroupLayout GUICreateVoteContentPaneLayout = new GroupLayout(GUICreateVoteContentPane);
            GUICreateVoteContentPane.setLayout(GUICreateVoteContentPaneLayout);
            GUICreateVoteContentPaneLayout.setHorizontalGroup(
                    GUICreateVoteContentPaneLayout.createParallelGroup()
                            .addComponent(separator1, GroupLayout.Alignment.TRAILING)
                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup()
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addComponent(startVoteButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(cancelButton))
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addComponent(addOptionButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(removeOptionButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                                                    .addComponent(optionField))
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addGap(10, 10, 10)
                                                    .addComponent(label1)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(durationComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            GUICreateVoteContentPaneLayout.setVerticalGroup(
                    GUICreateVoteContentPaneLayout.createParallelGroup()
                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(optionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(addOptionButton)
                                            .addComponent(removeOptionButton))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label1)
                                            .addComponent(durationComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(startVoteButton)
                                            .addComponent(cancelButton))
                                    .addContainerGap())
            );
            GUICreateVote.pack();
            GUICreateVote.setLocationRelativeTo(GUICreateVote.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick Kerns
    private JLabel option1Label;
    private JPanel pieChartPanel;
    private JLabel option2Label;
    private JLabel option3Label;
    private JLabel option4Label;
    private JLabel option5Label;
    private JLabel option6Label;
    private JLabel option7Label;
    private JLabel option8Label;
    private JLabel option9Label;
    private JLabel option10Label;
    private JSeparator separator2;
    private JButton newVoteButton;
    private JButton closeButton;
    private JButton cancelVoteButton;
    private JFrame GUICreateVote;
    private JScrollPane scrollPane1;
    private JTable optionTable;
    private JTextField optionField;
    private JButton addOptionButton;
    private JButton removeOptionButton;
    private JSeparator separator1;
    private JButton startVoteButton;
    private JButton cancelButton;
    private JLabel label1;
    private JComboBox<String> durationComboBox;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
