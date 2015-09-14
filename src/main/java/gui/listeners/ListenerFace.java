package gui.listeners;

import face.Face;
import face.FrankerFaceZ;
import face.TwitchFace;
import gui.forms.GUIMain;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Nick on 5/20/2015.
 */
public class ListenerFace extends MouseAdapter {

    @Override
    public void mouseReleased(MouseEvent e) {
        JTextPane editor = (JTextPane) e.getSource();
        Point pt = new Point(e.getX(), e.getY());
        int pos = editor.viewToModel(pt);
        try {
            Rectangle rect = editor.modelToView(pos);
            int lowerCorner = rect.y + rect.height;
            if (e.getX() < rect.x && e.getY() < lowerCorner && pos > 0) {
                pos--;
            }
        } catch (BadLocationException ex) {
            GUIMain.log(ex);
        }
        if (pos >= 0) {
            Document doc = editor.getDocument();
            if (doc instanceof DefaultStyledDocument) {
                DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                Element el = hdoc.getCharacterElement(pos);
                AttributeSet a = el.getAttributes();
                Face f = (Face) a.getAttribute("faceinfo");
                if (f != null) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.setEnabled(false);
                    if (f instanceof FrankerFaceZ) {
                        String channel = (String) a.getAttribute("channel");
                        JMenuItem item = new JMenuItem("FrankerFaceZ Face");
                        item.setEnabled(false);
                        popupMenu.add(item);
                        item = new JMenuItem(f.getRegex());
                        item.setEnabled(false);
                        popupMenu.add(item);
                        item = new JMenuItem(channel);
                        item.setEnabled(false);
                        popupMenu.add(item);
                    } else if (f instanceof TwitchFace) {
                        String regex = (String) a.getAttribute("regex");
                        JMenuItem item = new JMenuItem("Twitch Face");
                        item.setEnabled(false);
                        popupMenu.add(item);
                        item = new JMenuItem(regex);
                        item.setEnabled(false);
                        popupMenu.add(item);
                    } else {//normal face
                        String regex = (String) a.getAttribute("regex");
                        JMenuItem item = new JMenuItem("Normal Face");
                        item.setEnabled(false);
                        popupMenu.add(item);
                        item = new JMenuItem(regex);
                        item.setEnabled(false);
                        popupMenu.add(item);
                    }
                    popupMenu.show(editor, pt.x, pt.y);
                }
            }
        }
    }
}