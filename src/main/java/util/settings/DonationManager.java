package util.settings;

import gui.GUIMain;
import irc.Donor;
import irc.Message;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import util.misc.Donation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Nick on 11/22/2014.
 */
public class DonationManager {

    private Donation lastDonation = null;
    private CopyOnWriteArraySet<Donor> donors;
    private CopyOnWriteArraySet<Donation> donations;
    private String client_ID = "", access_code = "";
    public static char CURRENCY_SYMBOL = '$';//defaults to US Dollar
    public boolean ranFirstCheck = false;


    public DonationManager() {
        donors = new CopyOnWriteArraySet<>();
        donations = new CopyOnWriteArraySet<>();
    }

    public boolean canCheck() {
        return client_ID != null && access_code != null;
    }

    /**
     * Called from Settings#loadDonations() to give this class
     * the donations that you already know you have (saved locally).
     * <p>
     * You wouldn't want an alert for every donation you already know you have, right?
     *
     * @param d The HashSet of donations from loading through Settings.
     */
    public void fillDonations(HashSet<Donation> d) {
        donations.addAll(d);
    }

    public void addDonation(JSONObject tip) {
        addDonation(new Donation(tip.getString("_id"), tip.getString("username"), tip.getString("note"),
                tip.getDouble("amount"), Date.from(Instant.parse(tip.getString("date")))), false);
    }

    public void addDonation(Donation d, boolean isSub) {
        if (!donationsContains(d.getDonationID()) || isSub) {
            if (donations.add(d)) {
                Donor don = getDonor(d.getFromWho());
                if (don == null) {
                    don = new Donor(d.getFromWho(), d.getAmount());
                    addDonor(don);
                } else {
                    don.addDonated(d.getAmount());
                }
                GUIMain.currentSettings.saveDonations();
                GUIMain.currentSettings.saveDonors();
                if (isSub) GUIMain.currentSettings.saveSubscribers();
                else {
                    setLastDonation(d);
                    GUIMain.onMessage(new Message()
                            .setChannel(GUIMain.currentSettings.accountManager.getUserAccount().getName())
                            .setType(Message.MessageType.DONATION_NOTIFY)
                            .setContent(d.getFromWho() + " has just donated " + CURRENCY_SYMBOL + d.getAmount() + "!")
                            .setExtra(d));
                }
            }
        }
    }

    public void addDonor(Donor d) {
        donors.add(d);
    }

    public Donor getDonor(String name) {
        if (!donors.isEmpty()) {
            for (Donor d : donors) {
                if (d.getName().equalsIgnoreCase(name)) {
                    return d;
                }
            }
        }
        return null;
    }

    public boolean donationsContains(String tipID) {
        for (Donation d : donations) {
            if (d.getDonationID().equals(tipID)) return true;
        }
        return false;
    }

    public void setAccessCode(String access_code) {
        this.access_code = access_code;
    }

    public void setClientID(String client_ID) {
        this.client_ID = client_ID;
    }

    public String getClientID() {
        return client_ID;
    }

    public String getAccessCode() {
        return access_code;
    }

    public CopyOnWriteArraySet<Donor> getDonors() {
        return donors;
    }

    public CopyOnWriteArraySet<Donation> getDonations() {
        return donations;
    }

    public Donation getLastDonation() {
        return lastDonation;
    }

    public void setLastDonation(Donation d) {
        lastDonation = d;
    }

    public Donation checkDonations(boolean single) {
        Donation mostRecent = null;
        int limit = (single ? 5 : 100);
        String url = "https://streamtip.com/api/tips?client_id=" + getClientID() + "&access_token=" + getAccessCode() +
                "&limit=" + limit;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line = br.readLine();
            br.close();
            if (line != null) {
                JSONObject outerShell = new JSONObject(line);
                int status = outerShell.getInt("status");
                int count = outerShell.getInt("_count");
                if (status == 200) { //ensure there's no problem with the site
                    JSONArray tipsArray = outerShell.getJSONArray("tips");
                    for (int i = (single ? tipsArray.length() - 1 : count - 1); i > -1; i--) {
                        JSONObject tip = tipsArray.getJSONObject(i);
                        char cs = tip.getString("currencySymbol").charAt(0);
                        if (CURRENCY_SYMBOL != cs) CURRENCY_SYMBOL = cs;
                        if (lastDonation != null) {
                            if (lastDonation.getDonationID().equals(tip.getString("_id"))) {
                                mostRecent = lastDonation;
                                continue;
                            }
                        }
                        addDonation(tip);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return mostRecent;
    }
}