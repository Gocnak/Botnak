package gui;

import face.FaceManager;
import gui.listeners.ListenerName;
import gui.listeners.ListenerURL;
import gui.listeners.ListenerUserChat;
import gui.listeners.NewTabListener;
import irc.IRCBot;
import irc.IRCViewer;
import irc.message.Message;
import irc.message.MessageQueue;
import sound.SoundEngine;
import thread.TabPulse;
import thread.ThreadEngine;
import thread.heartbeat.*;
import util.Constants;
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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class GUIMain extends JFrame {

    public static ConcurrentHashMap<String, Color> userColMap;
    public static CopyOnWriteArraySet<Command> commandSet;
    public static CopyOnWriteArraySet<String> channelSet;
    public static ConcurrentHashMap<String, ChatPane> chatPanes;
    public static CopyOnWriteArraySet<CombinedChatPane> combinedChatPanes;
    public static ConcurrentHashMap<String, Color> keywordMap;

    public static ConcurrentHashMap<String, GUIViewerList> viewerLists;

    public static int userResponsesIndex = 0;
    public static ArrayList<String> userResponses;

    public static CopyOnWriteArraySet<ConsoleCommand> conCommands;

    public static IRCBot bot;
    public static IRCViewer viewer;
    public static GUISettings settings = null;
    public static GUIStreams streams = null;
    public static GUIAbout aboutGUI = null;
    public static AuthorizeAccountGUI accountGUI = null;

    public static boolean shutDown = false;

    public static SimpleAttributeSet norm = new SimpleAttributeSet();

    public static GUIMain instance;

    public static Settings currentSettings;

    public static CopyOnWriteArraySet<TabPulse> tabPulses;

    public static Heartbeat heartbeat;

    private static ChatPane systemLogsPane;

    public GUIMain() {
        new MessageQueue().start();
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
        ThreadEngine.init();
        FaceManager.init();
        SoundEngine.init();
        StyleConstants.setForeground(norm, Color.white);
        initComponents();
        chatPanes = new ConcurrentHashMap<>();
        systemLogsPane = new ChatPane("System Logs", allChatsScroll, allChats, 0);
        chatPanes.put("System Logs", systemLogsPane);
        currentSettings = new Settings();
        currentSettings.load();
        heartbeat = new Heartbeat();
        heartbeat.addHeartbeatThread(new ViewerCount());
        heartbeat.addHeartbeatThread(new UserManager());
        heartbeat.addHeartbeatThread(new BanQueue());
        //TODO if (GUISettings.trackDonations)
        heartbeat.addHeartbeatThread(new DonationCheck());
        heartbeat.start();
    }

    public static boolean loadedSettingsUser() {
        return currentSettings != null && currentSettings.accountManager.getUserAccount() != null;
    }

    public static boolean loadedSettingsBot() {
        return currentSettings != null && currentSettings.accountManager.getBotAccount() != null;
    }

    public static boolean loadedCommands() {
        return !commandSet.isEmpty();
    }

    public void chatButtonActionPerformed() {
        userResponsesIndex = 0;
        String channel = channelPane.getTitleAt(channelPane.getSelectedIndex());
        if (GUIMain.currentSettings.accountManager.getViewer() == null) return;
        if (!GUIMain.currentSettings.accountManager.getViewer().isConnected()) {
            logCurrent("Failed to send message, currently trying to reconnect!");
            return;
        }
        String userInput = userChat.getText().replaceAll("\n", "");
        if (channel != null && !channel.equalsIgnoreCase("system logs")) {
            CombinedChatPane ccp = Utils.getCombinedChatPane(channelPane.getSelectedIndex());
            boolean comboExists = ccp != null;
            if (comboExists) {
                String[] channels;
                if (!ccp.getActiveChannel().equalsIgnoreCase("All")) {
                    channels = new String[]{ccp.getActiveChannel()};
                } else {
                    channels = ccp.getChannels();
                }
                if (!Utils.checkText(userInput).equals("")) {
                    for (String c : channels) {
                        GUIMain.currentSettings.accountManager.getViewer().sendMessage("#" + c, userInput);
                    }
                    if (!userResponses.contains(userInput)) userResponses.add(userInput);
                }
                userChat.setText("");
            } else {
                if (!Utils.checkText(userInput).equals("")) {
                    GUIMain.currentSettings.accountManager.getViewer().sendMessage("#" + channel, userInput);
                    if (!userResponses.contains(userInput)) userResponses.add(userInput);
                }
                userChat.setText("");
            }
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
        String channel = channelPane.getTitleAt(channelPane.getSelectedIndex());
        if (message != null && GUIMain.chatPanes != null && !GUIMain.chatPanes.isEmpty())
            MessageQueue.addMessage(new Message().setChannel("#" + channel)
                    .setContent(message.toString()).setType(Message.MessageType.LOG_MESSAGE));
    }

    /**
     * Logs a message to the chat console under all white, SYS username.
     * This should only be used for serious reports, like exception reporting and
     * other status updates.
     *
     * @param message The message to log.
     */
    public static void log(Object message) {
        if (message != null && GUIMain.chatPanes != null && !GUIMain.chatPanes.isEmpty())
            MessageQueue.addMessage(new Message(message.toString(), Message.MessageType.LOG_MESSAGE));
    }

    public static void updateTitle(String viewerCount) {
        StringBuilder stanSB = new StringBuilder();
        stanSB.append("Botnak ");
        if (viewerCount != null) {
            stanSB.append("| ");
            stanSB.append(viewerCount);
            stanSB.append(" ");
        }
        if (currentSettings != null) {
            if (currentSettings.accountManager.getUserAccount() != null) {
                stanSB.append("| User: ");
                stanSB.append(currentSettings.accountManager.getUserAccount().getName());
            }
            if (currentSettings.accountManager.getBotAccount() != null) {
                stanSB.append(" | Bot: ");
                stanSB.append(currentSettings.accountManager.getBotAccount().getName());
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
        SoundEngine.getEngine().close();
        currentSettings.save();
        heartbeat.interrupt();
        if (currentSettings.logChat) {
            String[] keys = chatPanes.keySet().toArray(new String[chatPanes.keySet().size()]);
            for (String s : keys) {
                ChatPane cp = chatPanes.get(s);
                Utils.logChat(cp.getText().split("\\n"), s, 2);
            }
        }
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

    private void openBotnakFolderOptionActionPerformed() {
        Utils.openWebPage(Settings.defaultDir.toURI().toString());
    }

    private void openLogViewerOptionActionPerformed() {
        // TODO add your code here
    }

    private void openSoundsOptionActionPerformed() {
        Utils.openWebPage(new File(currentSettings.defaultSoundDir).toURI().toString());
    }

    private void autoReconnectToggleItemStateChanged(ItemEvent e) {
        // TODO check login status of both accounts to determine if they need relogging in, upon enable
    }

    private void alwaysOnTopToggleItemStateChanged(ItemEvent e) {
        Window[] windows = getWindows();
        for (Window w : windows) {
            w.setAlwaysOnTop(e.getStateChange() == ItemEvent.SELECTED);
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
        // TODO add your code here
    }

    private void startVoteOptionActionPerformed() {
        // TODO add your code here
    }

    private void soundsToggleItemStateChanged(ItemEvent e) {
        // TODO add your code here
    }

    private void manageTextCommandsOptionActionPerformed() {
        // TODO add your code here
    }

    private void updateStatusOptionActionPerformed() {
        // TODO add your code here
    }

    private void subOnlyToggleItemStateChanged(ItemEvent e) {
        //TODO viewer.getViewer().sendRawMessage();
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
        radioButtonMenuItem1 = new JRadioButtonMenuItem();
        radioButtonMenuItem3 = new JRadioButtonMenuItem();
        radioButtonMenuItem2 = new JRadioButtonMenuItem();
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
            setMinimumSize(new Dimension(680, 504));
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

                        //---- radioButtonMenuItem1 ----
                        radioButtonMenuItem1.setText("Reply to all");
                        botReplyMenu.add(radioButtonMenuItem1);

                        //---- radioButtonMenuItem3 ----
                        radioButtonMenuItem3.setText("Reply to you");
                        botReplyMenu.add(radioButtonMenuItem3);

                        //---- radioButtonMenuItem2 ----
                        radioButtonMenuItem2.setText("Reply to none");
                        radioButtonMenuItem2.setSelected(true);
                        botReplyMenu.add(radioButtonMenuItem2);
                    }
                    preferencesMenu.add(botReplyMenu);

                    //---- autoReconnectToggle ----
                    autoReconnectToggle.setText("Auto-Reconnect");
                    autoReconnectToggle.setSelected(true);
                    autoReconnectToggle.addItemListener(e -> autoReconnectToggleItemStateChanged(e));
                    preferencesMenu.add(autoReconnectToggle);

                    //---- alwaysOnTopToggle ----
                    alwaysOnTopToggle.setText("Always On Top");
                    alwaysOnTopToggle.setSelected(false);
                    alwaysOnTopToggle.addItemListener(e -> alwaysOnTopToggleItemStateChanged(e));
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
                    soundsToggle.addItemListener(e -> soundsToggleItemStateChanged(e));
                    toolsMenu.add(soundsToggle);

                    //======== soundDelayMenu ========
                    {
                        soundDelayMenu.setText("Sound Delay");

                        //---- soundDelayOffOption ----
                        soundDelayOffOption.setText("None (Off)");
                        soundDelayOffOption.addActionListener(e -> {

                        });
                        soundDelayMenu.add(soundDelayOffOption);

                        //---- soundDelay5secOption ----
                        soundDelay5secOption.setText("5 seconds");
                        soundDelayMenu.add(soundDelay5secOption);

                        //---- soundDelay10secOption ----
                        soundDelay10secOption.setText("10 seconds");
                        soundDelay10secOption.setSelected(true);
                        soundDelayMenu.add(soundDelay10secOption);

                        //---- soundDelay20secOption ----
                        soundDelay20secOption.setText("20 seconds");
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
                        soundPermissionMenu.add(soundPermEveryoneOption);

                        //---- soundPermSDMBOption ----
                        soundPermSDMBOption.setText("Subs, Donors, Mods, Broadcaster");
                        soundPermSDMBOption.setSelected(true);
                        soundPermissionMenu.add(soundPermSDMBOption);

                        //---- soundPermDMBOption ----
                        soundPermDMBOption.setText("Donors, Mods, Broadcaster");
                        soundPermissionMenu.add(soundPermDMBOption);

                        //---- soundPermModAndBroadOption ----
                        soundPermModAndBroadOption.setText("Mods and Broadcaster Only");
                        soundPermissionMenu.add(soundPermModAndBroadOption);

                        //---- soundPermBroadOption ----
                        soundPermBroadOption.setText("Broadcaster Only");
                        soundPermissionMenu.add(soundPermBroadOption);
                    }
                    toolsMenu.add(soundPermissionMenu);

                    //---- manageTextCommandsOption ----
                    manageTextCommandsOption.setText("Manage Text Commands...");
                    manageTextCommandsOption.addActionListener(e -> manageTextCommandsOptionActionPerformed());
                    toolsMenu.add(manageTextCommandsOption);
                    toolsMenu.addSeparator();

                    //======== runAdMenu ========
                    {
                        runAdMenu.setText("Run Ad");

                        //---- timeOption30sec ----
                        timeOption30sec.setText("30 sec");
                        runAdMenu.add(timeOption30sec);

                        //---- timeOption60sec ----
                        timeOption60sec.setText("1 min");
                        runAdMenu.add(timeOption60sec);

                        //---- timeOption90sec ----
                        timeOption90sec.setText("1 min 30 sec");
                        runAdMenu.add(timeOption90sec);

                        //---- timeOption120sec ----
                        timeOption120sec.setText("2 min");
                        runAdMenu.add(timeOption120sec);

                        //---- timeOption150sec ----
                        timeOption150sec.setText("2 min 30 sec");
                        runAdMenu.add(timeOption150sec);

                        //---- timeOption180sec ----
                        timeOption180sec.setText("3 min");
                        runAdMenu.add(timeOption180sec);
                    }
                    toolsMenu.add(runAdMenu);

                    //---- updateStatusOption ----
                    updateStatusOption.setText("Update Status...");
                    updateStatusOption.addActionListener(e -> updateStatusOptionActionPerformed());
                    toolsMenu.add(updateStatusOption);

                    //---- subOnlyToggle ----
                    subOnlyToggle.setText("Sub-only Chat");
                    subOnlyToggle.addItemListener(e -> subOnlyToggleItemStateChanged(e));
                    toolsMenu.add(subOnlyToggle);

                    //======== slowModeMenu ========
                    {
                        slowModeMenu.setText("Slow Mode");

                        //---- slowModeOffOption ----
                        slowModeOffOption.setText("Off");
                        slowModeOffOption.setSelected(true);
                        slowModeMenu.add(slowModeOffOption);

                        //---- slowMode5secOption ----
                        slowMode5secOption.setText("5 seconds");
                        slowModeMenu.add(slowMode5secOption);

                        //---- slowMode10secOption ----
                        slowMode10secOption.setText("10 seconds");
                        slowModeMenu.add(slowMode10secOption);

                        //---- slowMode15secOption ----
                        slowMode15secOption.setText("15 seconds");
                        slowModeMenu.add(slowMode15secOption);

                        //---- slowMode30secOption ----
                        slowMode30secOption.setText("30 seconds");
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
                    projectWikiOption.setText("Wiki");
                    projectWikiOption.addActionListener(e -> projectWikiOptionActionPerformed());
                    helpMenu.add(projectWikiOption);
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
                    allChats.addMouseListener(new ListenerURL());
                    allChats.addMouseListener(new ListenerName());
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
                                    .addComponent(channelPane, GroupLayout.PREFERRED_SIZE, 393, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
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
        botReplyGroup.add(radioButtonMenuItem1);
        botReplyGroup.add(radioButtonMenuItem3);
        botReplyGroup.add(radioButtonMenuItem2);

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
    private JRadioButtonMenuItem radioButtonMenuItem1;
    private JRadioButtonMenuItem radioButtonMenuItem3;
    private JRadioButtonMenuItem radioButtonMenuItem2;
    private JCheckBoxMenuItem autoReconnectToggle;
    private JCheckBoxMenuItem alwaysOnTopToggle;
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
    private JMenu runAdMenu;
    private JMenuItem timeOption30sec;
    private JMenuItem timeOption60sec;
    private JMenuItem timeOption90sec;
    private JMenuItem timeOption120sec;
    private JMenuItem timeOption150sec;
    private JMenuItem timeOption180sec;
    private JMenuItem updateStatusOption;
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