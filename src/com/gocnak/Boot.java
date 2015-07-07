package com.gocnak;

import com.gocnak.gui.GUIMain;
import com.gocnak.gui.GUIUpdate;
import com.gocnak.util.settings.Settings;

import javax.swing.*;
import java.awt.*;

public class Boot {
    public static void main(final String[] args) {

        /* Thread-safe initialization */
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
                    Settings.loadLAF();
                    UIManager.setLookAndFeel(Settings.lookAndFeel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}