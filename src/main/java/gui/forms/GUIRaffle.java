/*
 * Created by JFormDesigner on Thu Apr 27 17:56:21 EDT 2017
 */

package gui.forms;

import util.Permissions;
import util.Utils;
import util.misc.Raffle;
import util.settings.Settings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Nick Kerns
 */
public class GUIRaffle extends JFrame
{

    public GUIRaffle()
    {
        initComponents();
        Utils.populateComboBox(channelsComboBox);
    }

    @Override
    public void dispose()
    {
        reset();
        super.dispose();
    }

    private void reset()
    {
        previousWinnersTable.clearSelection();
        keywordField.setText("");
        durationSpinner.setValue(20);
        subscriberState.setSelectedIndex(0);
        donorState.setSelectedIndex(0);
        modState.setSelectedIndex(0);
    }

    private void startRaffleButtonActionPerformed()
    {
        String keyword = keywordField.getText();
        String channel = "#" + channelsComboBox.getItemAt(channelsComboBox.getSelectedIndex());
        int timeSeconds = (int) durationSpinner.getValue();

        // Permissions
        // 0 = N/A, 1 = INCLUDE, 2 = EXCLUDE
        ArrayList<Permissions.Permission> required = new ArrayList<>();
        ArrayList<Permissions.Permission> denied = new ArrayList<>();
        if (subscriberState.getSelectedIndex() == 1)
            required.add(Permissions.Permission.SUBSCRIBER);
        else if (subscriberState.getSelectedIndex() == 2)
            denied.add(Permissions.Permission.SUBSCRIBER);

        if (modState.getSelectedIndex() == 1)
            required.add(Permissions.Permission.MODERATOR);
        else if (modState.getSelectedIndex() == 2)
            denied.add(Permissions.Permission.MODERATOR);

        if (donorState.getSelectedIndex() == 1)
            required.add(Permissions.Permission.DONOR);
        else if (donorState.getSelectedIndex() == 2)
            denied.add(Permissions.Permission.DONOR);

        // Only add regular viewer if all three special types are excluded or indifferent
        if (required.isEmpty())
            required.add(Permissions.Permission.VIEWER);

        // Start this Raffle
        Raffle r = new Raffle(Settings.accountManager.getBot(), keyword, timeSeconds, channel, required, denied);
        GUIMain.bot.startRaffle(r);

        // Add it to the table
        addRaffle(r);

        // Lastly clear the fields
        reset();
    }

    // Called from the text command as well
    public void addRaffle(Raffle r)
    {
        DefaultTableModel dtm = (DefaultTableModel) previousWinnersTable.getModel();
        dtm.insertRow(0, new Object[]{r.getKeyword(), "N/A", 0});
    }


    // Called from inside the Raffle loop
    public void updateRaffle(Raffle r)
    {
        int row = findRow(r.getKeyword());
        if (row > -1)
        {
            if (r.getWinner() != null)
            {
                int winnerCol = previousWinnersTable.getColumn("Winner").getModelIndex();
                EventQueue.invokeLater(() -> previousWinnersTable.setValueAt(r.getWinner(), row, winnerCol));
            }

            int entrantsCol = previousWinnersTable.getColumn("# Entered").getModelIndex();
            EventQueue.invokeLater(() -> previousWinnersTable.setValueAt(r.getNumberEntrants(), row, entrantsCol));
        }
    }

    public void removeRaffle(Raffle r)
    {
        DefaultTableModel dtm = (DefaultTableModel) previousWinnersTable.getModel();
        int row = findRow(r.getKeyword());
        if (row > -1)
        {
            dtm.removeRow(row);
        }
    }

    private int findRow(String keyword)
    {
        int keyWordCol = previousWinnersTable.getColumn("Keyword").getModelIndex();
        for (int i = 0; i < previousWinnersTable.getRowCount(); i++)
        {
            if (previousWinnersTable.getValueAt(i, keyWordCol).equals(keyword))
                return i;
        }
        return -1;
    }

    private void previousWinnersTableMouseReleased()
    {
        int sel = previousWinnersTable.getSelectedRow();
        if (sel > -1)
        {
            int winnerCol = previousWinnersTable.getColumn("Winner").getModelIndex();
            String winner = (String) previousWinnersTable.getValueAt(sel, winnerCol);
            if (winner != null && "N/A".equals(winner))
                stopRaffleButton.setEnabled(true);
        }
    }

    private void stopRaffleButtonActionPerformed()
    {
        int keyWordCol = previousWinnersTable.getColumn("Keyword").getModelIndex();
        String keyword = (String) previousWinnersTable.getValueAt(previousWinnersTable.getSelectedRow(), keyWordCol);

        if (keyword != null)
        {
            Raffle stopped = GUIMain.bot.stopRaffle(keyword);
            if (stopped != null)
            {
                GUIMain.bot.sendStopRaffleMessage(stopped);

                // now we remove it
                removeRaffle(stopped);
            }
        }

        stopRaffleButton.setEnabled(false);
    }


    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick Kerns
        separator2 = new JSeparator();
        startRaffleButton = new JButton();
        stopRaffleButton = new JButton();
        scrollPane1 = new JScrollPane();
        previousWinnersTable = new JTable();
        previousWinnersTable.getTableHeader().setReorderingAllowed(false);
        label4 = new JLabel();
        subscriberState = new JComboBox<>();
        label6 = new JLabel();
        donorState = new JComboBox<>();
        label5 = new JLabel();
        modState = new JComboBox<>();
        label3 = new JLabel();
        channelsComboBox = new JComboBox<>();
        label7 = new JLabel();
        label2 = new JLabel();
        durationSpinner = new JSpinner();
        keywordField = new JTextField();
        label1 = new JLabel();

        //======== this ========
        setTitle("Raffles");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        //---- startRaffleButton ----
        startRaffleButton.setText("Start Raffle");
        startRaffleButton.setFocusable(false);
        startRaffleButton.addActionListener(e -> startRaffleButtonActionPerformed());

        //---- stopRaffleButton ----
        stopRaffleButton.setText("Stop Raffle");
        stopRaffleButton.setEnabled(false);
        stopRaffleButton.setFocusable(false);
        stopRaffleButton.addActionListener(e -> stopRaffleButtonActionPerformed());

        //======== scrollPane1 ========
        {

            //---- previousWinnersTable ----
            previousWinnersTable.setModel(new DefaultTableModel(
                    new Object[][]{
                    },
                    new String[]{
                            "Keyword", "Winner", "# Entered"
                    }
            )
            {
                Class<?>[] columnTypes = new Class<?>[]{
                        String.class, String.class, Integer.class
                };
                boolean[] columnEditable = new boolean[]{
                        false, false, false
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
                TableColumnModel cm = previousWinnersTable.getColumnModel();
                cm.getColumn(0).setResizable(false);
                cm.getColumn(0).setPreferredWidth(100);
                cm.getColumn(1).setResizable(false);
                cm.getColumn(1).setPreferredWidth(100);
                cm.getColumn(2).setResizable(false);
                cm.getColumn(2).setPreferredWidth(50);
            }
            previousWinnersTable.setRowSelectionAllowed(false);
            previousWinnersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            previousWinnersTable.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    previousWinnersTableMouseReleased();
                }
            });
            scrollPane1.setViewportView(previousWinnersTable);
        }

        //---- label4 ----
        label4.setText("Subscribers");

        //---- subscriberState ----
        subscriberState.setModel(new DefaultComboBoxModel<>(new String[]{
                "-",
                "Include",
                "Exclude"
        }));
        subscriberState.setFocusable(false);

        //---- label6 ----
        label6.setText("Donors");

        //---- donorState ----
        donorState.setModel(new DefaultComboBoxModel<>(new String[]{
                "-",
                "Include",
                "Exclude"
        }));
        donorState.setFocusable(false);

        //---- label5 ----
        label5.setText("Mods");

        //---- modState ----
        modState.setModel(new DefaultComboBoxModel<>(new String[]{
                "-",
                "Include",
                "Exclude"
        }));
        modState.setFocusable(false);

        //---- label3 ----
        label3.setText("Permissions:");
        label3.setToolTipText("<html>These are automatically exclusive if left unchecked.<br>\nIf you want everybody to enter, check all the boxes.<br>\nOtherwise the person has to be JUST what you checked.<br>\nExample: A moderator that is also a subscriber<br>\nCANNOT enter if only moderator OR only subscriber is chosen.</html>");

        //---- label7 ----
        label7.setText("Channel:");
        label7.setHorizontalAlignment(SwingConstants.RIGHT);

        //---- label2 ----
        label2.setText("Keyword:");
        label2.setHorizontalAlignment(SwingConstants.RIGHT);

        //---- durationSpinner ----
        durationSpinner.setFocusable(false);
        durationSpinner.setModel(new SpinnerNumberModel(20, 20, null, 1));

        //---- label1 ----
        label1.setText("Duration (in seconds):");
        label1.setHorizontalAlignment(SwingConstants.RIGHT);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                                                        .addComponent(separator2, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(6, 6, 6)
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(label4)
                                                                        .addComponent(subscriberState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(26, 26, 26)
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(donorState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(label6))
                                                                .addGap(27, 27, 27)
                                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                                        .addComponent(label5)
                                                                        .addComponent(modState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                        .addComponent(label3)
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(65, 65, 65)
                                                                .addComponent(label2)
                                                                .addGap(11, 11, 11)
                                                                .addComponent(keywordField, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(65, 65, 65)
                                                                .addComponent(label7)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(channelsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addComponent(label1)
                                                                .addGap(9, 9, 9)
                                                                .addComponent(durationSpinner, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 13, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(startRaffleButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                                                .addComponent(stopRaffleButton)
                                                .addGap(0, 6, Short.MAX_VALUE))))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(label3)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(label4)
                                                        .addComponent(label5)
                                                        .addComponent(label6))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addComponent(subscriberState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(donorState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(modState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(5, 5, 5)
                                                                .addComponent(label2))
                                                        .addComponent(keywordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGap(1, 1, 1)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(5, 5, 5)
                                                                .addComponent(label1))
                                                        .addComponent(durationSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(label7)
                                                .addGap(6, 6, 6))
                                        .addComponent(channelsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(23, 23, 23)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(startRaffleButton)
                                        .addComponent(stopRaffleButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick Kerns
    private JSeparator separator2;
    private JButton startRaffleButton;
    private JButton stopRaffleButton;
    private JScrollPane scrollPane1;
    private JTable previousWinnersTable;
    private JLabel label4;
    private JComboBox<String> subscriberState;
    private JLabel label6;
    private JComboBox<String> donorState;
    private JLabel label5;
    private JComboBox<String> modState;
    private JLabel label3;
    private JComboBox<String> channelsComboBox;
    private JLabel label7;
    private JLabel label2;
    private JSpinner durationSpinner;
    private JTextField keywordField;
    private JLabel label1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
