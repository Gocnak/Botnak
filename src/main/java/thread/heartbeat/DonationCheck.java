package thread.heartbeat;

import gui.GUIMain;
import util.Timer;

/**
 * Created by Nick on 11/15/2014.
 */
public class DonationCheck implements HeartbeatThread {

    private Timer toCheck;

    public DonationCheck() {
        toCheck = new Timer(7500);
    }

    @Override
    public boolean shouldBeat() {
        return GUIMain.currentSettings != null && GUIMain.currentSettings.donationManager != null
                && GUIMain.currentSettings.donationManager.canCheck()
                && GUIMain.currentSettings.donationManager.ranFirstCheck
                && !toCheck.isRunning();
    }

    @Override
    public void beat() {
        GUIMain.currentSettings.donationManager.checkDonations(true);
    }

    @Override
    public void afterBeat() {
        toCheck.reset();
    }
}
