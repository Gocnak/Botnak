package util.settings;

import face.*;
import gui.ChatPane;
import gui.CombinedChatPane;
import gui.forms.GUIMain;
import irc.Donor;
import irc.Subscriber;
import irc.account.Account;
import irc.account.AccountManager;
import irc.account.OAuth;
import irc.account.Task;
import lib.pircbot.ChannelManager;
import sound.Sound;
import sound.SoundEngine;
import thread.ShutdownHook;
import thread.ThreadEngine;
import util.Permissions;
import util.Utils;
import util.comm.Command;
import util.comm.ConsoleCommand;
import util.misc.Donation;

import javax.swing.filechooser.FileSystemView;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This class is the container for every setting Botnak has.
 * There's accounts, booleans of all sorts, and ints, you name it.
 */
public class Settings {

    //accounts
    public static AccountManager accountManager = null;
    public static ChannelManager channelManager = null;


    //donations
    public static DonationManager donationManager = null;
    public static boolean loadedDonationSounds = false;
    public static SubscriberManager subscriberManager = null;
    public static boolean loadedSubSounds = false;


    //directories
    public static File defaultDir = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()
            + File.separator + "Botnak");
    public static File tmpDir = new File(defaultDir + File.separator + "_temp");
    public static File faceDir = new File(defaultDir + File.separator + "Faces");
    public static File nameFaceDir = new File(defaultDir + File.separator + "NameFaces");
    public static File twitchFaceDir = new File(defaultDir + File.separator + "TwitchFaces");
    public static File frankerFaceZDir = new File(defaultDir + File.separator + "FrankerFaceZ");
    public static File subIconsDir = new File(defaultDir + File.separator + "SubIcons");
    public static File subSoundDir = new File(defaultDir + File.separator + "SubSounds");
    public static File donationSoundDir = new File(defaultDir + File.separator + "DonationSounds");
    public static File logDir = new File(defaultDir + File.separator + "Logs");
    //files
    public static File accountsFile = new File(defaultDir + File.separator + "acc.ini");
    public static File tabsFile = new File(defaultDir + File.separator + "tabs.txt");
    public static File soundsFile = new File(defaultDir + File.separator + "sounds.txt");
    public static File faceFile = new File(defaultDir + File.separator + "faces.txt");
    public static File twitchFaceFile = new File(defaultDir + File.separator + "twitchfaces.txt");
    public static File userColFile = new File(defaultDir + File.separator + "usercols.txt");
    public static File commandsFile = new File(defaultDir + File.separator + "commands.txt");
    public static File ccommandsFile = new File(defaultDir + File.separator + "chatcom.txt");
    public static File defaultsFile = new File(defaultDir + File.separator + "defaults.ini");
    public static File lafFile = new File(defaultDir + File.separator + "laf.txt");
    public static File windowFile = new File(defaultDir + File.separator + "window.txt");
    public static File keywordsFile = new File(defaultDir + File.separator + "keywords.txt");
    public static File donorsFile = new File(defaultDir + File.separator + "donors.txt");
    public static File donationsFile = new File(defaultDir + File.separator + "donations.txt");
    public static File subsFile = new File(defaultDir + File.separator + "subs.txt");

    //appearance
    public static String lookAndFeel = "lib.jtattoo.com.jtattoo.plaf.hifi.HiFiLookAndFeel";
    //Graphite = "lib.jtattoo.com.jtattoo.plaf.graphite.GraphiteLookAndFeel"

    public static String date;


    //System Tray
    public static Setting<Boolean> stShowMentions = new Setting<>("ST_DisplayDonations", false, Boolean.class);
    public static Setting<Boolean> stShowDonations = new Setting<>("ST_DisplayMentions", false, Boolean.class);
    public static Setting<Boolean> stShowActivity = new Setting<>("ST_DisplayActivity", false, Boolean.class);
    public static Setting<Boolean> stMuted = new Setting<>("", false, Boolean.class);
    public static Setting<Boolean> stUseSystemTray = new Setting<>("ST_UseSystemTray", false, Boolean.class);
    public static Setting<Boolean> stShowSubscribers = new Setting<>("ST_DisplaySubscribers", false, Boolean.class);
    public static Setting<Boolean> stShowNewFollowers = new Setting<>("ST_DisplayFollowers", false, Boolean.class);

    //Bot Reply
    public static Setting<Boolean> botAnnounceSubscribers = new Setting<>(""/*TODO*/, true, Boolean.class);
    public static Setting<Boolean> botShowYTVideoDetails = new Setting<>(""/*TODO*/, true, Boolean.class);
    public static Setting<Boolean> botShowTwitchVODDetails = new Setting<>(""/*TODO*/, true, Boolean.class);
    public static Setting<Boolean> botShowPreviousSubSound = new Setting<>(""/*TODO*/, true, Boolean.class);
    public static Setting<Boolean> botShowPreviousDonSound = new Setting<>(""/*TODO*/, true, Boolean.class);
    public static Setting<Boolean> botUnshortenURLs = new Setting<>(""/*TODO*/, true, Boolean.class);

    public static Setting<Boolean> ffzFacesEnable = new Setting<>("", true, Boolean.class);
    public static Setting<Boolean> ffzFacesUseAll = new Setting<>("", false, Boolean.class);
    public static Setting<Boolean> actuallyClearChat = new Setting<>("", false, Boolean.class);
    public static Setting<Boolean> showDonorIcons = new Setting<>("", true, Boolean.class);
    public static Setting<Boolean> showTabPulses = new Setting<>("", true, Boolean.class);
    public static Setting<Boolean> trackDonations = new Setting<>("TrackDonations", false, Boolean.class);
    public static Setting<Boolean> trackFollowers = new Setting<>("TrackFollowers", false, Boolean.class);

    public static Setting<Boolean> cleanupChat = new Setting<>("ClearChat", true, Boolean.class);
    public static Setting<Boolean> logChat = new Setting<>("LogChat", false, Boolean.class);


    //Icons TODO are these needed anymore?
    public static Setting<Boolean> useMod = new Setting<>("UseMod", false, Boolean.class);
    public static Setting<Boolean> useAdmin = new Setting<>("UseAdmin", false, Boolean.class);
    public static Setting<Boolean> useStaff = new Setting<>("UseStaff", false, Boolean.class);
    public static Setting<Boolean> useBroad = new Setting<>("UseBroad", false, Boolean.class);
    public static Setting<URL> modIcon = new Setting<>("CustomMod", Settings.class.getResource("/image/mod.png"), URL.class);
    public static Setting<URL> broadIcon = new Setting<>("CustomBroad", Settings.class.getResource("/image/broad.png"), URL.class);
    public static Setting<URL> adminIcon = new Setting<>("CustomAdmin", Settings.class.getResource("/image/mod.png"), URL.class);
    public static Setting<URL> staffIcon = new Setting<>("CustomStaff", Settings.class.getResource("/image/mod.png"), URL.class);
    public static Setting<URL> turboIcon = new Setting<>("", Settings.class.getResource("/image/turbo.png"), URL.class);

    public static Setting<Boolean> autoReconnectAccounts = new Setting<>("", true, Boolean.class);


    public static Setting<Float> soundVolumeGain = new Setting<>(""/*TODO*/, 100f, Float.class);
    public static Setting<Integer> faceMaxHeight = new Setting<>("FaceMaxHeight", 20, Integer.class);
    public static Setting<Integer> chatMax = new Setting<>("MaxChat", 100, Integer.class);
    public static Setting<Integer> botReplyType = new Setting<>("BotReplyType", 0, Integer.class);
    //0 = none, 1 = botnak user only, 2 = everyone

    public static Setting<String> lastFMAccount = new Setting<>("LastFMAccount", "", String.class);
    public static Setting<String> defaultSoundDir = new Setting<>("SoundDir", "", String.class);
    public static Setting<String> defaultFaceDir = new Setting<>("FaceDir", "", String.class);

    public static Setting<String> donationClientID = new Setting<>("DCID", "", String.class);
    public static Setting<String> donationAuthCode = new Setting<>("DCOAUTH", "", String.class);
    public static Setting<Boolean> scannedInitialDonations = new Setting<>("RanInitDonations", true, Boolean.class);
    public static Setting<Boolean> scannedInitialSubscribers = new Setting<>("RanInitSub", false, Boolean.class);
    public static Setting<Integer> soundEngineDelay = new Setting<>("SoundEngineDelay", 10000, Integer.class);
    public static Setting<Integer> soundEnginePermission = new Setting<>("SoundEnginePerm", 1, Integer.class);

    public static Setting<Boolean> alwaysOnTop = new Setting<>("AlwaysOnTop", false, Boolean.class);

    public static Setting<Font> font = new Setting<>("Font", new Font("Calibri", Font.PLAIN, 18), Font.class);

    private static ArrayList<Setting> settings;

    public static Window WINDOW = new Window();
    public static LookAndFeel LAF = new LookAndFeel();
    public static TabState TAB_STATE = new TabState();
    public static Subscribers SUBSCRIBERS = new Subscribers();
    public static Donations DONATIONS = new Donations();
    public static Donors DONORS = new Donors();
    public static Sounds SOUNDS = new Sounds();
    public static Commands COMMANDS = new Commands();
    public static UserColors USER_COLORS = new UserColors();
    public static TwitchFaces TWITCH_FACES = new TwitchFaces();
    public static Faces FACES = new Faces();
    public static Keywords KEYWORDS = new Keywords();

    public static void init() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        date = sdf.format(new Date(time));
        defaultDir.mkdirs();
        // This clears the temp directory
        if (tmpDir.exists())
            ShutdownHook.deleteTempOnExit();
        tmpDir.mkdirs();
        faceDir.mkdirs();
        nameFaceDir.mkdirs();
        twitchFaceDir.mkdirs();
        subIconsDir.mkdirs();
        subSoundDir.mkdirs();
        donationSoundDir.mkdirs();
        //collection for bulk saving/loading
        //(otherwise it would have been a bunch of hardcode...)
        settings = new ArrayList<>();
        try {
            Field[] fields = Settings.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getType().getName().equals(Setting.class.getName())) {
                    settings.add((Setting) f.get(null));
                }
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
        /**
         * "After the fact":
         * These change listeners are used to update the main GUI upon the setting change.
         */
        soundEngineDelay.addChangeListener(GUIMain.instance::updateSoundDelay);
        soundEnginePermission.addChangeListener(GUIMain.instance::updateSoundPermission);
        botReplyType.addChangeListener(GUIMain.instance::updateBotReplyPerm);
        font.addChangeListener(f -> {
            StyleConstants.setFontFamily(GUIMain.norm, f.getFamily());
            StyleConstants.setFontSize(GUIMain.norm, f.getSize());
            StyleConstants.setBold(GUIMain.norm, f.isBold());
            StyleConstants.setItalic(GUIMain.norm, f.isItalic());
        });
        alwaysOnTop.addChangeListener(GUIMain.instance::updateAlwaysOnTopStatus);
    }

    /**
     * This void loads everything Botnak will use, and sets the appropriate settings.
     */
    public static void load() {
        if (Utils.areFilesGood(WINDOW.getFile().getAbsolutePath())) WINDOW.load();
        accountManager = new AccountManager();
        channelManager = new ChannelManager();
        accountManager.start();
        donationManager = new DonationManager();
        subscriberManager = new SubscriberManager();
        if (Utils.areFilesGood(accountsFile.getAbsolutePath())) {
            GUIMain.log("Loading accounts...");
            loadPropData(0);
        }
        if (Utils.areFilesGood(defaultsFile.getAbsolutePath())) {
            GUIMain.log("Loading defaults...");
            loadPropData(1);
        }
        if (Utils.areFilesGood(tabsFile.getAbsolutePath()) && accountManager.getUserAccount() != null) {
            GUIMain.log("Loading tabs...");
            TAB_STATE.load();
            GUIMain.channelPane.setSelectedIndex(TabState.startIndex);
            GUIMain.log("Loaded tabs!");
        }
        if (Utils.areFilesGood(soundsFile.getAbsolutePath())) {
            GUIMain.log("Loading sounds...");
            SOUNDS.load();
            GUIMain.log("Loaded sounds!");
        }
        if (subSoundDir.exists())
        {
            String[] dirList = subSoundDir.list();
            if (dirList != null && dirList.length > 0)
            {
                GUIMain.log("Loading sub sounds...");
                doLoadSubSounds();
            }
        }
        if (donationSoundDir.exists())
        {
            String[] dirList = donationSoundDir.list();
            if (dirList != null && dirList.length > 0)
            {
                GUIMain.log("Loading donation sounds...");
                doLoadDonationSounds();
            }
        }
        if (Utils.areFilesGood(userColFile.getAbsolutePath())) {
            GUIMain.log("Loading user colors...");
            USER_COLORS.load();
            GUIMain.log("Loaded user colors!");
        }
        if (Utils.areFilesGood(donorsFile.getAbsolutePath())) {
            GUIMain.log("Loading donors...");
            DONORS.load();
            GUIMain.log("Loaded donors!");
        }
        if (Utils.areFilesGood(donationsFile.getAbsolutePath())) {
            GUIMain.log("Loading donations...");
            DONATIONS.load();//these are stored locally
            if (Donations.mostRecent != null) {
                donationManager.setLastDonation(Donations.mostRecent);
                GUIMain.log(String.format("Most recent donation: %s for %s", Donations.mostRecent.getFromWho(),
                        DonationManager.getCurrencyFormat().format(Donations.mostRecent.getAmount())));
            }
            if (!Donations.donations.isEmpty()) {
                donationManager.fillDonations(Donations.donations);
                Donations.donations.clear();
                Donations.donations = null;
                Donations.mostRecent = null;
                GUIMain.log("Loaded donations!");
            }
        }
        //checks online for offline donations and adds them
        if (donationManager.canCheck() && scannedInitialDonations.getValue()) {
            ThreadEngine.submit(() ->
            {
                donationManager.checkDonations(false);
                donationManager.ranFirstCheck = true;
            });
        }
        if (!scannedInitialDonations.getValue()) {
            ThreadEngine.submit(() ->
            {
                donationManager.scanInitialDonations(0);
                scannedInitialDonations.setValue(true);
            });
        }
        if (accountManager.getUserAccount() != null && accountManager.getUserAccount().getOAuth().canReadSubscribers())
        {
            if (!scannedInitialSubscribers.getValue() && accountManager.getUserAccount() != null) {
                subscriberManager.scanInitialSubscribers(accountManager.getUserAccount().getName(),
                        accountManager.getUserAccount().getOAuth(), 0, new HashSet<>());
            }
        }
        if (Utils.areFilesGood(subsFile.getAbsolutePath())) {
            GUIMain.log("Loading subscribers...");
            SUBSCRIBERS.load();
            if (Subscribers.mostRecent != null) {
                subscriberManager.setLastSubscriber(Subscribers.mostRecent);
                GUIMain.log("Most recent subscriber: " + Subscribers.mostRecent.getName());
            }
            if (!Subscribers.subscribers.isEmpty()) subscriberManager.fillSubscribers(Subscribers.subscribers);
            Subscribers.subscribers.clear();
            Subscribers.subscribers = null;
            Subscribers.mostRecent = null;
            GUIMain.log("Loaded subscribers!");
        }
        if (Utils.areFilesGood(commandsFile.getAbsolutePath())) {
            GUIMain.log("Loading text commands...");
            COMMANDS.load();
            GUIMain.log("Loaded text commands!");
        }
        if (subIconsDir.exists()) {
            File[] subIcons = subIconsDir.listFiles();
            if (subIcons != null && subIcons.length > 0) {
                GUIMain.log("Loading subscriber icons...");
                loadSubIcons(subIcons);
            }
        }
        File[] nameFaces = nameFaceDir.listFiles();
        if (nameFaces != null && nameFaces.length > 0) {
            GUIMain.log("Loading name faces...");
            loadNameFaces(nameFaces);
        }
        if (ffzFacesEnable.getValue()) {
            frankerFaceZDir.mkdirs();
            File[] files = frankerFaceZDir.listFiles();
            if (files != null && files.length > 0) {
                GUIMain.log("Loading FrankerFaceZ faces...");
                loadFFZFaces(files);
            }
        }
        if (keywordsFile.exists()) {
            GUIMain.log("Loading keywords...");
            KEYWORDS.load();
        } else {
            if (accountManager.getUserAccount() != null) {
                GUIMain.keywordMap.put(accountManager.getUserAccount().getName(), Color.orange);
            }
        }
        GUIMain.log("Loading console commands...");
        loadConsoleCommands();//has to be out of the check for files for first time boot
        if (Utils.areFilesGood(faceFile.getAbsolutePath())) {
            GUIMain.log("Loading custom faces...");
            FACES.load();
            FaceManager.doneWithFaces = true;
            GUIMain.log("Loaded custom faces!");
        }
        GUIMain.log("Loading default Twitch faces...");
        if (Utils.areFilesGood(twitchFaceFile.getAbsolutePath())) {
            TWITCH_FACES.load();
        }
        FaceManager.loadDefaultFaces();
    }

    /**
     * This handles saving all the settings that need saved.
     */
    public static void save() {
        LAF.save();
        WINDOW.save();
        if (accountManager.getUserAccount() != null || accountManager.getBotAccount() != null) savePropData(0);
        savePropData(1);
        if (!SoundEngine.getEngine().getSoundMap().isEmpty()) SOUNDS.save();
        if (!FaceManager.faceMap.isEmpty()) FACES.save();
        if (!FaceManager.twitchFaceMap.isEmpty()) TWITCH_FACES.save();
        TAB_STATE.save();
        if (!GUIMain.userColMap.isEmpty()) USER_COLORS.save();
        if (GUIMain.loadedCommands()) COMMANDS.save();
        if (!GUIMain.keywordMap.isEmpty()) KEYWORDS.save();
        if (!donationManager.getDonors().isEmpty()) DONORS.save();
        if (!donationManager.getDonations().isEmpty()) DONATIONS.save();
        if (!subscriberManager.getSubscribers().isEmpty()) SUBSCRIBERS.save();
        saveConCommands();
    }

    /**
     * *********VOIDS*************
     */

    public static void loadPropData(int type) {
        Properties p = new Properties();
        if (type == 0) {//accounts
            try {
                p.load(new FileInputStream(accountsFile));
                String userNorm = p.getProperty("UserNorm", "").toLowerCase();
                String userNormPass = p.getProperty("UserNormPass", "");
                String status = p.getProperty("CanStatus", "false");
                String commercial = p.getProperty("CanCommercial", "false");
                if (!userNorm.equals("") && !userNormPass.equals("") && userNormPass.contains("oauth")) {
                    boolean stat = Boolean.parseBoolean(status);
                    if (!stat) {
                        GUIMain.instance.updateStatusOption.setEnabled(false);
                        GUIMain.instance.updateStatusOption.setToolTipText("Enable \"Edit Title and Game\" in the Authorize GUI to use this feature.");
                    }
                    boolean ad = Boolean.parseBoolean(commercial);
                    if (!ad) {
                        GUIMain.instance.runAdMenu.setEnabled(false);
                        GUIMain.instance.runAdMenu.setToolTipText("Enable \"Play Commercials\" in the Authorize GUI to use this feature.");
                    }
                    boolean subs = Boolean.parseBoolean(p.getProperty("CanParseSubscribers", "false"));
                    boolean followed = Boolean.parseBoolean(p.getProperty("CanParseFollowedStreams", "false"));
                    if (followed) trackFollowers.setValue(true);
                    accountManager.setUserAccount(new Account(userNorm, new OAuth(userNormPass, stat, ad, subs, followed)));
                }
                String userBot = p.getProperty("UserBot", "").toLowerCase();
                String userBotPass = p.getProperty("UserBotPass", "");
                if (!userBot.equals("") && !userBotPass.equals("") && userBotPass.contains("oauth")) {
                    accountManager.setBotAccount(new Account(userBot, new OAuth(userBotPass, false, false, false, false)));
                }
                if (accountManager.getUserAccount() != null) {
                    accountManager.addTask(new Task(null, Task.Type.CREATE_VIEWER_ACCOUNT, null));
                }
                if (accountManager.getBotAccount() != null) {
                    accountManager.addTask(new Task(null, Task.Type.CREATE_BOT_ACCOUNT, null));
                }
            } catch (Exception e) {
                GUIMain.log(e);
            }
        }
        if (type == 1) {//defaults
            try {
                p.load(new FileInputStream(defaultsFile));
                settings.forEach(s -> s.load(p));
                if (logChat.getValue()) logDir.mkdirs();
                GUIMain.log("Loaded defaults!");
            } catch (Exception e) {
                GUIMain.log(e);
            }
        }
    }

    public static void savePropData(int type) {
        Properties p = new Properties();
        File writerFile = null;
        String detail = "";
        switch (type) {
            case 0:
                Account user = accountManager.getUserAccount();
                Account bot = accountManager.getBotAccount();
                if (user != null) {
                    OAuth key = user.getOAuth();
                    p.put("UserNorm", user.getName());
                    p.put("UserNormPass", key.getKey());
                    p.put("CanStatus", String.valueOf(key.canSetTitle()));
                    p.put("CanCommercial", String.valueOf(key.canPlayAd()));
                    p.put("CanParseSubscribers", String.valueOf(key.canReadSubscribers()));
                    p.put("CanParseFollowedStreams", String.valueOf(key.canReadFollowed()));
                }
                if (bot != null) {
                    OAuth key = bot.getOAuth();
                    p.put("UserBot", bot.getName());
                    p.put("UserBotPass", key.getKey());
                }
                writerFile = accountsFile;
                detail = "Account Info";
                break;
            case 1:
                settings.forEach(s -> s.save(p));
                writerFile = defaultsFile;
                detail = "Defaults/Other Settings";
                break;
            default:
                break;
        }
        if (writerFile != null) {
            try {
                p.store(new FileWriter(writerFile), detail);
            } catch (Exception e) {
                GUIMain.log(e);
            }
        } else {
            GUIMain.log("Failed to store settings due to some unforseen exception!");
        }
    }


    /**
     * Sounds
     */
    private static class Sounds extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split(",", 3);
            String[] split2add = split[2].split(",");//files
            int perm = 0;
            try {
                perm = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                GUIMain.log(split[0] + " has a problem. Making it public.");
            }
            SoundEngine.getEngine().getSoundMap().put(split[0].toLowerCase(), new Sound(perm, split2add));
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            Iterator<Map.Entry<String, Sound>> keys = SoundEngine.getEngine().getSoundMap().entrySet().iterator();
            StringBuilder sb = new StringBuilder();
            while (keys.hasNext()) {
                Map.Entry<String, Sound> entry = keys.next();
                sb.append(entry.getKey());
                sb.append(",");
                sb.append(entry.getValue().getPermission());
                for (String sound : entry.getValue().getSounds().data) {
                    sb.append(",");
                    sb.append(sound);
                }
                pw.println(sb.toString());
                sb.setLength(0);
            }
        }

        @Override
        public File getFile() {
            return soundsFile;
        }
    }


    /**
     * User Colors
     */
    public static class UserColors extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split(",");
            GUIMain.userColMap.put(split[0], new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            Iterator<Map.Entry<String, Color>> entries = GUIMain.userColMap.entrySet().iterator();
            StringBuilder sb = new StringBuilder();
            while (entries.hasNext()) {
                Map.Entry<String, Color> entry = entries.next();
                sb.append(entry.getKey());
                sb.append(",");
                sb.append(entry.getValue().getRed());
                sb.append(",");
                sb.append(entry.getValue().getGreen());
                sb.append(",");
                sb.append(entry.getValue().getBlue());
                pw.println(sb.toString());
                sb.setLength(0);
            }
        }

        @Override
        public File getFile() {
            return userColFile;
        }
    }

    /**
     * Normal faces
     */
    public static class Faces extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split(",");
            //                        name           name/regex   path
            FaceManager.faceMap.put(split[0], new Face(split[1], split[2]));
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            FaceManager.faceMap.keySet().stream().filter(s -> s != null && FaceManager.faceMap.get(s) != null).forEach(s -> {
                Face fa = FaceManager.faceMap.get(s);
                pw.println(s + "," + fa.getRegex() + "," + fa.getFilePath());
            });
        }

        @Override
        public File getFile() {
            return faceFile;
        }
    }

    /**
     * Twitch Faces
     */
    public static class TwitchFaces extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            try {
                String[] split = line.split(",");
                int emoteID = Integer.parseInt(split[0]);
                TwitchFace tf = new TwitchFace(split[1],
                        new File(twitchFaceDir + File.separator + String.valueOf(emoteID) + ".png").getAbsolutePath(),
                        Boolean.parseBoolean(split[2]));
                FaceManager.twitchFaceMap.put(emoteID, tf);
            } catch (Exception e) {
                GUIMain.log(e);
            }
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            FaceManager.twitchFaceMap.keySet().stream().filter(s -> s != null && FaceManager.twitchFaceMap.get(s) != null)
                    .forEach(s -> {
                        TwitchFace fa = FaceManager.twitchFaceMap.get(s);
                        pw.println(s + "," + fa.getRegex() + "," + Boolean.toString(fa.isEnabled()));
                    });
        }

        @Override
        public File getFile() {
            return twitchFaceFile;
        }
    }


    /**
     * Commands
     * <p>
     * trigger[message (content)[arguments?
     */
    public static class Commands extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split("\\[");
            String[] contents = split[1].split("]");
            Command c = new Command(split[0], contents);
            if (split.length > 2) {
                c.addArguments(split[2].split(","));
            }
            GUIMain.commandSet.add(c);
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            for (Command next : GUIMain.commandSet) {
                if (next != null) {
                    String name = next.getTrigger();
                    String[] contents = next.getMessage().data;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < contents.length; i++) {
                        sb.append(contents[i]);
                        if (i != (contents.length - 1)) sb.append("]");
                    }
                    pw.print(name + "[" + sb.toString());
                    if (next.hasArguments()) {
                        pw.print("[");
                        for (int i = 0; i < next.countArguments(); i++) {
                            pw.print(next.getArguments().get(i));
                            if (i != (next.countArguments() - 1)) pw.print(",");
                        }
                    }
                    pw.println();
                }
            }
        }

        @Override
        public File getFile() {
            return commandsFile;
        }
    }

    /**
     * Console Commands
     */
    public static void saveConCommands() {
        try (PrintWriter br = new PrintWriter(ccommandsFile)) {
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
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }

    private static ConsoleCommand.Action getAction(String key) {
        ConsoleCommand.Action act = null;
        for (ConsoleCommand.Action a : ConsoleCommand.Action.values()) {
            if (a.toString().equalsIgnoreCase(key)) {
                act = a;
                break;
            }
        }
        return act;
    }

    public static void loadConsoleCommands() {
        HashSet<ConsoleCommand> hardcoded = new HashSet<>();
        hardcoded.add(new ConsoleCommand("addface", ConsoleCommand.Action.ADD_FACE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("changeface", ConsoleCommand.Action.CHANGE_FACE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("removeface", ConsoleCommand.Action.REMOVE_FACE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("toggleface", ConsoleCommand.Action.TOGGLE_FACE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("addsound", ConsoleCommand.Action.ADD_SOUND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("changesound", ConsoleCommand.Action.CHANGE_SOUND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("removesound", ConsoleCommand.Action.REMOVE_SOUND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("setsound", ConsoleCommand.Action.SET_SOUND_DELAY, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("togglesound", ConsoleCommand.Action.TOGGLE_SOUND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("stopsound", ConsoleCommand.Action.STOP_SOUND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("stopallsounds", ConsoleCommand.Action.STOP_ALL_SOUNDS, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("addkeyword", ConsoleCommand.Action.ADD_KEYWORD, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("removekeyword", ConsoleCommand.Action.REMOVE_KEYWORD, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("setcol", ConsoleCommand.Action.SET_USER_COL, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("setpermission", ConsoleCommand.Action.SET_COMMAND_PERMISSION, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("addcommand", ConsoleCommand.Action.ADD_TEXT_COMMAND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("removecommand", ConsoleCommand.Action.REMOVE_TEXT_COMMAND, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("adddonation", ConsoleCommand.Action.ADD_DONATION, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("setsoundperm", ConsoleCommand.Action.SET_SOUND_PERMISSION, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("setnameface", ConsoleCommand.Action.SET_NAME_FACE, Permissions.Permission.SUBSCRIBER.permValue, null));
        hardcoded.add(new ConsoleCommand("removenameface", ConsoleCommand.Action.REMOVE_NAME_FACE, Permissions.Permission.SUBSCRIBER.permValue, null));
        hardcoded.add(new ConsoleCommand("playad", ConsoleCommand.Action.PLAY_ADVERT, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("settitle", ConsoleCommand.Action.SET_STREAM_TITLE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("title", ConsoleCommand.Action.SEE_STREAM_TITLE, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("setgame", ConsoleCommand.Action.SET_STREAM_GAME, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("game", ConsoleCommand.Action.SEE_STREAM_GAME, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("startraffle", ConsoleCommand.Action.START_RAFFLE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("addrafflewinner", ConsoleCommand.Action.ADD_RAFFLE_WINNER, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("stopraffle", ConsoleCommand.Action.STOP_RAFFLE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("removerafflewinner", ConsoleCommand.Action.REMOVE_RAFFLE_WINNER, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("winners", ConsoleCommand.Action.SEE_WINNERS, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("startpoll", ConsoleCommand.Action.START_POLL, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("vote", ConsoleCommand.Action.VOTE_POLL, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("pollresult", ConsoleCommand.Action.POLL_RESULT, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("cancelpoll", ConsoleCommand.Action.CANCEL_POLL, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("song", ConsoleCommand.Action.NOW_PLAYING, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("soundstate", ConsoleCommand.Action.SEE_SOUND_STATE, Permissions.Permission.MODERATOR.permValue, null));
        hardcoded.add(new ConsoleCommand("uptime", ConsoleCommand.Action.SHOW_UPTIME, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("lastsubsound", ConsoleCommand.Action.SEE_PREV_SOUND_SUB, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("lastdonationsound", ConsoleCommand.Action.SEE_PREV_SOUND_DON, Permissions.Permission.ALL.permValue, null));
        hardcoded.add(new ConsoleCommand("botreply", ConsoleCommand.Action.SEE_OR_SET_REPLY_TYPE, Permissions.Permission.BROADCASTER.permValue, null));
        hardcoded.add(new ConsoleCommand("volume", ConsoleCommand.Action.SEE_OR_SET_VOLUME, Permissions.Permission.MODERATOR.permValue, null));

        if (Utils.areFilesGood(ccommandsFile.getAbsolutePath())) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(ccommandsFile.toURI().toURL().openStream()))) {
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
                GUIMain.log(e);
            }
        } else { //first time boot/reset/deleted file etc.
            GUIMain.conCommands.addAll(new ArrayList<>(hardcoded));
        }
        GUIMain.log("Loaded console commands!");
    }


    /**
     * Keywords
     */
    public static class Keywords extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
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

        @Override
        public void handleLineSave(PrintWriter pw) {
            Set<String> keys = GUIMain.keywordMap.keySet();
            keys.stream().filter(Objects::nonNull).forEach(word ->
            {
                Color c = GUIMain.keywordMap.get(word);
                pw.println(word + "," + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
            });
        }

        @Override
        public File getFile() {
            return keywordsFile;
        }
    }

    /**
     * Donors
     */
    public static class Donors extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split(",");
            double amount;
            try {
                amount = Double.parseDouble(split[1]);
            } catch (Exception e) {
                amount = 0.0;
            }
            donationManager.getDonors().add(new Donor(split[0], amount));
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            donationManager.getDonors().forEach(d -> pw.println(d.getName() + "," +
                    DonationManager.getDecimalFormat().format(d.getDonated())));
        }

        @Override
        public File getFile() {
            return donorsFile;
        }
    }


    /**
     * Donations.
     */
    public static class Donations extends AbstractFileSave {

        private static HashSet<Donation> donations = new HashSet<>();
        private static Donation mostRecent = null;

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split("\\[");
            double amount;
            try {
                amount = Double.parseDouble(split[3]);
            } catch (Exception e) {
                amount = 0.0;
            }
            Donation d = new Donation(split[0], split[1], split[2], amount, Date.from(Instant.parse(split[4])));
            if ((mostRecent == null || mostRecent.compareTo(d) > 0) && !d.getDonationID().equals("LOCAL"))
                mostRecent = d;
            donations.add(d);
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            donationManager.getDonations().stream().sorted().forEach(d ->
                    pw.println(d.getDonationID() + "[" + d.getFromWho() + "[" + d.getNote() + "["
                            + DonationManager.getDecimalFormat().format(d.getAmount()) + "["
                            + Instant.ofEpochMilli(d.getDateReceived().getTime()).toString()));
        }

        @Override
        public File getFile() {
            return donationsFile;
        }
    }


    /**
     * Subscribers of your own channel
     * <p>
     * Saves each subscriber with the first date Botnak meets them
     * and each month check to see if they're still subbed, if not, make them an ex-subscriber
     */
    private static class Subscribers extends AbstractFileSave {

        private static HashSet<Subscriber> subscribers = new HashSet<>();
        private static Subscriber mostRecent = null;

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split("\\[");
            Subscriber s = new Subscriber(split[0], LocalDateTime.parse(split[1]), Boolean.parseBoolean(split[2]), Integer.parseInt(split[3]));
            subscribers.add(s);
            if (mostRecent == null || mostRecent.compareTo(s) > 0) mostRecent = s;
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            subscriberManager.getSubscribers().stream().sorted().forEach(
                    s -> pw.println(s.getName() + "[" + s.getStarted().toString() + "[" + String.valueOf(s.isActive())
                            + "[" + s.getStreak()));
        }

        @Override
        public File getFile() {
            return subsFile;
        }
    }


    /**
     * Tab State
     * <p>
     * This is for opening back up to the correct tab, with the correct pane showing.
     * This also handles saving the combined tabs.
     * Format:
     * <p>
     * Single:
     * bool      bool     string
     * single[  selected[ name
     * <p>
     * Combined:
     * bool    boolean    String         String     String[] (split(","))
     * single[ selected[ activeChannel[  title[ every,channel,in,it,separated,by,commas
     * <p>
     * No spaces though.
     * <p>
     * Single tabs can be invisible if they are in a combined tab.
     */
    private static class TabState extends AbstractFileSave {

        public static int startIndex = 0;

        @Override
        public void handleLineLoad(String line) {
            String[] split = line.split("\\[");
            boolean isSingle = Boolean.parseBoolean(split[0]);
            boolean isSelected = Boolean.parseBoolean(split[1]);
            if (isSingle) {
                String channel = split[2];
                if (accountManager.getUserAccount() != null) {
                    String channelName = "#" + channel;
                    GUIMain.channelSet.add(channelName);
                }
                ChatPane cp = ChatPane.createPane(channel);
                GUIMain.chatPanes.put(cp.getChannel(), cp);
                GUIMain.channelPane.insertTab(cp.getChannel(), null, cp.getScrollPane(), null, cp.getIndex());
                if (isSelected) startIndex = cp.getIndex();
            } else {
                String activeChannel = split[2];
                String title = split[3];
                String[] channels = split[4].split(",");
                ArrayList<ChatPane> cps = new ArrayList<>();
                for (String c : channels) {
                    if (accountManager.getUserAccount() != null) {
                        String channelName = "#" + c;
                        GUIMain.channelSet.add(channelName);
                    }
                    ChatPane cp = ChatPane.createPane(c);
                    GUIMain.chatPanes.put(cp.getChannel(), cp);
                    cps.add(cp);
                }
                CombinedChatPane ccp = CombinedChatPane.createCombinedChatPane(cps.toArray(new ChatPane[cps.size()]));
                GUIMain.channelPane.insertTab(ccp.getTabTitle(), null, ccp.getScrollPane(), null, ccp.getIndex());
                ccp.setCustomTitle(title);
                if (isSelected) startIndex = ccp.getIndex();
                if (!activeChannel.equalsIgnoreCase("all")) {
                    ccp.setActiveChannel(activeChannel);
                    ccp.setActiveScrollPane(activeChannel);
                }
                GUIMain.combinedChatPanes.add(ccp);
            }
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            int currentSelectedIndex = GUIMain.channelPane.getSelectedIndex();
            for (int i = 1; i < GUIMain.channelPane.getTabCount() - 1; i++) {
                ChatPane current = Utils.getChatPane(i);
                if (current != null) {
                    if (current.isTabVisible()) {
                        boolean selected = current.getIndex() == currentSelectedIndex;
                        pw.println("true[" + String.valueOf(selected) + "[" + current.getChannel());
                    }
                } else {
                    CombinedChatPane cc = Utils.getCombinedChatPane(i);
                    if (cc != null) {
                        //all the panes in them should be set to false
                        //their indexes are technically going to be -1
                        boolean selected = cc.getIndex() == currentSelectedIndex;
                        pw.print("false[" + String.valueOf(selected) + "[" + cc.getActiveChannel() + "[" + cc.getTabTitle() + "[");
                        String[] chans = cc.getChannels();
                        for (int s = 0; s < chans.length; s++) {
                            String toPrint = chans[s];
                            pw.print(toPrint);
                            if (s != chans.length - 1) pw.print(",");
                        }
                        pw.println();
                    }
                }
            }
        }

        @Override
        public File getFile() {
            return tabsFile;
        }
    }

    /**
     * Look and Feel
     */
    public static class LookAndFeel extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            if (line.contains("jtattoo")) lookAndFeel = line;
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            pw.println(lookAndFeel);
        }

        @Override
        public File getFile() {
            return lafFile;
        }
    }

    /**
     * Window Location and Size
     */
    private static class Window extends AbstractFileSave {

        @Override
        public void handleLineLoad(String line) {
            String[] parts = line.substring(1).split(",");
            String first = parts[0];
            String second = parts[1];
            if (line.startsWith("p")) {
                try {
                    int x = Integer.parseInt(first);
                    int y = Integer.parseInt(second);
                    GUIMain.instance.setLocation(x, y);
                } catch (Exception e) {
                    GUIMain.log(e);
                }
            } else if (line.startsWith("s")) {
                try {
                    double w = Double.parseDouble(first);
                    double h = Double.parseDouble(second);
                    GUIMain.instance.setSize((int) w, (int) h);
                } catch (Exception e) {
                    GUIMain.log(e);
                }
            }
        }

        @Override
        public void handleLineSave(PrintWriter pw) {
            pw.println("p" + GUIMain.instance.getLocationOnScreen().x + "," + GUIMain.instance.getLocationOnScreen().y);
            pw.println("s" + GUIMain.instance.getSize().getWidth() + "," + GUIMain.instance.getSize().getHeight());
        }


        @Override
        public File getFile() {
            return windowFile;
        }
    }

    //Other methods that don't follow standardization

    /**
     * Name faces
     */
    public static void loadNameFaces(File[] nameFaces) {
        try {
            for (File nameFace : nameFaces) {
                if (nameFace.isDirectory()) continue;
                String name = Utils.removeExt(nameFace.getName());
                FaceManager.nameFaceMap.put(name, new Face(name, nameFace.getAbsolutePath()));
            }
            GUIMain.log("Loaded name faces!");
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }

    private static void doLoadSubSounds() {
        loadSubSounds();
        loadedSubSounds = true;
        GUIMain.log("Loaded sub sounds!");
    }

    public static void loadSubSounds() {
        loadShuffleSet(subSoundDir, SoundEngine.getEngine().getSubStack());
    }

    private static void doLoadDonationSounds() {
        loadDonationSounds();
        loadedDonationSounds = true;
        GUIMain.log("Loaded donation sounds!");
    }

    public static void loadDonationSounds() {
        loadShuffleSet(donationSoundDir, SoundEngine.getEngine().getDonationStack());
    }

    private static void loadShuffleSet(File directory, Deque<Sound> stack) {
        try {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                ArrayList<Sound> sounds = new ArrayList<>();
                for (File f : files) {
                    sounds.add(new Sound(5, f.getAbsolutePath()));
                }
                Collections.shuffle(sounds);
                stack.addAll(sounds);
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }

    /**
     * Sub icons
     */
    public static void loadSubIcons(File[] subIconFiles) {
        for (File f : subIconFiles) {
            FaceManager.subIconSet.add(new SubscriberIcon(Utils.removeExt(f.getName()), f.getAbsolutePath()));
        }
    }

    /**
     * FrankerFaceZ
     * <p>
     * We can be a little more broad about this saving, since it's a per-channel basis
     */
    public static void loadFFZFaces(File[] channels) {
        for (File channel : channels) {
            if (channel.isDirectory() && channel.length() > 0) {
                File[] faces = channel.listFiles();
                if (faces != null) {
                    ArrayList<FrankerFaceZ> loadedFaces = new ArrayList<>();
                    for (File face : faces) {
                        if (face != null) {
                            loadedFaces.add(new FrankerFaceZ(face.getName(), face.getAbsolutePath(), true));
                        }
                    }
                    FaceManager.ffzFaceMap.put(channel.getName(), loadedFaces);
                } else {
                    channel.delete();
                }
            }
        }
    }
}