package util;

import gui.GUIMain;
import irc.IRCBot;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

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
     * Gets the extension of a file.
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

    public static String[] loadStreams(File f) {
        ArrayList<String> streams = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    streams.add(line);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return streams.toArray(new String[streams.size()]);
    }

    public static void loadSounds(HashMap<String, Sound> map, File f) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                if (split != null) {
                    int startIdx = line.indexOf(",", line.indexOf(",", 0) + 1);//name,0,<- bingo
                    String[] split2add = line.substring(startIdx + 1).split(",");//files
                    int perm = 0;
                    try {
                        perm = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        System.out.println(split[0] + " has a problem. Making it public.");
                    }
                    map.put(split[0], new Sound(perm, split2add));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks to see if a Pircbot is in a given channel.
     * @param v The Pircbot to check.
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

    public static String[] readList(JList<String> list) {
        ArrayList<String> things = new ArrayList<>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            String o = list.getModel().getElementAt(i);
            if (o != null) {
                things.add(o);
            }
        }
        return things.toArray(new String[things.size()]);
    }

    public static void handleList(HashSet<String> set, String[] toAdd) {
        if (set != null && toAdd != null && toAdd.length > 0) {
            for (String s : toAdd) {
                if (set.contains(s)) continue;
                set.add(s);
            }
        }
    }

    public static void saveStreams(HashSet<String> set, File f) {
        String[] save = set.toArray(new String[set.size()]);
        try {
            PrintWriter br = new PrintWriter(f);
            if (save.length > 0) {
                for (String s : save) {
                    if (s != null) {
                        br.println(s);
                    }
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveSounds(HashMap<String, Sound> map, File f) {
        try {
            PrintWriter br = new PrintWriter(f);
            String[] sounds = map.keySet().toArray(new String[map.keySet().size()]);
            for (String s : sounds) {
                if (s != null && map.get(s) != null) {
                    Sound boii = map.get(s);//you're too young to play that sound, boy
                    StringBuilder sb = new StringBuilder();
                    sb.append(s);
                    sb.append(",");
                    sb.append(boii.getPermission());
                    for (String soundboy : boii.getSounds().data) {
                        sb.append(",");
                        sb.append(soundboy);
                    }
                    br.println(sb.toString());
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single string to an array of strings, first checking to see if the array contains it.
     * @param toAdd The string(s) to add to the array.
     * @param array The array to add the string to.
     * @return The array of Strings.
     */
    public static String[] addStringsToArray(String[] array, String... toAdd) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        for (String s : toAdd) {
            if (!list.contains(s)) list.add(s);//gotta check those repetitives, maaaan.
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Compares two arrays of Strings and adds the non-repeating ones to the same one.
     * @param list List of strings to compare to.
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
     */
    public static String[] checkFiles(String... files) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : files) {
            File test = new File(s);
            if (test.exists() && test.length() > 0) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Checks to see if the files are actually existing and non-blank.
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

    public static void loadPropData(File file, int type) {
        Properties p = new Properties();
        if (type == 0) {//accounts
            try {
                p.load(new FileInputStream(file));
                GUIMain.userNorm = p.getProperty("UserNorm");
                GUIMain.userNormPass = p.getProperty("UserNormPass");
                GUIMain.userBot = p.getProperty("UserBot");
                GUIMain.userBotPass = p.getProperty("UserBotPass");
                if (p.getProperty("AutoLog") != null && p.getProperty("AutoLog").equalsIgnoreCase("true")) {
                    GUIMain.autoLog = true;
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        if (type == 1) { //defaults
            try {
                p.load(new FileInputStream(file));
                GUIMain.defaultFaceDir = p.getProperty("FaceDir");
                GUIMain.defaultSoundDir = p.getProperty("SoundDir");
                if (p.getProperty("UseMod") != null && p.getProperty("UseMod").equals("true")) {
                    GUIMain.useMod = true;
                    GUIMain.customMod = p.getProperty("CustomMod");
                }
                if (p.getProperty("UseBroad") != null && p.getProperty("UseBroad").equals("true")) {
                    GUIMain.useBroad = true;
                    GUIMain.customBroad = p.getProperty("CustomBroad");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveDefaults(String face, String sound, boolean useMod, boolean useBroad, String mod, String broad) {
        Properties p = new Properties();
        if (face != null) {
            p.put("FaceDir", face);
        }
        if (sound != null) {
            p.put("SoundDir", sound);
        }
        p.put("UseMod", String.valueOf(useMod));
        if (mod != null) {
            p.put("CustomMod", mod);
        }
        p.put("UseBroad", String.valueOf(useBroad));
        if (broad != null) {
            p.put("CustomBroad", broad);
        }
        try {
            p.store(new FileWriter(GUIMain.defaultsFile), "Default Settings");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean saveAccountData() {
        Properties p = new Properties();
        if (GUIMain.rememberNorm) {
            p.put("UserNorm", GUIMain.viewer.getMaster());
            p.put("UserNormPass", GUIMain.viewer.getPass());
        }
        if (GUIMain.rememberBot) {
            p.put("UserBot", GUIMain.bot.getUser());
            p.put("UserBotPass", GUIMain.bot.getPass());
        }
        if (GUIMain.autoLog) {
            p.put("AutoLog", String.valueOf(GUIMain.autoLog));
        }
        try {
            p.store(new FileWriter(GUIMain.accountsFile), "Account Info");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Removes a file extension from a path.
     * @param s The path to a file, or the file and its extension.
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
     * @param input The input in question.
     * @return The given input if it checks out, otherwise nothing.
     */
    public static String checkText(String input) {
        return input != null && input.length() > 0 && input.trim().length() > 0 ? input : "";
    }

    /**
     * Returns a number between a given minimum and maximum (exclusive).
     * @param min The minimum number to generate on.
     * @param max The non-inclusive maximum number to generate on.
     * @return Some random number between the given numbers.
     */
    public static int random(int min, int max) {
        return min + (max == min ? 0 : r.nextInt(max - min));
    }

    /**
     * Generates a color from the #hashCode() of any java.lang.Object.
     *
     * Author - Dr_Kegel from Gocnak's stream.
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


    public static void buildImages(HashMap<String, String> map, File f) {
        if (f != null && f.exists() && f.length() > 0) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(f.toURI().toURL().openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(",");
                    if (split != null) {
                        map.put(split[0], split[1]);
                    }
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            map.put("4Head", Utils.class.getResource("/resource/4Head.png").getFile());
            map.put("AsianGlow", Utils.class.getResource("/resource/AsianGlow.png").getFile());
            map.put("BCWarrior", Utils.class.getResource("/resource/BCWarrior.png").getFile());
            map.put("BibleThump", Utils.class.getResource("/resource/BibleThump.png").getFile());
            map.put("BionicBunion", Utils.class.getResource("/resource/BionicBunion.png").getFile());
            map.put("BloodTrail", Utils.class.getResource("/resource/BloodTrail.png").getFile());
            map.put("BORT", Utils.class.getResource("/resource/BORT.png").getFile());
            map.put("DansGame", Utils.class.getResource("/resource/DansGame.png").getFile());
            map.put("DatSheffy", Utils.class.getResource("/resource/DatSheffy.png").getFile());
            map.put("FailFish", Utils.class.getResource("/resource/FailFish.png").getFile());
            map.put("FrankerZ", Utils.class.getResource("/resource/FrankerZ.png").getFile());
            map.put("GabeN", Utils.class.getResource("/resource/GabeN.png").getFile());
            map.put("Kappa", Utils.class.getResource("/resource/Kappa.png").getFile());
            map.put("KevinTurtle", Utils.class.getResource("/resource/KevinTurtle.png").getFile());
            map.put("Kreygasm", Utils.class.getResource("/resource/Kreygasm.png").getFile());
            map.put("MVGame", Utils.class.getResource("/resource/MVGame.png").getFile());
            map.put("OpieOP", Utils.class.getResource("/resource/OpieOP.png").getFile());
            map.put("OneHand", Utils.class.getResource("/resource/OneHand.png").getFile());
            map.put("OMGScoots", Utils.class.getResource("/resource/OMGScoots.png").getFile());
            map.put("PogChamp", Utils.class.getResource("/resource/PogChamp.png").getFile());
            map.put("PJSalt", Utils.class.getResource("/resource/PJSalt.png").getFile());
            map.put("ResidentSleeper", Utils.class.getResource("/resource/ResidentSleeper.png").getFile());
            map.put("RuleFive", Utils.class.getResource("/resource/RuleFive.png").getFile());
            map.put("SwiftRage", Utils.class.getResource("/resource/SwiftRage.png").getFile());
            map.put("Sully", Utils.class.getResource("/resource/Sully.png").getFile());
            map.put("TriHard", Utils.class.getResource("/resource/TriHard.png").getFile());
            map.put("<3", Utils.class.getResource("/resource/heart.png").getFile());
            map.put("o_o", Utils.class.getResource("/resource/eh.png").getFile());
            map.put(":\\", Utils.class.getResource("/resource/ehh.png").getFile());
            map.put(":z", Utils.class.getResource("/resource/eyes.png").getFile());
            map.put(":d", Utils.class.getResource("/resource/grin.png").getFile());
            map.put("d:", Utils.class.getResource("/resource/aww.png").getFile());
            map.put(":)", Utils.class.getResource("/resource/smile.png").getFile());
            map.put(";p", Utils.class.getResource("/resource/winktongue.png").getFile());
            map.put(";)", Utils.class.getResource("/resource/wink.png").getFile());
            map.put(">(", Utils.class.getResource("/resource/wah.png").getFile());
            map.put("r)", Utils.class.getResource("/resource/pirate.png").getFile());
            map.put(":o", Utils.class.getResource("/resource/oh.png").getFile());
            map.put(":p", Utils.class.getResource("/resource/tongue.png").getFile());
            map.put("b)", Utils.class.getResource("/resource/cool.png").getFile());
            map.put(":(", Utils.class.getResource("/resource/cry.png").getFile());
        }
    }


    public static void saveFaces(HashMap<String, String> map, File f) {
        try {
            PrintWriter br = new PrintWriter(f);
            String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
            for (String s : keys) {
                if (s != null && map.get(s) != null) {
                    br.println(s + "," + map.get(s));
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadUserColors(HashMap<String, int[]> colorMap, File f) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                if (split != null) {
                    colorMap.put(split[0], new int[]{Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])});
                }                 //user                          r                            g                       b
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveUserColors(HashMap<String, int[]> colorMap, File f) {
        try {
            PrintWriter br = new PrintWriter(f);
            String[] keys = colorMap.keySet().toArray(new String[colorMap.size()]);
            for (String s : keys) {
                if (s != null && colorMap.get(s) != null) {
                    br.println(s + "," + colorMap.get(s)[0] + "," + colorMap.get(s)[1] + "," + colorMap.get(s)[2]);
                }              //user              R                   G                          B
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadCommands(HashMap<StringArray, Timer> map, File f) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\[");
                if (split != null) {
                    int time;
                    try {
                        time = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        time = 10000;
                    }
                    Timer local = new Timer(time);
                    map.put(new StringArray(new String[]{split[0], split[1]}), local);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCommands(HashMap<StringArray, Timer> map, File f) {
        try {
            PrintWriter br = new PrintWriter(f);
            StringArray[] set = map.keySet().toArray(new StringArray[map.keySet().size()]);
            for (StringArray next : set) {
                if (next != null && map.get(next) != null) {
                    String name = next.data[0];
                    String command = next.data[1];
                    int time = (int) map.get(next).period;
                    br.println(name + "[" + command + "[" + time);
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
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
     * Checks whether the given nick that sent a message is an Operator or not.
     *
     * @param c The bot that is connected to the channel. Viewer is usually the default.
     * @param channel The channel the user is in.
     * @param nick The nick of the user.
     * @return true if the user is an operator; otherwise false.
     */
    public static boolean isUserOp(PircBot c, String channel, String nick) {
        if (c == null || channel == null || nick == null) return false;
        User[] users = c.getUsers(channel);
        for (User u : users) {
            if (u != null) {
                if (u.getNick().equalsIgnoreCase(nick)) {
                    return u.isOp();
                }
            }
        }
        return false;
    }

    /**
     * Adds a command to the command map.
     *
     * To do this in chat, simply type !addcommand command time message
     * More examples at http://bit.ly/1366RwM
     * @param map The command map to add to.
     * @param s The string from the chat.
     */
    public static void addCommands(HashMap<StringArray, Timer> map, String s) {
        String[] split = s.split(" ");
        if (map != null && split != null) {
            String name = split[1];//name of the command, [0] is "addcommand"
            int time;//for timer
            try {
                try {
                    time = Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    return;
                }
                int firstspace = s.indexOf(" ");//IGNORE
                int secondspace = s.indexOf(" ", firstspace + 1);//IGNORE
                int thirdspace = s.indexOf(" ", secondspace + 1);//IGNORE
                String message = s.substring(thirdspace + 1);//BINGO
                time = handleInt(time);
                if (time > 0 && name != null && message != null) {
                    Timer local = new Timer(time);
                    map.put(new StringArray(new String[]{name, message}), local);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes a command from the command map.
     * @param map The map the commands are in.
     * @param key The !command trigger, or key.
     */
    public static void removeCommands(HashMap<StringArray, Timer> map, String key) {
        if (map != null && key != null) {
            Set<StringArray> set = map.keySet();
            for (StringArray next : set) {
                String name = next.data[0];
                if (key.equals(name)) {
                    map.remove(next);
                    return;
                }
            }
        }
    }

    /**
     * Checks to see if a certain string was a key to a command.
     * @param map The map the commands are in.
     * @param s The string in question.
     * @return True if the string in question was indeed a key for a command; else false.
     */
    public static boolean commandTrigger(HashMap<StringArray, Timer> map, String s) {
        if (map != null && s != null) {
            Set<StringArray> set = map.keySet();
            for (StringArray next : set) {
                String name = next.data[0];
                if (s.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Changes a face to have a new file, or add the face to the map.
     *
     * @param s The latter part of the !addface or !changeface command.
     */
    public static void handleFace(String s) {
        if (GUIMain.defaultFaceDir != null && !GUIMain.defaultFaceDir.equals("null")) {
            String[] split = s.split(" ");
            String name = split[1];
            String file = split[2];
            String filename = GUIMain.defaultFaceDir + File.separator + file;
            if (areFilesGood(filename)) {
                GUIMain.imgMap.put(name, filename);
            }
        }
    }

    /**
     * Get the Message from the !command trigger.
     * @param map The map the commands are stored in.
     * @param key The !command trigger, or key.
     * @return The message that the command triggers.
     */
    public static String getMessage(HashMap<StringArray, Timer> map, String key) {
        String mess = "";
        if (map != null && key != null) {
            Set<StringArray> set = map.keySet();
            for (StringArray next : set) {
                String name = next.data[0];
                if (name != null) {
                    if (key.equals(name)) {
                        mess = next.data[1];
                        break;
                    }
                }
            }
        }
        return mess;
    }

    /**
     * Gets the timer for a command.
     * @param map The command map the commands are stored in.
     * @param key The !command trigger, or key, of the command.
     * @return The Timer of the command.
     */
    public static Timer getTimer(HashMap<StringArray, Timer> map, String key) {
        Timer local = new Timer(10000);
        Set<StringArray> set = map.keySet();
        for (StringArray next : set) {
            String name = next.data[0];
            if (name != null) {
                if (key.equals(name)) {
                    local = map.get(next);
                    break;
                }
            }
        }
        return local;
    }

    /**
     * Checks the red, green, and blue in order to show up in botnak.
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return If the ints meet the specification.
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
     * @param s The string from the chat to manipulate.
     * @param change True for changing a sound, false for adding.
     */
     public static void handleSound(String s, boolean change) {
         if (GUIMain.defaultSoundDir != null && !GUIMain.defaultSoundDir.equals("null") && !GUIMain.defaultSoundDir.equals("")) {
             try {
                 String[] split = s.split(" ");
                 String name = split[1];//both commands have this in common.
                 int perm = -1;
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
                         String filename = GUIMain.defaultSoundDir + File.separator + files + ".wav";
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
                             list.add(GUIMain.defaultSoundDir + File.separator + str + ".wav");
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
                             if (GUIMain.soundMap.containsKey(name)) GUIMain.soundMap.put(name, new Sound(perm, filesSplit));
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
                             String test = GUIMain.defaultSoundDir + File.separator + split[2] + ".wav";
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
                                 list.add(GUIMain.defaultSoundDir + File.separator + str + ".wav");
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
                             String test = GUIMain.defaultSoundDir + File.separator + split[2] + ".wav";
                             if (Utils.areFilesGood(test)) {
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
                 e.printStackTrace();
             }
         }

     }

}
