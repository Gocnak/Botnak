package com.gocnak.face;

import com.gocnak.gui.GUIMain;
import com.gocnak.lib.JSON.JSONArray;
import com.gocnak.lib.JSON.JSONObject;
import com.gocnak.lib.scalr.Scalr;
import com.gocnak.thread.ThreadEngine;
import com.gocnak.util.Response;
import com.gocnak.util.Utils;
import org.jibble.pircbot.User;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 12/28/2014.
 */
public class FaceManager {

    public static final int DOWNLOAD_MAX_FACE_HEIGHT = 26;
    public static final int DOWNLOAD_MAX_ICON_HEIGHT = 26;

    //loading the faces
    public static boolean doneWithFaces = false;
    public static boolean doneWithTwitchFaces = false;
    public static boolean doneWithFrankerFaces = false;
    private static boolean checkedEmoteSets = false;

    //faces
    public static ConcurrentHashMap<String, Face> faceMap;
    public static ConcurrentHashMap<String, Face> nameFaceMap;
    //  twitch
    public static CopyOnWriteArraySet<SubscriberIcon> subIconSet;
    public static ConcurrentHashMap<Integer, TwitchFace> twitchFaceMap;
    public static ConcurrentHashMap<Integer, TwitchFace> onlineTwitchFaces;
    //  ffz
    public static ConcurrentHashMap<String, ArrayList<FrankerFaceZ>> ffzFaceMap;

    public static void init() {
        faceMap = new ConcurrentHashMap<>();
        nameFaceMap = new ConcurrentHashMap<>();
        twitchFaceMap = new ConcurrentHashMap<>();
        onlineTwitchFaces = new ConcurrentHashMap<>();
        ffzFaceMap = new ConcurrentHashMap<>();
        subIconSet = new CopyOnWriteArraySet<>();
    }

    public enum FACE_TYPE {
        NAME_FACE,
        TWITCH_FACE,
        FRANKER_FACE,
        NORMAL_FACE
    }

    /**
     * Removes a com.gocnak.face from the Face HashMap and deletes the com.gocnak.face picture file.
     *
     * @param key The name of the com.gocnak.face to remove.
     */
    public static Response removeFace(String key) {
        Response toReturn = new Response();
        if (!faceMap.containsKey(key)) {
            toReturn.setResponseText("Could not remove the com.gocnak.face, there is no such com.gocnak.face \"" + key + "\"!");
            return toReturn;
        }
        try {
            Face toDelete = faceMap.get(key);
            File f = new File(toDelete.getFilePath());
            if (f.delete()) {
                faceMap.remove(key);
                toReturn.wasSuccessful();
                toReturn.setResponseText("Successfully removed com.gocnak.face \"" + key + "\"!");
            } else {
                toReturn.setResponseText("Could not remove com.gocnak.face due to I/O error!");
            }
        } catch (Exception e) {
            toReturn.setResponseText("Could not delete com.gocnak.face due to Exception: " + e.getMessage());
        }
        return toReturn;
    }

    /**
     * Gets the subscriber icon for the given channel from either cache or downloads
     * it if you do not have it already.
     *
     * @param channel The channel the icon is for.
     * @return The URL of the subscriber icon.
     */
    public static URL getSubIcon(String channel) {
        for (SubscriberIcon i : subIconSet) {
            if (i.getChannel().equalsIgnoreCase(channel)) {
                try {
                    if (Utils.areFilesGood(i.getFileLoc())) {
                        return new File(i.getFileLoc()).toURI().toURL();
                    } else {
                        //This updates the icon, all you need to do is remove the file
                        subIconSet.remove(i);
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
            String line = irs.readLine();
            irs.close();
            String path = null;
            if (line != null) {
                JSONObject init = new JSONObject(line);
                if (init.has("subscriber")) {
                    JSONObject sub = init.getJSONObject("subscriber");
                    if (!sub.getString("src/gocnak/image").equalsIgnoreCase("null")) {
                        path = downloadIcon(sub.getString("src/gocnak/image"), channel);
                    }
                }
            }
            if (path != null) {
                subIconSet.add(new SubscriberIcon(channel, path));
                return getSubIcon(channel);
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        return null;
    }

    /**
     * Builds the giant all-containing Twitch Face map.
     */
    public static void buildMap() {
        try {
            // Load twitch faces
            URL url = new URL("https://api.twitch.tv/kraken/chat/emoticon_images");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = reader.readLine();
            reader.close();
            if (line != null) {
                try {
                    JSONObject init = new JSONObject(line);
                    JSONArray emotes = init.getJSONArray("emoticons");
                    for (int i = 0; i < emotes.length(); i++) {
                        JSONObject emote = emotes.getJSONObject(i);
                        int ID = emote.getInt("id");
                        if (twitchFaceMap.get(ID) != null) continue;
                        String regex = emote.getString("code").replaceAll("\\\\&lt\\\\;", "\\<").replaceAll("\\\\&gt\\\\;", "\\>");
                        String URL = "http://static-cdn.jtvnw.net/emoticons/v1/" + ID + "/1.0";
                        onlineTwitchFaces.put(ID, new TwitchFace(regex, URL, true));
                    }
                } catch (Exception e) {
                    GUIMain.log("Failed to load online Twitch faces, is the API endpoint down?");
                }
            }
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Loads the default Twitch faces. This downloads to the local folder in
     * <p>
     * /My Documents/Botnak/TwitchFaces/
     * <p>
     * It also checks to see if you may be missing a default com.gocnak.face, and downloads it.
     * <p>
     * This process is threaded, and will only show the faces when it's done downloading.
     */
    public static void loadDefaultFaces() {
        ThreadEngine.submit(() -> {
            buildMap();
            GUIMain.log("Loaded Twitch faces!");
            GUIMain.currentSettings.saveTwitchFaces();
            doneWithTwitchFaces = true;

            //TODO if currentSettings.FFZFacesEnable
            handleFFZChannel("global");//this corrects the global emotes and downloads them if we don't have them
            GUIMain.channelSet.stream().forEach(s -> handleFFZChannel(s.replaceAll("#", "")));
            doneWithFrankerFaces = true;
            GUIMain.log("Loaded FrankerFaceZ faces!");
            // END TODO
        });
    }

    /**
     * Toggles a twitch com.gocnak.face on/off.
     * <p>
     * Ex: !toggleface RitzMitz
     * would toggle RitzMitz off/on in showing up on botnak,
     * depending on current state.
     *
     * @param faceName The com.gocnak.face name to toggle.
     */
    public static Response toggleFace(String faceName) {
        Response toReturn = new Response();
        if (faceName == null || !doneWithTwitchFaces) {
            if (doneWithTwitchFaces) toReturn.setResponseText("Failed to toggle com.gocnak.face, the com.gocnak.face name is null!");
            else toReturn.setResponseText("Failed to toggle com.gocnak.face, not done checking Twitch faces!");
            return toReturn;
        }
        Set<Integer> set = twitchFaceMap.keySet();
        for (int es : set) {
            TwitchFace fa = twitchFaceMap.get(es);
            String regex = fa.getRegex();
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(faceName);
            if (m.find()) {
                boolean newStatus = !fa.isEnabled();
                fa.setEnabled(newStatus);
                toReturn.setResponseText("Toggled the com.gocnak.face " + faceName + (newStatus ? " ON" : " OFF"));
                toReturn.wasSuccessful();
                return toReturn;
            }
        }
        String errorMessage = "Could not find com.gocnak.face " + faceName + " in the loaded Twitch faces ";
        //TODO if currentSettings.ffzEnable
        Set<String> channels = ffzFaceMap.keySet();
        for (String chan : channels) {
            ArrayList<FrankerFaceZ> faces = ffzFaceMap.get(chan);
            for (FrankerFaceZ f : faces) {
                if (f.getRegex().equalsIgnoreCase(faceName)) {
                    boolean newStatus = !f.isEnabled();
                    f.setEnabled(newStatus);
                    toReturn.setResponseText("Toggled the FrankerFaceZ com.gocnak.face " + f.getRegex() + (newStatus ? " ON" : " OFF"));
                    toReturn.wasSuccessful();
                    return toReturn;
                }
            }
        }
        errorMessage += "or loaded FrankerFaceZ faces!";
        //end TODO
        toReturn.setResponseText(errorMessage);
        return toReturn;
    }

    public static void handleNameFaces(String object, SimpleAttributeSet set) {
        Set<String> names = nameFaceMap.keySet();
        for (String s : names) {
            if (object.equalsIgnoreCase(s)) {
                insertFace(set, nameFaceMap.get(s).getFilePath());
                break;
            }
        }
    }

    public static void handleFFZChannel(String channel) {
        ThreadEngine.submit(() -> {
            ArrayList<FrankerFaceZ> faces = ffzFaceMap.get(channel);
            ArrayList<FrankerFaceZ> fromOnline = new ArrayList<>();
            FrankerFaceZ.FFZParser.parse(channel, fromOnline);
            if (faces != null) { //already have the faces
                for (FrankerFaceZ online : fromOnline) {
                    boolean haveIt = false;
                    for (FrankerFaceZ f : faces) {//get the ones I need
                        if (online.getRegex().equalsIgnoreCase(f.getRegex())) {
                            haveIt = true;
                            break;
                        }
                    }
                    if (!haveIt) {
                        FrankerFaceZ downloaded = downloadFFZFace(channel, online);
                        if (downloaded != null) {
                            faces.add(downloaded);
                        }
                    }
                }
            } else { //don't have any of them
                faces = new ArrayList<>();
                for (FrankerFaceZ online : fromOnline) {
                    FrankerFaceZ downloaded = downloadFFZFace(channel, online);
                    if (downloaded != null) faces.add(downloaded);
                }
                ffzFaceMap.put(channel, faces);
            }
        });
    }

    public static void handleEmoteSet(String emotes) {
        if (checkedEmoteSets || !doneWithTwitchFaces) return;
        ThreadEngine.submit(() -> {
            try {
                checkedEmoteSets = true;
                URL url = new URL("https://api.twitch.tv/kraken/chat/emoticon_images?emotesets=" + emotes);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = br.readLine();
                br.close();
                if (line != null) {
                    User main = GUIMain.currentSettings.channelManager
                            .getUser(GUIMain.currentSettings.accountManager.getUserAccount().getName(), false);
                    JSONObject init = new JSONObject(line);
                    String[] keys = emotes.split(",");
                    JSONObject emote_sets = init.getJSONObject("emoticon_sets");
                    for (String s : keys) {
                        JSONArray set = emote_sets.getJSONArray(s);
                        for (int i = 0; i < set.length(); i++) {
                            JSONObject emote = set.getJSONObject(i);
                            int ID = emote.getInt("id");
                            main.addEmote(ID);
                            if (twitchFaceMap.get(ID) == null) {
                                downloadEmote(ID);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                GUIMain.log("FaceManager: Failed to download EmoteSets!");
                checkedEmoteSets = false;
            }
        });
    }

    public static void handleFaces(Map<Integer, Integer> ranges, Map<Integer, SimpleAttributeSet> rangeStyles,
                                   String object, FACE_TYPE type, String channel, Collection<Integer> emotes) {
        switch (type) {
            case TWITCH_FACE:
                if (doneWithTwitchFaces) {
                    for (int i : emotes) {
                        TwitchFace f = twitchFaceMap.get(i);
                        if (f == null) {
                            f = downloadEmote(i);
                        }
                        if (f == null || !f.isEnabled() || !Utils.areFilesGood(f.getFilePath())) continue;
                        String regex = f.getRegex();
                        if (!regex.matches("^\\W.*|.*\\W$")) {
                            //boundary checks are only necessary for emotes that start and end with a word character.
                            regex = "\\b" + regex + "\\b";
                        }
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(object);
                        while (m.find() && !GUIMain.shutDown) {
                            int start = m.start();
                            int end = m.end() - 1;
                            if (!Utils.inRanges(start, ranges) && !Utils.inRanges(end, ranges)) {
                                ranges.put(start, end);
                                SimpleAttributeSet attrs = new SimpleAttributeSet();
                                attrs.addAttribute("faceinfo", f);
                                attrs.addAttribute("regex", m.group());
                                insertFace(attrs, f.getFilePath());
                                attrs.addAttribute("start", start);
                                rangeStyles.put(start, attrs);
                            }
                        }
                    }
                }
                break;
            case NORMAL_FACE:
                if (doneWithFaces) {
                    Set<String> keys = faceMap.keySet();
                    for (String key : keys) {
                        Face f = faceMap.get(key);
                        if (!Utils.checkRegex(f.getRegex()) || !Utils.areFilesGood(f.getFilePath())) continue;
                        Pattern p = Pattern.compile(f.getRegex());
                        Matcher m = p.matcher(object);
                        while (m.find() && !GUIMain.shutDown) {
                            int start = m.start();
                            int end = m.end() - 1;
                            if (!Utils.inRanges(start, ranges) && !Utils.inRanges(end, ranges)) {
                                ranges.put(start, end);
                                SimpleAttributeSet attrs = new SimpleAttributeSet();
                                attrs.addAttribute("faceinfo", f);
                                attrs.addAttribute("regex", m.group());
                                insertFace(attrs, f.getFilePath());
                                attrs.addAttribute("start", start);
                                rangeStyles.put(start, attrs);
                            }
                        }
                    }
                }
                break;
            case FRANKER_FACE:
                if (doneWithFrankerFaces) {
                    //TODO check the option to loop through all the loaded ones, as per issue #81
                    //right now we just do loaded specific channel (if existing) & global
                    String[] channels = {"global", channel};
                    for (String currentChannel : channels) {
                        ArrayList<FrankerFaceZ> faces = ffzFaceMap.get(currentChannel);
                        if (faces != null) {
                            for (FrankerFaceZ f : faces) {
                                Pattern p = Pattern.compile(f.getRegex());
                                Matcher m = p.matcher(object);
                                while (m.find() && !GUIMain.shutDown) {
                                    int start = m.start();
                                    int end = m.end() - 1;
                                    if (!Utils.inRanges(start, ranges) && !Utils.inRanges(end, ranges)) {
                                        ranges.put(start, end);
                                        SimpleAttributeSet attrs = new SimpleAttributeSet();
                                        attrs.addAttribute("faceinfo", f);
                                        attrs.addAttribute("channel", currentChannel);
                                        insertFace(attrs, f.getFilePath());
                                        attrs.addAttribute("start", start);
                                        rangeStyles.put(start, attrs);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private static void insertFace(SimpleAttributeSet set, String face) {
        try {
            StyleConstants.setIcon(set, sizeIcon(new File(face).toURI().toURL()));
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    private static ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
            BufferedImage img = ImageIO.read(image);
            // Scale the icon if it's too big.
            int maxHeight = GUIMain.currentSettings.faceMaxHeight;
            if (img.getHeight() > maxHeight)
                img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, maxHeight);

            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e) {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    /**
     * Downloads the subscriber icon of the specified URL and channel.
     *
     * @param url     The url to download the icon from.
     * @param channel The channel the icon is for.
     * @return The path of the file of the icon.
     */
    public static String downloadIcon(String url, String channel) {
        File toSave = new File(GUIMain.currentSettings.subIconsDir + File.separator + Utils.setExtension(channel.substring(1), ".png"));
        if (download(url, toSave))
            return toSave.getAbsolutePath();
        else
            return null;
    }

    public static TwitchFace downloadEmote(int emote) {
        try {
            TwitchFace f = onlineTwitchFaces.get(emote);
            if (f == null) return null;
            String fileName = Utils.setExtension(String.valueOf(emote), ".png");
            File toSave = new File(GUIMain.currentSettings.twitchFaceDir.getAbsolutePath() + File.separator + fileName);
            if (download(f.getFilePath(), toSave)) {
                TwitchFace newFace = new TwitchFace(f.getRegex(), toSave.getAbsolutePath(), true);
                twitchFaceMap.put(emote, newFace);
                return newFace;
            }
        } catch (Exception e) {
            GUIMain.log("Failed to download emote ID " + emote + " due to exception " + e.getMessage());
        }
        return null;
    }

    private static FrankerFaceZ downloadFFZFace(String channel, FrankerFaceZ face) {//URL is stored in the FFZ com.gocnak.face
        FrankerFaceZ toReturn = null;
        try {
            String regex = face.getRegex();
            String fileName = Utils.setExtension(regex, ".png");
            //download into the channel's folder
            File directory = new File(GUIMain.currentSettings.frankerFaceZDir + File.separator + channel);
            directory.mkdirs();
            File toSave = new File(directory + File.separator + fileName);
            if (download(face.getFilePath(), toSave)) {
                toReturn = new FrankerFaceZ(Utils.removeExt(fileName), toSave.getAbsolutePath(), true);
            }
        } catch (Exception e) {
            GUIMain.log("Failed to download FFZ Faces due to Exception: " + e.getMessage());
        }
        return toReturn;
    }

    //overload method for below, rids the use of try{}catch{} in Utils class
    public static Response downloadFace(File f, String directory, String name, String regex, FACE_TYPE type) {
        Response toReturn = new Response();
        try {
            return downloadFace(f.toURI().toURL().toString(), directory, name, regex, type);
        } catch (Exception e) {
            toReturn.setResponseText("Failed to download com.gocnak.face due to a malformed URL!");
        }
        return toReturn;
    }


    /**
     * Downloads a com.gocnak.face off of the internet using the given URL and stores it in the given
     * directory with the given filename and extension. The regex (or "name") of the com.gocnak.face is put in the map
     * for later use/comparison.
     * <p>
     *
     * @param url       The URL to the com.gocnak.face.
     * @param directory The directory to save the com.gocnak.face in.
     * @param name      The name of the file for the com.gocnak.face, including the extension.
     * @param regex     The regex pattern ("name") of the com.gocnak.face.
     * @param type      What type of com.gocnak.face it is.
     */
    public static Response downloadFace(String url, String directory, String name, String regex, FACE_TYPE type) {
        Response toReturn = new Response();
        if (directory == null || name == null || directory.equals("") || name.equals("")) {
            toReturn.setResponseText("Failed to download com.gocnak.face, the directory or name is null!");
            return toReturn;
        }
        try {
            File toSave = new File(directory + File.separator + name);
            if (download(url, toSave)) {
                if (type == FACE_TYPE.NORMAL_FACE) {
                    Face face = new Face(regex, toSave.getAbsolutePath());
                    name = Utils.removeExt(name);
                    faceMap.put(name, face);//put it
                    toReturn.setResponseText("Successfully added the normal com.gocnak.face: " + name + " !");
                } else {
                    Face face = new Face(regex, toSave.getAbsolutePath());
                    name = Utils.removeExt(name);
                    nameFaceMap.put(name, face);
                    toReturn.setResponseText("Successfully added the nameface for user: " + name + " !");
                }
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("Failed to download the com.gocnak.face, perhaps a bad URL!");
            }
        } catch (Exception e) {
            toReturn.setResponseText("Failed to download com.gocnak.face due to Exception: " + e.getMessage());
        }
        return toReturn;
    }

    private static boolean download(String url, File toSave) {
        try {
            BufferedImage image;
            URL URL = new URL(url);//bad URL or something
            if (URL.getHost().equals("imgur.com")) {
                URL = new URL(Utils.setExtension("http://i.imgur.com" + URL.getPath(), ".png"));
            }
            if (sanityCheck(URL)) {
                image = ImageIO.read(URL);//just incase the file is null/it can't read it
                if (image.getHeight() > DOWNLOAD_MAX_FACE_HEIGHT) {//if it's too big, scale it
                    image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY,
                            Scalr.Mode.FIT_TO_HEIGHT, DOWNLOAD_MAX_FACE_HEIGHT);
                }
                return ImageIO.write(image, "PNG", toSave);//save it
            }
        } catch (Exception e) {
            if (!e.getMessage().contains("Unsupported"))
                GUIMain.log(e.getMessage());
        }
        return false;
    }

    /**
     * Tests to see if an com.gocnak.image is within reasonable downloading bounds (5000x5000)
     *
     * @param url The URL to the com.gocnak.image to check.
     * @return True if within downloadable bounds else false.
     */
    private static boolean sanityCheck(URL url) {
        try (ImageInputStream in = ImageIO.createImageInputStream(url.openStream())) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    Dimension d = new Dimension(reader.getWidth(0), reader.getHeight(0));
                    return d.getHeight() < 5000 && d.getWidth() < 5000;
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    /**
     * Either adds a com.gocnak.face to the com.gocnak.image map or changes a com.gocnak.face to another variant.
     * If the com.gocnak.face com.gocnak.image size is too big, it is scaled (using Scalr) to fit the 26 pixel height limit.
     *
     * @param s The string from the chat.
     * @return The response of the method.
     */
    public static Response handleFace(String s) {
        Response toReturn = new Response();
        boolean localCheck = (GUIMain.currentSettings.defaultFaceDir == null
                || GUIMain.currentSettings.defaultFaceDir.equals("")
                || GUIMain.currentSettings.defaultFaceDir.equals("null"));

        String[] split = s.split(" ");
        String command = split[0];
        String name = split[1];//name of the com.gocnak.face, used for file name, and if regex isn't supplied, becomes the regex
        String regex;
        String file;//or the URL...

        if (command.equalsIgnoreCase("addface")) {//a new com.gocnak.face

            if (faceMap.containsKey(name)) {//!addface is not !changeface, remove the com.gocnak.face first or do changeface
                toReturn.setResponseText("Failed to add com.gocnak.face, " + name + " already exists!");
                return toReturn;
            }

            if (split.length == 4) {//!addface <name> <regex> <URL or file>
                regex = split[2];
                //regex check
                if (!Utils.checkRegex(regex)) {
                    toReturn.setResponseText("Failed to add com.gocnak.face, the supplied regex does not compile!");
                    return toReturn;
                }
                //name check (for saving the file)
                if (Utils.checkName(name)) {
                    toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                    return toReturn;
                }

                file = split[3];
                if (file.startsWith("http")) {//online
                    return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"), regex, FACE_TYPE.NORMAL_FACE);//save locally

                } else {//local
                    if (Utils.checkName(file) || localCheck) {
                        if (!localCheck)
                            toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                        else toReturn.setResponseText("Failed to add com.gocnak.face, the local directory is not set properly!");
                        return toReturn;
                    }
                    return downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file),
                            GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"),
                            regex, FACE_TYPE.NORMAL_FACE);
                }
            } else if (split.length == 3) {//!addface <name> <URL or file> (name will be the regex, case sensitive)
                file = split[2];
                //regex (this should never be a problem, however...)
                if (!Utils.checkRegex(name)) {
                    toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not a valid regex!");
                    return toReturn;
                }
                //name check (for saving the file)
                if (Utils.checkName(name)) {
                    toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                    return toReturn;
                }
                if (file.startsWith("http")) {//online
                    return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"), name, FACE_TYPE.NORMAL_FACE);//name is regex, so case sensitive
                } else {//local
                    if (Utils.checkName(file) || localCheck) {
                        if (!localCheck)
                            toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                        else toReturn.setResponseText("Failed to add com.gocnak.face, the local directory is not set properly!");
                        return toReturn;
                    }
                    return downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file),
                            GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"),
                            name, //<- this will be the regex, so case sensitive
                            FACE_TYPE.NORMAL_FACE);
                }
            }
        } else if (command.equalsIgnoreCase("changeface")) {//replace entirely
            if (faceMap.containsKey(name)) {//!changeface is not !addface, the map MUST contain it
                if (split.length == 5) {//!changeface <name> 2 <new regex> <new URL/file>
                    try {//gotta make sure the number is the ^
                        if (Integer.parseInt(split[2]) != 2) {
                            toReturn.setResponseText("Failed to change com.gocnak.face, make sure to designate the \"2\" in the command!");
                            return toReturn;
                        }
                    } catch (Exception e) {
                        toReturn.setResponseText("Failed to change com.gocnak.face, the indicator number cannot be parsed!");
                        return toReturn;
                    }

                    regex = split[3];
                    //regex check
                    if (!Utils.checkRegex(regex)) {
                        toReturn.setResponseText("Failed to add com.gocnak.face, the supplied regex does not compile!");
                        return toReturn;
                    }

                    //name check (for saving the file)
                    if (Utils.checkName(name)) {
                        toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                        return toReturn;
                    }

                    file = split[4];
                    if (file.startsWith("http")) {//online
                        return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                Utils.setExtension(name, ".png"), regex, FACE_TYPE.NORMAL_FACE);//save locally
                    } else {//local
                        if (Utils.checkName(file) || localCheck) {
                            if (!localCheck)
                                toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                            else
                                toReturn.setResponseText("Failed to add com.gocnak.face, the local directory is not set properly!");
                            return toReturn;
                        }
                        return downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file),
                                GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                Utils.setExtension(name, ".png"),
                                regex, //< this will be the regex, so case sensitive
                                FACE_TYPE.NORMAL_FACE);
                    }
                } else if (split.length == 4) {//!changeface <name> <numb> <newregex>|<new URL or file>
                    int type;
                    try {//gotta check the number
                        type = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        toReturn.setResponseText("Failed to change com.gocnak.face, the indicator number cannot be parsed!");
                        return toReturn;
                    }
                    Face face = faceMap.get(name);
                    if (type == 0) {//regex change; !changeface <name> 0 <new regex>
                        regex = split[3];
                        if (Utils.checkRegex(regex)) {
                            faceMap.put(name, new Face(regex, face.getFilePath()));
                            toReturn.setResponseText("Successfully changed the regex for com.gocnak.face: " + name + " !");
                            toReturn.wasSuccessful();
                        } else {
                            toReturn.setResponseText("Failed to change the regex, the new regex could not be compiled!");
                        }
                    } else if (type == 1) {//file change; !changeface <name> 1 <new URL/file>
                        file = split[3];
                        if (file.startsWith("http")) {//online
                            return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                    Utils.setExtension(name, ".png"), face.getRegex(), FACE_TYPE.NORMAL_FACE);//save locally
                        } else {//local
                            if (Utils.checkName(file) || localCheck) {
                                if (!localCheck)
                                    toReturn.setResponseText("Failed to add com.gocnak.face, the supplied name is not Windows-friendly!");
                                else
                                    toReturn.setResponseText("Failed to add com.gocnak.face, the local directory is not set properly!");
                                return toReturn;
                            }
                            return downloadFace(new File(GUIMain.currentSettings.defaultFaceDir + File.separator + file),
                                    GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                    Utils.setExtension(name, ".png"),
                                    face.getRegex(), FACE_TYPE.NORMAL_FACE);
                        }
                    }
                }
            } else {
                toReturn.setResponseText("Failed to change com.gocnak.face, the com.gocnak.face " + name + " does not exist!");
            }
        }
        return toReturn;
    }
}