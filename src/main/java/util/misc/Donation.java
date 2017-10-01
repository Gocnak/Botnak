package util.misc;

import util.settings.DonationManager;

import java.time.Instant;
import java.util.Date;

/**
 * Created by Nick on 11/22/2014.
 */
public class Donation implements Comparable<Donation> {

    private double amount;
    private String donationID, note, fromWho;
    private Date received;

    public Donation(String ID, String fromWho, String note, double amount, Date received) {
        this.amount = amount;
        this.donationID = ID;
        this.note = note;
        this.fromWho = fromWho;
        this.received = received;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDateReceived() {
        return received;
    }

    public String getDonationID() {
        return donationID;
    }

    public String getFromWho() {
        return fromWho;
    }

    public String getNote() {
        return note;
    }

    /**
     * We sort in descending order based on date. The latest donation should
     * match the one that DonationCheck checks every 5 seconds.
     *
     * @param o The other donation.
     * @return Negative if this donation is older, 0 if it's equal, and positive if
     * this donation is more recent than Donation o.
     */
    @Override
    public int compareTo(Donation o) {
        if (this.getDateReceived().after(o.getDateReceived())) {
            //this donation is newer, put the other behind
            return -1;
        }
        if (o.getDateReceived().equals(this.getDateReceived())) {//two donations at the exact same time?
            //just incase
            return 0;
        } else {//the other donation is more recent
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Donation && ((Donation) obj).getAmount() == this.getAmount()
                && ((Donation) obj).getDateReceived().equals(this.getDateReceived()) &&
                ((Donation) obj).getFromWho().equals(this.getFromWho()) && ((Donation) obj).getNote().equals(this.getNote()) &&
                ((Donation) obj).getDonationID().equals(this.getDonationID()));
    }

    @Override
    public String toString() {
        return donationID + "[" + fromWho + "[" + note + "[" + DonationManager.getDecimalFormat().format(amount)
                + "[" + Instant.ofEpochMilli(received.getTime()).toString();
    }
}