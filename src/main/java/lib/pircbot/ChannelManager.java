package lib.pircbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

/**
 * Created by Nick on 12/22/13.
 */
public class ChannelManager {

    private Set<Channel> channels;
    private Map<Long, User> users;

    /**
     * Creates a blank ChannelManager.
     */
    public ChannelManager() {
        channels = new CopyOnWriteArraySet<>();
        users = new ConcurrentHashMap<>();
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
    public User getUser(final String name, final boolean create)
    {
        User found = users.values().stream().filter(u -> u.getNick().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (found == null && create)
        {
            found = new User(name);
            addUser(found);
        }
        return found;
    }


    public User getUser(final long ID, final boolean create)
    {
        User u = users.get(ID);
        if (u == null && create)
        {
            u = new User(ID);
            addUser(u);
        }
        return u;
    }
    /**
     * If a confirmed subscriber is found, this method handles finding and setting that user to true.
     *
     * @param channel The channel the user is in.
     * @param userID    The ID of the user.
     */
    public void handleSubscriber(String channel, long userID)
    {
        Channel c = getChannel(channel);
        if (c != null) c.addSubscriber(userID);
    }

    /**
     * Adds a user to the global user list.
     *
     * @param user The user to add.
     */
    public void addUser(User user) {
        users.put(user.getUserID(), user);
    }

    /**
     * Removes a user from the global user list.
     *
     * @param u The user to remove.
     */
    public void removeUser(User u) {
        users.remove(u.getUserID());
    }

    /**
     * Returns a list of users currently in the global user list.
     *
     * @return a Stream of users.
     */
    public Stream<User> getUsers()
    {
        return users.values().stream().sorted();
    }

    public User[] getUsers(String subWord) {
        return users.values().stream().sorted().filter(s -> s.getLowerNick().startsWith(subWord)).toArray(User[]::new);
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