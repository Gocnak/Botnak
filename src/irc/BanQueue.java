package irc;

import gui.GUIMain;

import java.util.HashSet;

/**
 * Created by Nick on 12/31/13.
 */
public class BanQueue extends Thread {

    private HashSet<User> banMap;
    User nilUser;

    public BanQueue() {
        nilUser = new User(null, null, -1);
        banMap = new HashSet<>();
    }

    @Override
    public synchronized void start() {
        banMap = new HashSet<>();
        super.start();
    }

    @Override
    public void interrupt() {
        banMap.clear();
        super.interrupt();
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown && GUIMain.viewer != null) {
            emptyMap();
            try {
                Thread.sleep(5000);
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void emptyMap() {
        if (!banMap.isEmpty()) {
            for (User u : banMap) {
                if (u.count > 1) {
                    GUIMain.onBan(u.channel, u.name + " has been banned/timed out " + u.count + " times!");
                } else {
                    GUIMain.onBan(u.channel, u.name + " has been banned/timed out!");
                }
            }
            banMap.clear();
        }
    }

    /**
     * Adds the ban of the given name to the map.
     *
     * @param name The name of the user.
     */
    public synchronized void addToMap(String channel, String name) {
        User u = getUser(name);
        if (!u.isNilUser()) {
            u.increment();
        } else {
            banMap.add(new User(channel, name, 1));
        }
    }

    public synchronized User getUser(String name) {
        if (!banMap.isEmpty()) {
            for (User u : banMap) {
                if (u.name.equals(name)) {
                    return u;
                }
            }
        }
        return nilUser;
    }

    class User {

        String name, channel;
        int count;

        public User(String channel, String name, int count) {
            this.channel = channel;
            this.name = name;
            this.count = count;
        }

        public void increment() {
            count++;
        }

        public boolean isNilUser() {
            return (channel == null && name == null && count == -1);
        }
    }


}
