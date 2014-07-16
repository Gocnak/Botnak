package sound;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple sound system.
 *
 * @author Dr. Kegel
 */
public final class SoundPlayer implements Closeable {

    /**
     * Provides strategies how to react, if being requested to play a sound file, which is already running.
     *
     * @author Dr. Kegel
     */
    public static enum PlayMode {
        /**
         * Restarts the sound from the beginning.
         */
        Restart,
        /**
         * Ignores the second play request.
         */
        Ignore,
        /**
         * Stops the first sound.
         */
        Toggle,
        /**
         * Plays both sounds.
         */
        Force,
    }

    //TODO: Debug counter
    /**
     * SoundEntries constructed
     */
    public static AtomicInteger sector = new AtomicInteger();
    /**
     * SoundEntries played
     */
    public static AtomicInteger sepl = new AtomicInteger();
    /**
     * SoundEntries closed (should be sector - number of sound files)
     */
    public static AtomicInteger secl = new AtomicInteger();

    /* immutable except counter */


    private final Map<File, SoundEntry> clips;

    /**
     * @param cacheSize The size of the player's cache.
     */
    public SoundPlayer(final int cacheSize) {
        this.clips = new LinkedHashMap<File, SoundEntry>(cacheSize, 0.75f, true) {
            private static final long serialVersionUID = -3892012877720705757L;

            @Override
            protected boolean removeEldestEntry(final Map.Entry<File, SoundEntry> eldest) {
                final boolean result = this.size() > cacheSize;
                if (result) {
                    if (eldest.getValue().getClip().isRunning()) {
                        new Closer(eldest.getValue());
                    } else eldest.getValue().close();
                }
                return result;
            }
        };
    }

    @Override
    public void close() {
        for (final SoundEntry clip : this.clips.values()) {
            clip.close();
        }
        this.clips.clear();
    }

    /**
     * Returns a {@code SoundEntry} instance based on the source file and {@link PlayMode} or creates a new one.
     *
     * @param file The source file.
     * @return a {@code SoundEntry} instance.
     * @throws IOException
     */
    private SoundEntry getClip(final File file) throws IOException {
        final SoundEntry result = new SoundEntry(file, this.clips.get(file));

        clips.put(file, result);
        return result;
    }

    /**
     * Returns a {@link Collection} with all playing files.
     *
     * @return a {@link Collection} with all playing files.
     */
    public Collection<SoundEntry> getPlayingSounds() {

        final Collection<SoundEntry> result = new ArrayList<>();

        for (final SoundEntry entry : this.clips.values()) {

            //TODO FIXME: add a splice method to SoundEntry that removes itself from the SoundEntry linked list if there's an error
            if (entry.getClip().isRunning()) {
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * Plays a sound file.
     *
     * @param file The sound file.
     * @param mode The strategy for handling the case that the sound is already playing.
     * @throws IOException
     */
    public void play(final File file, final PlayMode mode) throws IOException {
        this.getClip(file).play(mode);
    }


    /**
     * @param file The sound file.
     */
    public void stop(final File file) {
        final SoundEntry entry = this.clips.get(file);
        if (entry != null) {
            entry.stop();
        }
    }

}