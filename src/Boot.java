import gui.GUIMain;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 8/1/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class Boot {
    public static void main(String[] args) {
        GUIMain g = new GUIMain();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        g.setVisible(true);
    }
}
