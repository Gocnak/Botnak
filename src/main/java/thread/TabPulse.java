package thread;

import gui.ChatPane;
import gui.forms.GUIMain;
import lib.jtattoo.com.jtattoo.plaf.ColorHelper;

import javax.accessibility.AccessibleComponent;
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

    private Color[] totalColors = new Color[200];
    private final Color controlColorDark = new Color(32, 32, 32);

    private TimerListener tl;
    private javax.swing.Timer timer;
    private ChatPane cp = null;
    private AccessibleComponent ac = null;

    public TabPulse(final int index) {
        this(GUIMain.channelPane.getPage(index));
    }

    public TabPulse(ChatPane cp) {
        this(cp.getIndex());
        this.cp = cp;
    }

    public TabPulse(Component c) {

    }

    public TabPulse(AccessibleComponent ac) {

        tl = new TimerListener();
        this.ac = ac;
        tl.addColorChanger(c -> {
            ac.setBackground(c);
            GUIMain.channelPane.repaint(ac.getBounds());
        });
        timer = new javax.swing.Timer(20, tl);
    }

    @Override
    public void run() {
        if (cp.shouldPulse()) {
            timer.start();
            while (!GUIMain.shutDown && cp.shouldPulse()) {
                //do pulse
                if (tl.isActuallyDone()) {
                    timer.stop();
                    ac.setBackground(ColorHelper.darker(Color.orange, 15));
                    GUIMain.channelPane.repaint(ac.getBounds());
                }
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {
                }
            }
            timer.stop();
            timer = null;
            if (cp != null) cp.setPulsing(false);
            ac.setBackground(null);
        }
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
        if (cp != null) cp.setPulsing(true);
        super.start();
    }

    private void initColors() {
        Color[] colors = ColorHelper.createColorArr(controlColorDark, ColorHelper.darker(Color.orange, 20), 100);
        System.arraycopy(colors, 0, totalColors, 0, totalColors.length / 2);
        for (int i = 100; i < totalColors.length; i++) {
            totalColors[i] = colors[(colors.length - 1) - (i - colors.length)];
        }
    }

    private class TimerListener implements ActionListener {
        private int counter = 0;
        private int doneCounter = 0;
        private int setCounter = 0;
        private java.util.List<ColorChanger> colorChangerList = new ArrayList<>();
        private boolean isActuallyDone = false;

        public TimerListener() {
            this(2);
        }

        public TimerListener(int count) {
            setCounter = count;
        }

        public void actionPerformed(ActionEvent e) {
            for (ColorChanger cc : colorChangerList) {
                try {
                    cc.setColor(totalColors[counter]);
                } catch (Exception ex) {
                    doneCounter++;
                    isActuallyDone = (doneCounter >= setCounter);
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