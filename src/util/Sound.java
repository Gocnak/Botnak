package util;

import gui.GUIMain;
import irc.IRCBot;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * The Sound Wrapper Class
 * <p/>
 * Constructed to make sound playing in Botnak a lot easier.
 */
public class Sound {

    private static final int BUFFER_SIZE = 320000;

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

    public void play() {
        File soundFile;                //could be > 1, either way it will play either index 0 or a random sound
        soundFile = new File(filePaths.data[Utils.r.nextInt(filePaths.data.length)]);
        if (!soundFile.exists() || soundFile.length() == 0) {
            return;
        }
        final AudioInputStream audioStream;
        try {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e) {
            System.out.println(soundFile.getName() + " doesn't want to give its InputStream for some reason.");
            return;
        }
        if (audioStream == null) {
            return;
        }
        AudioFormat audioFormat = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        final SourceDataLine sourceLine;
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        long frames = audioStream.getFrameLength();
        double durationInSeconds = (frames + 0.0) / audioFormat.getFrameRate();
        if (((int) durationInSeconds) * 1000 > IRCBot.soundTimer.period) {//we're checking the duration to see
            IRCBot.soundTimer = new Timer(((int) durationInSeconds) * 1000);//if it's a dev sound/really long sound
            IRCBot.soundBackTimer = new Timer(((int) durationInSeconds) * 1000);//so we can setup the backup timers
        }
        sourceLine.start();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int nBytesRead = 0;
                byte[] abData = new byte[BUFFER_SIZE];
                while (nBytesRead != -1 && !IRCBot.stopSound && !GUIMain.shutDown) {
                    try {
                        nBytesRead = audioStream.read(abData, 0, abData.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (nBytesRead >= 0) {
                        sourceLine.write(abData, 0, nBytesRead);
                    }
                }
                sourceLine.drain();
                sourceLine.close();
                try {
                    audioStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (!IRCBot.stopSound) {
            t.start();
        }
    }


}
