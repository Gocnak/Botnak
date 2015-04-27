package face;

import gui.GUIMain;
import lib.JSON.JSONArray;
import lib.JSON.JSONObject;
import lib.scalr.Scalr;
import util.Response;
import util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
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

    //faces
    public static ConcurrentHashMap<String, Face> faceMap;
    public static ConcurrentHashMap<String, Face> nameFaceMap;
    public static CopyOnWriteArraySet<SubscriberIcon> subIconSet;
    /**
     * Due to the immense size of the Twitch face library, we need to cut this into two maps.
     * One map is going to be the one Botnak loads on every boot, filling it with every single twitch face
     * there is. The second one is going to be the one Botnak loads to fill with Twitch Faces that
     * you know you've used before in the past.
     * <p>
     * The plan is to cache these faces, and download the new ones upon meeting the emoteset
     * in IRC, in order to provide a cleaner file storage system for Botnak compared to the massive amount
     * of faces it had downloaded before.
     */
    public static ConcurrentHashMap<Integer, ArrayList<TwitchFace>> twitchFaceMap;
    public static ConcurrentHashMap<Integer, ArrayList<TwitchFace>> loadedTwitchFaces;

    public static void init() {
        faceMap = new ConcurrentHashMap<>();
        nameFaceMap = new ConcurrentHashMap<>();
        twitchFaceMap = new ConcurrentHashMap<>();
        loadedTwitchFaces = new ConcurrentHashMap<>();
        subIconSet = new CopyOnWriteArraySet<>();
    }

    public static void handleEmoteset(Integer... emotes) {
        if (doneWithTwitchFaces) {
            for (final int i : emotes) {
                if (!loadedTwitchFaces.containsKey(i)) {
                    loadedTwitchFaces.put(i, new ArrayList<>());
                    new Thread(() -> downloadEmoteSet(i)).start();
                }
            }
        }
    }

    public enum FACE_TYPE {
        NAME_FACE,
        TWITCH_FACE,
        NORMAL_FACE
    }

    /**
     * Removes a face from the Face HashMap and deletes the face picture file.
     *
     * @param key The name of the face to remove.
     */
    public static Response removeFace(String key) {
        Response toReturn = new Response();
        if (!faceMap.containsKey(key)) {
            toReturn.setResponseText("Could not remove the face, there is no such face \"" + key + "\"!");
            return toReturn;
        }
        try {
            Face toDelete = faceMap.get(key);
            File f = new File(toDelete.getFilePath());
            if (f.delete()) {
                faceMap.remove(key);
                toReturn.wasSuccessful();
                toReturn.setResponseText("Successfully removed face \"" + key + "\"!");
            } else {
                toReturn.setResponseText("Could not remove face due to I/O error!");
            }
        } catch (Exception e) {
            toReturn.setResponseText("Could not delete face due to Exception: " + e.getMessage());
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
            String line;
            String path = null;
            while (!GUIMain.shutDown && (line = irs.readLine()) != null) {
                JSONObject init = new JSONObject(line);
                JSONObject sub = init.getJSONObject("subscriber");
                if (!sub.getString("image").equalsIgnoreCase("null")) {
                    path = downloadIcon(sub.getString("image"), channel);
                    break;
                }
            }
            irs.close();
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
            URL url = new URL("http://api.twitch.tv/kraken/chat/emoticons?on_site=1");
            BufferedReader irs = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = irs.readLine();
            irs.close();
            if (line != null) {
                JSONObject init = new JSONObject(line);
                if (init.length() == 2) {
                    JSONArray emotes = init.getJSONArray("emoticons");
                    for (int i = 0; i < emotes.length(); i++) {
                        JSONObject emote = emotes.getJSONObject(i);
                        JSONObject imageStuff = emote.getJSONArray("images").getJSONObject(0);//3 is URL, 4 is height
                        String regex = emote.getString("regex").replaceAll("\\\\&lt\\\\;", "\\<").replaceAll("\\\\&gt\\\\;", "\\>");
                        if (imageStuff != null) {//split("-")[4] is the filename
                            int emoteSet;
                            if (imageStuff.isNull("emoticon_set")) { //global emotes
                                emoteSet = 0;
                            } else { //sub emotes
                                emoteSet = imageStuff.getInt("emoticon_set");
                            }
                            String uRL = imageStuff.getString("url");
                            if (twitchFaceMap.containsKey(emoteSet)) {
                                twitchFaceMap.get(emoteSet).add(new TwitchFace(regex, uRL, true));
                            } else {
                                ArrayList<TwitchFace> newList = new ArrayList<>();
                                newList.add(new TwitchFace(regex, uRL, true));
                                twitchFaceMap.put(emoteSet, newList);
                            }
                        }
                    }
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
     * It also checks to see if you may be missing a default face, and downloads it.
     * <p>
     * This process is threaded, and will only show the faces when it's done downloading.
     */
    public static void loadDefaultFaces() {
        try {
            faceCheck.start();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public static Thread faceCheck = new Thread(() -> {
        buildMap();

        //load global faces first if there's nothing here (first time boot?)
        if (loadedTwitchFaces.isEmpty()) {
            loadedTwitchFaces.put(0, new ArrayList<>());
            downloadEmoteSet(0);
        }

        Set<Integer> keyLocal = loadedTwitchFaces.keySet();
        for (int localEmoteSet : keyLocal) {
            ArrayList<TwitchFace> localEmotes = loadedTwitchFaces.get(localEmoteSet);
            ArrayList<TwitchFace> externalEmotes = twitchFaceMap.get(localEmoteSet);
            if (externalEmotes != null) {
                for (TwitchFace external : externalEmotes) {
                    boolean flag = false;
                    for (TwitchFace internal : localEmotes) {
                        if (internal.getFilePath().contains(external.getFilePath().split("-")[4])) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {//add new faces that I need
                        try {
                            String newFaceToAddFileName = Utils.setExtension(external.getFilePath().split("-")[4], ".png");
                            File newFaceToAdd = new File(GUIMain.currentSettings.twitchFaceDir + File.separator + newFaceToAddFileName);
                            if (download(external.getFilePath(), newFaceToAdd)) {
                                localEmotes.add(new TwitchFace(external.getRegex(), newFaceToAdd.getAbsolutePath(), true));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            //remove the ones I shouldn't have
            ArrayList<TwitchFace> toRemove = new ArrayList<>();
            for (TwitchFace internal : localEmotes) {
                boolean flag = false;
                if (externalEmotes != null) {//if it's null, that means the set is gone now, delete it
                    for (TwitchFace external : externalEmotes) {
                        if (internal.getFilePath().contains(external.getFilePath().split("-")[4])) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (!flag) {
                    toRemove.add(internal);
                }
            }
            toRemove.stream().forEach(localEmotes::remove);
        }
        GUIMain.log("Loaded Twitch faces!");
        GUIMain.currentSettings.saveTwitchFaces();
        doneWithTwitchFaces = true;
    });

    /**
     * Toggles a twitch face on/off.
     * <p>
     * Ex: !toggleface RitzMitz
     * would toggle RitzMitz off/on in showing up on botnak,
     * depending on current state.
     *
     * @param faceName The face name to toggle.
     */
    public static Response toggleFace(String faceName) {
        Response toReturn = new Response();
        if (faceName == null || !doneWithTwitchFaces) {
            if (doneWithTwitchFaces) toReturn.setResponseText("Failed to toggle face, the face name is null!");
            else toReturn.setResponseText("Failed to toggle face, not done checking Twitch faces!");
            return toReturn;
        }
        Set<Integer> set = loadedTwitchFaces.keySet();
        for (int es : set) {
            ArrayList<TwitchFace> faces = loadedTwitchFaces.get(es);
            for (TwitchFace fa : faces) {
                String regex = fa.getRegex();
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(faceName);
                if (m.find()) {
                    boolean newStatus = !fa.isEnabled();
                    fa.setEnabled(newStatus);
                    toReturn.setResponseText("Toggled the face " + faceName + " " + (newStatus ? "ON" : "OFF"));
                    return toReturn;
                }
            }
        }
        toReturn.setResponseText("Could not find face " + faceName + " in the loaded Twitch faces!");
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

    public static void handleFaces(Map<Integer, Integer> ranges, Map<Integer, SimpleAttributeSet> rangeStyles,
                                   String object, FACE_TYPE type, Integer[] emotes) {
        switch (type) {
            case TWITCH_FACE:
                if (doneWithTwitchFaces) {
                    for (int i : emotes) {
                        ArrayList<TwitchFace> faces = loadedTwitchFaces.get(i);
                        for (TwitchFace f : faces) {
                            if (!f.isEnabled() || !Utils.areFilesGood(f.getFilePath())) continue;
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
                                    insertFace(attrs, f.getFilePath());
                                    attrs.addAttribute("start", start);
                                    rangeStyles.put(start, attrs);
                                }
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
                                insertFace(attrs, f.getFilePath());
                                attrs.addAttribute("start", start);
                                rangeStyles.put(start, attrs);
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

    public static void downloadEmoteSet(int emoteSet) {
        ArrayList<TwitchFace> faces = twitchFaceMap.get(emoteSet);
        if (faces != null) {
            for (TwitchFace f : faces) {
                try {
                    String fileName = Utils.setExtension(f.getFilePath().split("-")[4], ".png");
                    File toSave = new File(GUIMain.currentSettings.twitchFaceDir.getAbsolutePath() + File.separator + fileName);
                    if (download(f.getFilePath(), toSave)) {
                        loadedTwitchFaces.get(emoteSet).add(new TwitchFace(f.getRegex(), toSave.getAbsolutePath(), true));
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    //overload method for below, rids the use of try{}catch{} in Utils class
    public static Response downloadFace(File f, String directory, String name, String regex, FACE_TYPE type) {
        Response toReturn = new Response();
        try {
            return downloadFace(f.toURI().toURL().toString(), directory, name, regex, type);
        } catch (Exception e) {
            toReturn.setResponseText("Failed to download face due to a malformed URL!");
        }
        return toReturn;
    }


    /**
     * Downloads a face off of the internet using the given URL and stores it in the given
     * directory with the given filename and extension. The regex (or "name") of the face is put in the map
     * for later use/comparison.
     * <p>
     *
     * @param url       The URL to the face.
     * @param directory The directory to save the face in.
     * @param name      The name of the file for the face, including the extension.
     * @param regex     The regex pattern ("name") of the face.
     * @param type      What type of face it is.
     */
    public static Response downloadFace(String url, String directory, String name, String regex, FACE_TYPE type) {
        Response toReturn = new Response();
        if (directory == null || name == null || directory.equals("") || name.equals("")) {
            toReturn.setResponseText("Failed to download face, the directory or name is null!");
            return toReturn;
        }
        try {
            if (url.contains("imgur.com") && !url.contains("i.imgur"))
                url = "http://i.imgur.com/" + url.substring(url.lastIndexOf("/") + 1);//fixes URLs of base-imgur
            File toSave = new File(directory + File.separator + name);
            if (download(url, toSave)) {
                if (type == FACE_TYPE.NORMAL_FACE) {
                    Face face = new Face(regex, toSave.getAbsolutePath());
                    name = Utils.removeExt(name);
                    faceMap.put(name, face);//put it
                    toReturn.setResponseText("Successfully added the normal face: " + name + " !");
                } else {
                    Face face = new Face(regex, toSave.getAbsolutePath());
                    name = Utils.removeExt(name);
                    nameFaceMap.put(name, face);
                    toReturn.setResponseText("Successfully added the nameface for user: " + name + " !");
                }
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("Failed to download the face, perhaps a bad URL!");
            }
        } catch (Exception e) {
            toReturn.setResponseText("Failed to download face due to Exception: " + e.getMessage());
        }
        return toReturn;
    }

    private static boolean download(String url, File toSave) {
        try {
            BufferedImage image;
            URL URL = new URL(url);//bad URL or something
            image = ImageIO.read(URL);//just incase the file is null/it can't read it
            if (image.getHeight() > DOWNLOAD_MAX_FACE_HEIGHT) {//if it's too big, scale it
                image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY,
                        Scalr.Mode.FIT_TO_HEIGHT, DOWNLOAD_MAX_FACE_HEIGHT);
            }
            return ImageIO.write(image, "PNG", toSave);//save it
        } catch (Exception e) {
            if (!e.getMessage().contains("Unsupported"))
                GUIMain.log(e.getMessage());
        }
        return false;
    }


    /**
     * Either adds a face to the image map or changes a face to another variant.
     * If the face image size is too big, it is scaled (using Scalr) to fit the 26 pixel height limit.
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
        String name = split[1];//name of the face, used for file name, and if regex isn't supplied, becomes the regex
        String regex;
        String file;//or the URL...

        if (command.equalsIgnoreCase("addface")) {//a new face

            if (faceMap.containsKey(name)) {//!addface is not !changeface, remove the face first or do changeface
                toReturn.setResponseText("Failed to add face, " + name + " already exists!");
                return toReturn;
            }

            if (split.length == 4) {//!addface <name> <regex> <URL or file>
                regex = split[2];
                //regex check
                if (!Utils.checkRegex(regex)) {
                    toReturn.setResponseText("Failed to add face, the supplied regex does not compile!");
                    return toReturn;
                }
                //name check (for saving the file)
                if (Utils.checkName(name)) {
                    toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                    return toReturn;
                }

                file = split[3];
                if (file.startsWith("http")) {//online
                    return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"), regex, FACE_TYPE.NORMAL_FACE);//save locally

                } else {//local
                    if (Utils.checkName(file) || localCheck) {
                        if (!localCheck)
                            toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                        else toReturn.setResponseText("Failed to add face, the local directory is not set properly!");
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
                    toReturn.setResponseText("Failed to add face, the supplied name is not a valid regex!");
                    return toReturn;
                }
                //name check (for saving the file)
                if (Utils.checkName(name)) {
                    toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                    return toReturn;
                }
                if (file.startsWith("http")) {//online
                    return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                            Utils.setExtension(name, ".png"), name, FACE_TYPE.NORMAL_FACE);//name is regex, so case sensitive
                } else {//local
                    if (Utils.checkName(file) || localCheck) {
                        if (!localCheck)
                            toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                        else toReturn.setResponseText("Failed to add face, the local directory is not set properly!");
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
                            toReturn.setResponseText("Failed to change face, make sure to designate the \"2\" in the command!");
                            return toReturn;
                        }
                    } catch (Exception e) {
                        toReturn.setResponseText("Failed to change face, the indicator number cannot be parsed!");
                        return toReturn;
                    }

                    regex = split[3];
                    //regex check
                    if (!Utils.checkRegex(regex)) {
                        toReturn.setResponseText("Failed to add face, the supplied regex does not compile!");
                        return toReturn;
                    }

                    //name check (for saving the file)
                    if (Utils.checkName(name)) {
                        toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                        return toReturn;
                    }

                    file = split[4];
                    if (file.startsWith("http")) {//online
                        return downloadFace(file, GUIMain.currentSettings.faceDir.getAbsolutePath(),
                                Utils.setExtension(name, ".png"), regex, FACE_TYPE.NORMAL_FACE);//save locally
                    } else {//local
                        if (Utils.checkName(file) || localCheck) {
                            if (!localCheck)
                                toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                            else
                                toReturn.setResponseText("Failed to add face, the local directory is not set properly!");
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
                        toReturn.setResponseText("Failed to change face, the indicator number cannot be parsed!");
                        return toReturn;
                    }
                    Face face = faceMap.get(name);
                    if (type == 0) {//regex change; !changeface <name> 0 <new regex>
                        regex = split[3];
                        if (Utils.checkRegex(regex)) {
                            faceMap.put(name, new Face(regex, face.getFilePath()));
                            toReturn.setResponseText("Successfully changed the regex for face: " + name + " !");
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
                                    toReturn.setResponseText("Failed to add face, the supplied name is not Windows-friendly!");
                                else
                                    toReturn.setResponseText("Failed to add face, the local directory is not set properly!");
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
                toReturn.setResponseText("Failed to change face, the face " + name + " does not exist!");
            }
        }
        return toReturn;
    }
}