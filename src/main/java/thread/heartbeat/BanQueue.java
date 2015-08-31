package thread.heartbeat;

import gui.forms.GUIMain;
import irc.message.Message;
import irc.message.MessageQueue;
import util.Timer;
import util.settings.Settings;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Nick on 8/29/2014.
 */
public class BanQueue implements HeartbeatThread {

    private static HashSet<User> banMap;
    private boolean beating;

    public BanQueue() {
        banMap = new HashSet<>();
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return !beating && !banMap.isEmpty();
    }

    @Override
    public void beat() {
        beating = true;
        emptyMap();
    }

    @Override
    public void afterBeat() {
        beating = false;
    }

    /**
     * Adds the ban of the given name to the map.
     *
     * @param name The name of the user.
     */
    public static synchronized void addToMap(String channel, String name) {
        User u = getUser(name);
        if (u != null) {
            u.increment();
        } else {
            banMap.add(new User(channel, name, 1));
        }
    }

    private synchronized void emptyMap() {
        Iterator<User> it = banMap.iterator();
        while (!GUIMain.shutDown && it.hasNext()) {
            User u = it.next();
            if (!u.timer.isRunning()) {
                String name = u.name.equalsIgnoreCase(Settings.accountManager.getViewer().getNick()) ?
                        ("You have ") : (u.name + " has ");
                if (u.count > 1) {
                    MessageQueue.addMessage(new Message().setChannel(u.channel.substring(1))
                            .setType(Message.MessageType.BAN_NOTIFY)
                            .setContent(name + "been banned/timed out " + u.count + " times!"));
                } else {
                    MessageQueue.addMessage(new Message().setChannel(u.channel.substring(1))
                            .setType(Message.MessageType.BAN_NOTIFY).setContent(name + "been banned/timed out!"));
                }
                it.remove();
            }
        }
    }

    private static synchronized User getUser(String name) {
        if (!banMap.isEmpty()) {
            for (User u : banMap) {
                if (u.name.equals(name)) {
                    return u;
                }
            }
        }
        return null;
    }

    private static class User {

        Timer timer;
        String name, channel;
        int count;

        public User(String channel, String name, int count) {
            timer = new Timer(3500);
            this.channel = channel;
            this.name = name;
            this.count = count;
        }

        public void increment() {
            count++;
            timer.setEndIn(1500);
        }
    }
}