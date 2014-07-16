package sound;

import gui.GUIMain;
import util.Timer;

import java.util.Collection;

/**
 * Created by Nick on 12/20/13.
 */
public class SoundEngine {

    private static SoundEngine engine = null;
    private static SoundPlayer player = null;

    public static SoundEngine getEngine() {
        return engine;
    }

    private int delay = 10000;
    private int permission = 1;//default to sub+ permission
    private boolean soundToggle = true;
    private Timer soundTimer = new Timer(delay);

    public static void init() {
        engine = new SoundEngine();
    }

    public SoundEngine() {
        player = new SoundPlayer(10);
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

    public void setPermission(int perm) {
        permission = perm;
    }

    public int getPermission() {
        return permission;
    }

    /**
     * Adds a sound to the sound set.
     * This respects the "one at a time"
     *
     * @param s The sound to add.
     */
    public void addSound(Sound s) {
        if (!soundTimer.isRunning()) {
            if (soundTimer.period == 0) {//alowing for spam
                try {
                    player.play(s.getFile(), SoundPlayer.PlayMode.Force);
                } catch (Exception ignored) {
                }
            } else {
                try {
                    player.play(s.getFile(), SoundPlayer.PlayMode.Ignore);
                } catch (Exception ignored) {
                }
                soundTimer.reset();
            }
        }
    }

    /**
     * Plays the new subscriber sound, ignoring the current playing ones.
     */
    public void playSubSound() {
        if (GUIMain.currentSettings.subSound != null) {
            try {
                player.play(GUIMain.currentSettings.subSound.getFile(), SoundPlayer.PlayMode.Force);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Gets the first playing sound in the queue.
     *
     * @return The first playing sound.
     */
    public SoundEntry getCurrentPlayingSound() {
        Collection<SoundEntry> coll = player.getPlayingSounds();
        if (!coll.isEmpty()) {
            for (SoundEntry s : coll) {
                if (s.getClip().isRunning()) {
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
    public Collection<SoundEntry> getCurrentPlayingSounds() {
        return player.getPlayingSounds();
    }

    public void close() {
        player.close();
    }
}
