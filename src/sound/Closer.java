package sound;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Provides listener for {@link SoundEntry} to close it, when the music is finished.
 *
 * @author Dr. Kegel
 */
public class Closer implements LineListener {
    private final SoundEntry entry;

    /**
     * @param entry The entry to hook.
     */
    public Closer(final SoundEntry entry) {
        this.entry = entry;

        if (this.entry.getClip().isRunning()) {
            this.entry.getClip().addLineListener(this);
        } else {
            this.entry.close();
        }
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            this.entry.getClip().removeLineListener(this);
            this.entry.close();
        }
    }
}
