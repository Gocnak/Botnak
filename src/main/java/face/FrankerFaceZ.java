package face;

import gui.GUIMain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 2/1/14.
 * Used for parsing and storing FrankerFaceZ faces.
 */
public class FrankerFaceZ extends ToggleableFace {

    /**
     * Constructs a face emote from the FrankerFaceZ site.
     *
     * @param regex    The regex of the face.
     * @param filePath The file path to the face.
     * @param enabled  If the face is enabled or not.
     */
    public FrankerFaceZ(String regex, String filePath, boolean enabled) {
        super(regex, filePath, enabled);
    }

    /**
     * Parses the CSS for emotes and mod icon. First finds all the parts in { }
     * and then checks what attributes (content, image, ..) are in there.
     * <p/>
     * Used with permission from TDuva, Developer of Chatty
     */
    static class FFZParser {

        private static final Pattern PARTS = Pattern.compile("\\{[^}]+(\\}|!important)");
        private static final Pattern CODE = Pattern.compile("[;{]content:\"([^\"]+)");
        private static final Pattern IMAGE = Pattern.compile("[;{]background-image:url\\(\"([^\"]+)\"\\)");

        public static void parse(String channel, ArrayList<FrankerFaceZ> faces) {
            try {
                URL url = new URL("http://cdn.frankerfacez.com/channel/" + channel + ".css");
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String input = br.readLine();
                br.close();
                // Remove all whitespace to be able to parse stuff without worrying
                // about spaces
                input = input.replaceAll("\\s", "");

                // Find possible emotes/icons, which is just stuff inside { }
                Matcher m = PARTS.matcher(input);
                while (m.find() && !GUIMain.shutDown) {
                    FrankerFaceZ fz = parsePart(m.group());
                    if (fz != null) faces.add(fz);
                }
            } catch (Exception e) {
                GUIMain.log("Failed to parse FFZ Channel due to Exception: " + e.getMessage());
            }
        }

        /**
         * Parses one possible emote or mod icon. It is assumed to be an emote
         * when there are content, url and width/height attributes. A mod icon
         * is assumed when there is no content attribute and the url contains
         * "modicon.png".
         *
         * @param part The part to parse.
         */
        private static FrankerFaceZ parsePart(String part) {
            String code = get(CODE, part);//aka "regex"
            String image = get(IMAGE, part); //entire URL
            if (image != null && code != null && !image.contains("modicon.png")) {
                return new FrankerFaceZ(code, image, true);
            }
            return null;
        }

        /**
         * Retrieves the text contained in this Patterns first match group from
         * the input.
         *
         * @param pattern The pattern to use.
         * @param input   The input text to search.
         * @return The string to get, otherwise null.
         */
        private static String get(Pattern pattern, String input) {
            Matcher matcher = pattern.matcher(input);
            return matcher.find() ? matcher.group(1) : null;
        }
    }
}