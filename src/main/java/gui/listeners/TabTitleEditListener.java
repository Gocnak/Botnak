package gui.listeners;

import util.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;


//Credit to http://java-swing-tips.blogspot.com/2008/09/double-click-on-each-tab-and-change-its.html
// TERAI Atsuhiro
public class TabTitleEditListener extends MouseAdapter implements ChangeListener, ActionListener {

    private final JTextField editor = new JTextField();
    private final JTabbedPane tabbedPane;

    public TabTitleEditListener(final JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                renameTabTitle();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    renameTabTitle();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                } else {
                    editor.setPreferredSize(editor.getText().length() > len ? null : dim);
                    tabbedPane.revalidate();
                }
            }
        });
        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        tabbedPane.getActionMap().put("start-editing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startEditing();
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        renameTabTitle();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (Utils.getCombinedChatPane(tabbedPane.getSelectedIndex()) == null) return;
        Rectangle rect = tabbedPane.getUI().getTabBounds(tabbedPane, tabbedPane.getSelectedIndex());
        if (rect != null && rect.contains(me.getPoint()) && me.getClickCount() == 2) {
            startEditing();
        } else {
            renameTabTitle();
        }
    }

    private int editing_idx = -1;
    private int len = -1;
    private Dimension dim;
    private Component tabComponent = null;

    private void startEditing() {
        editing_idx = tabbedPane.getSelectedIndex();
        tabComponent = tabbedPane.getTabComponentAt(editing_idx);
        tabbedPane.setTabComponentAt(editing_idx, editor);
        editor.setVisible(true);
        editor.setText(tabbedPane.getTitleAt(editing_idx));
        editor.selectAll();
        editor.requestFocusInWindow();
        len = editor.getText().length();
        dim = editor.getPreferredSize();
        editor.setMinimumSize(dim);
    }

    private void cancelEditing() {
        if (editing_idx >= 0) {
            tabbedPane.setTabComponentAt(editing_idx, tabComponent);
            editor.setVisible(false);
            editing_idx = -1;
            len = -1;
            tabComponent = null;
            editor.setPreferredSize(null);
            tabbedPane.requestFocusInWindow();
        }
    }

    private void renameTabTitle() {
        String title = editor.getText().trim();
        if (editing_idx >= 0 && !title.isEmpty()) {
            tabbedPane.setTitleAt(editing_idx, title);
        }
        cancelEditing();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        if (source != null && source.getText() != null) {
            String text = source.getText();
            if (text.startsWith("Rename")) startEditing();
        }
    }
}