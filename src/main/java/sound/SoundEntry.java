package sound;

import javax.sound.sampled.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

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
    public SoundEntry(final File file, final SoundEntry previous) throws IOException {
        try {
            this.key = file;
            this.previous = previous;
            this.source = AudioSystem.getAudioInputStream(file);

            final DataLine.Info info = new DataLine.Info(Clip.class, this.source.getFormat());

            this.clip = (Clip) AudioSystem.getLine(info);

            this.clip.open(this.source);

        } catch (final UnsupportedAudioFileException | LineUnavailableException e) {
            throw new IOException(e);
        }

        //sector.incrementAndGet(); TODO look into implementing a counter for how many of each sound is played
    }

    public void closePrevious() {
        if (previous != null) {
            previous.close();
            previous = null;
        }
    }

    @Override
    public void close() {
        if (this.clip.isActive()) {
            this.clip.stop();
        }
        if (this.clip.isOpen()) {
            this.clip.close();
        }
        try {
            this.source.close();

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
