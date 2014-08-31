package thread.heartbeat;

import gui.GUIMain;

import java.util.HashSet;

/**
 * Created by Nick on 3/12/14.
 * This class will be used to do normal checkups on things.
 * <p>
 * The things to check include and are not limited to:
 * 1. Connection
 * 2. Points for Users (TODO)
 * 4. Emote downloads
 * 5. TODO: increse this list
 */
public class Heartbeat extends Thread {

    private HashSet<HeartbeatThread> heartbeatThreads;
    private int delay;

    /**
     * Constructs a heartbeat monitor with a delay of half a second between beats.
     */
    public Heartbeat() {
        this(500);
    }

    /**
     * Constructs a heartbeat monitor with a specified delay.
     *
     * @param del The specified delay in milliseconds.
     */
    public Heartbeat(int del) {
        heartbeatThreads = new HashSet<>();
        delay = del;
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown) {
            heartbeatThreads.stream().filter(HeartbeatThread::shouldBeat).forEach(t -> {
                t.beat();
                t.afterBeat();
            });
            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void addHeartbeatThread(HeartbeatThread t) {
        heartbeatThreads.add(t);
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }
}
