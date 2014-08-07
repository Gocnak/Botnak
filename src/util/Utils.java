package util;

import face.Face;
import face.SubscriberIcon;
import face.TwitchFace;
import gui.GUIMain;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import lib.scalr.Scalr;
import sound.Sound;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 6/3/13
 * Time: 7:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    static Random r = new Random();

    /**
     * Returns a random number from 0 to the specified.
     *
     * @
     */
    public static int nextInt(int param) {
        return r.nextInt(param);
    }

    /**
     * Gets the extension of a file.
     *
     * @param f File to get the extension of.
     * @return The file's extension.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
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
            StringBuilder sb = new StringBuilder();
            sb.append(f.getName());
            sb.append(", ");
            sb.append(f.getSize());
            sb.append(", ");
            sb.append(toRet);
            toRet = sb.toString();
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
     * Checks to see if a Pircbot is in a given channel.
     *
     * @param v       The Pircbot to check.
     * @param channel The channel in question.
     * @return true if in the channel, otherwise false.
     */
    public static boolean isInChannel(PircBot v, String channel) {
        if (v == null) return false;
        if (!channel.startsWith("#")) channel = "#" + channel;
        String[] channels = v.getChannels();
        if (channels != null) {
            for (String s : channels) {
                if (s.equalsIgnoreCase(channel)) {
                    return true;
                }
            }
        }
        return false;
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
     */
    public static void logChat(String[] message, String channel) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(new File(GUIMain.currentSettings.sessionLogDir.getAbsolutePath() + File.separator + channel + ".txt"), true)));
            for (String s : message) {
                if (s != null && !s.equals("")) {
                    out.println(s);
                }
            }
            out.close();
        } catch (IOException e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Removes a file extension from a path.
     *
     * @param s The path to a file, or the file name with its extension.
     * @return The file/path name without the extension.
     */
    public static String removeExt(String s) {
        if (s != null) {
            int pos = s.lastIndexOf(".");
            if (pos == -1) return s;
            return s.substring(0, pos);
        }
        return s;
    }

    /**
     * Checks to see if the input is IRC-worthy of printing.
     *
     * @param input The input in question.
     * @return The given input if it checks out, otherwise nothing.
     */
    public static String checkText(String input) {
        return input != null && input.length() > 0 && input.trim().length() > 0 ? input : "";
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
     * <p/>
     * Author - Dr_Kegel from Gocnak's stream.
     *
     * @param seed The Hashcode of the object you want dynamic color for.
     * @return The Color of the object's hash.
     */
    public static Color getColor(final int seed) {
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


    public static URL getSubIcon(String channel) {
        for (SubscriberIcon i : GUIMain.subIconSet) {
            if (i.getChannel().equalsIgnoreCase(channel)) {
                try {
                    if (areFilesGood(i.getFileLoc())) {
                        return new File(i.getFileLoc()).toURI().toURL();
                    } else {
                        GUIMain.subIconSet.remove(i);
                        break;
                    }
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            }
        }
        try {
            URL toRead = new URL("https://api.twitch.tv/kraken/chat/" + channel.replace("#", "") + "/badges");
            BufferedReader irs = new BufferedReader(new InputStreamReader(toRead.openStream()));
            String line;
            String path = null;
            while ((line = irs.readLine()) != null) {
                JSONObject init = new JSONObject(line);
                JSONObject sub = init.getJSONObject("subscriber");
                if (!sub.getString("image").equalsIgnoreCase("null")) {
                    path = downloadIcon(sub.getString("image"), channel);
                    break;
                }
            }
            irs.close();
            if (path != null) {
                GUIMain.subIconSet.add(new SubscriberIcon(channel, path));
                return new URL(path);
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
        return null;
    }

    /**
     * Loads the default Twitch faces. This downloads to the local folder in
     * <p/>
     * /My Documents/Botnak/Faces/
     * <p/>
     * It also checks to see if you may be missing a default face, and downloads it.
     * <p/>
     * This process is threaded, and will only show the faces when it's done downloading.
     */
    public static void loadDefaultFaces() {
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    faceCheck.start();
                }
            });
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public static Thread faceCheck = new Thread(new Runnable() {
        @Override
        public void run() {
            HashSet<StringArray> fromSite = buildMap();
            if (GUIMain.currentSettings.twitchFaceDir != null) {
                if (GUIMain.currentSettings.twitchFaceDir.list().length > 0) {//has (some... all?) files
                    for (StringArray pick : fromSite) {//check default twitch faces...
                        String regex = pick.data[0];
                        String URL = pick.data[1];
                        String fileTheo = pick.data[2];//theoretically, you should have it
                        boolean flag = false;
                        String[] files = GUIMain.currentSettings.twitchFaceDir.list();
                        for (String fileActual : files) {
                            if (fileActual.equals(fileTheo)) {//but do you actually have it?
                                flag = true;//it exists, no need for downloading it
                                break;
                            }
                        }
                        if (!flag) { //guess not
                            GUIMain.log("Missing a face, downloading it... ");
                            downloadFace(URL, GUIMain.currentSettings.twitchFaceDir.getAbsolutePath(), fileTheo, regex, true);
                        }
                    }
                } else {//DOWNLOAD THEM ALLLL
                    for (StringArray pick : fromSite) {
                        String regex = pick.data[0];
                        String URL = pick.data[1];
                        String filename = pick.data[2];
                        downloadFace(URL, GUIMain.currentSettings.twitchFaceDir.getAbsolutePath(), filename, regex, true);
                    }
                }
                GUIMain.log("Loaded Twitch faces!");
                GUIMain.doneWithTwitchFaces = true;
            }
        }
    });

    /**
     * Downloads a face off of the internet using the given URL and stores it in the given
     * directory with the given filename and extension. The regex (or "name") of the sound is put in the map
     * for later use/comparison.
     * <p/>
     *
     * @param url       The URL to the face.
     * @param directory The directory to save the face in.
     * @param name      The name of the file for the face, including the extension.
     * @param regex     The regex pattern ("name") of the face.
     */
    public static void downloadFace(String url, String directory, String name, String regex, boolean defaultT) {
        if (directory == null || name == null || directory.equals("") || name.equals("")) return;
        try {
            BufferedImage image;
            URL URL = new URL(url);//bad URL or something
            image = ImageIO.read(URL);//just incase the file is null/it can't read it
            if (image.getHeight() > 26) {//if it's too big
                image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 26, Scalr.OP_ANTIALIAS);//scale it
            }
            File tosave = new File(directory + File.separator + name);
            ImageIO.write(image, "PNG", tosave);//save it
            if (defaultT) {
                TwitchFace faec = new TwitchFace(regex, tosave.getAbsolutePath(), true);
                name = removeExt(name);
                GUIMain.twitchFaceMap.put(name, faec);
            } else {
                Face faec = new Face(regex, tosave.getAbsolutePath());
                name = removeExt(name);
                GUIMain.faceMap.put(name, faec);//put it
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Downloads the subscriber icon of the specified URL and channel.
     *
     * @param url     The url to download the icon from.
     * @param channel The channel the icon is for.
     * @return The path of the file of the icon.
     */
    public static String downloadIcon(String url, String channel) {
        try {
            URL u = new URL(url);
            BufferedImage image = ImageIO.read(u);//just incase the file is null/it can't read it
            if (image.getHeight() > 26) {//if it's too big
                image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 26, Scalr.OP_ANTIALIAS);//scale it
            }
            File tosave = new File(GUIMain.currentSettings.subIconsDir + File.separator + channel.substring(1) + ".png");
            ImageIO.write(image, "PNG", tosave);//save it
            return tosave.getAbsolutePath();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        return null;
    }

    /**
     * Removes a face from the Face HashMap and deletes the face picture file.
     *
     * @param key The name of the face to remove.
     */
    public static void removeFace(String key) {
        Face toDelete = GUIMain.faceMap.get(key);
        File f = new File(toDelete.getFilePath());
        if (f.delete()) {
            GUIMain.faceMap.remove(key);
        }
    }

    /**
     * Builds a HashSet for the default JSON faces from Twitch's API site.
     * The info is stored in a StringArray for future reference.
     *
     * @return An array with the Face data from the website, 0 is regex, 1 is the full URL, 2 is the filename
     */
    public static HashSet<StringArray> buildMap() {
        HashSet<StringArray> set = new HashSet<>();
        try {
            URL url = new URL("http://api.twitch.tv/kraken/chat/emoticons?on_site=1");
            BufferedReader irs = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = irs.readLine()) != null) {
                JSONObject init = new JSONObject(line);
                if (init.length() == 2) {
                    JSONArray emotes = init.getJSONArray("emoticons");
                    for (int i = 0; i < emotes.length(); i++) {
                        JSONObject emote = emotes.getJSONObject(i);
                        JSONObject imageStuff = emote.getJSONArray("images").getJSONObject(0);//3 is URL, 4 is height
                        String regex = emote.getString("regex")
                            .replaceAll("\\\\&lt\\\\;", "\\<")
                            .replaceAll("\\\\&gt\\\\;", "\\>");
                        if (imageStuff != null) {//split("-")[4] is the filename
                            String uRL = imageStuff.getString("url");
                            set.add(new StringArray(new String[]{regex, uRL, (uRL.split("-")[4] + ".png")}));
                        }
                    }
                }
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        return set;
    }

    private static ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
            BufferedImage img = ImageIO.read(image);
            int initSize = img.getHeight();
            int scale = (20 - GUIMain.currentSettings.font.getSize());//let's shrink it a little bit
            int size = initSize;
            if (initSize > 20) {//if it's big
                size = (initSize - scale);//then we'll scale it
            }//otherwise it'll stay the same
            img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, size, Scalr.OP_ANTIALIAS);
            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e) {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    /**
     * Adds the faces to the chat, using Regex as keys in the Image Map.
     *
     * @param doc     The Styled Document from the GUIMain class.
     * @param start   The start index of the message.
     * @param message The message itself.
     */
    public static void handleFaces(StyledDocument doc, int start, String message) {
        if (!GUIMain.doneWithFaces) return;
        try {
            Set<String> set = GUIMain.faceMap.keySet();
            for (String name : set) {
                String regex = GUIMain.faceMap.get(name).getRegex();
                if (!checkRegex(regex)) continue;
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(message);
                int lastFound = -1;
                while (m.find() && !GUIMain.shutDown) {
                    lastFound++;//makes the index +1, 0 for start, rids having indexOf(lastFound + 1) later on in this code
                    final SimpleAttributeSet attrs = new SimpleAttributeSet(
                            //finds the index of the face while not replacing the old V ones
                            doc.getCharacterElement(start + message.indexOf(m.group(), lastFound)).getAttributes());
                    if (!areFilesGood(GUIMain.faceMap.get(name).getFilePath())) {// the file doesn't exist/didn't download right
                        return;
                    }
                    try {
                        StyleConstants.setIcon(attrs,
                                sizeIcon(new File(GUIMain.faceMap.get(name).getFilePath()).toURI().toURL()));
                    } catch (Exception e) {
                        GUIMain.log(e.getMessage());
                    }
                    //                        find the face V  from either 0 or the next index of it, and removes it\
                    lastFound = message.indexOf((m.group()), lastFound);
                    doc.remove(start + lastFound, m.group().length());
                    //            sets the index to the last index found, and adds the icon with the face text
                    doc.insertString(start + lastFound, m.group(), attrs);
                }
            }
        } catch (BadLocationException e1) {
            GUIMain.log(e1.getMessage());
        }
    }

    /**
     * Handles the twitch faces using Regex in the twitch faces map.
     *
     * @param doc     The Styled Document from the GUIMain class.
     * @param start   The start index of the message.
     * @param message The message itself.
     */
    public static void handleTwitchFaces(StyledDocument doc, int start, String message) {
        if (!GUIMain.doneWithTwitchFaces) return;
        try {
            Set<String> set = GUIMain.twitchFaceMap.keySet();
            for (String name : set) {
                String regex = GUIMain.twitchFaceMap.get(name).getRegex();
                if (!checkRegex(regex)) continue;
                if (!GUIMain.twitchFaceMap.get(name).isEnabled()) continue;
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(message);
                int lastFound = -1;
                while (m.find() && !GUIMain.shutDown) {
                    lastFound++;//makes the index +1, 0 for start, rids having indexOf(lastFound + 1) later on in this code
                    final SimpleAttributeSet attrs = new SimpleAttributeSet(
                            //finds the index of the face while not replacing the old V ones
                            doc.getCharacterElement(start + message.indexOf(m.group(), lastFound)).getAttributes());
                    if (!areFilesGood(GUIMain.twitchFaceMap.get(name).getFilePath())) {// the file doesn't exist/didn't download right
                        return;
                    }
                    try {
                        StyleConstants.setIcon(attrs,
                                sizeIcon(new File(GUIMain.twitchFaceMap.get(name).getFilePath()).toURI().toURL()));
                    } catch (Exception e) {
                        GUIMain.log(e.getMessage());
                    }
                    //                        find the face V  from either 0 or the next index of it, and removes it\
                    lastFound = message.indexOf((m.group()), lastFound);
                    doc.remove(start + lastFound, m.group().length());
                    //            sets the index to the last index found, and adds the icon with the face text
                    doc.insertString(start + lastFound, m.group(), attrs);
                }
            }
        } catch (BadLocationException e1) {
            GUIMain.log(e1.getMessage());
        }
    }

    /**
     * Finds and tags URLs in the chat so that you can click them.
     *
     * @param doc     The document (JTextPane) to search.
     * @param start   The start index of the message.
     * @param message The message itself.
     */
    public static void handleURLs(StyledDocument doc, int start, String message) {
        try {
            SimpleAttributeSet attrs;
            String[] split = message.split(" ");
            for (String s : split) {
                if (s != null) {
                    if (s.startsWith("http")) {
                        int index = message.indexOf(s);
                        attrs = new SimpleAttributeSet();
                        StyleConstants.setForeground(attrs, new Color(43, 162, 235));
                        StyleConstants.setFontFamily(attrs, GUIMain.currentSettings.font.getFamily());
                        StyleConstants.setFontSize(attrs, GUIMain.currentSettings.font.getSize());
                        StyleConstants.setUnderline(attrs, true);
                        attrs.addAttribute(HTML.Attribute.HREF, s);
                        doc.remove(start + index, s.length());
                        doc.insertString(start + index, s, attrs);
                    }
                }
            }
        } catch (BadLocationException e) {
            GUIMain.log((e.getMessage()));
        }
    }

    /**
     * Inserts the NAME attribute for the popup menus.
     *
     * @param doc   The document (JTextPane) to search.
     * @param start The start index of the user.
     * @param name  The name of the user.
     * @param set   The user's set.
     */
    public static void handleNames(StyledDocument doc, int start, String name, SimpleAttributeSet set) {
        try {
            set.addAttribute(HTML.Attribute.NAME, name);
            doc.remove(start, name.length());
            doc.insertString(start, name, set);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
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
     * Gets the Pircbot User with the given name/nick.
     *
     * @param c       The bot that is connected to the channel.
     * @param channel The channel the user is in.
     * @param nick    The nick of the user.
     * @return null if the user cannot be found/args are null, otherwise the specified user.
     */
    public static User getUser(PircBot c, String channel, String nick) {
        if (c == null || channel == null || nick == null) return null;
        for (User u : c.getUsers(channel)) {
            if (u != null) {
                if (u.getNick().equalsIgnoreCase(nick)) {
                    return u;
                }
            }
        }
        return null;
    }

    /**
     * Adds a command to the command map.
     * <p/>
     * To do this in chat, simply type !addcommand command time message
     * More examples at http://bit.ly/1366RwM
     *
     * @param s The string from the chat.
     */
    public static void addCommands(String s) {
        String[] split = s.split(" ");
        if (GUIMain.commandSet != null && split != null) {
            String name = split[1];//name of the command, [0] is "addcommand"
            int time;//for timer
            try {
                try {
                    time = Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    return;
                }
                int bingo = s.indexOf(" ", s.indexOf(" ", s.indexOf(" ") + 1) + 1);//Third space is the message
                String[] message = s.substring(bingo + 1).split("\\]");
                if (time > 0 && name != null && message != null) {
                    GUIMain.commandSet.add(new Command(name, time, message));
                }
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        }
    }

    /**
     * Removes a command from the command map.
     *
     * @param key The !command trigger, or key.
     */
    public static void removeCommands(String key) {
        if (GUIMain.commandSet != null && key != null) {
            for (Command c : GUIMain.commandSet) {
                if (key.equals(c.getTrigger())) {
                    GUIMain.commandSet.remove(c);
                    return;
                }
            }
        }
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
     * @param v       The irc bot to use (mostly viewer).
     * @param channel The channel the user is in.
     * @param user    The user name that requested the command.
     * @return The console command, or null if the user didn't meet the requirements.
     */
    public static ConsoleCommand getConsoleCommand(String key, PircBot v, String channel, String user) {
        if (!channel.contains(GUIMain.viewer.getMaster())) return null;
        User u = getUser(v, channel, user);
        if (u != null) {
            for (ConsoleCommand c : GUIMain.conCommands) {
                if (key.equalsIgnoreCase(c.getTrigger())) {
                    int conPerm = c.getClassPermission();
                    String[] certainPerms = c.getCertainPermissions();
                    if (conPerm == -1) {
                        for (String s : certainPerms) {//specified name permission
                            if (s.equalsIgnoreCase(u.getNick())) {
                                return c;
                            }
                        }
                    } else {//int class permission
                        int permission = Constants.PERMISSION_ALL;
                        if (u.isOp() || u.isAdmin() || u.isStaff()) {
                            permission = Constants.PERMISSION_MOD;
                        }
                        if (GUIMain.viewer != null && GUIMain.viewer.getMaster().equals(user)) {
                            permission = Constants.PERMISSION_DEV;
                        }
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
     * Sets the permission of a console command based on the input received.
     * <p/>
     * Ex:
     * !setpermission mod 0
     * >Everybody can now mod each other
     * <p/>
     * !setpermission mod gocnak,gmansoliver
     * >Only Gocnak and Gmansoliver can mod people
     * <p/>
     * etc.
     * <p/>
     * Note: This WILL reset the permissions and /then/ set it to specified.
     * If you wish to add another name, you will have to retype the ones already
     * allowed!
     *
     * @param mess The entire message to dissect.
     */
    public static void setCommandPermission(String mess) {
        if (mess == null) return;
        String trigger = mess.split(" ")[1];
        for (ConsoleCommand c : GUIMain.conCommands) {
            if (trigger.equalsIgnoreCase(c.getTrigger())) {
                int classPerm;
                String[] certainPerm = null;
                try {
                    classPerm = Integer.parseInt(mess.split(" ")[2]);
                } catch (Exception e) {
                    classPerm = -1;
                    certainPerm = mess.split(" ")[2].split(",");
                }
                c.setCertainPermission(certainPerm);
                c.setClassPermission(classPerm);
                break;
            }
        }
    }

    /**
     * Gets the SimpleAttributeSet with the correct color for the message.
     * Cycles through all of the keywords, so the first keyword it matches is the color.
     *
     * @param message The message to dissect.
     * @return The set with the correct color.
     */
    public static SimpleAttributeSet getSetForKeyword(String message) {
        SimpleAttributeSet setToRet = (SimpleAttributeSet) GUIMain.norm.clone();
        Set<String> keys = GUIMain.keywordMap.keySet();
        for (String s : keys) {
            if (message.toLowerCase().contains(s.toLowerCase())) {//case doesnt matter
                StyleConstants.setForeground(setToRet, GUIMain.keywordMap.get(s));
            }
        }
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
    public static void handleKeyword(String mess) {
        if (mess == null) return;
        String[] split = mess.split(" ");
        String trigger = split[0];
        String word = split[1];
        if (trigger.equalsIgnoreCase("addkeyword")) {
            int r, g, b;
            try {
                r = Integer.parseInt(split[2]);
            } catch (Exception e) {
                r = 255;
            }
            try {
                g = Integer.parseInt(split[3]);
            } catch (Exception e) {
                g = 255;
            }
            try {
                b = Integer.parseInt(split[4]);
            } catch (Exception e) {
                b = 255;
            }
            if (checkInts(r, g, b)) {
                GUIMain.keywordMap.put(word, new Color(r, g, b));
            }
        } else if (trigger.equalsIgnoreCase("removekeyword")) {
            Set<String> keys = GUIMain.keywordMap.keySet();
            for (String s : keys) {
                if (s.equalsIgnoreCase(word)) {
                    GUIMain.keywordMap.remove(s);
                    break;
                }
            }
        }
    }


    /**
     * Sets a color to the user based on either a R G B value in their message
     * or a standard color from the Color class.
     *
     * @param user User to change the color for.
     * @param mess Their message.
     */
    public static void handleColor(String user, String mess) {
        if (user != null && mess != null) {
            Color usercolor = getColor(user.hashCode());
            String[] split = mess.split(" ");
            if (split.length > 2) { //contains R, G, B
                int R;
                int G;
                int B;
                try {
                    R = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    R = 0;
                }
                try {
                    G = Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    G = 0;
                }
                try {
                    B = Integer.parseInt(split[3]);
                } catch (NumberFormatException e) {
                    B = 0;
                }
                if (!checkInts(R, G, B)) {//see if at least one is > 99
                    GUIMain.userColMap.put(user, new Color(usercolor.getRed(), usercolor.getGreen(), usercolor.getBlue()));
                } else {
                    GUIMain.userColMap.put(user, new Color(R, G, B));
                }
            } else {
                if (split.length == 2) { //contains String colorname
                    Color color = usercolor;
                    try {
                        Field[] fields = Color.class.getFields();
                        for (Field f : fields) {
                            if (f != null) {
                                String name = f.getName();
                                if (name.equalsIgnoreCase(split[1])) {
                                    color = (Color) f.get(null);
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    int R = color.getRed();
                    int G = color.getGreen();
                    int B = color.getBlue();
                    if (!checkInts(R, G, B)) {
                        GUIMain.userColMap.put(user, new Color(usercolor.getRed(), usercolor.getGreen(), usercolor.getBlue()));
                    } else {
                        GUIMain.userColMap.put(user, new Color(R, G, B));
                    }
                }
            }
        }
    }

    /**
     * Checks the red, green, and blue in order to show up in Botnak.
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return true if the Integers meet the specification.
     */
    public static boolean checkInts(int r, int g, int b) {
        if (r < 100) {
            if (g > 100 && g < 256 || b > 100 && b < 256) return true;
        } else { //r IS > 99
            if (r < 256) return true;
        }
        return false;
    }

    /**
     * Using the API that is generously free on unshorten.it, we can unshorten those sneaky short links.
     *
     * @param url The shortened URL to un-shortify.
     * @return The long URL (hopefully).
     */
    public static String getLongURL(String url) {
        String longUrl = "Cannot get full URL. Click with caution.";
        try {
            URL bitly = new URL("http://api.unshorten.it?shortURL=" + url + "&apiKey=58b389e3c442211daf2b34f537c01977");
            BufferedReader br = new BufferedReader(new InputStreamReader(bitly.openStream()));
            String line;
            while (!GUIMain.shutDown && (line = br.readLine()) != null) {
                if (!line.contains("error (")) {//resolved successfully
                    String woo = line.replaceAll("\\.", ",").substring(8);//make it non-clickable with http(s):// removed
                    String wooback = woo.substring(0, 25);
                    longUrl = "Shortened URL -> " + (woo.length() > 20 ? wooback + "[...]" + woo.substring(woo.length() - 5) : woo);
                    break;
                }
            }
            br.close();
        } catch (Exception e) {
            return longUrl;
        }
        return longUrl;
    }

    /**
     * Handles the adding/changing of a sound, its permission, and/or its files.
     *
     * @param s      The string from the chat to manipulate.
     * @param change True for changing a sound, false for adding.
     */
    public static void handleSound(String s, boolean change) {
        if (GUIMain.currentSettings.defaultSoundDir != null &&
                !GUIMain.currentSettings.defaultSoundDir.equals("null") &&
                !GUIMain.currentSettings.defaultSoundDir.equals("")) {
            try {
                String[] split = s.split(" ");
                String name = split[1];//both commands have this in common.
                int perm;
                if (split.length > 3) {//!add/changesound sound 0 sound(,maybe,more)
                    try {
                        perm = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        return;
                    }
                    String files = split[3];
                    if (perm == -1) return;
                    if (!files.contains(",")) {//isn't multiple
                        //this can be !addsound sound 0 sound or !changesound sound 0 newsound
                        String filename = GUIMain.currentSettings.defaultSoundDir + File.separator + files + ".wav";
                        if (areFilesGood(filename)) {
                            if (GUIMain.soundMap.containsKey(name)) {//they could technically change the permission here as well
                                if (!change) {//!addsound
                                    GUIMain.soundMap.put(name, new Sound(perm,// add it tooo it maaan
                                            addStringsToArray(GUIMain.soundMap.get(name).getSounds().data, filename)));
                                } else {//!changesound
                                    GUIMain.soundMap.put(name, new Sound(perm, filename));//replace it
                                }
                            } else { //*gasp* A NEW SOUND!?
                                if (!change) GUIMain.soundMap.put(name, new Sound(perm, filename));
                                //can't have !changesound act like !addsound
                            }
                        }
                    } else {//is multiple
                        //this can be !addsound sound 0 multi,sound or !changesound sound 0 multi,sound
                        ArrayList<String> list = new ArrayList<>();
                        String[] filesSplit = files.split(",");
                        for (String str : filesSplit) {
                            list.add(GUIMain.currentSettings.defaultSoundDir + File.separator + str + ".wav");
                        }             //calls the areFilesGood boolean in it (filters bad files already)
                        filesSplit = checkFiles(list.toArray(new String[list.size()]));
                        list.clear();//recycle time!
                        if (!change) { //adding sounds
                            if (GUIMain.soundMap.containsKey(name)) {//adding sounds, so get the old ones V
                                Collections.addAll(list, GUIMain.soundMap.get(name).getSounds().data);
                            }
                            checkAndAdd(list, filesSplit);//checks for repetition, will add anyway if list is empty
                            GUIMain.soundMap.put(name, new Sound(perm, list.toArray(new String[list.size()])));
                        } else {//!changesound, so replace it if it's in there
                            if (GUIMain.soundMap.containsKey(name))
                                GUIMain.soundMap.put(name, new Sound(perm, filesSplit));
                        }
                    }
                }
                if (split.length == 3) {//add/changesound sound perm/newsound
                    if (split[2].length() == 1) {//ASSUMING it's a permission change.
                        try {
                            perm = Integer.parseInt(split[2]);//I mean come on. What sound will have a 1 char name?
                            if (perm != -1) {
                                if (change)//because adding just a sound name and a permission is silly
                                    GUIMain.soundMap.put(name, new Sound(perm, GUIMain.soundMap.get(name).getSounds().data));//A pretty bad one...
                            }
                        } catch (NumberFormatException e) {//maybe it really is a 1-char-named sound?
                            String test = GUIMain.currentSettings.defaultSoundDir + File.separator + split[2] + ".wav";
                            if (areFilesGood(test)) { //wow...
                                if (change) {
                                    GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(), test));
                                } else {//adding a 1 char sound that exists to the pool...
                                    GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(),
                                            addStringsToArray(GUIMain.soundMap.get(name).getSounds().data, test)));
                                }
                            }
                        }
                    } else { //it's a/some new file(s) as replacement/to add!
                        if (split[2].contains(",")) {//multiple
                            String[] filesSplit = split[2].split(",");
                            ArrayList<String> list = new ArrayList<>();
                            for (String str : filesSplit) {
                                list.add(GUIMain.currentSettings.defaultSoundDir + File.separator + str + ".wav");
                            }             //calls the areFilesGood boolean in it (filters bad files already)
                            filesSplit = checkFiles(list.toArray(new String[list.size()]));
                            if (!change) {//!addsound soundname more,sounds
                                if (GUIMain.soundMap.containsKey(name)) {
                                    filesSplit = addStringsToArray(GUIMain.soundMap.get(name).getSounds().data, filesSplit);
                                    GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(), filesSplit));
                                } else { //use default permission
                                    GUIMain.soundMap.put(name, new Sound(filesSplit));
                                }
                            } else {//!changesound soundname new,sounds
                                if (GUIMain.soundMap.containsKey(name))//!changesound isn't !addsound
                                    GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(), filesSplit));
                            }
                        } else {//singular
                            String test = GUIMain.currentSettings.defaultSoundDir + File.separator + split[2] + ".wav";
                            if (areFilesGood(test)) {
                                if (!change) {//!addsound sound newsound
                                    if (GUIMain.soundMap.containsKey(name)) {//getting the old permission/files
                                        GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(),
                                                addStringsToArray(GUIMain.soundMap.get(name).getSounds().data, test)));
                                    } else {//use default permission
                                        GUIMain.soundMap.put(name, new Sound(test));
                                    }
                                } else { //!changesound sound newsound
                                    if (GUIMain.soundMap.containsKey(name))//!changesound isn't !addsound
                                        GUIMain.soundMap.put(name, new Sound(GUIMain.soundMap.get(name).getPermission(), test));
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        }
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

    /**
     * Toggles a default twitch face on/off.
     * <p/>
     * Ex: !toggleface RitzMitz
     * would toggle RitzMitz off/on in showing up on botnak,
     * depending on current state.
     *
     * @param facename The face name to toggle.
     */
    public static void toggleFace(String facename) {
        if (facename == null || !GUIMain.doneWithTwitchFaces) return;
        Set<String> set = GUIMain.twitchFaceMap.keySet();
        for (String name : set) {
            String regex = GUIMain.twitchFaceMap.get(name).getRegex();
            if (!checkRegex(regex)) continue;
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(facename);
            if (m.find()) {
                boolean old = GUIMain.twitchFaceMap.get(name).isEnabled();
                GUIMain.twitchFaceMap.get(name).setEnabled(!old);
                break;
            }
        }
    }

    /**
     * Either adds a face to the image map or changes a face to another variant.
     * If the face image size is too big, it is scaled (using Scalr) to fit the 26 pixel height limit.
     *
     * @param s The string from the chat.
     */
    public static void handleFace(String s) {
        if (GUIMain.currentSettings.defaultFaceDir == null
                || GUIMain.currentSettings.defaultFaceDir.equals("")
                || GUIMain.currentSettings.defaultFaceDir.equals("null"))
            return;
        try {
            String[] split = s.split(" ");
            if (split == null) return;
            String command = split[0];
            String name = split[1];//name of the face, used for file name, and if regex isn't supplied, becomes the regex
            String regex;
            String file;//or the URL...
            if (command.equalsIgnoreCase("addface")) {//a new face
                if (GUIMain.faceMap.containsKey(name))
                    return;//!addface is not !changeface, remove the face first or do changeface
                if (split.length == 4) {//!addface <name> <regex> <URL or file>
                    regex = split[2];
                    if (!checkRegex(regex)) return;
                    if (checkName(name)) return;
                    file = split[3];
                    if (file.startsWith("http")) {//online
                        downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(), name + ".png", regex, false);//save locally
                    } else {//local
                        if (checkName(file)) return;
                        downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file).toURI().toURL().toString(),
                                GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                name + ".png",
                                regex, false);
                    }
                }
                if (split.length == 3) {//!addface <name> <URL or file> (name will be the regex, case sensitive)
                    file = split[2];
                    if (!checkRegex(name)) return;
                    if (checkName(name)) return;
                    if (file.startsWith("http")) {//online
                        downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                name + ".png", name, false);//name is regex, so case sensitive
                    } else {//local
                        if (checkName(file)) return;
                        downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file).toURI().toURL().toString(),
                                GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                name + ".png",
                                name, //<- this will be the regex, so case sensitive
                                false);
                    }
                }
            }
            if (command.equalsIgnoreCase("changeface")) {//replace entirely
                if (GUIMain.faceMap.containsKey(name)) {//!changeface is not !addface, the map MUST contain it
                    if (split.length == 5) {//!changeface <name> 2 <new regex> <new URL/file>
                        try {//gotta make sure the number is the ^
                            if (Integer.parseInt(split[2]) != 2) return;
                        } catch (Exception e) {
                            return;
                        }
                        regex = split[3];
                        if (!checkRegex(regex)) return;
                        if (checkName(name)) return;
                        file = split[4];
                        if (file.startsWith("http")) {//online
                            downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(), (name + ".png"), regex, false);//save locally
                        } else {//local
                            if (checkName(file)) return;
                            downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file).toURI().toURL().toString(),
                                    GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                    (name + ".png"),
                                    regex, //< this will be the regex, so case sensitive
                                    false);
                        }
                    }
                    if (split.length == 4) {//!changeface <name> <numb> <newregex>|<new URL or file>
                        int type;
                        try {//gotta check the number
                            type = Integer.parseInt(split[2]);
                        } catch (Exception e) {
                            return;
                        }
                        if (type == 0) {//regex change; !changeface <name> 0 <new regex>
                            Face face = GUIMain.faceMap.get(name);
                            regex = split[3];
                            if (checkRegex(regex)) {
                                GUIMain.faceMap.put(name, new Face(regex, face.getFilePath()));
                            }
                        }
                        if (type == 1) {//file change; !changeface <name> 1 <new URL/file>
                            Face face = GUIMain.faceMap.get(name);
                            file = split[3];
                            if (file.startsWith("http")) {//online
                                downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(), (name + ".png"), face.getRegex(), false);//save locally
                            } else {//local
                                if (checkName(file)) return;
                                downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file).toURI().toURL().toString(),
                                        GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                        (name + ".png"),
                                        face.getRegex(), false);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }
}
