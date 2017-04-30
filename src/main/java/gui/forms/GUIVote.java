/*
 * Created by JFormDesigner on Thu Apr 27 17:58:12 EDT 2017
 */

package gui.forms;

import gui.PieChart;
import gui.VoteCellRenderer;
import util.Utils;
import util.misc.Vote;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Nick Kerns
 */
public class GUIVote extends JFrame
{

    private DefaultListModel<Vote.Option> voteListModel;

    public GUIVote()
    {
        voteListModel = new DefaultListModel<>();
        initComponents();

        // Check for past/current poll
        if (GUIMain.bot != null && GUIMain.bot.pollExists())
            addPoll(GUIMain.bot.getPoll());
    }

    private void newVoteButtonActionPerformed()
    {
        if (!GUICreateVote.isVisible())
            GUICreateVote.setVisible(true);
        else
            GUICreateVote.toFront();

        Utils.populateComboBox(channelsBox);
    }

    private void closeButtonActionPerformed()
    {
        dispose();
    }

    private void cancelVoteButtonActionPerformed()
    {
        GUIMain.bot.stopPoll();

        stopVoteButton.setEnabled(false);
    }

    private void addOptionButtonActionPerformed()
    {
        DefaultTableModel dtm = (DefaultTableModel) optionTable.getModel();
        String text = Utils.checkText(optionField.getText());
        if (!text.isEmpty())
        {
            dtm.addRow(new Object[]{dtm.getRowCount() + 1, text});
            optionField.setText("");
        }
    }

    private void removeOptionButtonActionPerformed()
    {
        // Remove the selected row
        int selRow = optionTable.getSelectedRow();
        DefaultTableModel dtm = (DefaultTableModel) optionTable.getModel();
        dtm.removeRow(selRow);

        // Update the list
        correctTable();

        // Set this button back to disabled
        removeOptionButton.setEnabled(false);
    }

    private void correctTable()
    {
        int optionNumCol = optionTable.getColumn("Option").getModelIndex();
        for (int i = 0; i < optionTable.getRowCount(); i++)
        {
            int next = i + 1;
            final int row = i; // Needed because of below for some reason
            EventQueue.invokeLater(() -> optionTable.setValueAt(next, row, optionNumCol));
        }
    }


    private void clearForm()
    {
        DefaultTableModel dtm = (DefaultTableModel) optionTable.getModel();
        while (dtm.getRowCount() > 0)
            dtm.removeRow(0);

        optionField.setText("");
        timeSpinner.setValue(((SpinnerNumberModel) timeSpinner.getModel()).getMinimum());
    }

    private void clearMainForm()
    {
        voteListModel.removeAllElements();
        pieChart.clear();
        stopVoteButton.setEnabled(false);
        newVoteButton.setEnabled(true);
    }

    // Adds a poll to the main form
    public void addPoll(Vote v)
    {
        // Reset our data
        clearMainForm();

        // Initialize the PieChart
        boolean isOngoing = !v.isDone() || v.isAlive();
        if (isOngoing)
            pieChart.initialize(v.options);
        else
            pieChart.update(v);

        // Populate the options list
        v.options.forEach(voteListModel::addElement);

        stopVoteButton.setEnabled(isOngoing);
        newVoteButton.setEnabled(!isOngoing);
    }

    public void updatePoll(Vote v)
    {
        if (voteListModel.isEmpty())
            addPoll(v);

        votesList.repaint();

        // Update the pie chart
        pieChart.update(v);
    }

    public void pollEnded(Vote v)
    {
        if (voteListModel.isEmpty())
            addPoll(v);

        stopVoteButton.setEnabled(false);
        newVoteButton.setEnabled(true);
    }

    private void startVoteButtonActionPerformed()
    {
        // Create the vote
        int valueCol = optionTable.getColumn("Value").getModelIndex();
        java.util.List<String> collect = new ArrayList<>();
        for (int i = 0; i < optionTable.getRowCount(); i++)
        {
            collect.add((String) optionTable.getValueAt(i, valueCol));
        }

        // Start the vote we made, if we did
        if (collect.size() > 1) // Because what's the point of a vote with only one option
        {
            Vote v = new Vote("#" + channelsBox.getSelectedItem(), (int) timeSpinner.getValue(), collect.toArray(new String[collect.size()]));
            GUIMain.bot.startPoll(v);
        }

        // Clear the form
        clearForm();
        GUICreateVote.setVisible(false);
    }

    private void cancelButtonActionPerformed()
    {
        clearForm();
        GUICreateVote.setVisible(false);
    }

    private void optionTableMouseReleased()
    {
        removeOptionButton.setEnabled(optionTable.getSelectedRow() > -1);
    }

    private void optionFieldKeyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            addOptionButtonActionPerformed();
        }
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick Kerns
        newVoteButton = new JButton();
        closeButton = new JButton();
        stopVoteButton = new JButton();
        scrollPane2 = new JScrollPane();
        votesList = new JList<>();
        pieChart = new PieChart();
        separator2 = new JSeparator();
        GUICreateVote = new JFrame();
        scrollPane1 = new JScrollPane();
        optionTable = new JTable();
        optionField = new JTextField();
        removeOptionButton = new JButton();
        separator1 = new JSeparator();
        startVoteButton = new JButton();
        cancelButton = new JButton();
        label1 = new JLabel();
        addOptionButton = new JButton();
        label2 = new JLabel();
        channelsBox = new JComboBox<>();
        timeSpinner = new JSpinner();

        //======== this ========
        setTitle("User Vote");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        //---- newVoteButton ----
        newVoteButton.setText("Create New Vote...");
        newVoteButton.setFocusable(false);
        newVoteButton.addActionListener(e -> newVoteButtonActionPerformed());

        //---- closeButton ----
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> closeButtonActionPerformed());

        //---- stopVoteButton ----
        stopVoteButton.setText("Stop Current Vote");
        stopVoteButton.setFocusable(false);
        stopVoteButton.setEnabled(false);
        stopVoteButton.addActionListener(e -> cancelVoteButtonActionPerformed());

        //======== scrollPane2 ========
        {

            //---- votesList ----
            votesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            votesList.setCellRenderer(new VoteCellRenderer());
            votesList.setModel(voteListModel);
            scrollPane2.setViewportView(votesList);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(pieChart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(newVoteButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(stopVoteButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                                                .addComponent(closeButton)))
                                .addContainerGap())
                        .addComponent(separator2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(pieChart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(separator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(newVoteButton)
                                        .addComponent(stopVoteButton)
                                        .addComponent(closeButton))
                                .addContainerGap(11, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //======== GUICreateVote ========
        {
            GUICreateVote.setTitle("Create New Vote");
            GUICreateVote.setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
            GUICreateVote.setResizable(false);
            GUICreateVote.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
                optionTable.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        optionTableMouseReleased();
                    }
                });
                optionTable.getTableHeader().setReorderingAllowed(false);
                scrollPane1.setViewportView(optionTable);
            }

            //---- optionField ----
            optionField.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyReleased(KeyEvent e)
                {
                    optionFieldKeyReleased(e);
                }
            });

            //---- removeOptionButton ----
            removeOptionButton.setText("Remove");
            removeOptionButton.setFocusable(false);
            removeOptionButton.setEnabled(false);
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
            label1.setText("Duration of Vote (in seconds):");

            //---- addOptionButton ----
            addOptionButton.setText("Add Option to List");
            addOptionButton.setFocusable(false);
            addOptionButton.addActionListener(e -> addOptionButtonActionPerformed());

            //---- label2 ----
            label2.setText("Channel:");

            //---- timeSpinner ----
            timeSpinner.setModel(new SpinnerNumberModel(20, 20, null, 1));

            GroupLayout GUICreateVoteContentPaneLayout = new GroupLayout(GUICreateVoteContentPane);
            GUICreateVoteContentPane.setLayout(GUICreateVoteContentPaneLayout);
            GUICreateVoteContentPaneLayout.setHorizontalGroup(
                    GUICreateVoteContentPaneLayout.createParallelGroup()
                            .addComponent(separator1, GroupLayout.Alignment.TRAILING)
                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup()
                                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addComponent(startVoteButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                                                    .addComponent(cancelButton))
                                            .addComponent(optionField, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addComponent(label2)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(channelsBox, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(0, 70, Short.MAX_VALUE))
                                            .addGroup(GroupLayout.Alignment.TRAILING, GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup()
                                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                                    .addComponent(addOptionButton)
                                                                    .addGap(0, 33, Short.MAX_VALUE))
                                                            .addComponent(label1, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup()
                                                            .addComponent(removeOptionButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(timeSpinner, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))))
                                    .addContainerGap())
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
                                    .addGap(18, 18, 18)
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label1, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(timeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGroup(GUICreateVoteContentPaneLayout.createParallelGroup()
                                            .addGroup(GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addGap(18, 18, 18)
                                                    .addComponent(label2)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE))
                                            .addGroup(GroupLayout.Alignment.TRAILING, GUICreateVoteContentPaneLayout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                                                    .addComponent(channelsBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)))
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
    private JButton newVoteButton;
    private JButton closeButton;
    private JButton stopVoteButton;
    private JScrollPane scrollPane2;
    private JList<Vote.Option> votesList;
    private PieChart pieChart;
    private JSeparator separator2;
    private JFrame GUICreateVote;
    private JScrollPane scrollPane1;
    private JTable optionTable;
    private JTextField optionField;
    private JButton removeOptionButton;
    private JSeparator separator1;
    private JButton startVoteButton;
    private JButton cancelButton;
    private JLabel label1;
    private JButton addOptionButton;
    private JLabel label2;
    private JComboBox<String> channelsBox;
    private JSpinner timeSpinner;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
