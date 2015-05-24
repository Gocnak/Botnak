package face;

/**
 * Created by Nick on 12/30/13.
 * Represents a subscriber icon on Twitch.
 */
public class SubscriberIcon {

    private String fileLoc = null;
    private String channel = null;

    public SubscriberIcon(String channel, String file) {
        this.channel = channel;
        fileLoc = file;
    }

    public String getChannel() {
        return channel;
    }

    public String getFileLoc() {
        return fileLoc;
    }
}