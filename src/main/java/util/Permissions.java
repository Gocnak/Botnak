package util;

import gui.forms.GUIMain;
import irc.Subscriber;
import lib.pircbot.User;
import util.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Nick on 8/8/2015.
 */
public class Permissions {

    public enum Permission implements Comparable<Permission> {
        VIEWER(0),        // Somebody in chat
        SUBSCRIBER(1),    // Somebody that is a subscriber
        DONOR(2),         // Somebody that has donated/cheered
        MODERATOR(3),     // Somebody that is a mod
        BROADCASTER(4);   // Somebody that is the broadcaster of the channel

        public int permValue;

        Permission(int perm) {
            permValue = perm;
        }
    }

    public static Permission asPermission(int perm) {
        for (Permission p : Permission.values()) {
            if (p.permValue == perm) {
                return p;
            }
        }
        return Permission.VIEWER;
    }

    /**
     * Gets the permission of the user based on their status.
     *
     * @param u       The user to check.
     * @param channel The channel this is for.
     * @return The permissions they have.
     */
    public static List<Permission> getUserPermissions(User u, String channel) {
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(Permission.VIEWER);
        if (Utils.isMainChannel(channel)) {
            Optional<Subscriber> sub = Settings.subscriberManager.getSubscriber(u.getUserID());
            if (sub.isPresent() && !sub.get().isActive()) {
                // Technically this is an EX_SUBSCRIBER but they are the same as a SUBSCRIBER, permission-wise
                permissionList.add(Permission.SUBSCRIBER);
            }
        }
        if (u.isSubscriber(channel)) {
            permissionList.add(Permission.SUBSCRIBER);
        }

        // Check cheer status
        if (u.getCheer(channel) >= Settings.cheerDonorCutoff.getValue())
            permissionList.add(Permission.DONOR);

        // Check donor status
        if (u.isDonor()) {
            if (u.getDonated() >= Settings.donorCutoff.getValue())
            {
                permissionList.add(Permission.DONOR);
            }
        }
        if (u.isOp(channel) || u.isAdmin() || u.isStaff() || u.isGlobalMod()) {
            permissionList.add(Permission.MODERATOR);
        }
        if (GUIMain.viewer != null && Settings.accountManager.getUserAccount().getName().equalsIgnoreCase(u.getNick())) {
            permissionList.add(Permission.BROADCASTER);
        }
        return permissionList;
    }

    /**
     * "Up-to" permissions, where the permission is exclusive to a lower bound. We check if that user
     * hasAtLeast that lower bound here. Eg: a sound file that is only for mods and up to play
     * @param permission The list of the user's permissions
     * @param perm The lower bound permission to check against
     * @return true if the user has at least the lower bound's permission, otherwise false
     */
    public static boolean hasAtLeast(List<Permission> permission, int perm) {
        return hasAtLeast(permission, asPermission(perm));
    }

    public static boolean hasAtLeast(List<Permission> permissions, Permission toCheck) {
        for (Permission p : permissions) {
            if (p.compareTo(toCheck) > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does an == check for a User in regards to the supplied permission(s)
     * @param u The user to test
     * @param channel The channel the user is in
     * @param permissions The permission(s) to test against
     * @return true if the User has all of the supplied permissions, else false
     */
    public static boolean userSatisfiesPermissions(User u, String channel, Permission... permissions) {
        List<Permission> userPermissions = getUserPermissions(u, channel);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }
}