package thread.heartbeat;

import gui.forms.GUIMain;
import util.settings.Settings;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 3/12/14.
 * This class will be used to do normal checkups on things.
 */
public class Heartbeat extends Thread {

    private ExecutorService executor;
    private ArrayList<HeartbeatThread> heartbeatThreads;
    private final int delay;

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
        heartbeatThreads = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        delay = del;
        addHeartbeatThread(new ViewerCount());
        addHeartbeatThread(new UserManager());
        addHeartbeatThread(new BanQueue());
        if (Settings.trackDonations.getValue()) {
            addHeartbeatThread(new DonationCheck());
        }
        if (Settings.trackFollowers.getValue()) {
            FollowCheck fc = new FollowCheck();
            fc.initialBeat();
            addHeartbeatThread(fc);
        }
        start();
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown) {
            heartbeatThreads.stream().filter(HeartbeatThread::shouldBeat).forEach(t ->
                    executor.execute(() -> {
                        try {
                            t.beat();
                            t.afterBeat();
                        } catch (Exception ignored) {
                        }
                    }));
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
        executor.shutdown();
        super.interrupt();
    }
}