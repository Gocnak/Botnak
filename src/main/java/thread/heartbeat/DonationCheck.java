package thread.heartbeat;

import util.Timer;
import util.settings.Settings;

/**
 * Created by Nick on 11/15/2014.
 */
public class DonationCheck implements HeartbeatThread {

    private Timer toCheck;
    private boolean beating;

    public DonationCheck() {
        toCheck = new Timer(10000L);
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return Settings.donationManager != null
                && Settings.donationManager.canCheck()
                && Settings.donationManager.ranFirstCheck
                && !toCheck.isRunning()
                && !beating;
    }

    @Override
    public void beat() {
        beating = true;
        Settings.donationManager.checkDonations(true);
    }

    @Override
    public void afterBeat() {
        beating = false;
        toCheck.reset();
    }
}