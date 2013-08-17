package util;

import gui.GUIMain;
import irc.IRCBot;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * The Sound Wrapper Class
 * <p/>
 * Constructed to make sound playing in Botnak a lot easier.
 */
public class Sound {


    /**
     * All users may use the sound
     */
    public static final int PERMISSION_ALL = 0;

    /**
     * Only mods and the person running Botnak can use the sound
     */
    public static final int PERMISSION_MOD = 1;

    /**
     * Only the person running Botnak can use the sound
     */
    public static final int PERMISSION_DEV = 2;

    private final int userPermission;
    private final StringArray filePaths;

    /**
     * Construct the sounds here. The Sound info itself should be stored here.
     *
     * @param permission The permission level for the sound. Use the PERMISSION constants.
     * @param file       The file path(s) of the sound file(s).
     */
    public Sound(int permission, String... file) {
        userPermission = permission;
        filePaths = new StringArray(file);
    }

    /**
     * Constructs a sound with default PERMISSION_ALL.
     *
     * @param files The file path(s) of the sound(s).
     */
    public Sound(String... files) {
        this(PERMISSION_ALL, files);
    }


    public StringArray getSounds() {
        return filePaths;
    }

    public int getPermission() {
        return userPermission;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Sound && (Arrays.equals(getSounds().data, ((Sound) other).getSounds().data))
                && (userPermission == ((Sound) other).getPermission());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getSounds().data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PERMISSION: ");
        sb.append(getPermission());
        sb.append(" WITH FILES: ");
        for (String s : getSounds().data) {
            sb.append(s);
        }
        return sb.toString();
    }

    private Clip clip = null;
    private AudioInputStream audioStream = null;

    public void stop() {
        if (clip != null && audioStream != null) {
            clip.stop();
            clip.flush();
            clip.close();
            try {
                audioStream.close();
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isActive();
    }

    public void play() {
        File soundFile;                //could be > 1, either way it will play either index 0 or a random sound
        soundFile = new File(filePaths.data[Utils.r.nextInt(filePaths.data.length)]);
        if (!Utils.areFilesGood(soundFile.getAbsolutePath())) {
            return;
        }
        try {
            clip = AudioSystem.getClip();
            audioStream = AudioSystem.getAudioInputStream(soundFile);
            if (!GUIMain.shutDown && !IRCBot.stopSound) {
                clip.open(audioStream);
                clip.start();
                long frames = audioStream.getFrameLength();
                double durationInSeconds = (frames + 0.0) / clip.getFormat().getFrameRate();
                if (IRCBot.soundTimer.period != 0) {//if being raided/allotting spam, ignore the following code
                    if (((int) durationInSeconds) * 1000 > IRCBot.soundTimer.period) {//we're checking the duration to see
                        IRCBot.soundTimer = new Timer(((int) durationInSeconds) * 1000);//if it's a dev sound/really long sound
                        IRCBot.soundBackTimer = new Timer(((int) durationInSeconds) * 1000);//so we can setup the backup timers
                    }
                }
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }
}
