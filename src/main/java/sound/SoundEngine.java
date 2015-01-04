package sound;

import gui.GUIMain;
import util.Timer;

import java.io.File;
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

    private int delay = 10000;//default to 10 seconds
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
    public void playSpecialSound(boolean isSub) {
        File f = (isSub ? GUIMain.currentSettings.subSound.getFile() : GUIMain.currentSettings.donationSound.getFile());
        try {
            player.play(f, SoundPlayer.PlayMode.Force);
        } catch (Exception ignored) {
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


    public String getSoundState() {
        int delay = (int) getSoundTimer().period / 1000;
        String onOrOff = (shouldPlay() ? "ON" : "OFF");
        int numSound = getCurrentPlayingSounds().size();
        int permission = getPermission();
        String numSounds = (numSound > 0 ? (numSound == 1 ? "one sound" : (numSound + " sounds")) : "no sounds") + " currently playing";
        String delayS = (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds."));
        String perm = (permission > 0 ? (permission > 1 ? (permission > 2 ? (permission > 3 ?
                "Only the Broadcaster" :
                "Only Mods and the Broadcaster") :
                "Donators, Mods, and the Broadcaster") :
                "Subscribers, Donators, Mods, and the Broadcaster") :
                "Everyone")
                + " can play sounds.";
        return "Sound is currently turned " + onOrOff + " with " + numSounds + " with " + delayS + " " + perm;
    }
}
