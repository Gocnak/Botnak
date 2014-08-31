package thread;

import gui.GUIMain;
import lib.jtattoo.com.jtattoo.plaf.ColorHelper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Nick on 1/4/14.
 * <p>
 * <p>
 * TODO: Rewrite this class to support components, as pulsing may come in handy for other things than tabs
 * <p>
 * This class is like a listener for tabs, but only for setting the colors of them.
 * Credit to http://www.coderanch.com/t/346366/GUI/java/macs-button-pulse
 * for the idea.
 */
public class TabPulse extends Thread {

    private int index;

    public int getIndex() {
        return index;
    }

    private Color[] totalColors = new Color[200];
    private final Color controlColorDark = new Color(32, 32, 32);

    private TimerListener tl;
    private javax.swing.Timer timer;

    public TabPulse(final int index) {
        this.index = index;
        tl = new TimerListener();
        tl.addColorChanger(c -> GUIMain.channelPane.setBackgroundAt(index, c));
        timer = new javax.swing.Timer(20, tl);
    }

    @Override
    public void run() {
        if (GUIMain.channelPane.getSelectedIndex() != index) {
            timer.start();
            while (!GUIMain.shutDown && GUIMain.channelPane.getSelectedIndex() != index) {
                //do pulse
                if (tl.isActuallyDone()) {
                    timer.stop();
                    GUIMain.channelPane.setBackgroundAt(index, ColorHelper.darker(Color.orange, 15));
                }
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {
                }
            }
            timer.stop();
            timer = null;
            GUIMain.channelPane.setBackgroundAt(index, null);
        }
        super.run();
        GUIMain.tabPulses.remove(this);
    }

    @Override
    public void interrupt() {
        GUIMain.tabPulses.remove(this);
        super.interrupt();
    }

    @Override
    public synchronized void start() {
        initColors();
        super.start();
    }

    private void initColors() {
        Color[] colors = ColorHelper.createColorArr(controlColorDark, ColorHelper.darker(Color.orange, 20), 100);
        for (int i = 0; i < (totalColors.length / 2); i++) {
            totalColors[i] = colors[i];
        }
        for (int i = 100; i < totalColors.length; i++) {
            totalColors[i] = colors[(colors.length - 1) - (i - colors.length)];
        }
    }

    private class TimerListener implements ActionListener {
        private int counter = 0;
        private int doneCounter = 0;
        private java.util.List<ColorChanger> colorChangerList = new ArrayList<>();
        private boolean isActuallyDone = false;

        public void actionPerformed(ActionEvent e) {
            for (ColorChanger cc : colorChangerList) {
                try {
                    cc.setColor(totalColors[counter]);
                } catch (Exception ex) {
                    doneCounter++;
                    isActuallyDone = (doneCounter >= 2);
                    counter = -1;
                }
            }
            if (!isActuallyDone) {
                counter++;
            }
        }

        public boolean isActuallyDone() {
            return isActuallyDone;
        }

        public void addColorChanger(ColorChanger cc) {
            colorChangerList.add(cc);
        }
    }

    // added to loosen coupling a little bit
    private interface ColorChanger {
        void setColor(Color c);
    }
}
