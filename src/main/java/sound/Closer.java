package sound;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides listener for {@link SoundEntry} to close it, when the music is finished.
 *
 * @author Dr. Kegel
 */
public class Closer implements LineListener {
    private SoundEntry entry;
    private ConcurrentHashMap<File, SoundEntry> map;
    /**
     * @param entry The entry to hook.
     */
    public Closer(final SoundEntry entry, ConcurrentHashMap<File, SoundEntry> map) {
        this.entry = entry;
        this.map = map;
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) { //this is the sound stopping itself
            this.entry.close();
        } else if (event.getType() == LineEvent.Type.CLOSE) {
            //this is the sound being closed
            //by either the line of code above, or the SoundEngine.stopSound command
            //we want to remove this listener and remove the sound from the map
            this.entry.getClip().removeLineListener(this);
            map.remove(entry.getKey());
        }
    }
}