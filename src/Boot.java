import gui.GUIMain;
import util.Settings;

import javax.swing.*;
import java.awt.*;

public class Boot {
    public static void main(String[] args) {

        /* Thread-safe initialization */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                /* make Java look pretty. */
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
                    Settings.loadLAF();
                    UIManager.setLookAndFeel(Settings.lookAndFeel);
                } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
