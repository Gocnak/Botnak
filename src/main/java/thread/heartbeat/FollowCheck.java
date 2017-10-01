package thread.heartbeat;

import gui.BotnakTrayIcon;
import gui.forms.GUIMain;
import irc.account.Account;
import util.APIRequests;
import util.Timer;
import util.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 7/31/2015.
 * <p>
 * This heartbeat thread checks the live channels you're following.
 */
public class FollowCheck implements HeartbeatThread {

    public static CopyOnWriteArraySet<String> followers, followedChannels;
    private Timer toUpdate;
    private boolean beating, initialBeat;
    private int count = 0;

    public FollowCheck() {
        beating = false;
        initialBeat = true;
        followedChannels = new CopyOnWriteArraySet<>();
        followers = new CopyOnWriteArraySet<>();
        toUpdate = new Timer(20000L);
    }

    @Override
    public boolean shouldBeat() {
        return !beating && !toUpdate.isRunning();
    }

    private Account getUserAccount() {
        return Settings.accountManager.getUserAccount();
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

        if (getUserAccount() == null)
            return;

        if (Settings.trackFollowers.getValue()) {
            ArrayList<String> livePeople = APIRequests.Twitch.getLiveFollowedChannels(getUserAccount().getOAuth().getKey().split(":")[1]);
            if (!livePeople.isEmpty() && count != livePeople.size()) {
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
                count = livePeople.size();
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        }
        List<String> lastFollowers = APIRequests.Twitch.getLast20Followers(getUserAccount().getName());
        if (!lastFollowers.isEmpty())
        {
            lastFollowers.stream().filter(f -> !followers.contains(f)).forEach(newFollower -> {
                followers.add(newFollower);
                if (!initialBeat && BotnakTrayIcon.shouldDisplayNewFollowers())
                {
                    GUIMain.getSystemTrayIcon().displayNewFollower(newFollower);
                }
            });
        }
    }

    @Override
    public void afterBeat() {
        toUpdate.reset();
        beating = false;
    }
}