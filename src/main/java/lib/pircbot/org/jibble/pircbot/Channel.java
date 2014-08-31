package lib.pircbot.org.jibble.pircbot;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Nick on 12/22/13.
 */
public class Channel {

    private HashSet<String> mods = new HashSet<>();
    private HashSet<String> subscribers = new HashSet<>();
    private String name = "";

    public Channel(String name) {
        this.name = name;
    }

    public synchronized boolean isMod(String user) {
        for (String s : mods) {
            if (user.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isSubscriber(User user) {
        for (String u : subscribers) {
            if (u.equals(user.getNick())) {
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
    public synchronized void addMods(String... mods) {
        Collections.addAll(this.mods, mods);
    }

    /**
     * Adds a subscriber name to the channel.
     *
     * @param name The name of the user to add.
     */
    public synchronized void addSubscriber(String name) {
        subscribers.add(name);
    }

    /**
     * Clear the channel of its mods and subscribers.
     */
    public synchronized void clear() {
        mods.clear();
        subscribers.clear();
    }


}
