package thread.heartbeat;

/**
 * Created by Nick on 7/31/2015.
 * <p>
 * This heartbeat thread checks the live channels you're following.
 * <p>
 * TODO: potentially hook up to alerting the user that a channel they follow went live
 */
public class FollowCheck implements HeartbeatThread {

    public FollowCheck() {

    }

    @Override
    public boolean shouldBeat() {
        return false;
    }

    @Override
    public void beat() {

    }

    @Override
    public void afterBeat() {

    }
}
