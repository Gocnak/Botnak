package lib.pircbot.org.jibble.pircbot;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Nick on 12/22/13.
 */
public class Channel {

    private HashSet<User> users = new HashSet<>();
    private String name = "";

    public Channel(String name) {
        this.name = name;
    }

    public Channel(String name, User... users) {
        this.name = name;
        Collections.addAll(this.users, users);
    }

    /**
     * Gets the user via the given nick.
     *
     * @param nick The nick of the user.
     * @return The user, or null if not found.
     */
    public User getUser(String nick) {
        for (User u : users) {
            if (u.getNick().equalsIgnoreCase(nick)) {
                return u;
            }
        }
        return null;
    }

    public void addUser(User u) {
        if (u != null) {
            users.add(u);
        }
    }

    public void removeUser(String nick) {
        removeUser(getUser(nick));
    }

    public void removeUser(User u) {
        if (u == null) return;
        users.remove(u);
    }

    /**
     * Gets the users in the channel.
     *
     * @return The users in the channel.
     */
    public User[] getUsers() {
        return users.toArray(new User[users.size()]);
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


}
