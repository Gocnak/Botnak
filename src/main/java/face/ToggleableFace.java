package face;

/**
 * Created by Nick on 3/12/14.
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
}
