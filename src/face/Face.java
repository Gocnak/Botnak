package face;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 8/11/13
 * Time: 8:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Face {


    private final String regex, filePath;

    public Face() {
        regex = "";
        filePath = "";
    }


    /**
     * This custom class was made to make Face storing a lot easier for Botnak.
     *
     * @param regex    The regex that triggers the name to be changed in the message in Botnak.
     * @param filePath The path to the picture.
     */
    public Face(String regex, String filePath) {
        this.regex = regex;
        this.filePath = filePath;
    }

    public String getRegex() {
        return regex;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object another) {
        return (another instanceof Face) && ((Face) another).getRegex().equals(getRegex())
                && ((Face) another).getFilePath().equals(getFilePath());
    }


}
