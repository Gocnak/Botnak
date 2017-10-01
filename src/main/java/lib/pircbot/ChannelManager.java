package lib.pircbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 12/22/13.
 */
public class ChannelManager {

    private Set<Channel> channels;
    private Set<User> users;

    /**
     * Creates a blank ChannelManager.
     */
    public ChannelManager() {
        channels = new CopyOnWriteArraySet<>();
        users = new CopyOnWriteArraySet<>();
    }

    /**
     * Adds a channel object to the Channels HashMap.
     *
     * @param toAdd The channel to add.
     */
    public void addChannel(Channel toAdd) {
        channels.add(toAdd);
    }

    /**
     * Removes a channel of a given name from the channels HashMap.
     *
     * @param name The name of the channel.
     */
    public void removeChannel(String name) {
        removeChannel(getChannel(name));
    }

    /**
     * Removes the channel from the channels HashSet.
     *
     * @param channel The channel object to remove.
     */
    public void removeChannel(Channel channel) {
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
    public Channel getChannel(String name) {
        if (name == null || "".equals(name)) return null;
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
    public User getUser(String name, boolean create) {
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
    public void handleSubscriber(String channel, String user) {
        Channel c = getChannel(channel);
        if (c != null) c.addSubscriber(user);
    }

    /**
     * Adds a user to the global user list.
     *
     * @param user The user to add.
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Removes a user from the global user list.
     *
     * @param u The user to remove.
     */
    public void removeUser(User u) {
        users.remove(u);
    }

    /**
     * Returns a list of users currently in the global user list.
     *
     * @return an array of users.
     */
    public User[] getUsers() {
        return users.stream().sorted().toArray(User[]::new);
    }

    public User[] getUsers(String subWord) {
        return users.stream().sorted().filter(s -> s.getLowerNick().startsWith(subWord)).toArray(User[]::new);
    }

    /**
     * Gets all of the names of the channels we are currently in.
     *
     * @return The names of the channels we are in.
     */
    public List<String> getChannelNames() {
        List<String> names = new ArrayList<>();
        channels.forEach(c -> names.add(c.getName()));
        return names;
    }

    /**
     * Disposes of the channel manager by clearing the channel objects.
     */
    public void dispose() {
        channels.clear();
        users.clear();
    }
}