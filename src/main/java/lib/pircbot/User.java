/*
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

*/

package lib.pircbot;

import face.IconEnum;
import irc.Donor;
import util.settings.Settings;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class is used to represent a user on an IRC server.
 * Instances of this class are returned by the getUsers method
 * in the PircBot class.
 * <p>
 * Note that this class no longer implements the Comparable interface
 * for Java 1.1 compatibility reasons.
 *
 * @author Paul James Mutton,
 *         <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version 1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 * @since 1.0.0
 */
public class User implements Comparable<User> {

    private boolean staff = false, admin = false, global_mod = false, turbo = false, prime = false, verified = false;
    private String _nick, _lowerNick, displayName = null;

    private CopyOnWriteArraySet<Integer> emotes = new CopyOnWriteArraySet<>();

    private Color color = null;
    private Donor donor = null;

    /**
     * Constructs a User object with a known prefix and nick.
     *
     * @param nick The nick of the user.
     */
    public User(String nick) {
        _nick = nick;
        _lowerNick = nick.toLowerCase();
    }

    /**
     * Returns the prefix of the user. If the User object has been obtained
     * from a list of users in a channel, then this will reflect the user's
     * status in that channel.
     *
     * @return The prefix of the user. If there is no prefix, then an empty
     * String is returned.
     */
    public String getPrefix() {
        StringBuilder foxStevenson = new StringBuilder();
        if (isTurbo()) {
            foxStevenson.append(" [Turbo]");
        }
        if (isGlobalMod()) {
            foxStevenson.append(" [Global Mod]");
        }
        if (isAdmin()) {
            foxStevenson.append(" [Admin]");
        }
        if (isStaff()) {
            foxStevenson.append(" [Staff]");
        }
        return foxStevenson.toString();
    }


    /**
     * Returns whether or not the user represented by this object is an
     * operator. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's operator status in
     * that channel.
     *
     * @return true if the user is an operator in the channel.
     */
    public boolean isOp(String channel) {
        Channel c = Settings.channelManager.getChannel(channel);
        return c != null && c.isMod(getNick());
    }

    public boolean isGlobalMod() {
        return global_mod;
    }

    public void setGlobalMod(boolean newBool) {
        global_mod = newBool;
    }
    /**
     * Checks to see if the user is an Admin on Twitch.
     *
     * @return true if they are an admin.
     */
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean newBool) {
        admin = newBool;
    }

    /**
     * Checks to see if the user is a part of the Staff of Twitch.
     *
     * @return true if they are a staff member.
     */
    public boolean isStaff() {
        return staff;
    }

    public void setStaff(boolean newBool) {
        staff = newBool;
    }

    /**
     * Checks to see if a user has twitch turbo.
     */
    public boolean isTurbo() {
        return turbo;
    }

    public void setTurbo(boolean newBool) {
        turbo = newBool;
    }


    /**
     * Checks to see if a user has Twitch Prime.
     */
    public boolean isPrime()
    {
        return prime;
    }

    public void setPrime(boolean bState)
    {
        prime = bState;
    }

    /**
     * Checks if a user is a verified Twitch Partner
     */
    public boolean isVerified()
    {
        return verified;
    }

    public void setVerified(boolean bState)
    {
        verified = bState;
    }

    /**
     * Checks to see if the user is a subscriber of the channel.
     *
     * @return true if the user is a subscriber of the channel, else false.
     */
    public boolean isSubscriber(String channel) {
        Channel c = Settings.channelManager.getChannel(channel);
        return c != null && c.isSubscriber(this);
    }

    public boolean isDonor() {
        donor = Settings.donationManager.getDonor(getNick());
        return donor != null;
    }

    public int getCheer(String channel)
    {
        Channel c = Settings.channelManager.getChannel(channel);
        return c.getCheer(getLowerNick());
    }
    public void addEmote(int emote) {
        this.emotes.add(emote);
    }

    public CopyOnWriteArraySet<Integer> getEmotes() {
        return emotes;
    }

    public IconEnum getDonationStatus() {
        return Donor.getDonationStatus(donor.getDonated());
    }

    public double getDonated() {
        return donor.getDonated();
    }

    /**
     * Returns the nick of the user.
     *
     * @return The user's nick.
     */
    public String getNick() {
        return _nick;
    }

    public String getLowerNick() {
        return _lowerNick;
    }

    public void setNick(String newNick) {
        _nick = newNick;
        _lowerNick = _nick.toLowerCase();
    }

    public String getDisplayName() {
        return displayName == null ? getLowerNick() : displayName;
    }

    /**
     * Since Twitch added Weaboo support, we need to determine if this name
     * is a Japanese/Korean/Chinese name. If it is, show their nick next to their name, otherwise,
     * set their name as is.
     */
    public void setDisplayName(String name) {
        if (displayName == null) {
            boolean displayIsNick = name.equalsIgnoreCase(_nick);
            displayName = displayIsNick ? name : name + " (" + _nick + ")";
        }
    }

    /**
     * @return The chat color of the user.
     */
    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    /**
     * Returns the nick of the user complete with their prefix if they
     * have one, e.g. "@Dave".
     *
     * @return The user's prefix and nick.
     */
    public String toString() {
        return getPrefix() + " " + getNick();
    }


    /**
     * Returns true if the nick represented by this User object is the same
     * as the argument. A case insensitive comparison is made.
     *
     * @return true if the nicks are identical (case insensitive).
     */
    public boolean equals(String nick) {
        return nick.toLowerCase().equals(_lowerNick);
    }


    /**
     * Returns true if the nick represented by this User object is the same
     * as the nick of the User object given as an argument.
     * A case insensitive comparison is made.
     *
     * @return true if o is a User object with a matching lowercase nick.
     */
    public boolean equals(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other._lowerNick.equals(_lowerNick);
        }
        return false;
    }


    /**
     * Returns the hash code of this User object.
     *
     * @return the hash code of the User object.
     */
    public int hashCode() {
        return _lowerNick.hashCode();
    }


    /**
     * Returns the result of calling the compareTo method on lowercased
     * nicks. This is useful for sorting lists of User objects.
     *
     * @return the result of calling compareTo on lowercased nicks.
     */
    public int compareTo(User o) {
        return o._lowerNick.compareTo(_lowerNick);
    }
}