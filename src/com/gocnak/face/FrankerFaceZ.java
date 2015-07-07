package com.gocnak.face;

import com.gocnak.gui.GUIMain;
import com.gocnak.lib.JSON.JSONArray;
import com.gocnak.lib.JSON.JSONObject;
import com.gocnak.util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

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
     *
     */
    static class FFZParser {

        private static void parseSet(int set, ArrayList<FrankerFaceZ> collection) {
            try {
                URL url = new URL("https://api.frankerfacez.com/v1/set/" + set);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();
                Utils.parseBufferedReader(br, sb);
                JSONObject init = new JSONObject(sb.toString());
                if (!init.has("error")) {
                    JSONObject setObj = init.getJSONObject("set");
                    JSONArray emotes = setObj.getJSONArray("emoticons");
                    for (int i = 0; i < emotes.length(); i++) {
                        JSONObject emote = emotes.getJSONObject(i);
                        String regex = emote.getString("name");
                        int ID = emote.getInt("id");
                        collection.add(new FrankerFaceZ(regex, "http://cdn.frankerfacez.com/emoticon/" + ID + "/1", true));
                    }
                }
            } catch (Exception e) {
                GUIMain.log("Failed to parse FFZ Channel due to Exception: " + e.getMessage());
            }
        }

        public static void parse(String channel, ArrayList<FrankerFaceZ> faces) {
            if ("global".equalsIgnoreCase(channel)) {
                parseSet(3, faces);
                return;
            }
            try {
                URL url = new URL("https://api.frankerfacez.com/v1/_room/" + channel);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();
                Utils.parseBufferedReader(br, sb);
                JSONObject init = new JSONObject(sb.toString());
                if (!init.has("error")) {
                    JSONObject room = init.getJSONObject("room");
                    int set = room.getInt("set");
                    parseSet(set, faces);
                }
            } catch (Exception ignored) {
                //the channel doesn't have any faces
            }
        }
    }
}