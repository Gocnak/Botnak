package com.gocnak.thread.heartbeat;

import com.gocnak.gui.GUIMain;
import com.gocnak.util.Timer;

/**
 * Created by Nick on 11/15/2014.
 */
public class DonationCheck implements HeartbeatThread {

    private Timer toCheck;
    private boolean beating;

    public DonationCheck() {
        toCheck = new Timer(7500);
        beating = false;
    }

    @Override
    public boolean shouldBeat() {
        return GUIMain.currentSettings != null && GUIMain.currentSettings.donationManager != null
                && GUIMain.currentSettings.donationManager.canCheck()
                && GUIMain.currentSettings.donationManager.ranFirstCheck
                && !toCheck.isRunning()
                && !beating;
    }

    @Override
    public void beat() {
        beating = true;
        GUIMain.currentSettings.donationManager.checkDonations(true);
    }

    @Override
    public void afterBeat() {
        beating = false;
        toCheck.reset();
    }
}