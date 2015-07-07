package com.gocnak.sound;

import com.gocnak.util.Constants;
import com.gocnak.util.StringArray;
import com.gocnak.util.Utils;

import java.io.File;
import java.util.Arrays;

/**
 * The Sound Wrapper Class
 * <p>
 * Constructed to make com.gocnak.sound playing in Botnak a lot easier.
 */
public class Sound {

    private final int userPermission;
    private final StringArray filePaths;
    private boolean isEnabled = true;

    /**
     * Construct the sounds here. The Sound info itself should be stored here.
     *
     * @param permission The permission level for the com.gocnak.sound. Use the PERMISSION constants.
     * @param file       The file path(s) of the com.gocnak.sound file(s).
     */
    public Sound(int permission, String... file) {
        userPermission = permission;
        filePaths = new StringArray(file);
    }

    /**
     * Allows for duplication of sounds.
     *
     * @param other The other com.gocnak.sound to play.
     */
    public Sound(Sound other) {
        userPermission = other.getPermission();
        filePaths = other.getSounds();
    }

    /**
     * Gets one of the sounds in the array.
     *
     * @return One of the sounds in the array.
     */
    public File getFile() {
        return new File(filePaths.data[Utils.nextInt(filePaths.data.length)]);
    }

    /**
     * Constructs a com.gocnak.sound with default PERMISSION_ALL.
     *
     * @param files The file path(s) of the com.gocnak.sound(s).
     */
    public Sound(String... files) {
        this(Constants.PERMISSION_ALL, files);
    }

    public void setEnabled(boolean newBool) {
        isEnabled = newBool;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public StringArray getSounds() {
        return filePaths;
    }

    public int getPermission() {
        return userPermission;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Sound && (Arrays.equals(getSounds().data, ((Sound) other).getSounds().data))
                && (userPermission == ((Sound) other).getPermission());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getSounds().data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PERMISSION: ");
        sb.append(getPermission());
        sb.append(" WITH FILES: ");
        for (String s : getSounds().data) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }
}
