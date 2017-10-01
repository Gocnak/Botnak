package gui.forms;

import face.FaceManager;
import gui.BotnakTrayIcon;
import gui.ChatPane;
import gui.CombinedChatPane;
import gui.DraggableTabbedPane;
import gui.listeners.ListenerUserChat;
import gui.listeners.NewTabListener;
import irc.IRCBot;
import irc.IRCViewer;
import irc.message.Message;
import irc.message.MessageQueue;
import sound.SoundEngine;
import thread.TabPulse;
import thread.ThreadEngine;
import thread.heartbeat.Heartbeat;
import util.Constants;
import util.Response;
import util.Utils;
import util.comm.Command;
import util.comm.ConsoleCommand;
import util.settings.Settings;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class GUIMain extends JFrame {

    public static Map<String, Color> userColMap, keywordMap;
    public static Set<Command> commandSet;
    public static Set<ConsoleCommand> conCommands;
    public static Set<String> channelSet;
    public static Map<String, ChatPane> chatPanes;
    public static Set<CombinedChatPane> combinedChatPanes;
    public static Set<TabPulse> tabPulses;
    public static Map<String, GUIViewerList> viewerLists;

    public static int userResponsesIndex = 0;
    public static ArrayList<String> userResponses;

    public static IRCBot bot;
    public static IRCViewer viewer;
    public static GUISettings settings = null;
    public static GUIStreams streams = null;
    public static GUIAbout aboutGUI = null;
    public static GUIStatus statusGUI = null;
    public static GUIRaffle raffleGUI = null;
    public static GUIVote voteGUI = null;

    public static boolean shutDown = false;

    public static SimpleAttributeSet norm = new SimpleAttributeSet();

    public static GUIMain instance;

    private static BotnakTrayIcon systemTrayIcon;

    public static Heartbeat heartbeat;

    private static ChatPane systemLogsPane;

    public GUIMain() {
        new MessageQueue();
        instance = this;
        channelSet = new CopyOnWriteArraySet<>();
        userColMap = new ConcurrentHashMap<>();
        commandSet = new CopyOnWriteArraySet<>();
        conCommands = new CopyOnWriteArraySet<>();
        keywordMap = new ConcurrentHashMap<>();
        tabPulses = new CopyOnWriteArraySet<>();
        combinedChatPanes = new CopyOnWriteArraySet<>();
        viewerLists = new ConcurrentHashMap<>();
        userResponses = new ArrayList<>();
        chatPanes = new ConcurrentHashMap<>();
        ThreadEngine.init();
        FaceManager.init();
        SoundEngine.init();
        StyleConstants.setForeground(norm, Color.white);
        StyleConstants.setFontFamily(norm, Settings.font.getValue().getFamily());
        StyleConstants.setFontSize(norm, Settings.font.getValue().getSize());
        StyleConstants.setBold(norm, Settings.font.getValue().isBold());
        StyleConstants.setItalic(norm, Settings.font.getValue().isItalic());
        initComponents();
        systemLogsPane = new ChatPane("System Logs", allChatsScroll, allChats, null, 0);
        chatPanes.put("System Logs", systemLogsPane);
        Settings.init();
        ThreadEngine.submit(() -> {
            Settings.load();
            if (Settings.stUseSystemTray.getValue()) getSystemTrayIcon();
            heartbeat = new Heartbeat();
        });
    }

    public static boolean loadedSettingsUser() {
        return Settings.accountManager != null && Settings.accountManager.getUserAccount() != null;
    }

    public static boolean loadedSettingsBot() {
        return Settings.accountManager != null && Settings.accountManager.getBotAccount() != null;
    }

    public static boolean loadedCommands() {
        return !commandSet.isEmpty();
    }

    public void chatButtonActionPerformed() {
        userResponsesIndex = 0;
        String channel = channelPane.getTitleAt(channelPane.getSelectedIndex());
        if (Settings.accountManager.getViewer() == null) {
            logCurrent("Failed to send message, there is no viewer account! Please set one up!");
            return;
        }
        if (!Settings.accountManager.getViewer().isConnected()) {
            logCurrent("Failed to send message, currently trying to (re)connect!");
            return;
        }
        String userInput = Utils.checkText(userChat.getText().replaceAll("\n", ""));
        if (channel != null && !channel.equalsIgnoreCase("system logs") && !"".equals(userInput)) {
            CombinedChatPane ccp = Utils.getCombinedChatPane(channelPane.getSelectedIndex());
            boolean comboExists = ccp != null;
            if (comboExists) {
                List<String> channels = ccp.getActiveChannel().equalsIgnoreCase("All") ?
                        ccp.getChannels() : Collections.singletonList(ccp.getActiveChannel());

                for (String c : channels)
                    Settings.accountManager.getViewer().sendMessage("#" + c, userInput);

                if (!userResponses.contains(userInput))
                    userResponses.add(userInput);
            }
            else
            {
                Settings.accountManager.getViewer().sendMessage("#" + channel, userInput);
                if (!userResponses.contains(userInput))
                    userResponses.add(userInput);
            }
            userChat.setText("");
        }
    }

    /**
     * Wrapper for ensuring no null chat pane is produced due to hash tags.
     *
     * @param channel The channel, either inclusive of the hash tag or not.
     * @return The chat pane if existent, otherwise to System Logs to prevent null pointers.
     * (Botnak will just print out to System Logs the message that was eaten)
     */
    public static ChatPane getChatPane(String channel) {
        ChatPane toReturn = chatPanes.get(channel.replaceAll("#", ""));
        return toReturn == null ? getSystemLogsPane() : toReturn;
    }

    /**
     * @return The System Logs chat pane.
     */
    public static ChatPane getSystemLogsPane() {
        return systemLogsPane;
    }

    /**
     * Logs a message to the current chat pane.
     *
     * @param message The message to log.
     */
    public static void logCurrent(Object message) {
        ChatPane current = getCurrentPane();
        if (message != null && chatPanes != null && !chatPanes.isEmpty() && current != null)
            MessageQueue.addMessage(new Message().setType(Message.MessageType.LOG_MESSAGE)
                    .setContent(message.toString()).setExtra(current));
    }

    public static ChatPane getCurrentPane() {
        ChatPane toReturn;
        int index = channelPane.getSelectedIndex();
        if (index == 0) return getSystemLogsPane();
        toReturn = Utils.getChatPane(index);
        if (toReturn == null) {
            toReturn = Utils.getCombinedChatPane(index);
        }
        return toReturn == null ? getSystemLogsPane() : toReturn;
    }

    /**
     * Logs a message to the chat console under all white, SYS username.
     * This should only be used for serious reports, like exception reporting and
     * other status updates.
     *
     * @param message The message to log.
     */
    public static void log(Object message) {
        if (message == null) return;
        String toPrint;
        Message.MessageType type = Message.MessageType.LOG_MESSAGE; // Moved here to allow for changing message type to something like error for throwables
        if (message instanceof Throwable) {
            Throwable t = (Throwable) message;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            toPrint = sw.toString(); // stack trace as a string
            pw.close();
        } else {
            // Not a throwable.. Darn strings
            toPrint = message.toString();
        }
        if (chatPanes == null || chatPanes.isEmpty()) {//allowing for errors to at least go somewhere
            System.out.println(toPrint == null ? "Null toPrint!" : toPrint);
        } else {
            MessageQueue.addMessage(new Message(toPrint, type));
        }
    }

    public static void updateTitle(String viewerCount) {
        StringBuilder stanSB = new StringBuilder();
        stanSB.append("Botnak ");
        if (viewerCount != null) {
            stanSB.append("| ");
            stanSB.append(viewerCount);
            stanSB.append(" ");
        }
        if (Settings.accountManager != null) {
            if (Settings.accountManager.getUserAccount() != null) {
                stanSB.append("| User: ");
                stanSB.append(Settings.accountManager.getUserAccount().getName());
            }
            if (Settings.accountManager.getBotAccount() != null) {
                stanSB.append(" | Bot: ");
                stanSB.append(Settings.accountManager.getBotAccount().getName());
            }
        }
        instance.setTitle(stanSB.toString());
    }

    public void exitButtonActionPerformed() {
        shutDown = true;
        if (viewer != null) {
            viewer.close(false);
        }
        if (bot != null) {
            bot.close(false);
        }
        if (!tabPulses.isEmpty()) {
            tabPulses.forEach(TabPulse::interrupt);
            tabPulses.clear();
        }
        if (systemTrayIcon != null) systemTrayIcon.close();
        SoundEngine.getEngine().close();
        Settings.save();
        heartbeat.interrupt();
        ThreadEngine.close();
        Set<Map.Entry<String, ChatPane>> entries = chatPanes.entrySet();
        for (Map.Entry<String, ChatPane> entry : entries)
        {
            String channel = entry.getKey();
            ChatPane pane = entry.getValue();
            if (Settings.logChat.getValue())
            {
                Utils.logChat(pane.getText().split("\\n"), channel, 2);
            }
        }
        System.gc();
        dispose();
        System.exit(0);
    }


    public synchronized void pulseTab(ChatPane cp) {
        if (shutDown) return;
        if (cp.isPulsing()) return;
        TabPulse tp = new TabPulse(cp);
        tp.start();
        tabPulses.add(tp);
    }

    public static BotnakTrayIcon getSystemTrayIcon() {
        if (systemTrayIcon == null) systemTrayIcon = new BotnakTrayIcon();
        return systemTrayIcon;
    }


    private void openBotnakFolderOptionActionPerformed() {
        Utils.openWebPage(Settings.defaultDir.toURI().toString());
    }

    private void openLogViewerOptionActionPerformed() {
        // TODO add your code here
    }

    private void openSoundsOptionActionPerformed() {
        Utils.openWebPage(new File(Settings.defaultSoundDir.getValue()).toURI().toString());
    }

    private void autoReconnectToggleItemStateChanged(ItemEvent e) {
        Settings.autoReconnectAccounts.setValue(e.getStateChange() == ItemEvent.SELECTED);
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (viewer != null && viewer.getViewer() != null) {
                if (!viewer.getViewer().getConnection().isConnected())
                    Settings.accountManager.createReconnectThread(viewer.getViewer().getConnection());
            }
            if (bot != null && bot.getBot() != null) {
                if (!bot.getBot().isConnected())
                    Settings.accountManager.createReconnectThread(bot.getBot().getConnection());
            }
        }
    }

    private void alwaysOnTopToggleItemStateChanged(ItemEvent e) {
        Settings.alwaysOnTop.setValue(e.getStateChange() == ItemEvent.SELECTED);
    }

    public void updateAlwaysOnTopStatus(boolean newBool) {
        if (alwaysOnTopToggle.isSelected() != newBool) {
            alwaysOnTopToggle.setSelected(newBool);
            //this is going to be called from the setting load,
            //which will probably, in turn, fire another
            //change event, setting the setting to the same setting it is,
            //but since no change happens, this void is not called again,
            //and we can continue on in the original call
        }
        Window[] windows = getWindows();
        for (Window w : windows) {
            w.setAlwaysOnTop(newBool);
        }
    }

    private void settingsOptionActionPerformed() {
        if (settings == null) {
            settings = new GUISettings();
        }
        if (!settings.isVisible()) {
            settings.setVisible(true);
        }
    }

    private void startRaffleOptionActionPerformed() {
        if (raffleGUI == null)
            raffleGUI = new GUIRaffle();
        if (!raffleGUI.isVisible())
            raffleGUI.setVisible(true);
        else
            raffleGUI.toFront();
    }

    private void startVoteOptionActionPerformed() {
        if (voteGUI == null)
            voteGUI = new GUIVote();
        if (!voteGUI.isVisible())
            voteGUI.setVisible(true);
        else
            voteGUI.toFront();
    }

    private void soundsToggleItemStateChanged() {
        Response r = SoundEngine.getEngine().toggleSound(null, false);
        if (r.isSuccessful()) {
            if (bot != null && bot.getBot() != null) {
                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                        r.getResponseText());
            }
        }
    }

    private void manageTextCommandsOptionActionPerformed() {
        // TODO add your code here
    }

    private void updateStatusOptionActionPerformed() {
        if (statusGUI == null) {
            statusGUI = new GUIStatus();
        }
        if (!statusGUI.isVisible()) {
            statusGUI.setVisible(true);
        }
    }

    private void subOnlyToggleItemStateChanged() {
        if (viewer != null) {
            viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                    subOnlyToggle.isSelected() ? "/subscribers" : "/subscribersoff");
        }
    }

    private void projectGithubOptionActionPerformed() {
        Utils.openWebPage("https://github.com/Gocnak/Botnak/");
    }

    private void projectWikiOptionActionPerformed() {
        Utils.openWebPage("https://github.com/Gocnak/Botnak/wiki");
    }

    private void projectDetailsOptionActionPerformed() {
        if (aboutGUI == null) {
            aboutGUI = new GUIAbout();
        }
        if (!aboutGUI.isVisible())
            aboutGUI.setVisible(true);
    }

    public void updateSoundDelay(int secDelay) {
        if (secDelay > 1000)
            secDelay /= 1000;
        switch (secDelay) {
            case 0:
                soundDelayOffOption.setSelected(true);
                break;
            case 5:
                soundDelay5secOption.setSelected(true);
                break;
            case 10:
                soundDelay10secOption.setSelected(true);
                break;
            case 20:
                soundDelay20secOption.setSelected(true);
                break;
            default:
                soundDelayCustomOption.setSelected(true);
                soundDelayCustomOption.setText(String.format("Custom: %d seconds", secDelay));
                break;
        }
        if (!soundDelayCustomOption.isSelected()) soundDelayCustomOption.setText("Custom (use chat)");
    }

    public void updateSoundPermission(int permission) {
        switch (permission) {
            case 0:
                soundPermEveryoneOption.setSelected(true);
                break;
            case 1:
                soundPermSDMBOption.setSelected(true);
                break;
            case 2:
                soundPermDMBOption.setSelected(true);
                break;
            case 3:
                soundPermModAndBroadOption.setSelected(true);
                break;
            case 4:
                soundPermBroadOption.setSelected(true);
                break;
            default:
                break;
        }
    }

    public void updateSoundToggle(boolean newBool) {
        soundsToggle.setSelected(newBool);
    }

    public void updateSubsOnly(String num) {
        subOnlyToggle.setSelected("1".equals(num));
    }

    public void updateSlowMode(String slowModeAmount) {
        switch (slowModeAmount) {
            case "0":
                slowModeOffOption.setSelected(true);
                break;
            case "5":
                slowMode5secOption.setSelected(true);
                break;
            case "10":
                slowMode10secOption.setSelected(true);
                break;
            case "15":
                slowMode15secOption.setSelected(true);
                break;
            case "30":
                slowMode30secOption.setSelected(true);
                break;
            default:
                slowModeCustomOption.setSelected(true);
                slowModeCustomOption.setText("Custom: " + slowModeAmount + " seconds");
                break;
        }
        if (!slowModeCustomOption.isSelected()) slowModeCustomOption.setText("Custom (use chat)");
    }

    public void updateBotReplyPerm(int perm) {
        switch (perm) {
            case 2:
                botReplyAll.setSelected(true);
                break;
            case 1:
                botReplyJustYou.setSelected(true);
                break;
            case 0:
                botReplyNobody.setSelected(true);
                break;
            default:
                break;
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        menuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        openBotnakFolderOption = new JMenuItem();
        openLogViewerOption = new JMenuItem();
        openSoundsOption = new JMenuItem();
        exitOption = new JMenuItem();
        preferencesMenu = new JMenu();
        botReplyMenu = new JMenu();
        botReplyAll = new JRadioButtonMenuItem();
        botReplyJustYou = new JRadioButtonMenuItem();
        botReplyNobody = new JRadioButtonMenuItem();
        autoReconnectToggle = new JCheckBoxMenuItem();
        alwaysOnTopToggle = new JCheckBoxMenuItem();
        settingsOption = new JMenuItem();
        toolsMenu = new JMenu();
        startRaffleOption = new JMenuItem();
        startVoteOption = new JMenuItem();
        soundsToggle = new JCheckBoxMenuItem();
        soundDelayMenu = new JMenu();
        soundDelayOffOption = new JRadioButtonMenuItem();
        soundDelay5secOption = new JRadioButtonMenuItem();
        soundDelay10secOption = new JRadioButtonMenuItem();
        soundDelay20secOption = new JRadioButtonMenuItem();
        soundDelayCustomOption = new JRadioButtonMenuItem();
        soundDelayCustomOption.setToolTipText("Set a custom sound delay with \"!setsound (time)\" in chat");
        soundPermissionMenu = new JMenu();
        soundPermEveryoneOption = new JRadioButtonMenuItem();
        soundPermSDMBOption = new JRadioButtonMenuItem();
        soundPermDMBOption = new JRadioButtonMenuItem();
        soundPermModAndBroadOption = new JRadioButtonMenuItem();
        soundPermBroadOption = new JRadioButtonMenuItem();
        manageTextCommandsOption = new JMenuItem();
        runAdMenu = new JMenu();
        timeOption30sec = new JMenuItem();
        timeOption60sec = new JMenuItem();
        timeOption90sec = new JMenuItem();
        timeOption120sec = new JMenuItem();
        timeOption150sec = new JMenuItem();
        timeOption180sec = new JMenuItem();
        updateStatusOption = new JMenuItem();
        subOnlyToggle = new JCheckBoxMenuItem();
        slowModeMenu = new JMenu();
        slowModeOffOption = new JRadioButtonMenuItem();
        slowMode5secOption = new JRadioButtonMenuItem();
        slowMode10secOption = new JRadioButtonMenuItem();
        slowMode15secOption = new JRadioButtonMenuItem();
        slowMode30secOption = new JRadioButtonMenuItem();
        slowModeCustomOption = new JRadioButtonMenuItem();
        slowModeCustomOption.setToolTipText("Set a custom slow mode time with \"/slow (time in seconds)\" in chat");
        helpMenu = new JMenu();
        projectGithubOption = new JMenuItem();
        projectWikiOption = new JMenuItem();
        projectDetailsOption = new JMenuItem();
        channelPane = new DraggableTabbedPane();
        allChatsScroll = new JScrollPane();
        allChats = new JTextPane();
        dankLabel = new JLabel();
        scrollPane1 = new JScrollPane();
        userChat = new JTextArea();

        //======== Botnak ========
        {
            setMinimumSize(new Dimension(640, 404));
            setName("Botnak Control Panel");
            setTitle("Botnak | Please go to Preferences->Settings!");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setIconImage(new ImageIcon(getClass().getResource("/image/icon.png")).getImage());
            Container BotnakContentPane = getContentPane();

            //======== menuBar1 ========
            {

                //======== fileMenu ========
                {
                    fileMenu.setText("File");

                    //---- openBotnakFolderOption ----
                    openBotnakFolderOption.setText("Open Botnak Folder");
                    openBotnakFolderOption.addActionListener(e -> openBotnakFolderOptionActionPerformed());
                    fileMenu.add(openBotnakFolderOption);

                    //---- openLogViewerOption ----
                    openLogViewerOption.setText("Open Log Viewer");
                    openLogViewerOption.addActionListener(e -> openLogViewerOptionActionPerformed());
                    openLogViewerOption.setEnabled(false);//TODO
                    fileMenu.add(openLogViewerOption);

                    //---- openSoundsOption ----
                    openSoundsOption.setText("Open Sound Directory");
                    openSoundsOption.addActionListener(e -> openSoundsOptionActionPerformed());
                    fileMenu.add(openSoundsOption);
                    fileMenu.addSeparator();

                    //---- exitOption ----
                    exitOption.setText("Save and Exit");
                    exitOption.addActionListener(e -> exitButtonActionPerformed());
                    fileMenu.add(exitOption);
                }
                menuBar1.add(fileMenu);

                //======== preferencesMenu ========
                {
                    preferencesMenu.setText("Preferences");

                    //======== botReplyMenu ========
                    {
                        botReplyMenu.setText("Bot Reply");

                        //---- botReplyAll ----
                        botReplyAll.setText("Reply to all");
                        botReplyAll.addActionListener(e -> {
                            if (bot != null) {
                                Response r = bot.parseReplyType("2", Settings.accountManager.getUserAccount().getName());
                                logCurrent(r.getResponseText());
                            }
                        });
                        botReplyMenu.add(botReplyAll);

                        //---- botReplyJustYou ----
                        botReplyJustYou.setText("Reply to you");
                        botReplyJustYou.addActionListener(e -> {
                            if (bot != null) {
                                Response r = bot.parseReplyType("1", Settings.accountManager.getUserAccount().getName());
                                logCurrent(r.getResponseText());
                            }
                        });
                        botReplyMenu.add(botReplyJustYou);

                        //---- botReplyNobody ----
                        botReplyNobody.setText("Reply to none");
                        botReplyNobody.addActionListener(e -> {
                            if (bot != null) {
                                Response r = bot.parseReplyType("0", Settings.accountManager.getUserAccount().getName());
                                logCurrent(r.getResponseText());
                            }
                        });
                        botReplyNobody.setSelected(true);
                        botReplyMenu.add(botReplyNobody);
                    }
                    preferencesMenu.add(botReplyMenu);

                    //---- autoReconnectToggle ----
                    autoReconnectToggle.setText("Auto-Reconnect");
                    autoReconnectToggle.setSelected(true);
                    autoReconnectToggle.addItemListener(this::autoReconnectToggleItemStateChanged);
                    preferencesMenu.add(autoReconnectToggle);

                    //---- alwaysOnTopToggle ----
                    alwaysOnTopToggle.setText("Always On Top");
                    alwaysOnTopToggle.setSelected(false);
                    alwaysOnTopToggle.addItemListener(this::alwaysOnTopToggleItemStateChanged);
                    preferencesMenu.add(alwaysOnTopToggle);
                    preferencesMenu.addSeparator();

                    //---- settingsOption ----
                    settingsOption.setText("Settings...");
                    settingsOption.addActionListener(e -> settingsOptionActionPerformed());
                    preferencesMenu.add(settingsOption);
                }
                menuBar1.add(preferencesMenu);

                //======== toolsMenu ========
                {
                    toolsMenu.setText("Tools");

                    //---- startRaffleOption ----
                    startRaffleOption.setText("Create Raffle...");
                    startRaffleOption.addActionListener(e -> startRaffleOptionActionPerformed());
                    toolsMenu.add(startRaffleOption);

                    //---- startVoteOption ----
                    startVoteOption.setText("Create Vote...");
                    startVoteOption.addActionListener(e -> startVoteOptionActionPerformed());
                    toolsMenu.add(startVoteOption);

                    //---- soundsToggle ----
                    soundsToggle.setText("Enable Sounds");
                    soundsToggle.setSelected(true);
                    soundsToggle.addActionListener(e -> soundsToggleItemStateChanged());
                    toolsMenu.add(soundsToggle);

                    //======== soundDelayMenu ========
                    {
                        soundDelayMenu.setText("Sound Delay");

                        //---- soundDelayOffOption ----
                        soundDelayOffOption.setText("None (Off)");
                        soundDelayOffOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null && Settings.soundEngineDelay.getValue() != 0)
                            {
                                Response r = SoundEngine.getEngine().setSoundDelay("0");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundDelayMenu.add(soundDelayOffOption);

                        //---- soundDelay5secOption ----
                        soundDelay5secOption.setText("5 seconds");
                        soundDelay5secOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null && Settings.soundEngineDelay.getValue() != 5000)
                            {
                                Response r = SoundEngine.getEngine().setSoundDelay("5");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundDelayMenu.add(soundDelay5secOption);

                        //---- soundDelay10secOption ----
                        soundDelay10secOption.setText("10 seconds");
                        soundDelay10secOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null && Settings.soundEngineDelay.getValue() != 10000)
                            {
                                Response r = SoundEngine.getEngine().setSoundDelay("10");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundDelay10secOption.setSelected(true);
                        soundDelayMenu.add(soundDelay10secOption);

                        //---- soundDelay20secOption ----
                        soundDelay20secOption.setText("20 seconds");
                        soundDelay20secOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null && Settings.soundEngineDelay.getValue() != 20000)
                            {
                                Response r = SoundEngine.getEngine().setSoundDelay("20");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundDelayMenu.add(soundDelay20secOption);

                        //---- soundDelayCustomOption ----
                        soundDelayCustomOption.setText("Custom (Use chat)");
                        soundDelayCustomOption.setEnabled(false);
                        soundDelayMenu.add(soundDelayCustomOption);
                    }
                    toolsMenu.add(soundDelayMenu);

                    //======== soundPermissionMenu ========
                    {
                        soundPermissionMenu.setText("Sound Permission");

                        //---- soundPermEveryoneOption ----
                        soundPermEveryoneOption.setText("Everyone");
                        soundPermEveryoneOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = SoundEngine.getEngine().setSoundPermission("0");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundPermissionMenu.add(soundPermEveryoneOption);

                        //---- soundPermSDMBOption ----
                        soundPermSDMBOption.setText("Subs, Donors, Mods, Broadcaster");
                        soundPermSDMBOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = SoundEngine.getEngine().setSoundPermission("1");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundPermSDMBOption.setSelected(true);
                        soundPermissionMenu.add(soundPermSDMBOption);

                        //---- soundPermDMBOption ----
                        soundPermDMBOption.setText("Donors, Mods, Broadcaster");
                        soundPermDMBOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = SoundEngine.getEngine().setSoundPermission("2");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundPermissionMenu.add(soundPermDMBOption);

                        //---- soundPermModAndBroadOption ----
                        soundPermModAndBroadOption.setText("Mods and Broadcaster Only");
                        soundPermModAndBroadOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = SoundEngine.getEngine().setSoundPermission("3");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundPermissionMenu.add(soundPermModAndBroadOption);

                        //---- soundPermBroadOption ----
                        soundPermBroadOption.setText("Broadcaster Only");
                        soundPermBroadOption.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = SoundEngine.getEngine().setSoundPermission("4");
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                            }
                        });
                        soundPermissionMenu.add(soundPermBroadOption);
                    }
                    toolsMenu.add(soundPermissionMenu);

                    //---- manageTextCommandsOption ----
                    manageTextCommandsOption.setText("Manage Text Commands...");
                    manageTextCommandsOption.setEnabled(false);//TODO
                    manageTextCommandsOption.addActionListener(e -> manageTextCommandsOptionActionPerformed());
                    toolsMenu.add(manageTextCommandsOption);
                    toolsMenu.addSeparator();

                    //======== runAdMenu ========
                    {
                        runAdMenu.setText("Run Ad");

                        //---- timeOption30sec ----
                        timeOption30sec.setText("30 sec");
                        timeOption30sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "30", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(30000);
                                        logCurrent("The 30-second advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption30sec);

                        //---- timeOption60sec ----
                        timeOption60sec.setText("1 min");
                        timeOption60sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "1m", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(60000);
                                        logCurrent("The 1-minute advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption60sec);

                        //---- timeOption90sec ----
                        timeOption90sec.setText("1 min 30 sec");
                        timeOption90sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "1m30s", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(90000);
                                        logCurrent("The 1-minute 30-second advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption90sec);

                        //---- timeOption120sec ----
                        timeOption120sec.setText("2 min");
                        timeOption120sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "2m", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(120000);
                                        logCurrent("The 2 minute advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption120sec);

                        //---- timeOption150sec ----
                        timeOption150sec.setText("2 min 30 sec");
                        timeOption150sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "2m30s", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(150000);
                                        logCurrent("The 2 minute 30 second advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption150sec);

                        //---- timeOption180sec ----
                        timeOption180sec.setText("3 min");
                        timeOption180sec.addActionListener(e -> {
                            if (bot != null && bot.getBot() != null) {
                                Response r = bot.playAdvert(Settings.accountManager.getUserAccount().getOAuth(),
                                        "3m", Settings.accountManager.getUserAccount().getName());
                                bot.getBot().sendMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        r.getResponseText());
                                ThreadEngine.submit(() -> {
                                    try {
                                        Thread.sleep(180000);
                                        logCurrent("The 3 minute advertisement has ended.");
                                    } catch (InterruptedException ignored) {
                                    }
                                });
                            }
                        });
                        runAdMenu.add(timeOption180sec);
                    }
                    toolsMenu.add(runAdMenu);

                    //---- updateStatusOption ----
                    updateStatusOption.setText("Update Status...");
                    updateStatusOption.addActionListener(e -> updateStatusOptionActionPerformed());
                    toolsMenu.add(updateStatusOption);

                    //---- subOnlyToggle ----
                    subOnlyToggle.setText("Sub-only Chat");
                    subOnlyToggle.addActionListener(e -> subOnlyToggleItemStateChanged());
                    toolsMenu.add(subOnlyToggle);

                    //======== slowModeMenu ========
                    {
                        slowModeMenu.setText("Slow Mode");

                        //---- slowModeOffOption ----
                        slowModeOffOption.setText("Off");
                        slowModeOffOption.addActionListener(e -> {
                            if (viewer != null) {
                                viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        "/slowoff");
                            }
                        });
                        slowModeOffOption.setSelected(true);
                        slowModeMenu.add(slowModeOffOption);

                        //---- slowMode5secOption ----
                        slowMode5secOption.setText("5 seconds");
                        slowMode5secOption.addActionListener(e -> {
                            if (viewer != null) {
                                viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        "/slow 5");
                            }
                        });
                        slowModeMenu.add(slowMode5secOption);

                        //---- slowMode10secOption ----
                        slowMode10secOption.setText("10 seconds");
                        slowMode10secOption.addActionListener(e -> {
                            if (viewer != null) {
                                viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        "/slow 10");
                            }
                        });
                        slowModeMenu.add(slowMode10secOption);

                        //---- slowMode15secOption ----
                        slowMode15secOption.setText("15 seconds");
                        slowMode15secOption.addActionListener(e -> {
                            if (viewer != null) {
                                viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        "/slow 15");
                            }
                        });
                        slowModeMenu.add(slowMode15secOption);

                        //---- slowMode30secOption ----
                        slowMode30secOption.setText("30 seconds");
                        slowMode30secOption.addActionListener(e -> {
                            if (viewer != null) {
                                viewer.getViewer().sendRawMessage("#" + Settings.accountManager.getUserAccount().getName(),
                                        "/slow 30");
                            }
                        });
                        slowModeMenu.add(slowMode30secOption);

                        //---- slowModeCustomOption ----
                        slowModeCustomOption.setText("Custom (use chat)");
                        slowModeCustomOption.setEnabled(false);
                        slowModeMenu.add(slowModeCustomOption);
                    }
                    toolsMenu.add(slowModeMenu);
                }
                menuBar1.add(toolsMenu);

                //======== helpMenu ========
                {
                    helpMenu.setText("Help");

                    //---- projectGithubOption ----
                    projectGithubOption.setText("Botnak Github");
                    projectGithubOption.addActionListener(e -> projectGithubOptionActionPerformed());
                    helpMenu.add(projectGithubOption);

                    //---- projectWikiOption ----
                    projectWikiOption.setText("Botnak Wiki");
                    projectWikiOption.addActionListener(e -> projectWikiOptionActionPerformed());
                    helpMenu.add(projectWikiOption);
                    JMenuItem bugReport = new JMenuItem("Report an Issue");
                    bugReport.addActionListener(e -> Utils.openWebPage("https://github.com/Gocnak/Botnak/issues/new"));
                    helpMenu.add(bugReport);
                    helpMenu.addSeparator();

                    //---- projectDetailsOption ----
                    projectDetailsOption.setText("About...");
                    projectDetailsOption.addActionListener(e -> projectDetailsOptionActionPerformed());
                    helpMenu.add(projectDetailsOption);
                }
                menuBar1.add(helpMenu);
            }
            setJMenuBar(menuBar1);

            //======== channelPane ========
            {
                channelPane.setFocusable(false);
                channelPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                channelPane.setAutoscrolls(true);
                channelPane.addChangeListener(Constants.tabListener);
                channelPane.addMouseListener(Constants.tabListener);

                //======== allChatsScroll ========
                {
                    allChatsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                    //---- allChats ----
                    allChats.setEditable(false);
                    allChats.setForeground(Color.white);
                    allChats.setBackground(Color.black);
                    allChats.setFont(new Font("Calibri", Font.PLAIN, 16));
                    allChats.setMargin(new Insets(0, 0, 0, 0));
                    allChats.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    allChats.addMouseListener(Constants.listenerURL);
                    allChats.addMouseListener(Constants.listenerName);
                    allChats.setEditorKit(Constants.wrapEditorKit);
                    allChatsScroll.setViewportView(allChats);
                }
                channelPane.addTab("System Logs", allChatsScroll);

                //---- dankLabel ----
                dankLabel.setText("Dank memes");
                channelPane.addTab("+", dankLabel);
                channelPane.setEnabledAt(channelPane.getTabCount() - 1, false);
                channelPane.addMouseListener(new NewTabListener());
            }

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- userChat ----
                userChat.setFont(new Font("Consolas", Font.PLAIN, 12));
                userChat.setLineWrap(true);
                userChat.setWrapStyleWord(true);
                userChat.addKeyListener(new ListenerUserChat(userChat));
                scrollPane1.setViewportView(userChat);
            }

            GroupLayout BotnakContentPaneLayout = new GroupLayout(BotnakContentPane);
            BotnakContentPane.setLayout(BotnakContentPaneLayout);
            BotnakContentPaneLayout.setHorizontalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addComponent(channelPane, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(scrollPane1)
            );
            BotnakContentPaneLayout.setVerticalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                    .addComponent(channelPane, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
            );
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (channelPane != null) {
                        channelPane.scrollDownPanes();
                    }
                }
            });
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    exitButtonActionPerformed();
                }
            });
            pack();
            setLocationRelativeTo(getOwner());
        }

        //---- botReplyGroup ----
        ButtonGroup botReplyGroup = new ButtonGroup();
        botReplyGroup.add(botReplyAll);
        botReplyGroup.add(botReplyJustYou);
        botReplyGroup.add(botReplyNobody);

        //---- soundDelayGroup ----
        ButtonGroup soundDelayGroup = new ButtonGroup();
        soundDelayGroup.add(soundDelayOffOption);
        soundDelayGroup.add(soundDelay5secOption);
        soundDelayGroup.add(soundDelay10secOption);
        soundDelayGroup.add(soundDelay20secOption);
        soundDelayGroup.add(soundDelayCustomOption);

        //---- soundPermissionGroup ----
        ButtonGroup soundPermissionGroup = new ButtonGroup();
        soundPermissionGroup.add(soundPermEveryoneOption);
        soundPermissionGroup.add(soundPermSDMBOption);
        soundPermissionGroup.add(soundPermDMBOption);
        soundPermissionGroup.add(soundPermModAndBroadOption);
        soundPermissionGroup.add(soundPermBroadOption);

        //---- slowModeGroup ----
        ButtonGroup slowModeGroup = new ButtonGroup();
        slowModeGroup.add(slowModeOffOption);
        slowModeGroup.add(slowMode5secOption);
        slowModeGroup.add(slowMode10secOption);
        slowModeGroup.add(slowMode15secOption);
        slowModeGroup.add(slowMode30secOption);
        slowModeGroup.add(slowModeCustomOption);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    private JMenuBar menuBar1;
    private JMenu fileMenu;
    private JMenuItem openBotnakFolderOption;
    private JMenuItem openLogViewerOption;
    private JMenuItem openSoundsOption;
    private JMenuItem exitOption;
    private JMenu preferencesMenu;
    private JMenu botReplyMenu;
    private JRadioButtonMenuItem botReplyAll;
    private JRadioButtonMenuItem botReplyJustYou;
    private JRadioButtonMenuItem botReplyNobody;
    private JCheckBoxMenuItem autoReconnectToggle;
    public JCheckBoxMenuItem alwaysOnTopToggle;
    private JMenuItem settingsOption;
    private JMenu toolsMenu;
    private JMenuItem startRaffleOption;
    private JMenuItem startVoteOption;
    private JCheckBoxMenuItem soundsToggle;
    private JMenu soundDelayMenu;
    private JRadioButtonMenuItem soundDelayOffOption;
    private JRadioButtonMenuItem soundDelay5secOption;
    private JRadioButtonMenuItem soundDelay10secOption;
    private JRadioButtonMenuItem soundDelay20secOption;
    private JRadioButtonMenuItem soundDelayCustomOption;
    private JMenu soundPermissionMenu;
    private JRadioButtonMenuItem soundPermEveryoneOption;
    private JRadioButtonMenuItem soundPermSDMBOption;
    private JRadioButtonMenuItem soundPermDMBOption;
    private JRadioButtonMenuItem soundPermModAndBroadOption;
    private JRadioButtonMenuItem soundPermBroadOption;
    private JMenuItem manageTextCommandsOption;
    public JMenu runAdMenu;
    private JMenuItem timeOption30sec;
    private JMenuItem timeOption60sec;
    private JMenuItem timeOption90sec;
    private JMenuItem timeOption120sec;
    private JMenuItem timeOption150sec;
    private JMenuItem timeOption180sec;
    public JMenuItem updateStatusOption;
    private JCheckBoxMenuItem subOnlyToggle;
    private JMenu slowModeMenu;
    private JRadioButtonMenuItem slowModeOffOption;
    private JRadioButtonMenuItem slowMode5secOption;
    private JRadioButtonMenuItem slowMode10secOption;
    private JRadioButtonMenuItem slowMode15secOption;
    private JRadioButtonMenuItem slowMode30secOption;
    private JRadioButtonMenuItem slowModeCustomOption;
    private JMenu helpMenu;
    private JMenuItem projectGithubOption;
    private JMenuItem projectWikiOption;
    private JMenuItem projectDetailsOption;
    public static DraggableTabbedPane channelPane;
    private JScrollPane allChatsScroll;
    private JTextPane allChats;
    private JLabel dankLabel;
    private JScrollPane scrollPane1;
    public static JTextArea userChat;
}