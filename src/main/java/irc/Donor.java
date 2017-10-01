package irc;

import face.IconEnum;
import util.settings.DonationManager;

/**
 * Created by Nick on 1/26/14.
 * Created to handle the amounts a person donates.
 * This class was a better solution over PircBot Users.
 */
public class Donor {

    private double donated;
    private String name;

    public Donor(String nick, double amt) {
        name = nick;
        donated = amt;
    }

    /**
     * Gets the amount this person has donated.
     *
     * @return The double amount the person has donated.
     */
    public double getDonated() {
        return donated;
    }

    /**
     * Gets the name of the donator.
     *
     * @return The name of the donator.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the donation status of the user. (Used by ChatPanes)
     * <p>
     * $0.001 thru $9.99 = green
     * $10 thru $49.99 = bronze
     * $50 thru $99.99 = silver
     * $100 thru $499.99 = gold
     * $500+ = diamond
     *
     * TODO make this user specified
     *
     * @param donated The amount donated. Most of the time this is
     * @return The int status of the user, or -1 if they have none.
     */
    public static IconEnum getDonationStatus(Double donated) {
        if (donated >= 500) return IconEnum.DONOR_INSANE;
        else if (donated >= 100) return IconEnum.DONOR_HIGH;
        else if (donated >= 50) return IconEnum.DONOR_MEDIUM;
        else if (donated >= 10) return IconEnum.DONOR_LOW;
        else if (donated > 0) return IconEnum.DONOR_BASIC;
        return IconEnum.NONE;
    }

    public static IconEnum getCheerStatus(int amount)
    {
        if (amount >= 100000) return IconEnum.CHEER_100K;
        else if (amount >= 10000) return IconEnum.CHEER_10K_99K;
        else if (amount >= 5000) return IconEnum.CHEER_5K_9K;
        else if (amount >= 1000) return IconEnum.CHEER_1K_4K;
        else if (amount >= 100) return IconEnum.CHEER_100_999;
        else if (amount > 0) return IconEnum.CHEER_1_99;
        return IconEnum.NONE;
    }

    public static IconEnum getCheerAmountStatus(int amount)
    {
        if (amount >= 10000) return IconEnum.CHEER_BIT_AMT_RED;
        else if (amount >= 5000) return IconEnum.CHEER_BIT_AMT_BLUE;
        else if (amount >= 1000) return IconEnum.CHEER_BIT_AMT_GREEN;
        else if (amount >= 100) return IconEnum.CHEER_BIT_AMT_PURPLE;
        else if (amount > 0) return IconEnum.CHEER_BIT_AMT_GRAY;
        return IconEnum.NONE;
    }

    /**
     * Adds the specified amount to the donator's donated amount.
     *
     * @param toAdd The amount to add.
     */
    public void addDonated(double toAdd) {
        donated += toAdd;
    }

    @Override
    public String toString() {
        return name + "," + DonationManager.getDecimalFormat().format(donated);
    }
}