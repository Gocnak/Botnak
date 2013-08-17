import gui.GUIMain;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 8/1/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class Boot {
    public static void main(String[] args) {

        /* Thread-safe initialization */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                /* make Java look "native" if possible. */
                setLookAndFeel();

                GUIMain g = new GUIMain();
                g.setVisible(true);
            }

            /**
             * Tries to set the swing look and feel.
             * All relevant exceptions are caught.
             */
            private void setLookAndFeel() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        });
    }
}
