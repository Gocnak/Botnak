package util;

import face.Face;
import face.SubscriberIcon;
import face.TwitchFace;
import gui.ChatPane;
import gui.CombinedChatPane;
import gui.GUIMain;
import irc.Donator;
import irc.IRCBot;
import irc.IRCViewer;
import sound.Sound;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is the container for every setting Botnak has.
 * There's accounts, booleans of all sorts, and ints, you name it.
 * <p/>
 * What is unique about this is you can define an "account" which
 * may be in the future to prevent unnecessary logging out. For now
 * we can continue to use a "default" account.
 */
public class Settings {

    //accounts
    public Account user = null;
    public Account bot = null;
    public boolean autoLogin = false;
    public boolean rememberNorm = false;
    public boolean rememberBot = false;

    //custom directories
    public String defaultSoundDir = "";
    public String defaultFaceDir = "";

    //custom sound
    public Sound subSound = null;

    //icons
    public URL modIcon;
    public URL broadIcon;
    public URL adminIcon;
    public URL staffIcon;
    public URL turboIcon;
    public boolean useMod = false;//"should use a custom mod icon"
    public boolean useBroad = false;
    public boolean useAdmin = false;
    public boolean useStaff = false;

    //font
    public Font font;

    //directories
    public static File defaultDir = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()
            + File.separator + "Botnak");
    public File faceDir = new File(defaultDir + File.separator + "Faces");
    public File nameFaceDir = new File(defaultDir + File.separator + "NameFaces");
    public File twitchFaceDir = new File(defaultDir + File.separator + "TwitchFaces");
    public File subIconsDir = new File(defaultDir + File.separator + "SubIcons");
    public File logDir = new File(defaultDir + File.separator + "Logs");
    //files
    public File accountsFile = new File(defaultDir + File.separator + "acc.ini");
    public File tabsFile = new File(defaultDir + File.separator + "tabs.txt");
    public File soundsFile = new File(defaultDir + File.separator + "sounds.txt");
    public File faceFile = new File(defaultDir + File.separator + "faces.txt");
    public File twitchFaceFile = new File(defaultDir + File.separator + "twitchfaces.txt");
    public File userColFile = new File(defaultDir + File.separator + "usercols.txt");
    public File commandsFile = new File(defaultDir + File.separator + "commands.txt");
    public File ccommandsFile = new File(defaultDir + File.separator + "chatcom.txt");
    public File defaultsFile = new File(defaultDir + File.separator + "defaults.ini");
    public static File lafFile = new File(defaultDir + File.separator + "laf.txt");
    public File keywordsFile = new File(defaultDir + File.separator + "keywords.txt");
    public File subIconsFile = new File(defaultDir + File.separator + "subIcons.txt");
    public File donatorsFile = new File(defaultDir + File.separator + "donators.txt");
    public File namefaceFile = new File(defaultDir + File.separator + "namefaces.txt");

    //appearance
    public boolean logChat = false;
    public int chatMax = 100;
    public boolean cleanupChat = true;
    public static String lookAndFeel = "lib.jtattoo.com.jtattoo.plaf.hifi.HiFiLookAndFeel";
	public int faceMaxHeight = 20;
    //Graphite = "lib.jtattoo.com.jtattoo.plaf.graphite.GraphiteLookAndFeel"


    public String date;

    public Settings() {//default account
        modIcon = Settings.class.getResource("/resource/mod.png");
        broadIcon = Settings.class.getResource("/resource/broad.png");
        adminIcon = Settings.class.getResource("/resource/admin.png");
        staffIcon = Settings.class.getResource("/resource/staff.png");
        turboIcon = Settings.class.getResource("/resource/turbo.png");
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        date = sdf.format(new Date(time));
        logDir.mkdirs();
        font = new Font("Calibri", Font.PLAIN, 18);
        defaultDir.mkdirs();
        faceDir.mkdirs();
        nameFaceDir.mkdirs();
        twitchFaceDir.mkdirs();
        subIconsDir.mkdirs();
    }

    /**
     * This void loads everything Botnak will use, and sets the appropriate settings.
     */
    public void load() {
        if (Utils.areFilesGood(accountsFile.getAbsolutePath())) {
            GUIMain.log("Loading accounts...");
            loadPropData(0);
        }
        if (Utils.areFilesGood(defaultsFile.getAbsolutePath())) {
            GUIMain.log("Loading defaults...");
            loadPropData(1);
        }
        if (Utils.areFilesGood(tabsFile.getAbsolutePath())) {
            GUIMain.log("Loading tabs...");
            loadTabState();
        }
        if (Utils.areFilesGood(soundsFile.getAbsolutePath())) {
            GUIMain.log("Loading sounds...");
            loadSounds();
        }
        if (Utils.areFilesGood(userColFile.getAbsolutePath())) {
            GUIMain.log("Loading user colors...");
            loadUserColors();
        }
        if (Utils.areFilesGood(donatorsFile.getAbsolutePath())) {
            GUIMain.log("Loading donators...");
            loadDonators();
        }
        if (Utils.areFilesGood(commandsFile.getAbsolutePath())) {
            GUIMain.log("Loading text commands...");
            loadCommands();
        }
        if (Utils.areFilesGood(subIconsFile.getAbsolutePath())) {
            GUIMain.log("Loading subscriber icons...");
            loadSubIcons();
        }
        if (Utils.areFilesGood(namefaceFile.getAbsolutePath())) {
            GUIMain.log("Loading Subscriber name faces...");
            loadNameFaces();
        }
        GUIMain.log("Loading keywords...");
        loadKeywords();//first time boot adds the username
        GUIMain.log("Loading console commands...");
        loadConsoleCommands();//has to be out of the check for files for first time boot
        if (Utils.areFilesGood(faceFile.getAbsolutePath())) {
            GUIMain.log("Loading custom faces...");
            loadFaces();
        }
        GUIMain.log("Loading default Twitch faces...");
        if (Utils.areFilesGood(twitchFaceFile.getAbsolutePath())) {
            loadDefaultTwitchFaces();
        }
        Utils.loadDefaultFaces();
    }

    /**
     * This handles saving all the settings that need saved.
     */
    public void save() {
        if (rememberBot || rememberNorm) savePropData(0);
        savePropData(1);
        if (!GUIMain.soundMap.isEmpty()) saveSounds();
        if (!GUIMain.faceMap.isEmpty()) saveFaces();
        saveTwitchFaces();
        saveTabState();
        if (!GUIMain.userColMap.isEmpty()) saveUserColors();
        if (GUIMain.loadedCommands()) saveCommands();
        if (!GUIMain.keywordMap.isEmpty()) saveKeywords();
        if (!GUIMain.subIconSet.isEmpty()) saveSubIcons();
        if (!GUIMain.donators.isEmpty()) saveDonators();
        if (!GUIMain.nameFaceMap.isEmpty()) saveNameFaces();
        saveConCommands();
        saveLAF();
    }

    public static class Account {
        String name, pass;

        public Account(String name, String pass) {
            this.name = name;
            this.pass = pass;
        }

        public String getAccountName() {
            return name;
        }

        public String getAccountPass() {
            return pass;
        }

    }

    /**
     * *********VOIDS*************
     */
    public void loadPropData(int type) {
        Properties p = new Properties();
        if (type == 0) {//accounts
            try {
                p.load(new FileInputStream(accountsFile));
                String userNorm = p.getProperty("UserNorm").toLowerCase();
                String userNormPass = p.getProperty("UserNormPass");
                if (userNorm != null && !userNorm.equals("") && userNormPass != null && !userNormPass.equals("") && userNormPass.contains("oauth")) {
                    user = new Account(userNorm, userNormPass);
                    rememberNorm = true;
                }
                String userBot = p.getProperty("UserBot").toLowerCase();
                String userBotPass = p.getProperty("UserBotPass");
                if (userBot != null && !userBot.equals("") && userBotPass != null && !userBotPass.equals("") && userBotPass.contains("oauth")) {
                    bot = new Account(userBot, userBotPass);
                    rememberBot = true;
                }
                if (p.getProperty("AutoLog") != null && p.getProperty("AutoLog").equalsIgnoreCase("true")) {
                    autoLogin = true;
                }
                if (autoLogin) {
                    if (user != null) {
                        GUIMain.viewer = new IRCViewer(user.getAccountName(), user.getAccountPass());
                    }
                    if (bot != null) {
                        GUIMain.bot = new IRCBot(bot.getAccountName(), bot.getAccountPass());
                    }
                }
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        }
        if (type == 1) {//defaults
            try {
                p.load(new FileInputStream(defaultsFile));
                defaultFaceDir = p.getProperty("FaceDir", "");
                defaultSoundDir = p.getProperty("SoundDir", "");
                useMod = Boolean.parseBoolean(p.getProperty("UseMod", "false"));
                String files = p.getProperty("SubSound", "null");
                if (!files.equalsIgnoreCase("null")) {
                    subSound = new Sound(15, files.split(","));
                }
                try {
                    modIcon = new URL(p.getProperty("CustomMod", modIcon.toString()));
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
                useBroad = Boolean.parseBoolean(p.getProperty("UseBroad", "false"));
                try {
                    broadIcon = new URL(p.getProperty("CustomBroad", broadIcon.toString()));
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
                useAdmin = Boolean.parseBoolean(p.getProperty("UseAdmin", "false"));
                try {
                    adminIcon = new URL(p.getProperty("CustomAdmin", adminIcon.toString()));
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
                useStaff = Boolean.parseBoolean(p.getProperty("UseStaff", "false"));
                try {
                    staffIcon = new URL(p.getProperty("CustomStaff", staffIcon.toString()));
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
                cleanupChat = Boolean.parseBoolean(p.getProperty("ClearChat", "true"));
                logChat = Boolean.parseBoolean(p.getProperty("LogChat", "false"));
                chatMax = Integer.parseInt(p.getProperty("MaxChat", "100"));
	            faceMaxHeight = Integer.parseInt(p.getProperty("FaceMaxHeight", "20"));
                font = Utils.stringToFont(p.getProperty("Font", "Calibri, 18, Plain").split(","));
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        }
    }

    public void savePropData(int type) {
        Properties p = new Properties();
        if (type == 0) {//account data
            if (rememberNorm) {
                p.put("UserNorm", user.getAccountName());
                p.put("UserNormPass", user.getAccountPass());
            }
            if (rememberBot) {
                p.put("UserBot", bot.getAccountName());
                p.put("UserBotPass", bot.getAccountPass());
            }
            if (autoLogin) {
                p.put("AutoLog", "true");
            }
            try {
                p.store(new FileWriter(accountsFile), "Account Info");
            } catch (IOException e) {
                GUIMain.log(e.getMessage());
            }
        }
        if (type == 1) {//deaults data
            if (defaultFaceDir != null && !defaultFaceDir.equals("")) {
                p.put("FaceDir", defaultFaceDir);
            }
            if (defaultSoundDir != null && !defaultSoundDir.equals("")) {
                p.put("SoundDir", defaultSoundDir);
            }
            p.put("UseMod", String.valueOf(useMod));
            p.put("CustomMod", modIcon.toString());
            p.put("UseBroad", String.valueOf(useBroad));
            p.put("CustomBroad", broadIcon.toString());
            p.put("UseAdmin", String.valueOf(useAdmin));
            p.put("CustomAdmin", adminIcon.toString());
            p.put("UseStaff", String.valueOf(useStaff));
            p.put("CustomStaff", staffIcon.toString());
            p.put("MaxChat", String.valueOf(chatMax));
	        p.put("FaceMaxHeight", String.valueOf(faceMaxHeight));
            p.put("ClearChat", String.valueOf(cleanupChat));
            p.put("LogChat", String.valueOf(logChat));
            p.put("Font", Utils.fontToString(font));
            if (subSound != null) {
                String toPut = "";
                for (int i = 0; i < subSound.getSounds().data.length; i++) {
                    toPut = toPut + subSound.getSounds().data[i];
                    if (i != subSound.getSounds().data.length - 1) toPut = toPut + ",";
                }
                p.put("SubSound", toPut);
            } else {
                p.put("SubSound", "null");
            }
            try {
                p.store(new FileWriter(defaultsFile), "Default Settings");
            } catch (IOException e) {
                GUIMain.log(e.getMessage());
            }
        }
    }


    /**
     * Sounds
     */
    public void loadSounds() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(soundsFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                int startIdx = line.indexOf(",", line.indexOf(",", 0) + 1);//name,0,<- bingo
                String[] split2add = line.substring(startIdx + 1).split(",");//files
                int perm = 0;
                try {
                    perm = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    GUIMain.log(split[0] + " has a problem. Making it public.");
                }
                GUIMain.soundMap.put(split[0], new Sound(perm, split2add));
            }
            GUIMain.log("Loaded sounds!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void saveSounds() {
        try {
            PrintWriter br = new PrintWriter(soundsFile);
            for (String s : GUIMain.soundMap.keySet()) {
                if (s != null && GUIMain.soundMap.get(s) != null) {
                    Sound boii = GUIMain.soundMap.get(s);//you're too young to play that sound, boy
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
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * User Colors
     */
    public void loadUserColors() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(userColFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                GUIMain.userColMap.put(split[0], new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
            }
            GUIMain.log("Loaded user colors!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void saveUserColors() {
        try {
            PrintWriter br = new PrintWriter(userColFile);
            for (String s : GUIMain.userColMap.keySet()) {
                if (s != null && GUIMain.userColMap.get(s) != null) {
                    br.println(s + "," +
                            GUIMain.userColMap.get(s).getRed() + "," +
                            GUIMain.userColMap.get(s).getGreen() + "," +
                            GUIMain.userColMap.get(s).getBlue());
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Saves the faces to the given text file.
     * The map is unique, as the key is the name of the face, which could be the same as the regex
     * if it was added via !addface and no regex was specified.
     */
    public void saveFaces() {
        try {
            PrintWriter br = new PrintWriter(faceFile);
            for (String s : GUIMain.faceMap.keySet()) {
                if (s != null && GUIMain.faceMap.get(s) != null) {
                    Face fa = GUIMain.faceMap.get(s);
                    br.println(s + "," + fa.getRegex() + "," + fa.getFilePath());
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Saves the default twitch faces.
     */
    public void saveTwitchFaces() {
        try {
            PrintWriter br = new PrintWriter(twitchFaceFile);
            for (String s : GUIMain.twitchFaceMap.keySet()) {
                if (s != null && GUIMain.twitchFaceMap.get(s) != null) {
                    TwitchFace fa = GUIMain.twitchFaceMap.get(s);
                    br.println(s + "," + fa.getRegex() + "," + fa.getFilePath() + "," +
                            Boolean.toString(GUIMain.twitchFaceMap.get(s).isEnabled()));
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Loads the face data stored in the faces.txt file. This only gets called
     * if that file exists.
     */
    public void loadFaces() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(faceFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                //                    name           name/regex   path
                GUIMain.faceMap.put(split[0], new Face(split[1], split[2]));
            }
            GUIMain.doneWithFaces = true;
            GUIMain.log("Loaded custom faces!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Loads the default twitch faces already saved on the computer.
     */
    public void loadDefaultTwitchFaces() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(twitchFaceFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                boolean enabled = Boolean.parseBoolean(split[3]);
                GUIMain.twitchFaceMap.put(split[0], new TwitchFace(split[1], split[2], enabled));
            }
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Commands
     */
    public void loadCommands() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(commandsFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\[");
                int time;
                try {
                    time = Integer.parseInt(split[2]);
                } catch (Exception e) {
                    time = 10;
                }
                String[] contents = split[1].split("\\]");
                GUIMain.commandSet.add(new Command(split[0], time, contents));
            }
            GUIMain.log("Loaded text commands!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void saveCommands() {
        try {
            PrintWriter br = new PrintWriter(commandsFile);
            for (Command next : GUIMain.commandSet) {
                if (next != null) {
                    String name = next.getTrigger();
                    String[] contents = next.getMessage().data;
                    int time = next.getDelay();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < contents.length; i++) {
                        sb.append(contents[i]);
                        if (i != (contents.length - 1)) sb.append("]");
                    }
                    br.println(name + "[" + sb.toString() + "[" + time);
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Console Commands
     */
    public void saveConCommands() {
        try {
            PrintWriter br = new PrintWriter(ccommandsFile);
            for (ConsoleCommand next : GUIMain.conCommands) {
                if (next != null) {
                    String name = next.getTrigger();
                    String action = next.getAction().toString();
                    int classPerm = next.getClassPermission();
                    String certainPerm = "null";
                    if (next.getCertainPermissions() != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < next.getCertainPermissions().length; i++) {
                            sb.append(next.getCertainPermissions()[i]);
                            if (i != (next.getCertainPermissions().length - 1)) sb.append(",");
                        }
                        certainPerm = sb.toString();
                    }
                    br.println(name + "[" + action + "[" + classPerm + "[" + certainPerm);
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    ConsoleCommand.Action getAction(String key) {
        ConsoleCommand.Action act = null;
        for (ConsoleCommand.Action a : ConsoleCommand.Action.values()) {
            if (a.toString().equalsIgnoreCase(key)) {
                act = a;
                break;
            }
        }
        return act;
    }

    public void loadConsoleCommands() {
        HashSet<ConsoleCommand> hardcoded = new HashSet<>();
        hardcoded.add(new ConsoleCommand("addface", ConsoleCommand.Action.ADD_FACE, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("changeface", ConsoleCommand.Action.CHANGE_FACE, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("removeface", ConsoleCommand.Action.REMOVE_FACE, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("toggleface", ConsoleCommand.Action.TOGGLE_FACE, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("addsound", ConsoleCommand.Action.ADD_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("changesound", ConsoleCommand.Action.CHANGE_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("removesound", ConsoleCommand.Action.REMOVE_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("setsound", ConsoleCommand.Action.SET_SOUND_DELAY, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("togglesound", ConsoleCommand.Action.TOGGLE_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("stopsound", ConsoleCommand.Action.STOP_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("stopallsounds", ConsoleCommand.Action.STOP_ALL_SOUNDS, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("mod", ConsoleCommand.Action.MOD_USER, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("addkeyword", ConsoleCommand.Action.ADD_KEYWORD, Constants.PERMISSION_DEV, null));
        hardcoded.add(new ConsoleCommand("removekeyword", ConsoleCommand.Action.REMOVE_KEYWORD, Constants.PERMISSION_DEV, null));
        hardcoded.add(new ConsoleCommand("setcol", ConsoleCommand.Action.SET_USER_COL, Constants.PERMISSION_ALL, null));
        hardcoded.add(new ConsoleCommand("setpermission", ConsoleCommand.Action.SET_COMMAND_PERMISSION, Constants.PERMISSION_DEV, null));
        hardcoded.add(new ConsoleCommand("addcommand", ConsoleCommand.Action.ADD_TEXT_COMMAND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("removecommand", ConsoleCommand.Action.REMOVE_TEXT_COMMAND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("adddonation", ConsoleCommand.Action.ADD_DONATION, Constants.PERMISSION_DEV, null));
        hardcoded.add(new ConsoleCommand("setsubsound", ConsoleCommand.Action.SET_SUB_SOUND, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("setsoundperm", ConsoleCommand.Action.SET_SOUND_PERMISSION, Constants.PERMISSION_MOD, null));
        hardcoded.add(new ConsoleCommand("setnameface", ConsoleCommand.Action.SET_NAME_FACE, Constants.PERMISSION_SUB, null));
        hardcoded.add(new ConsoleCommand("removenameface", ConsoleCommand.Action.REMOVE_NAME_FACE, Constants.PERMISSION_SUB, null));

        if (Utils.areFilesGood(ccommandsFile.getAbsolutePath())) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(ccommandsFile.toURI().toURL().openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split("\\[");
                    ConsoleCommand.Action a = getAction(split[1]);
                    int classPerm;
                    try {
                        classPerm = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        classPerm = -1;
                    }
                    String[] customUsers = null;
                    if (!split[3].equalsIgnoreCase("null")) {
                        customUsers = split[3].split(",");
                    }
                    GUIMain.conCommands.add(new ConsoleCommand(split[0], a, classPerm, customUsers));
                }
                br.close();
                if (GUIMain.conCommands.size() != hardcoded.size()) { //something's not right...
                    for (ConsoleCommand hard : hardcoded) {
                        boolean isAdded = false;
                        for (ConsoleCommand loaded : GUIMain.conCommands) {
                            if (hard.getTrigger().equalsIgnoreCase(loaded.getTrigger())) {
                                isAdded = true;
                                //this ensures every hard coded ConCommand is loaded for Botnak
                                break;
                            }
                        }
                        if (!isAdded) {//if it isn't in the file, add it
                            GUIMain.conCommands.add(hard);
                        }
                    }
                }
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else { //first time boot/reset/deleted file etc.
            for (ConsoleCommand c : hardcoded) {
                GUIMain.conCommands.add(c);
            }
        }
        GUIMain.log("Loaded console commands!");
    }


    /**
     * Keywords
     */
    public void loadKeywords() {
        if (Utils.areFilesGood(keywordsFile.getAbsolutePath())) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(keywordsFile.toURI().toURL().openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(",");
                    int r, g, b;
                    try {
                        r = Integer.parseInt(split[1]);
                    } catch (Exception e) {
                        r = 255;
                    }
                    try {
                        g = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        g = 255;
                    }
                    try {
                        b = Integer.parseInt(split[3]);
                    } catch (Exception e) {
                        b = 255;
                    }
                    GUIMain.keywordMap.put(split[0], new Color(r, g, b));
                }
                br.close();
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else {
            if (user != null) {
                GUIMain.keywordMap.put(user.getAccountName(), Color.orange);
            }
        }
        GUIMain.log("Loaded keywords!");
    }

    public void saveKeywords() {
        try {
            PrintWriter br = new PrintWriter(keywordsFile);
            Set<String> keys = GUIMain.keywordMap.keySet();
            for (String word : keys) {
                if (word != null) {
                    Color c = GUIMain.keywordMap.get(word);
                    br.println(word + "," + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Sub icons
     */
    public void saveSubIcons() {
        try {
            PrintWriter br = new PrintWriter(subIconsFile);
            for (SubscriberIcon i : GUIMain.subIconSet) {
                br.println(i.getChannel() + "," + i.getFileLoc());
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void loadSubIcons() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(subIconsFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                GUIMain.subIconSet.add(new SubscriberIcon(split[0], split[1]));
            }
            GUIMain.log("Loaded subscriber icons!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    /**
     * Donators
     */
    public void loadDonators() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(donatorsFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                double amount;
                try {
                    amount = Double.parseDouble(split[1]);
                } catch (Exception e) {
                    amount = 0.0;
                }
                GUIMain.donators.add(new Donator(split[0], amount));
            }
            GUIMain.log("Loaded Donators!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }

    }


    public void saveDonators() {
        try {
            PrintWriter br = new PrintWriter(donatorsFile);
            for (Donator d : GUIMain.donators) {
                br.println(d.getName() + "," + d.getDonated());
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Subscribers of your own channel
     */
    //TODO save each subscriber with the first date Botnak meets them
    //and each month check to see if they're still subbed, if not, make them donator
    //with (months subbed * $2.50) as their amount.

    /**
     * Tab State
     * <p/>
     * This is for opening back up to the correct tab, with the correct pane showing.
     * This also handles saving the combined tabs.
     * Format:
     * <p/>
     * Single:
     * bool      bool     string
     * single[  selected[ name
     * <p/>
     * Combined:
     * bool    boolean    String         String     String[] (split(","))
     * single[ selected[ activeChannel[  title[ every,channel,in,it,separated,by,commas
     * <p/>
     * No spaces though.
     * <p/>
     * Single tabs can be invisible if they are in a combined tab.
     */
    public void saveTabState() {
        try {
            PrintWriter br = new PrintWriter(tabsFile);
            int currentSelectedIndex = GUIMain.channelPane.getSelectedIndex();
            for (int i = 1; i < GUIMain.channelPane.getTabCount() - 1; i++) {
                ChatPane current = Utils.getChatPane(i);
                if (current != null) {
                    if (current.isTabVisible()) {
                        boolean selected = current.getIndex() == currentSelectedIndex;
                        br.println("true[" + String.valueOf(selected) + "[" + current.getChannel());
                    }
                } else {
                    CombinedChatPane cc = Utils.getCombinedChatPane(i);
                    if (cc != null) {
                        //all the panes in them should be set to false
                        //their indexes are technically going to be -1
                        boolean selected = cc.getIndex() == currentSelectedIndex;
                        br.print("false[" + String.valueOf(selected) + "[" + cc.getActiveChannel() + "[" + cc.getTabTitle() + "[");
                        String[] chans = cc.getChannels();
                        for (int s = 0; s < chans.length; s++) {
                            String toPrint = chans[s];
                            br.print(toPrint);
                            if (s != chans.length - 1) br.print(",");
                        }
                        br.println();
                    }
                }
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void loadTabState() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(tabsFile.toURI().toURL().openStream()));
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\[");
                boolean isSingle = Boolean.parseBoolean(split[0]);
                boolean isSelected = Boolean.parseBoolean(split[1]);
                if (isSingle) {
                    String channel = split[2];
                    //todo join the channel here? or pass it to the accountmanager which is on heartbeat thread?
                    if (GUIMain.viewer != null) GUIMain.viewer.doConnect(channel);
                    if (GUIMain.bot != null) GUIMain.bot.doConnect(channel);
                    ChatPane cp = ChatPane.createPane(channel);
                    GUIMain.chatPanes.put(cp.getChannel(), cp);
                    GUIMain.channelPane.insertTab(cp.getChannel(), null, cp.getScrollPane(), null, cp.getIndex());
                    if (isSelected) index = cp.getIndex();
                } else {
                    String activeChannel = split[2];
                    String title = split[3];
                    String[] channels = split[4].split(",");
                    ArrayList<ChatPane> cps = new ArrayList<>();
                    for (String c : channels) {
                        //todo join the channel here? or pass it to the accountmanager which is on heartbeat thread?
                        if (GUIMain.viewer != null) GUIMain.viewer.doConnect(c);
                        if (GUIMain.bot != null) GUIMain.bot.doConnect(c);
                        ChatPane cp = ChatPane.createPane(c);
                        GUIMain.chatPanes.put(cp.getChannel(), cp);
                        cps.add(cp);
                    }
                    CombinedChatPane ccp = CombinedChatPane.createCombinedChatPane(cps.toArray(new ChatPane[cps.size()]));
                    GUIMain.channelPane.insertTab(ccp.getTabTitle(), null, ccp.getScrollPane(), null, ccp.getIndex());
                    ccp.setCustomTitle(title);
                    if (isSelected) index = ccp.getIndex();
                    if (!activeChannel.equalsIgnoreCase("all")) {
                        ccp.setActiveChannel(activeChannel);
                        ccp.setActiveScrollPane(activeChannel);
                    }
                    GUIMain.combinedChatPanes.add(ccp);
                }
            }
            GUIMain.channelPane.setSelectedIndex(index);
            GUIMain.log("Loaded Channels and Tabs!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Name faces
     */
    public void loadNameFaces() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(namefaceFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                GUIMain.nameFaceMap.put(split[0], new Face(split[0], split[1]));
            }
            GUIMain.log("Loaded Namefaces!");
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }

    public void saveNameFaces() {
        try {
            PrintWriter br = new PrintWriter(namefaceFile);
            Set<String> keys = GUIMain.nameFaceMap.keySet();
            for (String s : keys) {
                Face toPrint = GUIMain.nameFaceMap.get(s);
                br.println(s + "," + toPrint.getFilePath());
            }
            br.flush();
            br.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


    /**
     * Look and Feel
     */
    public static void loadLAF() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(lafFile.toURI().toURL().openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("jtattoo")) {
                    lookAndFeel = line;
                    break;
                }
            }
            br.close();
        } catch (Exception e) {
            lookAndFeel = "lib.jtattoo.com.jtattoo.plaf.hifi.HiFiLookAndFeel";//default to HiFi
        }
    }

    public void saveLAF() {
        try {
            PrintWriter pr = new PrintWriter(lafFile);
            pr.println(lookAndFeel);
            pr.flush();
            pr.close();
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
    }


}
