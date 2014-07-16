package gui.listeners;

import gui.GUIMain;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * Created by Nick on 12/31/13.
 */
public class ListenerURL extends MouseAdapter {

    public ListenerURL() {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        JTextPane editor = (JTextPane) e.getSource();
        Point pt = new Point(e.getX(), e.getY());
        int pos = editor.viewToModel(pt);
        if (pos >= 0) {
            Document doc = editor.getDocument();
            if (doc instanceof DefaultStyledDocument) {
                DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                Element el = hdoc.getCharacterElement(pos);
                AttributeSet a = el.getAttributes();
                String href = (String) a.getAttribute(HTML.Attribute.HREF);
                if (href != null) {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        URI uri = new URI(href);
                        desktop.browse(uri);
                    } catch (Exception ev) {
                        GUIMain.log((ev.getMessage()));
                    }
                }
            }
        }
        super.mouseReleased(e);
    }
}
