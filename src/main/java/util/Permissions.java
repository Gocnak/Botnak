package util;

import gui.forms.GUIMain;
import irc.Subscriber;
import lib.pircbot.org.jibble.pircbot.User;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Nick on 8/8/2015.
 */
public class Permissions {

    public enum Permission implements Comparable<Permission> {
        ALL(0),
        EX_SUBSCRIBER(1),
        SUBSCRIBER(1),
        DONOR(2),
        MODERATOR(3),
        BROADCASTER(4);

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
        return Permission.ALL;
    }

    /**
     * Gets the permission of the user based on their status.
     *
     * @param u       The user to check.
     * @param channel The channel this is for.
     * @return The permissions they have.
     */
    public static ArrayList<Permission> getUserPermissions(User u, String channel) {
        ArrayList<Permission> permissionList = new ArrayList<>();
        permissionList.add(Permission.ALL);
        if (Utils.isMainChannel(channel)) {
            Optional<Subscriber> sub = GUIMain.currentSettings.subscriberManager.getSubscriber(u.getNick());
            if (sub.isPresent() && !sub.get().isActive()) {
                permissionList.add(Permission.EX_SUBSCRIBER);
            }
        }
        if (u.isSubscriber(channel)) {
            permissionList.add(Permission.SUBSCRIBER);
        }
        if (u.isDonor()) {
            if (u.getDonated() >= 2.50) {
                permissionList.add(Permission.DONOR);
            }
        }
        if (u.isOp(channel) || u.isAdmin() || u.isStaff() || u.isGlobalMod()) {
            permissionList.add(Permission.MODERATOR);
        }
        if (GUIMain.viewer != null && GUIMain.currentSettings.accountManager.getUserAccount().getName().equalsIgnoreCase(u.getNick())) {
            permissionList.add(Permission.BROADCASTER);
        }
        return permissionList;
    }

    public static boolean hasAtLeast(ArrayList<Permission> permission, int perm) {
        return hasAtLeast(permission, asPermission(perm));
    }

    public static boolean hasAtLeast(ArrayList<Permission> permissions, Permission toCheck) {
        for (Permission p : permissions) {
            if (p.compareTo(toCheck) > -1) {
                return true;
            }
        }
        return false;
    }
}