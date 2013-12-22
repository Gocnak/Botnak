package util;

public class TwitchFace {

    private final String regex, filePath;
    private boolean enabled;

    /**
     * This custom class was made to make Face storing a lot easier for Botnak.
     * This class is for the default Twitch faces, and will be used to toggle off certain ones.
     *
     * @param regex    The regex that triggers the name to be changed in the message in Botnak.
     * @param filePath The path to the picture.
     */
    public TwitchFace(String regex, String filePath, boolean enabled) {
        this.regex = regex;
        this.filePath = filePath;
        this.enabled = enabled;
    }

    public String getRegex() {
        return regex;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean newBool) {
        enabled = newBool;
    }

    @Override
    public boolean equals(Object another) {
        return (another instanceof TwitchFace) && ((TwitchFace) another).getRegex().equals(getRegex())
                && ((TwitchFace) another).getFilePath().equals(getFilePath());
    }

}
