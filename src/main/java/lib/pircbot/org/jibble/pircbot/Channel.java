package lib.pircbot.org.jibble.pircbot;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 12/22/13.
 */
public class Channel {

    private CopyOnWriteArraySet<String> mods = new CopyOnWriteArraySet<>();
    private CopyOnWriteArraySet<String> subscribers = new CopyOnWriteArraySet<>();
    private String name = "";

    public Channel(String name) {
        this.name = name;
    }

    public boolean isMod(String user) {
        for (String s : mods) {
            if (user.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSubscriber(User u) {
        for (String s : subscribers) {
            if (s.equals(u.getNick().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the name of the channel.
     * INCLUDES THE HASHTAG!
     *
     * @return The name of the channel with the hashtag included.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the username of the channel.
     *
     * @return The name of the channel without the hashtag.
     */
    public String getUserName() {
        return name.substring(1);
    }

    /**
     * Adds a multitude of mods to the mod list.
     *
     * @param mods The mod names to add.
     */
    public void addMods(String... mods) {
        Collections.addAll(this.mods, mods);
    }

    /**
     * Adds a subscriber name to the channel.
     *
     * @param sub The subscriber to add.
     */
    public void addSubscriber(String sub) {
        subscribers.add(sub);
    }

    /**
     * Clear the channel of its mods and subscribers.
     */
    public void clear() {
        mods.clear();
        subscribers.clear();
    }
}