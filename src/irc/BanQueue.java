package irc;

import gui.GUIMain;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Nick on 12/31/13.
 */
public class BanQueue extends Thread {

    public BanQueue() {
    }

    @Override
    public synchronized void start() {
        GUIMain.banMap = new HashMap<>();
        super.start();
    }

    @Override
    public void interrupt() {
        GUIMain.banMap.clear();
        super.interrupt();
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown && GUIMain.viewer != null) {
            emptyMap();
            try {
                Thread.sleep(5000);
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void emptyMap() {
        Set<String> names = GUIMain.banMap.keySet();
        for (String name : names) {
            int value = GUIMain.banMap.get(name);
            if (value > 1) {
                GUIMain.onBan(name + " has been banned/timed out " + value + " times!");
            } else {
                GUIMain.onBan(name + " has been banned/timed out!");
            }
        }
        if (GUIMain.banMap.size() > 0)
            GUIMain.banMap.clear();
    }
}
