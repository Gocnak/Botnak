package irc;

import gui.GUIMain;
import lib.chatbot.ChatterBotSession;
import lib.chatbot.Cleverbot;
import lib.pircbot.org.jibble.pircbot.PircBot;
import util.Sound;
import util.StringArray;
import util.Timer;
import util.Utils;
import lib.chatbot.ChatterBot;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * //TODO
 * Finish "Nice" soundlist
 * Expand the files to detect other audio formats (why doesn't mp3 work...) (look into Media class)
 * Look into adding newline for commands, for example !keygasm
 */
public class IRCBot extends PircBot {
    public static int soundTime = 5000;
    public static int chatTime = 5000;

    public static boolean stopSound = false;
    public static String masterChannel;
    private String pass;
    public ChatterBot chatBot;
    public ChatterBotSession session;
    public static Timer botnakTimer, soundTimer, soundBackTimer;

    public static String name;

    public String getUser() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public IRCBot(String user, String password) {
        name = user;
        setName(name);
        setLogin(name);
        pass = password;
        masterChannel = GUIMain.viewer.getMaster();
        GUIMain.botUser.setText(name);
        try {
            chatBot = Cleverbot.create();
            session = chatBot.createSession();
            connect("irc.twitch.tv", 6667, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setVerbose(true);
        botnakTimer = new Timer(chatTime);
        soundTimer = new Timer(soundTime);
        soundBackTimer = new Timer(0);
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!isConnected()) {
            try {
                connect("irc.twitch.tv", 6667, pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            joinChannel(channelName);
            if (Utils.isInChannel(this, channelName)) {
                if (!GUIMain.channelMap.contains(channel)) GUIMain.channelMap.add(channel);
            }
        }
    }

    public void doLeave(String channel, boolean forget) {
        String channelName = "#" + channel;
        if (Utils.isInChannel(this, channelName)) {
            partChannel(channelName);
        }
        if (forget) {
            if (GUIMain.channelMap.contains(channel)) {
                GUIMain.channelMap.remove(channel);
            }
        }
    }

    public static String lastChannel = "";
    public static String lastMessage = "";
    public static boolean firstTime = true;
    public static String secondToLastMessage = "";


    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (message != null && channel != null && sender != null) {
            String low = message.toLowerCase();
            //un-shorten short URLs
            if (low.contains("bit.ly/") || low.contains("tinyurl.com/") || low.contains("goo.gl")) {
                String[] split = message.split(" ");
                for (String s : split) {
                    if (s.contains("http")) {
                        sendMessage(channel, Utils.getLongURL(s));
                    }
                }
            }
            //commands
            if (message.startsWith("!")) {
                String content = message.substring(1).split(" ")[0].toLowerCase();
                if (content != null) {
                    //dev
                    if (sender.equals(GUIMain.viewer.getMaster())) {
                        handleDev(channel, message.substring(1));
                    }
                    //mod
                    if (GUIMain.modMap.contains(sender)) {
                        handleMod(channel, message.substring(1));
                    }
                    //sound
                    if (soundTrigger(content, sender, channel)) {
                        GUIMain.soundMap.get(content).play();
                    }
                    //color
                    if (content.startsWith("setcol")) {
                        handleColor(sender, message.substring(1));
                    }
                    //reply
                    if (content.equals("ask")) {
                        if (!botnakTimer.isRunning()) {
                            talkBack(channel, sender, message.substring(4));
                            botnakTimer.reset();
                        }
                    }
                    //command
                    if (Utils.commandTrigger(GUIMain.commandMap, content)) {
                        handleMessage(channel, content);
                    }
                }
            }
        }
    }

    public void talkBack(String channel, String sender, String message) {
        if (channel != null && sender != null && message != null && session != null && chatBot != null) {
            String reply = "";
            if (sender.equals(GUIMain.viewer.getMaster())) {
                sender = "Master";
            }
            try {
                reply = session.think(message);
            } catch (Exception e) {
                if (e.getCause() instanceof ConnectException) {
                    session = chatBot.createSession();
                }
            }
            if (!reply.equals("")) {
                sendMessage(channel, sender + ", " + reply);
            }
        }
    }

    public static void handleColor(String user, String mess) {
        if (user != null && mess != null) {
            Color usercolor = Utils.getColor(user.hashCode());
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
                if (!Utils.checkInts(R, G, B)) {//see if at least one is > 99
                    Utils.updateHashMap(GUIMain.userColMap, user, new int[]{usercolor.getRed(), usercolor.getGreen(), usercolor.getBlue()});
                } else {
                    Utils.updateHashMap(GUIMain.userColMap, user, new int[]{R, G, B});
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
                    if (!Utils.checkInts(R, G, B)) {
                        Utils.updateHashMap(GUIMain.userColMap, user, new int[]{usercolor.getRed(), usercolor.getGreen(), usercolor.getBlue()});
                    } else {
                        Utils.updateHashMap(GUIMain.userColMap, user, new int[]{R, G, B});
                    }
                }
            }

        }
    }


    /**
     * Boot trigger for sounds. Checks if a dev sound is not playing, if the general delay is up,
     * if the channel is yours, and if the user can even play the sound if it exists.
     *
     * @param s       Sound command's trigger/name.
     * @param send    The sender of the command.
     * @param channel Channel the command was in.
     * @return true to play the sound, else false
     */
    public static boolean soundTrigger(String s, String send, String channel) {
        if (!soundBackTimer.isRunning()) {//check from a dev song
            soundBackTimer = new Timer(0);//reset the backup sound timer, and
            if (soundTimer.period > soundTime) {//check if the sound was longer (which is mostly true)
                soundTimer = new Timer(soundTime);//reset it so you don't have to
            }
        }
        if (!soundTimer.isRunning()) {//not on a delay
            if (channel.equalsIgnoreCase("#" + masterChannel)) {//is in main channel
                if (soundCheck(s, send)) {//let's check the existence/permission
                    soundTimer.reset();
                    return true;//HIT THAT SHIT
                }
            }
        }
        return false;
    }

    /**
     * Checks the existence of a sound, and the permission of the requester.
     *
     * @param sound  Sound trigger
     * @param sender Sender of the command trigger.
     * @return false if the sound is not allowed, else true if it is.
     */
    public static boolean soundCheck(String sound, String sender) {
        //set the permission
        int permission = Sound.PERMISSION_ALL;
        if (GUIMain.modMap.contains(sender)) {
            permission = Sound.PERMISSION_MOD;
        }
        if (GUIMain.viewer.getMaster().equals(sender)) {
            permission = Sound.PERMISSION_DEV;
        }
        String[] keys = GUIMain.soundMap.keySet().toArray(new String[GUIMain.soundMap.keySet().size()]);
        for (String s : keys) {
            if (s != null && s.equalsIgnoreCase(sound)) {
                Sound snd = GUIMain.soundMap.get(s);
                if (snd != null) {
                    int perm = snd.getPermission();
                    //check permission
                    if (permission >= perm) {//descending permission, this should work; devs can play mod and all sounds, etc.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void handleMessage(String channel, String message) {
        String cont = Utils.getMessage(GUIMain.commandMap, message);
        Timer timer = Utils.getTimer(GUIMain.commandMap, message);
        if (!cont.equals("")) {
            if (!timer.isRunning()) {
                sendMessage(channel, cont);
                if (channel.equals(lastChannel)) {
                    if (message.equals(lastMessage) || message.equals(secondToLastMessage)) {
                        Utils.updateHashMap(GUIMain.commandMap, new StringArray(new String[]{message, cont}), new Timer(timer.period));
                    }
                }
                if (firstTime) {
                    firstTime = !firstTime;
                } else {
                    secondToLastMessage = message;
                    firstTime = !firstTime;
                }
                lastChannel = channel;
                lastMessage = message;
            }
        }
    }


    public static String privateMessage = "";

    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        privateMessage = message;
    }

    public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        if (GUIMain.viewer != null) {
            if (channel.substring(1).equals(GUIMain.viewer.getMaster())) {
                if (!GUIMain.modMap.contains(recipient)) {
                    GUIMain.modMap.add(recipient);
                }
            }
        }
    }

    public String getPrivateMessage() {
        return privateMessage;
    }

    public void handleDev(String channel, String s) {
        if (s.startsWith("setreply")) {
            try {
                chatTime = Integer.parseInt(s.split(" ")[1]);
            } catch (Exception e) {
                chatTime = (int) botnakTimer.period;
            }
            chatTime = Utils.handleInt(chatTime);
            botnakTimer = new Timer(chatTime);
        }
        if (s.startsWith("removesound")) {
            String remove = s.split(" ")[1];
            if (GUIMain.soundMap.containsKey(remove)) {
                GUIMain.soundMap.remove(remove);
            }
        }
        if (s.startsWith("setmods")) {
            Utils.buildMods(this, channel, GUIMain.modMap);
        }
        if (s.startsWith("savemods")) {
            Utils.saveMods(GUIMain.modMap, GUIMain.modsFile);
        }
        handleMod(channel, s);
    }

    public void handleMod(String channel, String s) {
        if (channel.substring(1).equals(GUIMain.viewer.getMaster())) {
/*            if (s.startsWith("songrq")) {
                String req = s.split(" ")[1];
                if (req != null && !req.equals("")) {
                    if (req.contains("youtube")) {
                        String id = req.split("=")[1];
                        String base = "http://www.youtube.com/embed/";
                        util.Utils.openSong(base + id); TODO make this work without focus
                    }
                }
            }*/
            if (s.startsWith("addcommand")) {
                Utils.addCommands(GUIMain.commandMap, s);
            }
            if (s.startsWith("removecommand")) {
                Utils.removeCommands(GUIMain.commandMap, s.split(" ")[1]);
            }
            if (s.startsWith("changeface")) {
                if (GUIMain.defaultFaceDir != null && !GUIMain.defaultFaceDir.equals("null")) {
                    String[] split = s.split(" ");
                    String name = split[1];
                    String file = split[2];
                    String filename = GUIMain.defaultFaceDir + File.separator + file;
                    File test = new File(filename);
                    if (test.exists() && test.length() > 0 && GUIMain.imgMap.containsKey(name)) {
                        Utils.updateHashMap(GUIMain.imgMap, name, filename);
                    }
                }
            }
            if (s.startsWith("addface")) {
                if (GUIMain.defaultFaceDir != null && !GUIMain.defaultFaceDir.equals("null")) {
                    String[] split = s.split(" ");
                    String name = split[1];
                    String file = split[2];
                    String filename = GUIMain.defaultFaceDir + File.separator + file;
                    File test = new File(filename);
                    if (test.exists() && test.length() > 0 && !GUIMain.imgMap.containsKey(name)
                            && !GUIMain.imgMap.containsValue(filename)) {
                        GUIMain.imgMap.put(name, filename);
                    }
                }
            }
            if (s.startsWith("removeface")) {
                String[] split = s.split(" ");
                String toremove = split[1];
                if (GUIMain.imgMap.containsKey(toremove)) {
                    GUIMain.imgMap.remove(toremove);
                }
            }
            //                                       0 |  1  |2|    3
            if (s.startsWith("addsound")) { //!addsound gaben 0 gaben0(,gaben1,etc)
                if (GUIMain.defaultSoundDir != null && !GUIMain.defaultSoundDir.equals("null") && !GUIMain.defaultSoundDir.equals("")) {
                    try {
                        HashMap<String, Sound> map = GUIMain.soundMap;
                        String[] split = s.split(" ");
                        String name = split[1];
                        int perm = -1;
                        try {
                            perm = Integer.parseInt(split[2]);
                        } catch (Exception e) {
                            return;
                        }
                        String files = split[3];
                        if (perm == -1) return;
                        if (!files.contains(",")) {//isn't multiple
                            String filename = GUIMain.defaultSoundDir + File.separator + files + ".wav";
                            if (Utils.areFilesGood(filename)) {
                                if (map.containsKey(name)) {//they could technically change the permission here as well
                                    Utils.updateHashMap(map, name, new Sound(perm,// add it tooo it maaan
                                            Utils.addStringToArray(filename, map.get(name).getSounds().data)));
                                } else { //*gasp* A NEW SOUND!?
                                    GUIMain.soundMap.put(name, new Sound(perm, filename));
                                }
                            }
                        } else {//is multiple
                            ArrayList<String> list = new ArrayList<>();
                            String[] filesSplit = files.split(",");
                            for (String str : filesSplit) {
                                list.add(GUIMain.defaultSoundDir + File.separator + str + ".wav");
                            }             //calls the areFilesGood boolean in it (filters bad files already)
                            filesSplit = Utils.checkFiles(list.toArray(new String[list.size()]));
                            list.clear();//recycle time!
                            if (map.containsKey(name)) {
                                Collections.addAll(list, map.get(name).getSounds().data);
                                Utils.checkAndAdd(list, filesSplit);//checks for repetition
                                Utils.updateHashMap(map, name, new Sound(perm, list.toArray(new String[list.size()])));
                            } else {// NEW SOUNDS CONFIRMED
                                map.put(name, new Sound(perm, filesSplit));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (s.startsWith("changesound")) {
                try {
                    int perm = -1;
                    HashMap<String, Sound> map = GUIMain.soundMap;
                    String[] split = s.split(" ");// 0     1       2                 3
                    String name = split[1];//!changesound name perm/newfile(s) maybe(,some,more,sounds)
                    if (split.length > 3) {//has the full shebang ^
                        try {
                            perm = Integer.parseInt(split[2]);
                        } catch (Exception e) {//bitch! yo numba's BROKE
                            e.printStackTrace();
                            return;
                        }
                        if (perm != -1) {
                            if (split[3].contains(",")) {//multiple
                                String[] filesSplit = split[3].split(",");
                                ArrayList<String> list = new ArrayList<>();
                                for (String str : filesSplit) {
                                    list.add(GUIMain.defaultSoundDir + File.separator + str + ".wav");
                                }             //calls the areFilesGood boolean in it (filters bad files already)
                                filesSplit = Utils.checkFiles(list.toArray(new String[list.size()]));
                                Utils.updateHashMap(map, name, new Sound(perm, filesSplit));
                            } else {//singular
                                String test = GUIMain.defaultSoundDir + File.separator + split[3] + ".wav";
                                if (Utils.areFilesGood(test)) {
                                    Utils.updateHashMap(map, name, new Sound(perm, test));
                                }
                            }
                        }
                    }
                    if (split.length == 3) {
                        if (split[2].length() == 1) {//ASSUMING it's a permission change.
                            try {
                                perm = Integer.parseInt(split[2]);//I mean come on. What sound will have a 1 char name?
                                if (perm != -1) {
                                    Utils.updateHashMap(map, name, new Sound(perm,
                                            map.get(name).getSounds().data));//A pretty bad one...
                                }
                            } catch (NumberFormatException e) {//maybe it really is a 1-char-named sound?
                                String test = GUIMain.defaultSoundDir + File.separator + split[2] + ".wav";
                                if (Utils.areFilesGood(test)) { //wow...
                                    Utils.updateHashMap(map, name, new Sound(map.get(name).getPermission(), test));
                                }
                            }
                        } else { //it's a/some new file(s) as replacement!
                            if (split[2].contains(",")) {//multiple
                                String[] filesSplit = split[2].split(",");
                                ArrayList<String> list = new ArrayList<>();
                                for (String str : filesSplit) {
                                    list.add(GUIMain.defaultSoundDir + File.separator + str + ".wav");
                                }             //calls the areFilesGood boolean in it (filters bad files already)
                                filesSplit = Utils.checkFiles(list.toArray(new String[list.size()]));
                                Utils.updateHashMap(map, name, new Sound(map.get(name).getPermission(), filesSplit));
                            } else {//not
                                String test = GUIMain.defaultSoundDir + File.separator + split[2] + ".wav";
                                if (Utils.areFilesGood(test)) {
                                    Utils.updateHashMap(map, name, new Sound(map.get(name).getPermission(), test));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (s.startsWith("soundstate")) {
                int delay = soundTime / 1000;
                sendMessage(channel, "Sound is currently turned " + (stopSound ? "OFF" : "ON") + " with "
                        + (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds.")));
            }
            if (s.startsWith("togglesound")) {
                stopSound = !stopSound;
                sendMessage(channel, "Sound is now turned " + (stopSound ? "OFF" : "ON"));
                soundTimer = new Timer(soundTime);
            }
            if (s.startsWith("setsound")) {
                try {
                    soundTime = Integer.parseInt(s.split(" ")[1]);
                } catch (Exception e) {
                    return;
                }
                soundTime = Utils.handleInt(soundTime);
                int delay = soundTime / 1000;
                sendMessage(channel, "Sound delay " + (delay < 2 ? (delay == 0 ? "off." : "is now 1 second.") : ("is now " + delay + " seconds.")));
                soundTimer = new Timer(soundTime);
            }
        }
    }


}
