package lib.pircbot.org.jibble.pircbot;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Nick on 12/22/13.
 */
public class ChannelManager {

    private HashSet<Channel> channels = new HashSet<>();

    /**
     * Creates a blank ChannelManager.
     */
    public ChannelManager() {
        channels = new HashSet<>();
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
        if (channel != null)
            channels.remove(channel);
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
     * Updates a user to have a special tag (admin/staff/turbo/sub).
     * This will apply to all channels.
     *
     * @param userMode The usermode that took place.
     * @param nick     Recipient of the usermode event change.
     */
    public synchronized void updateUser(int userMode, String nick) {
        for (Channel c : channels) {
            User u = c.getUser(nick);
            if (u != null) {
                switch (userMode) {
                    case PircBot.ADMIN:
                        if (!u.isAdmin()) {
                            u.setAdmin(true);
                        }
                        break;
                    case PircBot.STAFF:
                        if (!u.isStaff()) {
                            u.setStaff(true);
                        }
                        break;
                    case PircBot.TURBO:
                        if (!u.isTurbo()) {
                            u.setTurbo(true);
                        }
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }


    /**
     * If a confirmed subscriber is found, this method handles finding and setting that user to true.
     *
     * @param channel The channel the user is in.
     * @param user    The name of the user.
     */
    public synchronized void handleSubscriber(String channel, String user) {
        if (getChannel(channel).getUser(user) == null) {
            getChannel(channel).addUser(new User(user));
        }
        User u = getChannel(channel).getUser(user);
        if (!u.isSubscriber()) u.setSubscriber(true);
    }

    /**
     * Remove a user from a specified channel or otherwise all channels in our memory.
     *
     * @param chnl The channel in which to remove the specified user.
     * @param nick The nick of the user to be removed.
     */
    public synchronized void removeUser(String chnl, String nick) {
        if (nick == null) return;
        if (chnl == null) {//all channels
            for (Channel c : channels) {
                c.removeUser(nick);
            }
        } else {//specified
            Channel c = getChannel(chnl);
            if (c != null) {
                c.removeUser(nick);
            }
        }
    }

    /**
     * Adds a user to the given channel. If the channel doesn't exist, the channel is made.
     *
     * @param channel The channel to add the user to.
     * @param user    The user to add.
     */
    public synchronized void addUser(String channel, User user) {
        //If it's adding a user to a channel that doesn't exist yet,
        //make the channel.
        if (channel == null || user == null) return;
        Channel c = getChannel(channel);
        if (c != null) {
            c.addUser(user);
        } else {
            addChannel(new Channel(channel, user));
        }
    }

    /**
     * Gets the user object of the given channel and name.
     *
     * @param channel The channel the user is in.
     * @param nick    The nick of the user to get.
     * @return The user if they exist, or null if not.
     */
    public synchronized User getUser(String channel, String nick) {
        if (getChannel(channel) != null) {
            return getChannel(channel).getUser(nick);
        }
        return null;
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
    }

}
