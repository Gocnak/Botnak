package gui;

import gui.listeners.ListenerName;
import thread.heartbeat.UserManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * @author Nick K
 */
public class GUIViewerList extends JFrame {

    private boolean isFiltering = false;

    private DefaultTreeModel defaultModel;

    public DefaultMutableTreeNode staff, admins, global_mods, mods, viewers, default_root, filtered_root;

    public GUIViewerList(String channel) {
        initComponents(channel);
        UserManager.toUpdate.setEndIn(0L);//force an update
    }

    public enum ViewerType {
        STAFF,
        ADMIN,
        GLOBAL_MOD,
        MOD,
        VIEWER
    }

    public Enumeration<TreePath> getExpandedDescendants() {
        return viewerTree.getExpandedDescendants(viewerTree.getPathForRow(0));
    }

    public synchronized void updateCategory(ViewerType type, HashSet<String> names) {
        final DefaultMutableTreeNode node;
        Enumeration<TreePath> userPath = getExpandedDescendants();
        int scrollAmount = scrollPane.getVerticalScrollBar().getValue();
        switch (type) {
            case STAFF:
                staff.removeAllChildren();
                node = staff;
                break;
            case ADMIN:
                admins.removeAllChildren();
                node = admins;
                break;
            case MOD:
                mods.removeAllChildren();
                node = mods;
                break;
            case VIEWER:
                viewers.removeAllChildren();
                node = viewers;
                break;
            case GLOBAL_MOD:
                global_mods.removeAllChildren();
                node = global_mods;
                break;
            default:
                node = null;
                break;
        }
        if (node != null) {
            if (!names.isEmpty()) {
                names.stream().sorted().forEach(s -> node.add(new DefaultMutableTreeNode(s)));
            }
            updateRoot(userPath, scrollAmount);
        }
    }

    private synchronized void updateRoot(Enumeration<TreePath> userPath, int scrollAmount) {
        DefaultMutableTreeNode root = default_root;
        root.removeAllChildren();
        if (staff.getChildCount() > 0) root.add(staff);
        if (admins.getChildCount() > 0) root.add(admins);
        if (global_mods.getChildCount() > 0) root.add(global_mods);
        if (mods.getChildCount() > 0) root.add(mods);
        root.add(viewers);
        if (!isFiltering) defaultModel.reload(root);
        if (userPath != null) {
            while (userPath.hasMoreElements()) {
                viewerTree.expandPath(userPath.nextElement());
            }
        }
        scrollPane.getVerticalScrollBar().setValue(scrollAmount);
    }

    private void setViewerTreeModel(DefaultTreeModel model) {
        viewerTree.setModel(model);
    }

    public synchronized void buildFilteredModel(String text) {
        filtered_root.removeAllChildren();
        ArrayList<DefaultMutableTreeNode> rows = new ArrayList<>();
        if (staff.getChildCount() > 0) {
            DefaultMutableTreeNode filtered = new DefaultMutableTreeNode("Staff");
            filterNode(staff, text, filtered);
            if (filtered.getChildCount() > 0) {
                filtered_root.add(filtered);
                rows.add(filtered);
            }
        }
        if (admins.getChildCount() > 0) {
            DefaultMutableTreeNode filtered = new DefaultMutableTreeNode("Admins");
            filterNode(admins, text, filtered);
            if (filtered.getChildCount() > 0) {
                filtered_root.add(filtered);
                rows.add(filtered);
            }
        }
        if (global_mods.getChildCount() > 0) {
            DefaultMutableTreeNode filtered = new DefaultMutableTreeNode("Global Moderators");
            filterNode(global_mods, text, filtered);
            if (filtered.getChildCount() > 0) {
                filtered_root.add(filtered);
                rows.add(filtered);
            }
        }
        if (mods.getChildCount() > 0) {
            DefaultMutableTreeNode filtered = new DefaultMutableTreeNode("Moderators");
            filterNode(mods, text, filtered);
            if (filtered.getChildCount() > 0) {
                filtered_root.add(filtered);
                rows.add(filtered);
            }
        }
        if (viewers.getChildCount() > 0) {
            DefaultMutableTreeNode filtered = new DefaultMutableTreeNode("Viewers");
            filterNode(viewers, text, filtered);
            if (filtered.getChildCount() > 0) {
                filtered_root.add(filtered);
                rows.add(filtered);
            }
        }
        setViewerTreeModel(new DefaultTreeModel(filtered_root));
        for (DefaultMutableTreeNode row : rows) {
            TreePath tp = new TreePath(row.getPath());
            viewerTree.expandPath(tp);
        }
    }

    private synchronized void filterNode(DefaultMutableTreeNode node, String text, DefaultMutableTreeNode filtered) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode nameNode = (DefaultMutableTreeNode) node.getChildAt(i);
            String name = (String) nameNode.getUserObject();
            if (name.startsWith(text)) {
                filtered.add(new DefaultMutableTreeNode(name));
            }
        }
    }

    private void initComponents(String channel) {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        searchBar = new JTextField();
        scrollPane = new JScrollPane();
        viewerTree = new JTree();

        //======== this ========
        setTitle("Viewers");
        setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);
        Container contentPane = getContentPane();

        //---- searchBar ----
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = searchBar.getText();
                if (text != null) {
                    text = text.trim();
                    if (text.length() > 0) {
                        if (!isFiltering) {
                            isFiltering = true;
                        }
                        buildFilteredModel(text);
                    } else {
                        isFiltering = false;
                        setViewerTreeModel(defaultModel);
                    }
                } else {
                    isFiltering = false;
                    setViewerTreeModel(defaultModel);
                }
            }
        });

        //======== scrollPane2 ========
        {
            //---- viewerTree ---
            default_root = new DefaultMutableTreeNode(channel);
            defaultModel = new DefaultTreeModel(default_root);
            filtered_root = new DefaultMutableTreeNode(channel);
            viewerTree.setShowsRootHandles(true);
            staff = new DefaultMutableTreeNode("Staff");
            admins = new DefaultMutableTreeNode("Admins");
            global_mods = new DefaultMutableTreeNode("Global Moderators");
            mods = new DefaultMutableTreeNode("Moderators");
            viewers = new DefaultMutableTreeNode("Viewers");
            setViewerTreeModel(defaultModel);
            viewerTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        TreePath selPath = viewerTree.getPathForLocation(e.getX(), e.getY());
                        if (selPath != null && selPath.getPathCount() == 3) {
                            //should be a name
                            DefaultMutableTreeNode nameNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                            ListenerName.createUserPopup(channel, nameNode.getUserObject().toString(), viewerTree, e.getX(), e.getY());
                        }
                    }
                }
            });
            scrollPane.setViewportView(viewerTree);
        }
        setMinimumSize(new Dimension(250, 490));
        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(searchBar, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addComponent(searchBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
        );
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GUIMain.viewerLists.remove(channel);
            }
        });
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    private JTextField searchBar;
    private JTree viewerTree;
    private JScrollPane scrollPane;
}