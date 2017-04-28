import gui.forms.GUIMain;
import gui.forms.GUIUpdate;
import thread.ExceptionHandler;
import thread.ShutdownHook;
import util.settings.Settings;

import javax.swing.*;
import java.awt.*;


/**
 * TODO:
 * Settings GUI
 * Lotsofs: change the output AudioLine
 * Gif faces
 * Voting GUI
 * TextCommands GUI
 * (autocommand)
 * Log Viewer GUI
 * <p>
 * Bug:
 * Sound delay shows milliseconds
 * Cheer donors should be able to play sounds/have donor benefits after a certain amount
 * fix heartbeat threads from spouting issues (connection-wise) -- use WebSockets?
 */


public class Boot {
    public static void main(final String[] args) {
        /* Thread-safe initialization */
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLookAndFeel();
                GUIMain g = new GUIMain();
                g.setVisible(true);
                if (GUIUpdate.checkForUpdate()) {
                    GUIUpdate gu = new GUIUpdate();
                    gu.setVisible(true);
                }
            }

            /**
             * Tries to set the swing look and feel.
             * All relevant exceptions are caught.
             */
            private void setLookAndFeel() {
                try {
                    Settings.LAF.load();
                    UIManager.setLookAndFeel(Settings.lookAndFeel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}