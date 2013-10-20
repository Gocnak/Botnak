package gui;

import irc.IRCBot;
import irc.IRCViewer;
import util.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;

public class GUIMain extends JFrame {

    public static HashMap<String, Sound> soundMap;
    public static HashMap<String, int[]> userColMap;
    public static HashSet<Command> commandSet;
    public static HashSet<String> channelSet;
    public static HashMap<String, ChatPane> chatPanes;
    /**
     * Okay so. The String key is a name for the Face. IT IS NOT THE REGEX.
     * <p/>
     * The name is never used to see if it's in chat, the REGEX IS.
     * <p/>
     * When people add custom faces, they will _specify a name_ for
     * easier future referencing to it. Twitch faces will be the filenames
     * minus the extension, so they won't be able to change them unless
     * they guess the cache file name.
     * <p/>
     * tl;dr: NAME (KEY) IS JUST A PLACEHOLDER HERE
     *///                 name    Face(Regex, Path)
    public static HashMap<String, Face> faceMap;

    public static IRCBot bot;
    public static IRCViewer viewer;
    public static GUISettings settings = null;
    public static GUIStreams streams = null;

    public static boolean shutDown = false;
    public static boolean doneWithFaces = false;


    public static SimpleAttributeSet norm = new SimpleAttributeSet();
    public static SimpleAttributeSet user = new SimpleAttributeSet();
    public static SimpleAttributeSet color = new SimpleAttributeSet();

    public static void setFont() {
        allChats.setFont(currentSettings.font);
        StyleConstants.setFontFamily(norm, currentSettings.font.getFamily());
        StyleConstants.setFontSize(norm, currentSettings.font.getSize());
        StyleConstants.setFontFamily(user, currentSettings.font.getFamily());
        StyleConstants.setFontSize(user, currentSettings.font.getSize());
        StyleConstants.setFontFamily(color, currentSettings.font.getFamily());
        StyleConstants.setFontSize(color, currentSettings.font.getSize());
    }

    public static String lastSoundDir = "";

    public static Sound currentSound = null;

    private static GUIMain instance;

    public static Settings currentSettings;

    public GUIMain() {
        instance = this;
        soundMap = new HashMap<>();
        channelSet = new HashSet<>();
        faceMap = new HashMap<>();
        userColMap = new HashMap<>();
        commandSet = new HashSet<>();
        StyleConstants.setForeground(norm, Color.white);
        StyleConstants.setForeground(color, Color.orange);
        initComponents();
        chatPanes = new HashMap<>();
        chatPanes.put("All Chats", new ChatPane("All Chats", allChats));
        currentSettings = new Settings();
        currentSettings.load();
        if (loadedSettingsUser()) {
            if (currentSettings.autoLogin) {
                viewer = new IRCViewer(currentSettings.user.getAccountName(), currentSettings.user.getAccountPass());
            }
        }
        if (loadedSettingsBot()) {
            if (currentSettings.autoLogin) {
                bot = new IRCBot(currentSettings.bot.getAccountName(), currentSettings.bot.getAccountPass());
            }
        }
        if (loadedStreams()) {
            String[] channels = channelSet.toArray(new String[channelSet.size()]);
            for (String c : channels) {
                if (c != null) {
                    if (viewer != null) viewer.doConnect(c);
                    if (bot != null) bot.doConnect(c);
                    ChatPane.createPane(c);
                }
            }
            log("LOADED STREAMS");
        }
        if (loadedCommands()) {
            log("LOADED COMMANDS");
        }
        addStream.setEnabled(loadedSettingsUser());
    }


    public static boolean loadedSettingsUser() {
        return currentSettings != null && currentSettings.user != null;
    }

    public static boolean loadedSettingsBot() {
        return currentSettings != null && currentSettings.bot != null;
    }

    public static boolean loadedStreams() {
        return !channelSet.isEmpty();
    }

    public static boolean loadedCommands() {
        return !commandSet.isEmpty();
    }


    public void keyTypedEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) chatButtonActionPerformed();
    }


    public void chatButtonActionPerformed() {
        if (GUIMain.viewer == null) return;
        String channel = channelPane.getTitleAt(channelPane.getSelectedIndex());
        String userInput = userChat.getText().replaceAll("\n", "");
        if (channel != null) {
            if (channel.equalsIgnoreCase("all chats")) {
                String[] channels = channelSet.toArray(new String[channelSet.size()]);
                for (String c : channels) {
                    if (!Utils.checkText(userInput).equals("")) {
                        viewer.sendMessage("#" + c, userInput);
                        boolean isMe = userInput.startsWith("/me");
                        if (isMe) userInput = userInput.replaceAll("/me ", "");
                        onMessage("#" + c, viewer.getMaster(), userInput, isMe);
                        userChat.setText("");
                    }
                }
            } else {
                if (!Utils.checkText(userInput).equals("")) {
                    viewer.sendMessage("#" + channel, userInput);
                    boolean isMe = userInput.startsWith("/me");
                    if (isMe) userInput = userInput.replaceAll("/me ", "");
                    onMessage("#" + channel, viewer.getMaster(), userInput, isMe);
                    userChat.setText("");
                }
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
        if (message != null) GUIMain.chatPanes.get("All Chats").log(message.toString());
    }

    //called from IRCViewer
    public static void onMessage(final String channel, final String sender, final String message, final boolean isMe) {
        Runnable handler = new Runnable() {
            public void run() {
                instance.onMessageAction(channel, sender, message, isMe);
            }
        };
        if (EventQueue.isDispatchThread()) {
            handler.run();
        } else {
            try {
                EventQueue.invokeAndWait(handler);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
    }


    private void onMessageAction(String channel, String sender, String message, boolean isMe) {
        if (message != null && channel != null && sender != null && GUIMain.viewer != null) {
            String[] keys = chatPanes.keySet().toArray(new String[chatPanes.keySet().size()]);
            for (String pane : keys) {
                if (channel.contains(pane)) {
                    chatPanes.get(pane).onMessage(channel, sender, message, isMe);
                    break;
                }
            }
            chatPanes.get("All Chats").onMessage(channel, sender, message, isMe);
        }
    }


    private static int viewerPeak = 0;
    private static int viewerCount = 0;
    public static Thread viewerCheck = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!shutDown) {
                countViewers();
                updateTitle();
                try {//sleep for a random time between 2.5 to 6 seconds
                    Thread.sleep(Utils.random(2500, 6000));
                } catch (Exception ignored) {
                }
            }
        }
    });

    public static void updateTitle() {
        StringBuilder stanSB = new StringBuilder();
        stanSB.append("Botnak | Viewer count: ");
        stanSB.append(viewerCount);
        stanSB.append(" (");
        stanSB.append(viewerPeak);
        stanSB.append(") | User: ");
        String userNorm = (currentSettings != null && currentSettings.user != null)
                ? currentSettings.user.getAccountName() : "<none>";
        stanSB.append(userNorm);
        stanSB.append(" | Bot: ");
        String userBot = (currentSettings != null && currentSettings.bot != null)
                ? currentSettings.bot.getAccountName() : "<none>";
        stanSB.append(userBot);
        instance.setTitle(stanSB.toString());
    }


    public static void countViewers() {
        try {//this could be parsed with JSON, but patterns work, and if it ain't broke...
            int count = 0;
            URL twitch = new URL("http://api.justin.tv/api/stream/summary.json?channel=" + viewer.getMaster());
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line;
            while (!shutDown && (line = br.readLine()) != null) {
                Matcher m = Constants.viewerPattern.matcher(line);
                if (m.find()) {
                    try {
                        count = Integer.parseInt(m.group(1));
                        if (count >= viewerPeak) viewerPeak = count;
                        break;
                    } catch (Exception ignored) {
                    }//bad Int parsing
                }
            }
            br.close();
            viewerCount = count;
        } catch (Exception e) {
            viewerCount = 0;
        }
    }

    public void addStreamActionPerformed() {
        if (streams == null) {
            streams = new GUIStreams();
        }
        if (!streams.isVisible()) {
            streams.setVisible(true);
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
        currentSettings.save();
        if (viewerCheck != null && viewerCheck.isAlive()) viewerCheck.interrupt();
        if (Utils.faceCheck != null && Utils.faceCheck.isAlive()) Utils.faceCheck.interrupt();
        String[] keys = chatPanes.keySet().toArray(new String[chatPanes.keySet().size()]);
        if (currentSettings.logChat) {
            for (String s : keys) {
                ChatPane cp = chatPanes.get(s);
                Utils.logChat(cp.getText().split("\\n"), s);
            }
        }
        dispose();
        System.exit(0);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        channelPane = new JTabbedPane();
        allChatsScroll = new JScrollPane();
        allChats = new JTextPane();
        scrollPane1 = new JScrollPane();
        userChat = new JTextArea();
        chatButton = new JButton();
        exitButton = new JButton();
        loginsButton = new JButton();
        addStream = new JButton();

        //======== Botnak ========
        {
            setMinimumSize(new Dimension(680, 380));
            setName("Botnak Control Panel");
            setTitle("Botnak | Viewer Count: 0 | User: <none> | Bot: <none>");
            setResizable(false);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
            Container BotnakContentPane = getContentPane();

            //======== channelPane ========
            {
                channelPane.setTabPlacement(SwingConstants.BOTTOM);
                channelPane.setFocusable(false);

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
                    allChats.addMouseListener(new MouseListener() {
                        public void mouseClicked(MouseEvent e) {
                        }

                        public void mousePressed(MouseEvent e) {
                        }

                        public void mouseEntered(MouseEvent e) {
                        }

                        public void mouseExited(MouseEvent e) {
                        }

                        //credit to Fenerista from
                        //http://www.daniweb.com/software-development/java/threads/331500/how-can-i-add-a-clickable-url-in-a-jtextpane#post1422477
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            JTextPane editor = (JTextPane) e.getSource();
                            Point pt = new Point(e.getX(), e.getY());
                            int pos = editor.viewToModel(pt);
                            if (pos >= 0) {
                                Document doc = editor.getDocument();
                                if (doc instanceof DefaultStyledDocument) {
                                    DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                                    Element el = hdoc.getCharacterElement(pos);
                                    AttributeSet a = el.getAttributes();
                                    String href = (String) a.getAttribute(HTML.Attribute.HREF);
                                    if (href != null) {
                                        try {
                                            Desktop desktop = Desktop.getDesktop();
                                            URI uri = new URI(href);
                                            desktop.browse(uri);
                                        } catch (Exception ev) {
                                            GUIMain.log((ev.getMessage()));
                                        }
                                    }
                                }
                            }
                        }
                    });
                    allChatsScroll.setViewportView(allChats);
                }
                channelPane.addTab("All Chats", allChatsScroll);
            }

            //======== scrollPane1 ========
            {
                //---- userChat ----
                userChat.setFont(new Font("Consolas", Font.PLAIN, 10));
                userChat.setLineWrap(true);
                userChat.setWrapStyleWord(true);
                userChat.addKeyListener(new KeyListener() {
                    public void keyTyped(KeyEvent e) {
                    }

                    public void keyPressed(KeyEvent e) {
                    }

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
            chatButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chatButtonActionPerformed();
                }
            });

            //---- exitButton ----
            exitButton.setText("Save & Exit");
            exitButton.setFocusable(false);
            exitButton.setToolTipText("Gracefully save all settings, exit all connected streams, and shutdown the program.");
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exitButtonActionPerformed();
                }
            });

            //---- loginsButton ----
            loginsButton.setText("Settings");
            loginsButton.setFocusable(false);
            loginsButton.setToolTipText("Manage the settings of Botnak.");
            loginsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settingsButtonActionPerformed();
                }
            });

            //---- addStream ----
            addStream.setText("Add/Remove a Stream Chat");
            addStream.setFocusable(false);
            addStream.setFocusPainted(false);
            addStream.setToolTipText("Add (or remove) a stream to (not) get their chat's messages.");
            addStream.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addStreamActionPerformed();
                }
            });

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
                                            .addComponent(addStream, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
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
                                                    .addComponent(addStream))
                                            .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(chatButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(channelPane, GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
            );
            pack();
            setLocationRelativeTo(getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JTabbedPane channelPane;
    public static JScrollPane allChatsScroll;
    public static JTextPane allChats;
    public static JScrollPane scrollPane1;
    public static JTextArea userChat;
    public static JButton chatButton;
    public static JButton exitButton;
    public static JButton loginsButton;
    public static JButton addStream;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}