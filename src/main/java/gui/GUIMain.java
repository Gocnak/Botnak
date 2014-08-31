package gui;

import face.Face;
import face.SubscriberIcon;
import face.TwitchFace;
import gui.listeners.ListenerName;
import gui.listeners.ListenerURL;
import gui.listeners.NewTabListener;
import irc.Donator;
import irc.IRCBot;
import irc.IRCViewer;
import irc.Message;
import sound.Sound;
import sound.SoundEngine;
import thread.TabPulse;
import thread.heartbeat.BanQueue;
import thread.heartbeat.Heartbeat;
import thread.heartbeat.UserManager;
import thread.heartbeat.ViewerCount;
import util.*;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GUIMain extends JFrame {

    public static HashMap<String, Sound> soundMap;
    public static HashMap<String, Color> userColMap;
    public static HashSet<Command> commandSet;
    public static HashSet<String> channelSet;
    public static HashSet<SubscriberIcon> subIconSet;
    public static HashMap<String, ChatPane> chatPanes;
    public static HashSet<CombinedChatPane> combinedChatPanes;
    /**
     * Okay so. The String key is a name for the Face. IT IS NOT THE REGEX.
     * <p>
     * The name is never used to see if it's in chat, the REGEX IS.
     * <p>
     * When people add custom faces, they will _specify a name_ for
     * easier future referencing to it. Twitch faces will be the filenames
     * minus the extension, so they won't be able to change them unless
     * they guess the cache file name.
     * <p>
     * tl;dr: NAME (KEY) IS JUST A PLACEHOLDER HERE
     *///                 name    Face(Regex, Path)
    public static HashMap<String, Face> faceMap;
    public static HashMap<String, Face> nameFaceMap;
    public static HashMap<String, TwitchFace> twitchFaceMap;
    public static HashMap<String, Color> keywordMap;
    public static HashSet<Donator> donators;

    private static int userResponsesIndex = 0;
    public static ArrayList<String> userResponses;

    public static HashSet<ConsoleCommand> conCommands;

    public static IRCBot bot;
    public static IRCViewer viewer;
    public static GUISettings settings = null;
    public static GUIStreams streams = null;
    public static AuthorizeAccountGUI accountGUI = null;

    public static boolean shutDown = false;
    public static boolean doneWithFaces = false;
    public static boolean doneWithTwitchFaces = false;

    public static SimpleAttributeSet norm = new SimpleAttributeSet();

    public static String lastSoundDir = "";

    public static GUIMain instance;

    public static Settings currentSettings;

    public static HashSet<TabPulse> tabPulses;

    public static Heartbeat heartbeat;

    public GUIMain() {
        instance = this;
        soundMap = new HashMap<>();
        channelSet = new HashSet<>();
        faceMap = new HashMap<>();
        userColMap = new HashMap<>();
        commandSet = new HashSet<>();
        twitchFaceMap = new HashMap<>();
        nameFaceMap = new HashMap<>();
        conCommands = new HashSet<>();
        keywordMap = new HashMap<>();
        subIconSet = new HashSet<>();
        tabPulses = new HashSet<>();
        donators = new HashSet<>();
        combinedChatPanes = new HashSet<>();
        userResponses = new ArrayList<>();
        SoundEngine.init();
        StyleConstants.setForeground(norm, Color.white);
        initComponents();
        chatPanes = new HashMap<>();
        chatPanes.put("System Logs", new ChatPane("System Logs", allChatsScroll, allChats, 0));
        currentSettings = new Settings();
        currentSettings.load();
        StyleConstants.setFontFamily(norm, currentSettings.font.getFamily());
        StyleConstants.setFontSize(norm, currentSettings.font.getSize());
        heartbeat = new Heartbeat();
        heartbeat.addHeartbeatThread(new ViewerCount());
        heartbeat.addHeartbeatThread(new UserManager());
        heartbeat.addHeartbeatThread(new BanQueue());
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


    public void keyTypedEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            chatButtonActionPerformed();
        }
        int initial = userResponsesIndex;
        if (!userResponses.isEmpty()) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                if (userChat.getText().equals("")) {
                    userResponsesIndex = userResponses.size() - 1;
                } else if (userResponses.contains(userChat.getText())) {
                    if (userResponsesIndex == 0) userResponsesIndex = userResponses.size() - 1;
                    else userResponsesIndex--;
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                if (userChat.getText().equals("")) {
                    userResponsesIndex = 0;
                } else if (userResponses.contains(userChat.getText())) {
                    if (userResponsesIndex == userResponses.size() - 1) userResponsesIndex = 0;
                    else userResponsesIndex++;
                }
            }
        }
        if (initial != userResponsesIndex) {
            EventQueue.invokeLater(() -> userChat.setText(userResponses.get(userResponsesIndex)));
        }
    }


    public void chatButtonActionPerformed() {
        if (GUIMain.currentSettings.accountManager.getViewer() == null) return;
        String channel = channelPane.getTitleAt(channelPane.getSelectedIndex());
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
     * Logs a message to the chat console under all white, SYS username.
     * This should only be used for serious reports, like exception reporting and
     * other status updates.
     *
     * @param message The thing to log.
     */
    public static void log(Object message) {
        if (message != null && GUIMain.chatPanes != null && !GUIMain.chatPanes.isEmpty())
            onMessage(new Message(message.toString(), Message.MessageType.LOG_MESSAGE));
    }

    /**
     * The central message handle.
     *
     * @param message The message to handle.
     */
    public static void onMessage(final Message message) {
        Runnable handler = () -> instance.onMessageAction(message);
        if (EventQueue.isDispatchThread()) {
            handler.run();
        } else {
            try {
                EventQueue.invokeLater(handler);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
    }


    private synchronized void onMessageAction(Message mess) {
        if (mess != null && mess.getType() != null) {
            if (mess.getType() == Message.MessageType.LOG_MESSAGE) {
                chatPanes.get("System Logs").log(mess.getContent());
            }
            if (mess.getType() == Message.MessageType.NORMAL_MESSAGE ||
                    mess.getType() == Message.MessageType.ACTION_MESSAGE) {
                if (!combinedChatPanes.isEmpty()) {
                    for (CombinedChatPane cc : combinedChatPanes) {
                        for (String chan : cc.getChannels()) {
                            if (mess.getChannel().substring(1).equalsIgnoreCase(chan)) {
                                //TODO a "don't show channel source" setting?
                                cc.onMessage(mess, true);
                                break;
                            }
                        }
                    }
                }
                if (chatPanes.get(mess.getChannel().substring(1)) != null)
                    chatPanes.get(mess.getChannel().substring(1)).onMessage(mess, false);
            }
            if (mess.getType() == Message.MessageType.SUB_NOTIFY) {
                String channel = mess.getChannel().substring(1);
                chatPanes.get(channel).onSub(mess);
                if (channel.equalsIgnoreCase(
                        GUIMain.currentSettings.accountManager.getUserAccount().getName())
                        && currentSettings.subSound != null) {
                    SoundEngine.getEngine().playSubSound();
                }
                //TODO add the sub as a monthly donator
                //that is; keep track of how many months they are subbed,
                //and once they lose sub have their donor status instead.
            }
            if (mess.getType() == Message.MessageType.BAN_NOTIFY) {
                chatPanes.get(mess.getChannel().substring(1)).onBan(mess.getContent());
            }
            if (mess.getType() == Message.MessageType.HOSTED_NOTIFY) {//being hosted
                chatPanes.get(mess.getChannel()).onBeingHosted(mess.getContent());
            }
            if (mess.getType() == Message.MessageType.HOSTING_NOTIFY) {//is hosting someone else
                chatPanes.get(mess.getChannel()).log(mess.getContent());
            }
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


    public void manageAccountActionPerformed() {
        if (accountGUI == null) {
            accountGUI = new AuthorizeAccountGUI();
        }
        if (!accountGUI.isVisible()) {
            accountGUI.setVisible(true);
        }
    }

    public void settingsButtonActionPerformed() {
        if (settings == null) {
            settings = new GUISettings();
        }
        if (!settings.isVisible()) {
            settings.setVisible(true);
        }
    }

    public void exitButtonActionPerformed() {
        shutDown = true;
        if (viewer != null) {
            viewer.close(false);
        }
        if (bot != null) {
            bot.close(false);
        }
        tabPulses.clear();
        currentSettings.save();
        heartbeat.interrupt();
        if (Utils.faceCheck != null && Utils.faceCheck.isAlive()) Utils.faceCheck.interrupt();
        String[] keys = chatPanes.keySet().toArray(new String[chatPanes.keySet().size()]);
        if (currentSettings.logChat) {
            for (String s : keys) {
                ChatPane cp = chatPanes.get(s);
                Utils.logChat(cp.getText().split("\\n"), s, 2);
            }
        }
        dispose();
        System.exit(0);
    }


    public synchronized void pulseTab(final int index) {
        if (shutDown) return;
        ChatPane cp = Utils.getChatPane(index);
        CombinedChatPane ccp = Utils.getCombinedChatPane(index);
        boolean first = cp != null;
        boolean second = ccp != null;
        //TODO check currentSettings.pulseTabs here
        if (first || second) {
            if (!tabPulses.isEmpty()) {
                for (TabPulse tb : tabPulses) {
                    if (tb.getIndex() == index) {
                        return;
                    }
                }
            }
            TabPulse tp = new TabPulse(index);
            tp.start();
            tabPulses.add(tp);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        channelPane = new DraggableTabbedPane();
        allChatsScroll = new JScrollPane();
        allChats = new JTextPane();
        scrollPane1 = new JScrollPane();
        userChat = new JTextArea();
        chatButton = new JButton();
        exitButton = new JButton();
        loginsButton = new JButton();
        manageAccount = new JButton();

        //======== Botnak ========
        {
            setMinimumSize(new Dimension(750, 420));
            setName("Botnak");
            setTitle("Botnak | User: <none> | Bot: <none>");
            setResizable(true);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
            Container BotnakContentPane = getContentPane();

            //======== channelPane ========
            {
                channelPane.setTabPlacement(SwingConstants.BOTTOM);
                channelPane.setFocusable(false);
                channelPane.addChangeListener(Constants.tabListener);
                channelPane.addMouseListener(Constants.tabListener);

                //======== allChatsScroll ========
                {
                    allChatsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                    //---- allChats ----
                    allChats.setEditable(false);
                    allChats.setForeground(Color.white);
                    allChats.setBackground(Color.black);
                    allChats.setMargin(new Insets(0, 0, 0, 0));
                    allChats.setFont(new Font("Calibri", Font.PLAIN, 18));
                    allChats.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    allChats.addMouseListener(new ListenerURL());
                    allChats.addMouseListener(new ListenerName());
                    allChatsScroll.setViewportView(allChats);
                }
                channelPane.addTab("System Logs", allChatsScroll);
            }

            {
                JTextPane blank = new JTextPane();
                JScrollPane blankParent = new JScrollPane();
                blank.setEditable(false);
                blank.setForeground(Color.white);
                blank.setBackground(Color.black);
                blank.setMargin(new Insets(0, 0, 0, 0));
                blank.setFont(new Font("Calibri", Font.PLAIN, 18));
                blank.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                blankParent.setViewportView(blank);
                channelPane.addTab("+", blankParent);
                channelPane.addMouseListener(new NewTabListener());
            }

            //======== scrollPane1 ========
            {
                //---- userChat ----
                userChat.setFont(new Font("Consolas", Font.PLAIN, 10));
                userChat.setLineWrap(true);
                userChat.setWrapStyleWord(true);
                userChat.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        keyTypedEvent(e);
                    }
                });
                scrollPane1.setViewportView(userChat);
            }

            //---- chatButton ----
            chatButton.setText("Chat");
            chatButton.setFocusable(false);
            chatButton.setToolTipText("Send a chat message.");
            chatButton.addActionListener(e -> chatButtonActionPerformed());

            //---- exitButton ----
            exitButton.setText("Save & Exit");
            exitButton.setFocusable(false);
            exitButton.setToolTipText("Gracefully save all settings, exit all connected streams, and shutdown the program.");
            exitButton.addActionListener(e -> exitButtonActionPerformed());

            //---- loginsButton ----
            loginsButton.setText("Settings");
            loginsButton.setFocusable(false);
            loginsButton.setToolTipText("Manage the settings of Botnak.");
            loginsButton.addActionListener(e -> settingsButtonActionPerformed());

            //---- manageAccount ----
            manageAccount.setText("Manage Accounts");
            manageAccount.setFocusable(false);
            manageAccount.setFocusPainted(false);
            manageAccount.setEnabled(false);
            manageAccount.setToolTipText("Use the setting GUI to manage accounts.");
            manageAccount.addActionListener(e -> manageAccountActionPerformed());

            GroupLayout BotnakContentPaneLayout = new GroupLayout(BotnakContentPane);
            BotnakContentPane.setLayout(BotnakContentPaneLayout);
            BotnakContentPaneLayout.setHorizontalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 446, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(chatButton)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                            .addComponent(loginsButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(manageAccount, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(exitButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap())
                            .addComponent(channelPane)
            );
            BotnakContentPaneLayout.setVerticalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, BotnakContentPaneLayout.createSequentialGroup()
                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(exitButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(loginsButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(manageAccount))
                                            .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(chatButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(channelPane, GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
            );
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (channelPane != null) {
                        channelPane.scrollDownPanes();
                    }
                    super.componentResized(e);
                }
            });
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    exitButtonActionPerformed();
                    super.windowClosing(e);
                }
            });
            pack();
            setLocationRelativeTo(getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static DraggableTabbedPane channelPane;
    public static JScrollPane allChatsScroll;
    public static JTextPane allChats;
    public static JScrollPane scrollPane1;
    public static JTextArea userChat;
    public static JButton chatButton;
    public static JButton exitButton;
    public static JButton loginsButton;
    public static JButton manageAccount;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}