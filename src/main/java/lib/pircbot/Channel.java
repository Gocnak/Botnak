package lib.pircbot;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 12/22/13.
 */
public class Channel {

    private CopyOnWriteArraySet<String> mods;
    private CopyOnWriteArraySet<String> subscribers;
    private ConcurrentHashMap<String, Integer> cheers;
    private String name;

    /**
     * Constructs a channel object of the given name.
     *
     * @param name The name to assign to the channel (includes the hashtag).
     */
    public Channel(String name) {
        this.name = name;
        this.mods = new CopyOnWriteArraySet<>();
        this.subscribers = new CopyOnWriteArraySet<>();
        this.cheers = new ConcurrentHashMap<>();
    }

    /**
     * Checks to see if a given user is a moderator of a channel.
     *
     * @param user The user to check.
     * @return True if the user is a mod, else false.
     */
    public boolean isMod(String user) {
        for (String s : mods) {
            if (user.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if a given user is a subscriber to a channel.
     *
     * @param u The user to check.
     * @return True if the user is a subscriber, else false.
     */
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
     *  Sets a user's cheer amount.
     * @param user   The user to set.
     * @param amount Their cheer amount.
     */
    public void setCheer(String user, int amount)
    {
        cheers.put(user, amount);
    }

    /**
     * Gets the cheer amount of bits this user has cheered, otherwise -1.
     *
     * @param user The user in question.
     * @return The amount of bits this user has cheered, otherwise -1.
     */
    public int getCheer(String user)
    {
        return cheers.containsKey(user) ? cheers.get(user) : -1;
    }

    /**
     * Clear the channel of its mods and subscribers.
     */
    public void clear() {
        mods.clear();
        subscribers.clear();
        cheers.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Channel && ((Channel) obj).getName().equals(this.getName()));
    }
}