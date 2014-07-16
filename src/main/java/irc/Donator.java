package irc;

/**
 * Created by Nick on 1/26/14.
 * Created to handle the amounts a person donates.
 * This class was a better solution over PircBot Users.
 */
public class Donator {

    private double donated;
    private String name;

    public Donator(String nick, double amt) {
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
     * <p/>
     * $0.001 thru $9.99 = green
     * $10 thru $49.99 = bronze
     * $50 thru $99.99 = silver
     * $100 thru $499.99 = gold
     * $500+ = diamond
     *
     * @return The int status of the user, or -1 if they have none.
     */
    public int getDonationStatus() {
        if (donated > 0) {
            if (donated >= 10) {
                if (donated >= 50) {
                    if (donated >= 100) {
                        if (donated >= 500) {
                            return 10;
                        }
                        return 9;
                    }
                    return 8;
                }
                return 7;
            }
            return 6;
        }
        return -1;
    }

    /**
     * Adds the specified amount to the donator's donated amount.
     *
     * @param toAdd The amount to add.
     */
    public void addDonated(double toAdd) {
        donated += toAdd;
    }
}
