package sound;

import gui.GUIMain;
import util.Utils;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Created by Nick on 12/21/13.
 */
public class SoundThread extends Thread {

    private Sound sound = null;

    public Sound getSound() {
        return sound;
    }

    public SoundThread(Sound s) {
        sound = s;
    }


    private Clip clip = null;
    private AudioInputStream audioStream = null;

    public boolean isPlaying() {
        return playing;
    }

    private boolean playing = false;

    /**
     * A.K.A "Play"
     */
    @Override
    public synchronized void start() {
        File soundFile;//could be > 1, either way it will play either index 0 or a random sound
        soundFile = new File(sound.getSounds().data[Utils.nextInt(sound.getSounds().data.length)]);
        if (!Utils.areFilesGood(soundFile.getAbsolutePath())) {
            return;
        }
        try {
            clip = AudioSystem.getClip();
            audioStream = AudioSystem.getAudioInputStream(soundFile);
            if (!GUIMain.shutDown && SoundEngine.getEngine().shouldPlay()) {
                clip.open(audioStream);
                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (event.getType().equals(LineEvent.Type.STOP)) {
                            playing = false;
                        }
                    }
                });
                clip.start();
                playing = true;
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        super.start();
    }

    /**
     * The playing of the sound.
     * (Waiting for it to end)
     */
    @Override
    public void run() {
        while (!GUIMain.shutDown && playing && SoundEngine.getEngine().shouldPlay()) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
            super.run();
        }
    }

    /**
     * "Stop" playing the sound.
     */
    @Override
    public void interrupt() {
        if (clip != null && audioStream != null) {
            clip.stop();
            clip.flush();
            clip.close();
            try {
                audioStream.close();
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
            clip = null;
            audioStream = null;
            sound = null;
        }
        playing = false;
        super.interrupt();
    }
}
