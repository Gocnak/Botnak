package util;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import irc.account.Oauth;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.org.jibble.pircbot.User;
import util.comm.Command;
import util.comm.ConsoleCommand;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 6/3/13
 * Time: 7:46 PM
 * <p>
 * <p>
 * This class is literally a clusterfuck of useful snippets of code I found myself
 * using in various other classes. Some of these are horrendously sloppy and I plan
 * to clean up half of these methods someday. But until then, enjoy Botnak's biggest
 * homebrew class file!
 */
public class Utils {


    static Random r = new Random();

    /**
     * Returns a random number from 0 to the specified.
     *
     * @param param The max number to choose.
     */
    public static int nextInt(int param) {
        return r.nextInt(param);
    }

    /**
     * Calls the #getExtension(String) method using the file name of the file.
     *
     * @param f The file to get the extension of.
     * @return The extension of the file, or null if there is none.
     */
    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    /**
     * Gets the extension of a file.
     *
     * @param fileName Name of the file to get the extension of.
     * @return The file's extension (ex: ".png" or ".wav"), or null if there is none.
     */
    public static String getExtension(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');

        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i).toLowerCase();
        }
        return ext;
    }

    /**
     * Sets the extension of a file to the specified extension.
     * <p>
     * This can also be used as an assurance that the extension of the
     * file is the specified extension.
     * <p>
     * It's expected that this method will be called before any file saving is
     * done.
     *
     * @param fileName  The name of the file to change the extension of.
     * @param extension The extension (ex: ".png" or ".wav") for the file.
     * @return The filename with the new extension.
     */
    public static String setExtension(String fileName, String extension) {
        String ext = getExtension(fileName);
        if (ext != null) {
            if (!ext.equalsIgnoreCase(extension)) {
                fileName = fileName.substring(0, fileName.indexOf(ext)) + extension;
            }
        } else {
            fileName = fileName + extension;
        }
        return fileName;
    }

    /**
     * Converts a font to string. Only really used in the Settings GUI.
     * (Font#toString() was too messy for me, and fuck making a wrapper class.
     *
     * @return The name, size, and style of the font.
     */
    public static String fontToString(Font f) {
        String toRet = "";
        if (f != null) {
            if (f.isBold()) { //a little bit of recycling
                toRet = f.isItalic() ? "Bold Italic" : "Bold";
            } else {
                toRet = f.isItalic() ? "Italic" : "Plain";
            }
            toRet = f.getName() + ", " + f.getSize() + ", " + toRet;
        }
        return toRet;
    }

    /**
     * Converts a formatted string (@see #fontToString()) into a font.
     *
     * @param toFont The string to be turned into a font.
     * @return The font.
     */
    public static Font stringToFont(String[] toFont) {
        Font f = new Font("Calibri", Font.PLAIN, 18);
        if (toFont != null && toFont.length == 3) {
            String name = toFont[0];
            int size;
            int type;
            try {
                size = Integer.parseInt(toFont[1]);
            } catch (Exception e) {
                size = 18;
            }
            switch (toFont[2]) {
                case "Plain":
                    type = Font.PLAIN;
                    break;
                case "Italic":
                    type = Font.ITALIC;
                    break;
                case "Bold Italic":
                    type = Font.BOLD + Font.ITALIC;
                    break;
                case "Bold":
                    type = Font.BOLD;
                    break;
                default:
                    type = Font.PLAIN;
                    break;
            }
            f = new Font(name, type, size);
        }
        return f;
    }


    /**
     * Adds a single string to an array of strings, first checking to see if the array contains it.
     *
     * @param toAdd The string(s) to add to the array.
     * @param array The array to add the string to.
     * @return The array of Strings.
     */
    public static String[] addStringsToArray(String[] array, String... toAdd) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        checkAndAdd(list, toAdd);
        return list.toArray(new String[list.size()]);
    }

    /**
     * Compares two arrays of Strings and adds the non-repeating ones to the same one.
     *
     * @param list  List of strings to compare to.
     * @param toAdd String(s) to add to the list.
     * @return The list with filtered Strings.
     */
    public static ArrayList<String> checkAndAdd(ArrayList<String> list, String... toAdd) {
        for (String s : toAdd) {
            if (!list.contains(s)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Checks individual files one by one like #areFilesGood(String...) and
     * returns the good and legitimate files.
     *
     * @param files The path(s) to the file(s) to check.
     * @return The array of paths to files that actually exist.
     * @see #areFilesGood(String...) for determining if files exist.
     */
    public static String[] checkFiles(String... files) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : files) {
            if (areFilesGood(s)) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Checks to see if the file(s)  is (are) actually existing and non-blank.
     *
     * @param files The path(s) to the file(s) to check.
     * @return true if (all) the file(s) exist(s)
     * @see #checkFiles(String...) For removing bad files and adding the others anyway.
     */
    public static boolean areFilesGood(String... files) {
        int i = 0;
        for (String s : files) {
            File test = new File(s);
            if (test.exists() && test.length() > 0) i++;
        }
        return i == files.length;
    }

    /**
     * Logs the chat to a file.
     *
     * @param message The chat separated by newline characters.
     * @param channel The channel the chat was in.
     * @param type    The int that determines what the logger should do.
     *                0 = boot
     *                1 = append (clear chat)
     *                2 = shutdown
     */
    public static void logChat(String[] message, String channel, int type) {
        if (channel.startsWith("#")) channel = channel.substring(1);
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(new File(GUIMain.currentSettings.logDir.getAbsolutePath() + File.separator + channel + ".txt"), true)));
            if (type == 0) {
                out.println("====================== " + GUIMain.currentSettings.date + " ======================");
            }
            if (message != null && !(message.length == 0 || (message.length == 1 && message[0].equalsIgnoreCase("")))) {
                for (String s : message) {
                    if (s != null && !s.equals("") && !s.equals("\n")) {
                        out.println(s);
                    }
                }
            }
            if (type == 2) {
                out.println("====================== End of " + GUIMain.currentSettings.date + " ======================");
            }
            out.close();
        } catch (IOException e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Checks a channel to see if it's live (streaming).
     *
     * @param channelName The name of the channel to check.
     * @return true if the specified channel is live and streaming, else false.
     */
    public static boolean isChannelLive(String channelName) {
        boolean isLive = false;
        try {
            URL twitch = new URL("https://api.twitch.tv/kraken/streams/" + channelName);
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line = br.readLine();
            br.close();
            if (line != null) {
                JSONObject jsonObject = new JSONObject(line);
                isLive = !jsonObject.isNull("stream") && !jsonObject.getJSONObject("stream").isNull("preview");
            }
        } catch (Exception ignored) {
        }
        return isLive;
    }
    

    

    /**
     * Gets the amount of viewers for a channel.
     *
     * @param channelName The name of the channel to check.
     * @return The int amount of viewers watching the given channel.
     */
    public static int countViewers(String channelName) {
        int count = -1;
        try {//this could be parsed with JSON, but patterns work, and if it ain't broke...
            URL twitch = new URL("https://api.twitch.tv/kraken/streams/" + channelName);
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line;
            line = br.readLine();
            if (line != null) {
                Matcher m = Constants.viewerTwitchPattern.matcher(line);
                if (m.find()) {
                    try {
                        count = Integer.parseInt(m.group(1));
                    } catch (Exception ignored) {
                    }//bad Int parsing
                }
            }
            br.close();
        } catch (Exception e) {
            count = -1;
        }
        return count;
    }

    /**
     * Gets the status of a channel, which is the title and game of the stream.
     *
     * @param channel The channel to get the status of.
     * @return A string array with the status as first index and game as second.
     */
    public static String[] getStatusOfStream(String channel) {
        String[] toRet = new String[2];
        try {
            if (channel.contains("#")) channel = channel.replace("#", "");
            URL twitch = new URL("https://api.twitch.tv/kraken/channels/" + channel);
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line = br.readLine();
            if (line != null) {
                JSONObject base = new JSONObject(line);
                if (!base.isNull("status")) {
                    toRet[0] = base.getString("status");
                    if (toRet[0].equals("")) {
                        toRet[0] = "Untitled Broadcast";
                    }
                }
                if (base.isNull("game")) {
                    toRet[1] = "";
                } else {
                    toRet[1] = base.getString("game");
                }
            }
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        return toRet;
    }

    /**
     * Gets the title of a given channel.
     *
     * @param channel The channel to get the title of.
     * @return The title of the stream.
     */
    public static String getTitleOfStream(String channel) {
        String[] status = getStatusOfStream(channel);
        return status[0];
    }

    /**
     * Gets the game of a given channel.
     *
     * @param channel The channel to get the game of.
     * @return An empty string if not playing, otherwise the game being played.
     */
    public static String getGameOfStream(String channel) {
        String[] status = getStatusOfStream(channel);
        return status[1];
    }

    /**
     * Updates the stream's status to a given parameter.
     *
     * @param key     The oauth key which MUST be authorized to edit the status of the stream.
     * @param channel The channel to edit.
     * @param message The message containing the new title/game to update to.
     * @param isTitle If the change is for the title or game.
     * @return The response Botnak has for the method.
     */
    public static Response setStreamStatus(Oauth key, String channel, String message, boolean isTitle) {
        Response toReturn = new Response();
        if (key.canSetTitle()) {
            String add = isTitle ? "title" : "game";
            if (message.split(" ").length > 1) {
                String toChangeTo = message.substring(message.indexOf(' ') + 1);
                if (toChangeTo.equals(" ") || toChangeTo.equals("null")) toChangeTo = "";
                if (toChangeTo.equalsIgnoreCase(isTitle ? getTitleOfStream(channel) : getGameOfStream(channel))) {
                    toReturn.setResponseText("Failed to set " + add + ", the " + add + " is already set to that!");
                } else {
                    Response status = setStatusOfStream(key.getKey(), channel,
                            isTitle ? toChangeTo : getTitleOfStream(channel),
                            isTitle ? getGameOfStream(channel) : toChangeTo);
                    if (status.isSuccessful()) {
                        toReturn.wasSuccessful();
                        toChangeTo = "".equals(toChangeTo) ? (isTitle ? "(untitled broadcast)" : "(not playing a game)") : toChangeTo;
                        toReturn.setResponseText("Successfully set " + add + " to: \"" + toChangeTo + "\" !");
                    } else {
                        toReturn.setResponseText(status.getResponseText());
                    }
                }
            } else {
                toReturn.setResponseText("Failed to set status status of the " + add + ", usage: !set" + add + " (new " + add + ") or \"null\"");
            }
        } else {
            toReturn.setResponseText("This OAuth key cannot update the status of the stream! Try re-authenticating in the Settings GUI!");
        }
        return toReturn;
    }

    /**
     * Sets the status of a stream.
     *
     * @param key     The oauth key which MUST be authorized to edit the status of a stream.
     * @param channel The channel to edit.
     * @param title   The title to set.
     * @param game    The game to set.
     * @return The response Botnak has for the method.
     */
    private static Response setStatusOfStream(String key, String channel, String title, String game) {
        Response toReturn = new Response();
        try {
            if (channel.contains("#")) channel = channel.replace("#", "");
            String request = "https://api.twitch.tv/kraken/channels/" + channel +
                    "?channel[status]=" + URLEncoder.encode(title, "UTF-8") +
                    "&channel[game]=" + URLEncoder.encode(game, "UTF-8") +
                    "&oauth_token=" + key.split(":")[1] + "&_method=put";
            URL twitch = new URL(request);
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line = br.readLine();
            br.close();
            if (line.contains(title) && line.contains(game)) {
                toReturn.wasSuccessful();
            }
        } catch (Exception e) {
            String error = e.getMessage().length() > 20 ? (e.getMessage().substring(0, e.getMessage().length() / 2) + "...") : e.getMessage();
            toReturn.setResponseText("Failed to update status due to Exception: " + error);
        }
        return toReturn;
    }

    /**
     * Plays an ad on stream.
     *
     * @param key     The oauth key which MUST be authorized to play a commercial on a stream.
     * @param channel The channel to play the ad for.
     * @param length  How long
     * @return true if the commercial played, else false.
     */
    public static boolean playAdvert(String key, String channel, int length) {
        boolean toReturn = false;
        try {
            if ((length % 30) != 0 || length < 30 || length > 180) length = 30;//has to be divisible by 30 seconds
            if (channel.contains("#")) channel = channel.replace("#", "");
            String request = "https://api.twitch.tv/kraken/channels/" + channel + "/commercial";
            URL twitch = new URL(request);
            HttpURLConnection c = (HttpURLConnection) twitch.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            String toWrite = "length=" + length;
            c.setRequestProperty("Authorization", "OAuth " + key.split(":")[1]);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setRequestProperty("Content-Length", String.valueOf(toWrite.length()));

            OutputStreamWriter op = new OutputStreamWriter(c.getOutputStream());
            op.write(toWrite);
            op.close();
            try {
                int response = c.getResponseCode();
                toReturn = (response == 204);
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
            c.disconnect();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        return toReturn;
    }

    /**
     * Removes a file extension from a path.
     *
     * @param s The path to a file, or the file name with its extension.
     * @return The file/path name without the extension.
     */
    public static String removeExt(String s) {
        int pos = s.lastIndexOf(".");
        if (pos == -1) return s;
        return s.substring(0, pos);
    }

    /**
     * Checks to see if the input is IRC-worthy of printing.
     *
     * @param input The input in question.
     * @return The given input if it checks out, otherwise nothing.
     */
    public static String checkText(String input) {
        return (input != null && input.length() > 0 && input.trim().length() > 0) ? input : "";
    }

    /**
     * Returns a number between a given minimum and maximum (exclusive).
     *
     * @param min The minimum number to generate on.
     * @param max The non-inclusive maximum number to generate on.
     * @return Some random number between the given numbers.
     */
    public static int random(int min, int max) {
        return min + (max == min ? 0 : r.nextInt(max - min));
    }

    /**
     * Generates a color from the #hashCode() of any java.lang.Object.
     * <p>
     * Author - Dr_Kegel from Gocnak's stream.
     *
     * @param seed The Hashcode of the object you want dynamic color for.
     * @return The Color of the object's hash.
     */
    public static Color getColorFromHashcode(final int seed) {
        /* We do some bit hacks here
           hashCode has 32 bit, we use every bit as a random source */
        final int HUE_BITS = 12, HUE_MASK = ((1 << HUE_BITS) - 1);
        final int SATURATION_BITS = 8, SATURATION_MASK = ((1 << SATURATION_BITS) - 1);
        final int BRIGHTNESS_BITS = 12, BRIGHTNESS_MASK = ((1 << BRIGHTNESS_BITS) - 1);
        int t = seed;
        /*
         * We want the full hue spectrum, that means all colors of the color
		 * circle
		 */
        /* [0 .. 1] */
        final float h = (t & HUE_MASK) / (float) HUE_MASK;
        t >>= HUE_BITS;
        final float s = (t & SATURATION_MASK) / (float) SATURATION_MASK;
        t >>= SATURATION_BITS;
        final float b = (t & BRIGHTNESS_MASK) / (float) BRIGHTNESS_MASK;
        /* some tweaks that nor black nor white can be reached */
        /* at the moment h,s,b are in the range of [0 .. 1) */
        /* For s and b this is restricted to [0.75 .. 1) at the moment. */
        return Color.getHSBColor(h, s * 0.25f + 0.75f, b * 0.25f + 0.75f);
    }

    /**
     * Returns the SimpleAttributeSet for a specified URL.
     *
     * @param URL The link to make into a URL.
     * @return The SimpleAttributeSet of the URL.
     */
    public static SimpleAttributeSet URLStyle(String URL) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, new Color(43, 162, 235));
        StyleConstants.setFontFamily(attrs, GUIMain.currentSettings.font.getFamily());
        StyleConstants.setFontSize(attrs, GUIMain.currentSettings.font.getSize());
        StyleConstants.setUnderline(attrs, true);
        attrs.addAttribute(HTML.Attribute.HREF, URL);
        return attrs;
    }

    /**
     * Credit: TDuva
     *
     * @param URL The URL to check
     * @return True if the URL can be formed, else false
     */
    public static boolean checkURL(String URL) {
        try {
            new URI(URL);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given integer is within the range of any of the key=value
     * pairs of the Map (inclusive).
     * <p>
     * Credit: TDuva
     *
     * @param i      The integer to check.
     * @param ranges The map of the ranges to check.
     * @return true if the given int is within the range set, else false
     */
    public static boolean inRanges(int i, Map<Integer, Integer> ranges) {
        for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
            if (i >= range.getKey() && i <= range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a given int to the correct millis form, except for 0.
     *
     * @param given Integer to convert.
     * @return The correct Integer in milliseconds.
     */
    public static int handleInt(int given) {
        if (given < 1000 && given > 0) {// not in millis
            given = given * 1000; //convert to millis
        }
        return given;
    }

    /**
     * Gets a time (in seconds) from a parsable string.
     *
     * @param toParse The string to parse.
     * @return A time (in seconds) as an integer.
     */
    public static int getTime(String toParse) {
        int toRet;
        if (toParse.contains("m")) {
            String toParseSub = toParse.substring(0, toParse.indexOf("m"));
            try {
                toRet = Integer.parseInt(toParseSub) * 60;
                if (toParse.contains("s")) {
                    toParseSub = toParse.substring(toParse.indexOf("m") + 1, toParse.indexOf("s"));
                    toRet += Integer.parseInt(toParseSub);
                }
            } catch (Exception e) {
                toRet = -1;
            }

        } else {
            try {
                toRet = Integer.parseInt(toParse);
            } catch (Exception e) {
                toRet = -1;
            }
        }
        return toRet;
    }

    /**
     * Adds a command to the command map.
     * <p>
     * To do this in chat, simply type !addcommand command message
     * More examples at http://bit.ly/1366RwM
     *
     * @param s The string from the chat.
     * @return true if added, false if fail
     */
    public static Response addCommands(String s) {
        Response toReturn = new Response();
        String[] split = s.split(" ");
        if (GUIMain.commandSet != null) {
            try {
                String name = split[1];//name of the command, [0] is "addcommand"
                if (name.startsWith("!")) name = name.substring(1);
                if (getCommand(name) != null) {
                    toReturn.setResponseText("Failed to add command, !" + name + " already exists!");
                    return toReturn;
                }
                ArrayList<String> arguments = new ArrayList<>();
                if (s.contains(" | ")) {//command with arguments
                    StringTokenizer st = new StringTokenizer(s.substring(0, s.indexOf(" | ")), " ");
                    while (st.hasMoreTokens()) {
                        String work = st.nextToken();
                        if (work.startsWith("%") && work.endsWith("%")) {
                            arguments.add(work);
                        }
                    }
                }
                int bingo;
                if (!arguments.isEmpty()) {
                    bingo = s.indexOf(" | ") + 3;//message comes after the pipe separator
                } else {
                    bingo = s.indexOf(" ", s.indexOf(" ") + 1) + 1;//after second space is the message without arguments
                }
                String[] message = s.substring(bingo).split("\\]");
                Command c = new Command(name, message);
                if (!arguments.isEmpty()) c.addArguments(arguments.toArray(new String[arguments.size()]));
                if (GUIMain.commandSet.add(c)) {
                    toReturn.wasSuccessful();
                    toReturn.setResponseText("Successfully added command \"!" + name + "\"");
                }
            } catch (Exception e) {
                toReturn.setResponseText("Failed to add command due to Exception: " + e.getMessage());
            }
        }
        return toReturn;
    }

    /**
     * Removes a command from the command map.
     *
     * @param key The !command trigger, or key.
     * @return true if removed, else false
     */
    public static Response removeCommands(String key) {
        Response toReturn = new Response();
        if (GUIMain.commandSet != null && key != null) {
            Command c = getCommand(key);
            if (c != null) {
                if (GUIMain.commandSet.remove(c)) {
                    toReturn.wasSuccessful();
                    toReturn.setResponseText("Successfully removed command \"" + key + "\"");
                }
            } else {
                toReturn.setResponseText("Failed to remove command, " + key + " does not exist!");
            }
        }
        return toReturn;
    }

    /**
     * Checks to see if a chat pane tab of a given name is visible.
     *
     * @param name The name of the chat pane.
     * @return True if the tab is visible in the TabbedPane, else false.
     */
    public static boolean isTabVisible(String name) {
        if (!GUIMain.chatPanes.isEmpty()) {
            Set<String> keys = GUIMain.chatPanes.keySet();
            for (String s : keys) {
                ChatPane cp = GUIMain.chatPanes.get(s);
                if (cp.getChannel().equalsIgnoreCase(name)) {
                    return cp.isTabVisible();
                }
            }
        }
        return false;
    }

    /**
     * Gets a chat pane of the given index.
     *
     * @param index The index of the tab.
     * @return The chat pane if it exists on the index, or null.
     */
    public static ChatPane getChatPane(int index) {
        if (GUIMain.chatPanes != null && !GUIMain.chatPanes.isEmpty()) {
            Set<String> keys = GUIMain.chatPanes.keySet();
            for (String s : keys) {
                ChatPane cp = GUIMain.chatPanes.get(s);
                if (cp.isTabVisible() && cp.getIndex() == index) return cp;
            }
        }
        return null;
    }

    /**
     * Gets the combined chat pane of the given index.
     *
     * @param index The index of the tab.
     * @return The combined chat pane if it exists, or null.
     */
    public static CombinedChatPane getCombinedChatPane(int index) {
        if (!GUIMain.combinedChatPanes.isEmpty()) {
            for (CombinedChatPane cp : GUIMain.combinedChatPanes) {
                if (cp.getIndex() == index) return cp;
            }
        }
        return null;
    }

    /**
     * Get the Command from the given !<string> trigger.
     *
     * @param key The !command trigger, or key.
     * @return The Command that the key relates to, or null if there is no command.
     */
    public static Command getCommand(String key) {
        if (GUIMain.commandSet != null && key != null) {
            for (Command c1 : GUIMain.commandSet) {
                if (key.equals(c1.getTrigger())) {
                    return c1;
                }
            }
        }
        return null;
    }

    /**
     * Gets the console command if the user met the trigger and permission of it.
     *
     * @param key     The name of the command.
     * @param channel The channel the user is in.
     * @return The console command, or null if the user didn't meet the requirements.
     */
    public static ConsoleCommand getConsoleCommand(String key, String channel, User u) {
        String master = GUIMain.currentSettings.accountManager.getUserAccount().getName();
        if (!channel.contains(master)) return null;
        if (u != null) {
            for (ConsoleCommand c : GUIMain.conCommands) {
                if (key.equalsIgnoreCase(c.getTrigger())) {
                    int conPerm = c.getClassPermission();
                    String[] certainPerms = c.getCertainPermissions();
                    if (conPerm == -1) {
                        if (certainPerms != null) {
                            for (String s : certainPerms) {//specified name permission
                                if (s.equalsIgnoreCase(u.getNick())) {
                                    return c;
                                }
                            }
                        }
                    } else {//int class permission
                        int permission = getUserPermission(u, channel);
                        if (permission >= conPerm) {
                            return c;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the permission of the user based on their status.
     *
     * @param u The user to check.
     * @param channel The channel this is for.
     * @return The permission they have.
     */
    public static int getUserPermission(User u, String channel) {
        int permission = Constants.PERMISSION_ALL;
        if (u.isSubscriber(channel)) {
            permission = Constants.PERMISSION_SUB;
        }
        if (u.isDonor()) {
            if (u.getDonated() >= 2.50) {
                permission = Constants.PERMISSION_DONOR;
            }
        }
        if (u.isOp(channel) || u.isAdmin() || u.isStaff() || u.isGlobalMod()) {
            permission = Constants.PERMISSION_MOD;
        }
        if (GUIMain.viewer != null && GUIMain.currentSettings.accountManager.getUserAccount().getName().equalsIgnoreCase(u.getNick())) {
            permission = Constants.PERMISSION_DEV;
        }
        return permission;
    }

    /**
     * Sets the permission of a console command based on the input received.
     * <p>
     * Ex:
     * !setpermission mod 0
     * >Everybody can now mod each other
     * <p>
     * !setpermission mod gocnak,gmansoliver
     * >Only Gocnak and Gmansoliver can mod people
     * <p>
     * etc.
     * <p>
     * Note: This WILL reset the permissions and /then/ set it to specified.
     * If you wish to add another name, you will have to retype the ones already
     * allowed!
     *
     * @param mess The entire message to dissect.
     */
    public static Response setCommandPermission(String mess) {
        Response toReturn = new Response();
        if (mess == null) {
            toReturn.setResponseText("Failed to set command permission, message is null!");
            return toReturn;
        }
        String[] split = mess.split(" ");
        String trigger = split[1];
        for (ConsoleCommand c : GUIMain.conCommands) {
            if (trigger.equalsIgnoreCase(c.getTrigger())) {
                int classPerm;
                String[] certainPerm = null;
                try {
                    classPerm = Integer.parseInt(split[2]);
                } catch (Exception e) {
                    classPerm = -1;
                    certainPerm = split[2].split(",");
                }
                c.setCertainPermission(certainPerm);
                c.setClassPermission(classPerm);
                toReturn.wasSuccessful();
                toReturn.setResponseText("Successfully set the command permission for " + trigger + " !");
                break;
            }
        }
        return toReturn;
    }

    /**
     * Gets the currently playing song, given Botnak was properly setup to read it.
     *
     * @return The name of the song, else an empty string.
     */
    public static Response getCurrentlyPlaying() {
        Response toReturn = new Response();
        if ("".equals(GUIMain.currentSettings.lastFMAccount)) {
            toReturn.setResponseText("Failed to fetch current playing song, the user has no last.fm account set!");
            return toReturn;
        }
        //TODO check the song requests engine to see if that is currently playing
        String tracks_url = "http://www.last.fm/user/" + GUIMain.currentSettings.lastFMAccount + "/now";
        try {
            URL request = new URL("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=" +
                    GUIMain.currentSettings.lastFMAccount + "&api_key=e0d3467ebb54bb110787dd3d77705e1a&format=json");
            BufferedReader br = new BufferedReader(new InputStreamReader(request.openStream()));
            String line = br.readLine();
            br.close();
            JSONObject outermost = new JSONObject(line);
            JSONObject recentTracks = outermost.getJSONObject("recenttracks");
            JSONArray songsArray = recentTracks.getJSONArray("track");
            if (songsArray.length() > 0) {
                JSONObject mostRecent = songsArray.getJSONObject(0);
                JSONObject artist = mostRecent.getJSONObject("artist");
                String artistOfSong = artist.getString("#text");
                String nameOfSong = mostRecent.getString("name");
                if (mostRecent.has("@attr")) {//it's the current song
                    toReturn.setResponseText("The current song is: " + artistOfSong + " - " + nameOfSong + " || " + tracks_url);
                } else {
                    toReturn.setResponseText("The most recent song was: " + artistOfSong + " - " + nameOfSong + " || " + tracks_url);
                }
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("Failed to fetch current song; last.fm shows no recent tracks!");
            }
        } catch (Exception e) {
            toReturn.setResponseText("Failed to fetch current playing song due to an Exception!");
        }
        return toReturn;
    }

    /**
     * Gets stream uptime.
     *
     * @return the current stream uptime.
     */
    public static Response getUptimeString(String channelName) {
        if (channelName.contains("#")) channelName = channelName.replace("#", "");
        Response toReturn = new Response();
        try {
            URL nightdev = new URL("https://nightdev.com/hosted/uptime.php?channel=" + channelName);
            BufferedReader br = new BufferedReader(new InputStreamReader(nightdev.openStream()));
            String line = br.readLine();
            br.close();
            if (line != null) {
                if (line.contains("is not")) {
                    toReturn.setResponseText("The stream is not live!");
                } else if (line.contains("No chan")) {
                    toReturn.setResponseText("Error checking uptime, no channel specified!");
                } else {
                    toReturn.wasSuccessful();
                    toReturn.setResponseText("The stream has been live for: " + line);
                }
            }
        } catch (Exception ignored) {
            toReturn.setResponseText("Error checking uptime due to Exception!");
        }
        return toReturn;
    }

    /**
     * Gets the SimpleAttributeSet with the correct color for the message.
     * Cycles through all of the keywords, so the first keyword it matches is the color.
     *
     * @param message The message to dissect.
     * @return The set with the correct color.
     */
    public static SimpleAttributeSet getSetForKeyword(String message) {
        SimpleAttributeSet setToRet = new SimpleAttributeSet(GUIMain.norm);
        Set<String> keys = GUIMain.keywordMap.keySet();
        //case doesnt matter
        keys.stream().filter(
                s -> message.toLowerCase().contains(s.toLowerCase())).forEach(
                s -> StyleConstants.setForeground(setToRet, GUIMain.keywordMap.get(s)));
        return setToRet;
    }

    /**
     * Checks to see if the message contains a keyword.
     *
     * @param message The message to check.
     * @return True if the message contains a keyword, else false.
     */
    public static boolean mentionsKeyword(String message) {
        Set<String> keys = GUIMain.keywordMap.keySet();
        for (String s : keys) {
            if (message.toLowerCase().contains(s.toLowerCase())) {//case doesnt matter
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the adding/removing of a keyword and the colors.
     *
     * @param mess The entire message to dissect.
     */
    public static Response handleKeyword(String mess) {
        Response toReturn = new Response();
        if (mess == null || "".equals(mess)) {
            toReturn.setResponseText("Failed to handle keyword, the message is null!");
            return toReturn;
        }
        String[] split = mess.split(" ");
        if (split.length > 1) {
            String trigger = split[0];
            String word = split[1];
            if (trigger.equalsIgnoreCase("addkeyword")) {
                String color = mess.substring(mess.indexOf(" ", mess.indexOf(" ") + 1) + 1);
                Color c = getColor(color, null);
                if (!c.equals(Color.white)) {
                    GUIMain.keywordMap.put(word, c);
                    toReturn.wasSuccessful();
                    toReturn.setResponseText("Successfully added keyword \"" + word + "\" !");
                } else {
                    toReturn.setResponseText("Failed to add keyword, the color cannot be white!");
                }
            } else if (trigger.equalsIgnoreCase("removekeyword")) {
                Set<String> keys = GUIMain.keywordMap.keySet();
                for (String s : keys) {
                    if (s.equalsIgnoreCase(word)) {
                        GUIMain.keywordMap.remove(s);
                        toReturn.wasSuccessful();
                        toReturn.setResponseText("Successfully removed keyword \"" + word + "\" !");
                        return toReturn;
                    }
                }
                toReturn.setResponseText("Failed to remove keyword, \"" + word + "\" does not exist!");
            }
        } else {
            toReturn.setResponseText("Failed to handle keyword, the keyword is null!");
        }
        return toReturn;
    }

    /**
     * Generates a pseudo-random color that works for Botnak.
     *
     * @return The randomly generated color.
     */
    public static Color getRandomColor() {
        return new Color(random(100, 256), random(100, 256), random(100, 256));
    }

    /**
     * Gets the color from the given string. Supports hexadecimal, RGB, and color
     * name.
     *
     * @param message  The message to dissect.
     * @param fallback The fallback color to set to if the parsing failed. Defaults to white if null.
     * @return The parsed color from the message, or the fallback color if parsing failed.
     */
    public static Color getColor(String message, Color fallback) {
        Color toRet = (fallback == null ? new Color(255, 255, 255) : fallback);
        String[] split = message.split(" ");
        if (split.length > 1) { //R G B
            int R;
            int G;
            int B;
            try {
                R = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                R = 0;
            }
            try {
                G = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                G = 0;
            }
            try {
                B = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                B = 0;
            }
            if (checkInts(R, G, B)) toRet = new Color(R, G, B);
        } else {
            try {
                //this is for hexadecimal
                Color toCheck = Color.decode(split[0]);
                if (checkColor(toCheck)) toRet = toCheck;
            } catch (Exception e) {
                //didn't parse it right, so it may be a name of a color
                for (NamedColor nc : Constants.namedColors) {
                    if (split[0].equalsIgnoreCase(nc.getName())) {
                        toRet = nc.getColor();
                        break;
                    }
                }
                if (split[0].equalsIgnoreCase("random")) {
                    toRet = getRandomColor();
                }
            }
        }
        return toRet;
    }


    /**
     * Sets a color to the user based on either a R G B value in their message
     * or a standard color from the Color class.
     *
     * @param user User to change the color for.
     * @param mess Their message.
     */
    public static Response handleColor(String user, String mess, Color old) {
        Response toReturn = new Response();
        if (user != null && mess != null) {
            //mess = "!setcol r g b" or "!setcol #cd4fd5"
            //so let's send just the second part.
            Color newColor = getColor(mess.substring(mess.indexOf(" ") + 1), old);
            if (!newColor.equals(old)) {
                GUIMain.userColMap.put(user, newColor);
                toReturn.setResponseText("Successfully set color for user " + user + " !");
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("Failed to update color, it may be too dark!");
            }
        } else {
            toReturn.setResponseText("Failed to update user color, user or message is null!");
        }
        return toReturn;
    }

    /**
     * Checks a color to see if it will show up in botnak.
     *
     * @param c The color to check.
     * @return True if the color is not null, and shows up in botnak.
     */
    public static boolean checkColor(Color c) {
        return c != null && checkInts(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Checks if the red, green, and blue show up in Botnak,
     * using the standard Luminance formula.
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return true if the Integers meet the specification.
     */
    public static boolean checkInts(int r, int g, int b) {
        double luma = (0.3 * (double) r) + (0.6 * (double) g) + (0.1 * (double) b);
        return luma > (double) 35;
    }

    /**
     * Checks to see if the regex is valid.
     *
     * @param toCheck The regex to check.
     * @return <tt>true</tt> if valid regex.
     */
    public static boolean checkRegex(String toCheck) {
        try {
            Pattern.compile(toCheck);
            return true;
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
            return false;
        }
    }

    /**
     * Checks the file name to see if Windows will store it properly.
     *
     * @param toCheck The name to check.
     * @return true if the name is invalid.
     */
    public static boolean checkName(String toCheck) {
        Matcher m = Constants.fileExclPattern.matcher(toCheck);
        return m.find();
    }
}