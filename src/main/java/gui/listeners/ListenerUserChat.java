package gui.listeners;

import gui.forms.GUIMain;
import lib.pircbot.org.jibble.pircbot.User;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by Nick on 12/29/2014.
 */
public class ListenerUserChat extends KeyAdapter {

    private JTextArea userChat;

    public ListenerUserChat(JTextArea text) {
        userChat = text;
    }

    public void keyReleased(KeyEvent e) {
        int initial = GUIMain.userResponsesIndex;
        if (suggestion != null) {
            if (!userChat.getText().contains("@")) {
                suggestion.hide();
                shouldShow = false;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (suggestion != null) {
                final String select = suggestion.getSelection();
                final SuggestionPanel sugg = suggestion;
                EventQueue.invokeLater(() -> {
                    userChat.setText(userChat.getText().replaceAll("\n", " "));
                    sugg.insertSelection(select);
                });
            } else {
                EventQueue.invokeLater(GUIMain.instance::chatButtonActionPerformed);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (suggestion != null) {
                showSuggestionLater();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (suggestion != null) {
                final String select = this.suggestion.getSelection();
                final SuggestionPanel sugg = suggestion;
                EventQueue.invokeLater(() -> {
                    userChat.setText(userChat.getText().replaceAll("\t", " "));
                    sugg.insertSelection(select);
                });
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (!GUIMain.userResponses.isEmpty()) {
                if (userChat.getText().equals("")) {
                    GUIMain.userResponsesIndex = GUIMain.userResponses.size() - 1;
                } else if (GUIMain.userResponses.contains(userChat.getText())) {
                    if (GUIMain.userResponsesIndex == 0) GUIMain.userResponsesIndex = GUIMain.userResponses.size() - 1;
                    else GUIMain.userResponsesIndex--;
                }
            }
            if (suggestion != null) {
                suggestion.moveUp();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!GUIMain.userResponses.isEmpty()) {
                if (userChat.getText().equals("")) {
                    GUIMain.userResponsesIndex = 0;
                } else if (GUIMain.userResponses.contains(userChat.getText())) {
                    if (GUIMain.userResponsesIndex == GUIMain.userResponses.size() - 1) GUIMain.userResponsesIndex = 0;
                    else GUIMain.userResponsesIndex++;
                }
            }
            if (suggestion != null) {
                suggestion.moveDown();
            }
        } else if (Character.isWhitespace(e.getKeyChar())) {
            hideSuggestion();//null check in the method
        } else if (shouldShow && Character.isLetterOrDigit(e.getKeyChar())) {
            showSuggestionLater();
        } else if (e.getKeyChar() == '@') {
            shouldShow = true;
        }
        if (initial != GUIMain.userResponsesIndex) {
            EventQueue.invokeLater(() -> GUIMain.userChat.setText(GUIMain.userResponses.get(GUIMain.userResponsesIndex)));
        }
    }


    private static boolean shouldShow = false;

    private class SuggestionPanel {
        private JList<String> list;
        private JPopupMenu popupMenu;
        private String subWord;
        private int insertionPosition;

        public SuggestionPanel(int position, String subWord, Point location, String[] names) {
            this.insertionPosition = position;
            this.subWord = subWord;
            popupMenu = new JPopupMenu();
            popupMenu.removeAll();
            popupMenu.setInvoker(userChat);
            popupMenu.setFocusable(false);
            popupMenu.add(list = createSuggestionList(names), BorderLayout.CENTER);
            popupMenu.show(userChat, location.x, userChat.getBaseline(0, 0) + location.y);

        }

        public void hide() {
            popupMenu.setVisible(false);
            if (suggestion == this) {
                subWord = null;
                suggestion = null;
            }
        }

        private JList<String> createSuggestionList(String[] names) {
            JList<String> list = new JList<>(names);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            return list;
        }

        public String getSelection() {
            return list.getSelectedValue();
        }

        public void insertSelection(String selection) {
            if (selection != null) {
                try {
                    final String selectedSuggestion = selection.substring(subWord.length());
                    userChat.getDocument().insertString(insertionPosition, selectedSuggestion, null);
                    shouldShow = false;
                    hide();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void moveUp() {
            int index = Math.min(list.getSelectedIndex() - 1, 0);
            selectIndex(index);
        }

        public void moveDown() {
            int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
            selectIndex(index);
        }

        private void selectIndex(int index) {
            final int position = userChat.getCaretPosition();
            list.setSelectedIndex(index);
            SwingUtilities.invokeLater(() -> userChat.setCaretPosition(position));
        }
    }

    private SuggestionPanel suggestion;

    protected void showSuggestionLater() {
        SwingUtilities.invokeLater(this::showSuggestion);
    }

    protected void showSuggestion() {
        hideSuggestion();
        int position = userChat.getCaretPosition();
        Point location;
        try {
            location = userChat.modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            e2.printStackTrace();
            return;
        }
        String text = userChat.getText();
        int start = Math.max(0, position - 1);
        while (start > 0) {
            if (text.charAt(start) != '@') {
                start--;
            } else {
                break;
            }
        }
        if (start > position) {
            return;
        }
        String subText = text.substring(start, position).toLowerCase();
        if ("".equals(subText) || !subText.contains("@")) {
            shouldShow = false;
            return;
        }
        String subWord = subText.replace("@", "");
        if (subWord.length() < 3) {
            return;
        }
        User[] users = GUIMain.currentSettings.channelManager.getUsers(subWord);
        if (users.length > 0) {
            ArrayList<String> names = new ArrayList<>();
            for (User u : users) {
                names.add(u.getLowerNick());
            }
            suggestion = new SuggestionPanel(position, subWord, location, names.toArray(new String[names.size()]));
            SwingUtilities.invokeLater(userChat::requestFocusInWindow);
        }
    }

    private void hideSuggestion() {
        if (suggestion != null) {
            suggestion.hide();
        }
    }
}