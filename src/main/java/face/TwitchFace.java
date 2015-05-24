package face;

public class TwitchFace extends ToggleableFace {

    /**
     * This custom class was made to make Face storing a lot easier for Botnak.
     * This class is for the default Twitch faces, and will be used to toggle off certain ones.
     *
     * @param regex    The regex that triggers the name to be changed in the message in Botnak.
     * @param filePath The path to the picture.
     */
    public TwitchFace(String regex, String filePath, boolean enabled) {
        super(regex, filePath, enabled);
    }
}