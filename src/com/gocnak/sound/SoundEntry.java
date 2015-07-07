package com.gocnak.sound;

import com.gocnak.gui.GUIMain;

import javax.sound.sampled.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a sound with its source and use counter.
 *
 * @author Dr. Kegel
 */
public class SoundEntry implements Closeable {
    private final File key;
    private SoundEntry previous;
    private final AudioInputStream source;
    private final Clip clip;

    /**
     * Instantiates a new {@link SoundEntry} based on a file.
     *
     * @param file The sound file.
     * @throws java.io.IOException
     */
    public SoundEntry(File file, SoundEntry previous, ConcurrentHashMap<File, SoundEntry> map) throws IOException {
        try {
            key = file;
            source = AudioSystem.getAudioInputStream(file);
            this.previous = previous;
            DataLine.Info info = new DataLine.Info(Clip.class, source.getFormat());

            clip = (Clip) AudioSystem.getLine(info);

            clip.addLineListener(new Closer(this, map));
            clip.open(source);
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            throw new IOException(e);
        }
        //sector.incrementAndGet(); TODO look into implementing a counter for how many of each sound is played
    }

    @Override
    public void close() {
        if (clip.isActive()) {
            clip.stop();
        }
        if (clip.isOpen()) {
            clip.flush();
            clip.drain();
            clip.close();
        }
        try {
            source.close();
        } catch (Exception ignored) {
        }

        // secl.incrementAndGet(); TODO count
        if (previous != null) {
            previous.close();
            previous = null;
        }
    }

    /**
     * Returns the audio clip.
     *
     * @return the audio clip.
     */
    public Clip getClip() {
        return this.clip;
    }

    /**
     * Returns the file.
     *
     * @return the file.
     */
    public File getKey() {
        return this.key;
    }

    /**
     * Plays this sound.
     *
     * @param mode Strategy for handling the case that the sound is already playing.
     */
    public void play(final SoundPlayer.PlayMode mode) {
        if (this.clip.isRunning()) {
            switch (mode) {
                case Ignore:
                    return;
                case Restart:
                    this.stop();
                    break;
                case Toggle:
                    this.stop();
                    return;
                default:
                    break;
            }
        } else {
            this.clip.setFramePosition(0);
        }
         FloatControl volume = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
            /** @author Chrisazy
             * I've decided that -75.0 is what qualifies as silent, so that's our baseline
             * We need to counter the logarithmic nature of gain, so we use Pow with base 10
             * We use it on 100 - volume setting because we're actually considering 0 gain to be 100%
             *      and -75.0 gain to be 0%
             * We subtract by 1 because we want Math.Pow(10,(100-100)) to be 0, instead of 1.
             * Lastly, we normalize our gain by multiplying by our "silent" level divided by the actual 0% level
             *      (Since without the normalization at 0% we get a gain of -9, that's out 0% level)
             * Shit's so cash.
             */
            float vol = -(float) ((75F / 9F) * (Math.pow(10, ((100 - GUIMain.currentSettings.soundVolumeGain) / 100)) - 1));
            volume.setValue(vol);
            this.clip.start();
            //sepl.incrementAndGet(); TODO counter
    }

    /**
     * Stops this sound.
     */
    public void stop() {
        this.clip.stop();
        this.clip.flush();
        this.clip.setFramePosition(0);
    }

}
