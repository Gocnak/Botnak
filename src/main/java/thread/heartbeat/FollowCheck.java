package thread.heartbeat;

import gui.BotnakTrayIcon;
import gui.forms.GUIMain;
import irc.account.Account;
import util.APIRequests;
import util.Timer;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 7/31/2015.
 * <p>
 * This heartbeat thread checks the live channels you're following.
 */
public class FollowCheck implements HeartbeatThread {

    public static CopyOnWriteArraySet<String> followedChannels;
    public static CopyOnWriteArraySet<String> followers;
    private Timer toUpdate;
    private boolean beating;
    private boolean initialBeat;

    public FollowCheck() {
        beating = false;
        initialBeat = true;
        followedChannels = new CopyOnWriteArraySet<>();
        followers = new CopyOnWriteArraySet<>();
        toUpdate = new Timer(10000L);
    }

    @Override
    public boolean shouldBeat() {
        return !beating && !toUpdate.isRunning();
    }

    private Account getUserAccount() {
        return GUIMain.currentSettings.accountManager.getUserAccount();
    }

    //called to initialize
    public void initialBeat() {
        beat();
        afterBeat();
        initialBeat = false;
    }

    @Override
    public void beat() {
        beating = true;
        ArrayList<String> livePeople = APIRequests.Twitch.getLiveFollowedChannels(getUserAccount().getKey().getKey().split(":")[1]);
        if (!livePeople.isEmpty()) {
            livePeople.forEach(p -> {
                if (!followedChannels.contains(p)) {
                    followedChannels.add(p);
                    if (!initialBeat && BotnakTrayIcon.shouldDisplayFollowedActivity()) {
                        GUIMain.getSystemTrayIcon().displayLiveChannel(p);
                    }
                }
            });
            followedChannels.removeIf(s -> !livePeople.contains(s));
            if (GUIMain.streams != null && GUIMain.streams.isVisible())
                GUIMain.streams.parseFollowed();
        }
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
        String[] lastFollowers = APIRequests.Twitch.getLast20Followers(getUserAccount().getName());
        if (lastFollowers.length > 0) {
            for (String follower : lastFollowers) {
                if (!followers.contains(follower)) {
                    followers.add(follower);
                    if (!initialBeat && BotnakTrayIcon.shouldDisplayNewFollowers()) {
                        GUIMain.getSystemTrayIcon().displayNewFollower(follower);
                    }
                }
            }
        }
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
        beating = false;
    }
}