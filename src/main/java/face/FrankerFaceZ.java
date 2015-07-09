package face;

import gui.GUIMain;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import util.Utils;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
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
                // Since FFZ is self-signed, we need to either have that cert ... Or just don't care.
                // Since FFZ is kind of whatever, let's just choose to look the other way on all certificates
                // @author Chrisazy
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };
                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = ((String hostname, SSLSession session) -> true);

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                // Now we can safely ignore all safety for FFZ api!
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
                GUIMain.log("Failed to parse FFZ Channel due to Exception: ");
                GUIMain.log(e);
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