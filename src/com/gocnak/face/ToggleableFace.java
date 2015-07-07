package com.gocnak.face;

/**
 * Created by Nick on 3/12/14.
 * Represents a com.gocnak.face that can be toggled ON (to display) or OFF (not display).
 */
public class ToggleableFace extends Face {

    public ToggleableFace() {
        this("", "", false);
    }

    private boolean isEnabled;

    public ToggleableFace(String regex, String filepath, boolean enabled) {
        super(regex, filepath);
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean newBool) {
        isEnabled = newBool;
    }

    @Override
    public boolean equals(Object another) {
        return super.equals(another) && ((ToggleableFace) another).isEnabled() == this.isEnabled();
    }
}