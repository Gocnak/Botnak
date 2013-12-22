package sound;

import gui.GUIMain;
import util.Timer;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Nick on 12/20/13.
 */
public class SoundEngine extends Thread {

    private static SoundEngine engine = null;

    public static SoundEngine getEngine() {
        return engine;
    }

    private HashSet<SoundThread> soundSet = new HashSet<>();
    private int delay = 5000;
    private boolean soundToggle = true;
    private Timer soundTimer = new Timer(delay);

    public static void init() {
        engine = new SoundEngine();
        engine.start();
    }

    public void setDelay(int newDelay) {
        delay = newDelay;
        soundTimer = new Timer(delay);
    }

    public Timer getSoundTimer() {
        return soundTimer;
    }

    public void setShouldPlay(boolean newBool) {
        soundToggle = newBool;
    }

    public boolean shouldPlay() {
        return soundToggle;
    }

    /**
     * Adds a sound to the sound set.
     * This respects the "one at a time"
     *
     * @param s The sound to add.
     */
    public void addSound(Sound s) {
        if (!soundTimer.isRunning()) {
            if (soundTimer.period == 0) {//alowing for a raid
                SoundThread t = new SoundThread(s);
                t.start();
                soundSet.add(t);
            } else {
                if (getCurrentPlayingSound() == null) {//else wait for the current sound to end
                    SoundThread t = new SoundThread(s);
                    t.start();
                    soundSet.add(t);
                }
            }
        }
    }

    /**
     * Gets the first playing sound in the queue.
     *
     * @return The first playing sound.
     */
    public SoundThread getCurrentPlayingSound() {
        if (!soundSet.isEmpty()) {
            for (SoundThread s : soundSet) {
                if (s.isPlaying()) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Gets all of the current playing sounds.
     *
     * @return All of the currently playing sounds.
     */
    public SoundThread[] getCurrentPlayingSounds() {
        ArrayList<SoundThread> arrayList = new ArrayList<>();
        if (!soundSet.isEmpty()) {
            for (SoundThread s : soundSet) {
                if (s.isPlaying()) {
                    arrayList.add(s);
                }
            }
        }
        return arrayList.toArray(new SoundThread[arrayList.size()]);
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown) {
            if (soundSet != null && !soundSet.isEmpty()) {
                ArrayList<SoundThread> arrayList = new ArrayList<>();
                synchronized (soundSet) {
                    for (SoundThread s : soundSet) {
                        if (!s.isPlaying()) {
                            arrayList.add(s);
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        for (SoundThread s : arrayList) {
                            soundSet.remove(s);
                            soundTimer.reset();
                        }
                    }
                }
                arrayList.clear();
            }
            try {
                Thread.sleep(1500);
            } catch (Exception ignored) {
            }
            super.run();
        }
    }

    @Override
    public void interrupt() {
        for (SoundThread s : soundSet) {
            s.interrupt();
        }
        soundSet.clear();
        super.interrupt();
    }
}
