package com.gocnak.util.settings;

import com.gocnak.gui.GUIMain;
import com.gocnak.irc.Donor;
import com.gocnak.irc.message.Message;
import com.gocnak.irc.message.MessageQueue;
import com.gocnak.lib.JSON.JSONArray;
import com.gocnak.lib.JSON.JSONObject;
import com.gocnak.util.Response;
import com.gocnak.util.misc.Donation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Currency;
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
    private static NumberFormat CURRENCY_FORMAT = null;

    public static NumberFormat getCurrencyFormat() {
        if (CURRENCY_FORMAT == null) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(2);
            CURRENCY_FORMAT = nf;
        }
        return CURRENCY_FORMAT;
    }
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
                    MessageQueue.addMessage(new Message()
                            .setChannel(GUIMain.currentSettings.accountManager.getUserAccount().getName())
                            .setType(Message.MessageType.DONATION_NOTIFY)
                            .setContent(String.format("%s has just donated %s! Lifetime total: %s ", d.getFromWho(),
                                    getCurrencyFormat().format(d.getAmount()), getCurrencyFormat().format(don.getDonated())))
                            .setExtra(d));
                }
            }
        }
    }

    public Response parseDonation(String[] lines) {
        Response toReturn = new Response();
        if (lines.length > 2) {
            String name = lines[1];
            try {
                Double amount = Double.parseDouble(lines[2]);
                if (amount > 0.0) {
                    addDonation(new Donation("LOCAL", name, "Added manually.", amount, java.util.Date.from(Instant.now())), true);
                    toReturn.setResponseText("Successfully added local donation for " + name + " !");
                    toReturn.wasSuccessful();
                } else {
                    toReturn.setResponseText("Failed to add donation, the amount must be greater than 0!");
                }
            } catch (Exception ignored) {
                toReturn.setResponseText("Failed to add donation, the amount must have a decimal point!");
            }
        } else {
            toReturn.setResponseText("Failed to add donation, usage: !adddonation (user) (amount)");
        }
        return toReturn;
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

    public void checkDonations(boolean single) {
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
                        try {
                            Currency c = Currency.getInstance(tip.getString("currencyCode"));
                            getCurrencyFormat().setCurrency(c);
                        } catch (Exception e) {
                            GUIMain.log("Unknown currency code: " + tip.getString("currencyCode"));
                            getCurrencyFormat().setCurrency(Currency.getInstance("USD"));
                        }
                        if (lastDonation != null) {
                            if (lastDonation.getDonationID().equals(tip.getString("_id"))) {
                                continue;
                            }
                        }
                        addDonation(tip);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}