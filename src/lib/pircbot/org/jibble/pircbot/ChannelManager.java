package lib.pircbot.org.jibble.pircbot;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Nick on 12/22/13.
 */
public class ChannelManager {

    private HashSet<Channel> channels = new HashSet<>();

    public ChannelManager() {
        channels = new HashSet<>();
    }

    public synchronized void addChannel(Channel toAdd) {
        channels.add(toAdd);
    }

    public synchronized void removeChannel(String name) {
        removeChannel(getChannel(name));
    }

    public synchronized void removeChannel(Channel channel) {
        if (channel != null)
            channels.remove(channel);
    }

    public synchronized Channel getChannel(String name) {
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
                        u.setAdmin(true);
                        break;
                    case PircBot.STAFF:
                        u.setStaff(true);
                        break;
                    case PircBot.TURBO:
                        u.setTurbo(true);
                        break;
                    case PircBot.SUBSCRIBER:
                        //TODO
                        break;
                    default:
                        break;
                }
            }
        }
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

    public synchronized void renameUser(String oldNick, String newNick) {
        for (Channel c : channels) {
            User u = c.getUser(oldNick);
            if (u != null) {
                u.setNick(newNick);
            }
        }
    }

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


    public synchronized void dispose() {
        channels.clear();
    }

}
