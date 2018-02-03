package util.settings;

import gui.BotnakTrayIcon;
import gui.forms.GUIMain;
import irc.Subscriber;
import irc.account.OAuth;
import irc.message.Message;
import irc.message.MessageQueue;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.User;
import sound.SoundEngine;
import util.Utils;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Nick on 11/28/2014.
 * <p>
 * This class keeps track of subscribers of your own channel, along
 * with providing the date of which Botnak recognized them to have subbed.
 * <p>
 * Every time their sub status is called, this manager will tell Botnak
 * how long they have been subbed for, so when one month after they subbed
 * comes around, Botnak will know that it needs to up the donation by $2.50.
 */
public class SubscriberManager {

    private Map<Long, Subscriber> subscribers;

    private Subscriber lastSubscriber = null;

    public void setLastSubscriber(Subscriber lastSubscriber) {
        this.lastSubscriber = lastSubscriber;
    }

    public Subscriber getLastSubscriber() {
        return lastSubscriber;
    }

    public List<Subscriber> getLastSubscribers(int count) {
        return subscribers.values().stream().sorted().limit(count).collect(Collectors.toList());
    }

    public Map<Long, Subscriber> getSubscribers()
    {
        return subscribers;
    }

    public SubscriberManager() {
        subscribers = new ConcurrentHashMap<>();
    }

    public Optional<Subscriber> getSubscriber(final long subID)
    {
        return Optional.ofNullable(subscribers.get(subID));
    }

    /**
     * How's our little friend doing?
     * <p>
     * Called from the Channel class of Pircbot, this method
     * updates the sub's status (donation-wise) based on how
     * many months it's been since they first subbed, and checks
     * to see if the person subscribed while offline (new or not).
     *
     * @param u               The user object of the potential subscriber.
     * @param channel         Your channel name, for the messages.
     * @param currentlyActive Boolean used to determine current sub status of the user.
     */
    public void updateSubscriber(User u, String channel, boolean currentlyActive) {
        if (u.getNick().equalsIgnoreCase(Settings.accountManager.getUserAccount().getName()))
            return;
        //you will always be your own sub, silly

        Optional<Subscriber> s = getSubscriber(u.getUserID());
        if (s.isPresent()) {
            if (s.get().isActive()) {
                int streak = s.get().getStreak();
                int monthsSince = (int) (s.get().getStarted().until(LocalDateTime.now(), ChronoUnit.DAYS) / 32);
                if (monthsSince > streak) {
                    if (currentlyActive) {
                        String content = u.getDisplayName() + " has continued their subscription for over "
                                + (monthsSince) + ((monthsSince) > 1 ? " months!" : " month!");
                        MessageQueue.addMessage(new Message().setChannel(channel).setType(Message.MessageType.SUB_NOTIFY).setContent(content));
                        s.get().incrementStreak(monthsSince - streak);//this will most likely be 1
                        playSubscriberSound();
                    } else {//we're offering a month to re-sub
                        s.get().setActive(false);
                        s.get().resetStreak();
                    }
                }
            } else {
                if (currentlyActive) {
                    // this has the potential to be an offline re-sub:
                    // botnak will know that the sub is currently alive if it catches it live, (see the other use of SUB_NOTIFY)
                    // however if the person subscribes offline, botnak has no way of telling, and
                    // the next time they talk is the only time Botnak (and perhaps you as well) knows for sure that they did
                    // so, we need to update the date the user subbed to now, ensure their streak is reset, and
                    // make botnak send a "thanks for subbing offline" message

                    //or twitchnotify could have been a douchenozzle and did not send the message
                    String content = u.getDisplayName() + " has RE-subscribed offline!";
                    if (Settings.botAnnounceSubscribers.getValue()) {
                        Settings.accountManager.getBot().sendMessage(channel, ".me " + u.getNick() + " has just RE-subscribed!");
                    }
                    MessageQueue.addMessage(new Message().setContent(content).setType(Message.MessageType.SUB_NOTIFY).setChannel(channel));
                    s.get().resetStreak();
                    s.get().setStarted(LocalDateTime.now());
                    s.get().setActive(true);
                    playSubscriberSound();
                    setLastSubscriber(s.get());
                    notifyTrayIcon(content, false);
                }
            }
        } else {
            if (currentlyActive) {
                // this is a new, offline sub. Botnak is going to throw a new sub message just
                // as if they had subbed the instant they sent the message
                //or twitchnotify could have been a douchenozzle and did not send the message
                if (Settings.botAnnounceSubscribers.getValue()) {
                    Settings.accountManager.getBot().sendMessage(channel, ".me " + u.getNick() + " has just subscribed!");
                }
                String content = u.getNick().toLowerCase() + " has subscribed offline!";
                MessageQueue.addMessage(new Message().setContent(content).setType(Message.MessageType.SUB_NOTIFY).setChannel(channel));
                addSub(new Subscriber(u.getUserID(), LocalDateTime.now(), true, 0));
                playSubscriberSound();
                notifyTrayIcon(content, false);
            }
        }
    }

    public void fillSubscribers(Set<Subscriber> set)
    {
        set.forEach(sub -> subscribers.put(sub.getSubscriberID(), sub));
    }

    public void addSub(Subscriber s) {
        subscribers.put(s.getSubscriberID(), s);
        setLastSubscriber(s);
    }

    public boolean addNewSubscriber(User newSub, String channel)
    {
        Optional<Subscriber> subscriber = getSubscriber(newSub.getUserID());
        if (!subscriber.isPresent()) {//brand spanking new sub, live as botnak caught it
            addSub(new Subscriber(newSub.getUserID(), LocalDateTime.now(), true, 0));
            notifyTrayIcon(newSub.getDisplayName() + " has just subscribed!", false);
            playSubscriberSound();
            //we're going to return false (end of method) so that Botnak generates the message
            //like it did before this manager was created and implemented (and because less of the same code is better eh?)
        } else if (subscriber.get().isActive()) {
            //this may have been twitchnotify telling us twice, discard without messing up sounds
            return true;
        } else {
            //re-sub!
            //if we got the message, this means a month has passed, and they cancelled
            //question: should the streak still matter, if they're quick enough to resub?
            //answer: Botnak automatically acknowledges them for their continued support anyways, and
            // if they decide to cancel just to get the notification again, they deserve their streak to be reset
            String content = newSub.getDisplayName() + " has just RE-subscribed!";
            MessageQueue.addMessage(new Message().setContent(content).setChannel(channel).setType(Message.MessageType.SUB_NOTIFY));
            notifyTrayIcon(content, false);
            playSubscriberSound();
            subscriber.get().resetStreak();
            subscriber.get().setStarted(LocalDateTime.now());
            subscriber.get().setActive(true);
            setLastSubscriber(subscriber.get());
            return true;
        }
        return false;
    }

    private void playSubscriberSound() {
        if (Settings.loadedSubSounds)
            SoundEngine.getEngine().playSpecialSound(true);
    }

    public void notifyTrayIcon(String content, boolean continuation) {
        if (BotnakTrayIcon.shouldDisplayNewSubscribers()) {
            GUIMain.getSystemTrayIcon().displaySubscriber(content, continuation);
        }
    }


    public void scanInitialSubscribers(String channel, OAuth key, int passesCompleted, Set<Subscriber> set)
    {
        String oauth = key.getKey().split(":")[1];
        String urlString = "https://api.twitch.tv/kraken/channels/" + channel + "/subscriptions?oauth_token=" +
                oauth + "&limit=100";
        String offset = "&offset=" + String.valueOf(100 * passesCompleted);
        urlString += offset;
        try {
            String line = Utils.createAndParseBufferedReader(new URL(urlString).openStream());
            if (!line.isEmpty()) {
                JSONObject entire = new JSONObject(line);
                if (entire.has("error")) {
                    GUIMain.log("Error scanning for initial subs, does your OAuth key allow for this?");
                } else {
                    int total = entire.getInt("_total");
                    int passes = (total > 100 ? (int) Math.ceil((double) total / 100.0) : 1);
                    if (passes == passesCompleted) {
                        fillSubscribers(set);
                        GUIMain.log("Successfully scanned " + set.size() + " subscriber(s)!");
                        Settings.scannedInitialSubscribers.setValue(true);
                    } else {
                        JSONArray subs = entire.getJSONArray("subscriptions");
                        for (int subIndex = 0; subIndex < subs.length(); subIndex++) {
                            JSONObject outer = subs.getJSONObject(subIndex);
                            JSONObject user = outer.getJSONObject("user");
                            String name = user.getString("name");
                            long ID = user.getLong("_id");
                            if (name.equalsIgnoreCase(channel)) continue;//don't want to add yourself
                            LocalDateTime started = LocalDateTime.parse(outer.getString("created_at"), DateTimeFormatter.ISO_DATE_TIME);
                            int streak = (int) started.until(LocalDateTime.now(), ChronoUnit.MONTHS);
                            Subscriber s = new Subscriber(ID, started, true, streak);
                            set.add(s);
                        }
                        scanInitialSubscribers(channel, key, passesCompleted + 1, set);
                    }
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("422")) {
                //the user does not have a sub button
                key.setCanReadSubscribers(false);
                GUIMain.log("Failed to parse subscribers; your channel is not partnered!");
            } else GUIMain.log(e);
        }
    }
}