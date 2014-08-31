package lib.pircbot.org.jibble.pircbot;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Nick on 12/22/13.
 */
public class ChannelManager {

    private HashSet<Channel> channels = new HashSet<>();
    private HashSet<User> users = new HashSet<>();

    /**
     * Creates a blank ChannelManager.
     */
    public ChannelManager() {
    }

    /**
     * Adds a channel object to the Channels HashMap.
     *
     * @param toAdd The channel to add.
     */
    public synchronized void addChannel(Channel toAdd) {
        channels.add(toAdd);
    }

    /**
     * Removes a channel of a given name from the channels HashMap.
     *
     * @param name The name of the channel.
     */
    public synchronized void removeChannel(String name) {
        removeChannel(getChannel(name));
    }

    /**
     * Removes the channel from the channels HashSet.
     *
     * @param channel The channel object to remove.
     */
    public synchronized void removeChannel(Channel channel) {
        if (channel != null) {
            channel.clear();
            channels.remove(channel);
        }
    }

    /**
     * Gets the specified channel if it exists. The channel will not be created
     * if the channel does not exist.
     *
     * @param name The name of the channel.
     * @return The channel object if it exists, else null.
     */
    public synchronized Channel getChannel(String name) {
        if (!name.startsWith("#")) name = "#" + name;
        for (Channel c : channels) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Gets a user by name, and creates the user if non-existent.
     *
     * @param name   The name of the user.
     * @param create If true, create the user and add to the user list, else return null.
     * @return The user that either exists, was created if create is true, or null.
     */
    public synchronized User getUser(String name, boolean create) {
        for (User u : users) {
            if (u.getNick().equalsIgnoreCase(name)) {
                return u;
            }
        }
        if (create) {
            User u = new User(name);
            addUser(u);
            return u;
        } else {
            return null;
        }
    }

    /**
     * If a confirmed subscriber is found, this method handles finding and setting that user to true.
     *
     * @param channel The channel the user is in.
     * @param user    The name of the user.
     */
    public synchronized void handleSubscriber(String channel, String user) {
        getChannel(channel).addSubscriber(user);
    }

    /**
     * Adds a user to the global user list.
     *
     * @param user The user to add.
     */
    public synchronized void addUser(User user) {
        users.add(user);
    }

    /**
     * Removes a user from the global user list.
     *
     * @param u The user to remove.
     */
    public synchronized void removeUser(User u) {
        users.remove(u);
    }

    /**
     * Returns a list of users currently in the global user list.
     *
     * @return an array of users.
     */
    public User[] getUsers() {
        return users.toArray(new User[users.size()]);
    }

    /**
     * Gets all of the names of the channels we are currently in.
     *
     * @return The names of the channels we are in.
     */
    public synchronized String[] getChannelNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Channel c : channels) {
            names.add(c.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Disposes of the channel manager by clearing the channel objects.
     */
    public synchronized void dispose() {
        channels.clear();
        users.clear();
    }

}
