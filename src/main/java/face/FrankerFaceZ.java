package face;

import gui.forms.GUIMain;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import util.Utils;

import java.net.URL;
import java.util.List;

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
     * Parses FFZ API for faces
     */
    static class FFZParser {

        private static void parseSet(int set, List<FrankerFaceZ> collection)
        {
            try {
                URL url = new URL("https://api.frankerfacez.com/v1/set/" + set);
                String line = Utils.createAndParseBufferedReader(url.openStream());
                if (!line.isEmpty()) {
                    JSONObject init = new JSONObject(line);
                    if (!init.has("error")) {
                        JSONObject setObj = init.getJSONObject("set");
                        JSONArray emotes = setObj.getJSONArray("emoticons");
                        for (int i = 0; i < emotes.length(); i++) {
                            JSONObject emote = emotes.getJSONObject(i);
                            String regex = emote.getString("name");
                            int ID = emote.getInt("id");
                            collection.add(new FrankerFaceZ(regex, "https://cdn.frankerfacez.com/emoticon/" + ID + "/1", true));
                        }
                    }
                }
            } catch (Exception e) {
                GUIMain.log("Failed to parse FFZ Channel due to Exception: ");
                GUIMain.log(e);
            }
        }

        public static void parse(String channel, List<FrankerFaceZ> faces)
        {
            if ("global".equalsIgnoreCase(channel)) {
                parseSet(3, faces);
                return;
            }
            try {
                URL url = new URL("https://api.frankerfacez.com/v1/_room/" + channel);
                String line = Utils.createAndParseBufferedReader(url.openStream());
                if (!line.isEmpty()) {
                    JSONObject init = new JSONObject(line);
                    if (!init.has("error")) {
                        JSONObject room = init.getJSONObject("room");
                        int set = room.getInt("set");
                        parseSet(set, faces);
                    }
                }
            } catch (Exception ignored) {
                //the channel doesn't have any faces
            }
        }
    }
}