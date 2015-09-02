package util.settings;

import gui.BotnakTrayIcon;
import gui.forms.GUIMain;
import irc.Donor;
import irc.message.Message;
import irc.message.MessageQueue;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import util.Response;
import util.Utils;
import util.misc.Donation;

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

    public boolean ranFirstCheck;
    private Donation lastDonation;
    private CopyOnWriteArraySet<Donor> donors;
    private CopyOnWriteArraySet<Donation> donations;
    private static NumberFormat CURRENCY_FORMAT, DECIMAL_FORMAT;

    //for displaying the numbers
    public static NumberFormat getCurrencyFormat() {
        if (CURRENCY_FORMAT == null) {
            CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
            CURRENCY_FORMAT.setMinimumFractionDigits(0);
            CURRENCY_FORMAT.setMaximumFractionDigits(2);
        }
        return CURRENCY_FORMAT;
    }

    //for saving the numbers
    public static NumberFormat getDecimalFormat() {
        if (DECIMAL_FORMAT == null) {
            DECIMAL_FORMAT = NumberFormat.getNumberInstance();
            DECIMAL_FORMAT.setMinimumFractionDigits(2);
            DECIMAL_FORMAT.setMaximumFractionDigits(2);
        }
        return DECIMAL_FORMAT;
    }

    public DonationManager() {
        lastDonation = null;
        ranFirstCheck = false;
        CURRENCY_FORMAT = null;
        DECIMAL_FORMAT = null;
        donors = new CopyOnWriteArraySet<>();
        donations = new CopyOnWriteArraySet<>();
    }

    public boolean canCheck() {
        return !getClientID().isEmpty() && !getAccessCode().isEmpty();
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

    public void addDonation(JSONObject tip, boolean isLocal) {
        addDonation(new Donation(tip.getString("_id"), tip.getString("username"), tip.getString("note"),
                tip.getDouble("amount"), Date.from(Instant.parse(tip.getString("date")))), isLocal);
    }

    public void addDonation(Donation d, boolean isLocal) {
        if (!donationsContains(d.getDonationID()) || isLocal) {
            if (donations.add(d)) {
                Donor don = getDonor(d.getFromWho());
                if (don == null) {
                    don = new Donor(d.getFromWho(), d.getAmount());
                    addDonor(don);
                } else {
                    don.addDonated(d.getAmount());
                }
                if (!isLocal) {
                    Settings.DONATIONS.save();
                    Settings.DONORS.save();
                    setLastDonation(d);
                    if (BotnakTrayIcon.shouldDisplayDonations()) {
                        GUIMain.getSystemTrayIcon().displayDonation(d);
                    }
                    MessageQueue.addMessage(new Message()
                            .setChannel(Settings.accountManager.getUserAccount().getName())
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

    public String getClientID() {
        return Settings.donationClientID.getValue();
    }

    public String getAccessCode() {
        return Settings.donationAuthCode.getValue();
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
            String line = Utils.createAndParseBufferedReader(new URL(url).openStream());
            if (!line.isEmpty()) {
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
                        addDonation(tip, false);
                    }
                }
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }

    public void scanInitialDonations(int passesCompleted) {
        String url = "https://streamtip.com/api/tips?client_id=" + getClientID() + "&access_token=" + getAccessCode() +
                "&limit=100&direction=asc";
        try {
            String offset = "&offset=" + String.valueOf(100 * passesCompleted);
            String line = Utils.createAndParseBufferedReader(new URL(url + offset).openStream());
            if (!line.isEmpty()) {
                JSONObject outerShell = new JSONObject(line);
                int status = outerShell.getInt("status");
                int count = outerShell.getInt("_count");
                if (count > 0) {
                    if (status == 200) { //ensure there's no problem with the site
                        JSONArray tipsArray = outerShell.getJSONArray("tips");
                        for (int i = 0; i < tipsArray.length(); i++) {
                            JSONObject tip = tipsArray.getJSONObject(i);
                            if (lastDonation != null) {
                                if (lastDonation.getDonationID().equals(tip.getString("_id"))) {
                                    continue;
                                }
                            }
                            addDonation(tip, true);
                            //we're simulating a local donation here to not spam the chat with all the donations
                        }
                        scanInitialDonations(passesCompleted + 1);
                    } else {
                        GUIMain.log("Failed to scan initial donations due to an error on Streamtip!");
                    }
                } else {
                    //finished!
                    GUIMain.log("Successfully scanned initial donations!");
                }
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }
}