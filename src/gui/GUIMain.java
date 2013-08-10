package gui;

import irc.IRCBot;
import irc.IRCViewer;
import lib.pircbot.org.jibble.pircbot.User;
import util.Sound;
import util.StringArray;
import util.Timer;
import util.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIMain extends JFrame {

    //command, sound
    public static HashMap<String, Sound> soundMap;
    public static HashMap<String, String> imgMap;
    public static HashMap<String, int[]> userColMap;
    public static HashMap<StringArray, Timer> commandMap;
    public static HashSet<String> channelMap;
    public static File defaultDir;
    public static String userNorm, userBot, userNormPass, userBotPass;

    public static IRCBot bot;
    public static IRCViewer viewer;

    public static File accountsFile;
    public static File streamsFile;
    public static File soundsFile;
    public static File imgFile;
    public static File userColFile;
    public static File commandsFile;
    public static File defaultsFile;

    public static String customMod = GUIMain.class.getResource("/resource/mod.png").getFile();
    public static String customBroad = GUIMain.class.getResource("/resource/broad.png").getFile();

    public static GUIMain mainGUI;

    public static boolean shutDown = false;
    public static boolean rememberNorm = false;
    public static boolean rememberBot = false;
    public static boolean autoLog = false;

    SimpleDateFormat format = new SimpleDateFormat("[h:mm a]", Locale.getDefault());
    SimpleAttributeSet norm = new SimpleAttributeSet();
    SimpleAttributeSet user = new SimpleAttributeSet();
    SimpleAttributeSet color = new SimpleAttributeSet();
    SimpleAttributeSet line = new SimpleAttributeSet();

    public static String lastSoundDir = "";
    public static String defaultSoundDir = "";
    public static String defaultFaceDir = "";
    public static boolean useMod = false;
    public static boolean useBroad = false;

    public static Sound currentSound = null;


    public GUIMain() {
        soundMap = new HashMap<>();
        channelMap = new HashSet<>();
        imgMap = new HashMap<>();
        userColMap = new HashMap<>();
        commandMap = new HashMap<>();
        defaultDir = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()
                + File.separator + "Botnak");
        soundsFile = new File(defaultDir + File.separator + "sounds.txt");
        streamsFile = new File(defaultDir + File.separator + "streams.txt");
        accountsFile = new File(defaultDir + File.separator + "acc.ini");
        imgFile = new File(defaultDir + File.separator + "faces.txt");
        userColFile = new File(defaultDir + File.separator + "usercols.txt");
        commandsFile = new File(defaultDir + File.separator + "commands.txt");
        defaultsFile = new File(defaultDir + File.separator + "defaults.ini");
        StyleConstants.setFontFamily(norm, "Calibri");
        StyleConstants.setFontSize(norm, 18);
        StyleConstants.setForeground(norm, Color.white);
        StyleConstants.setFontFamily(user, "Calibri");
        StyleConstants.setFontSize(user, 18);
        StyleConstants.setForeground(color, Color.orange);
        StyleConstants.setFontFamily(color, "Calibri");
        StyleConstants.setFontSize(color, 18);
        initComponents();
        initListener();
        Utils.buildImages(imgMap, imgFile);
        if (defaultDir.exists() && defaultDir.list().length > 0) {
            try {
                loadSettings();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            defaultDir.mkdirs();
        }
        if (loadedSettingsUser()) {
            System.out.println("LOADED USER");
            rememberNorm = true;
            if (autoLog) {
                viewer = new IRCViewer(userNorm, userNormPass);
            }
        }
        if (loadedSettingsBot()) {
            System.out.println("LOADED BOT");
            rememberBot = true;
            if (autoLog) {
                bot = new IRCBot(userBot, userBotPass);
            }
        }
        if (loadedStreams()) {
            System.out.println("LOADED STREAMS");
            String[] channels = channelMap.toArray(new String[channelMap.size()]);
            DefaultComboBoxModel<String> d = new DefaultComboBoxModel<>();
            d.addElement("All Chats");
            for (String c : channels) {
                if (c != null) {
                    if (viewer != null) viewer.doConnect(c);
                    if (bot != null) bot.doConnect(c);
                    d.addElement(c);
                }
            }
            streamList.setModel(d);
        }
        if (loadedCommands()) {
            System.out.println("LOADED COMMANDS");
        }
        addStream.setEnabled(loadedSettingsUser());

        mainGUI = this;
    }

    public static void loadSettings() {
        if (accountsFile != null && accountsFile.exists()) {
            System.out.println("Loading accounts...");
            Utils.loadPropData(accountsFile, 0);
        }
        if (defaultsFile != null && defaultsFile.exists()) {
            System.out.println("Loading defaults...");
            Utils.loadPropData(defaultsFile, 1);
        }
        if (streamsFile != null && streamsFile.exists()) {
            System.out.println("Loading streams...");
            String[] readStreams = Utils.loadStreams(streamsFile);
            if (readStreams != null) {
                Collections.addAll(channelMap, readStreams);
            }
        }
        if (soundsFile != null && soundsFile.exists()) {
            System.out.println("Loading sounds...");
            Utils.loadSounds(soundMap, soundsFile);
        }
        if (userColFile != null && userColFile.exists()) {
            System.out.println("Loading user colors...");
            Utils.loadUserColors(userColMap, userColFile);
        }
        if (commandsFile != null && commandsFile.exists()) {
            System.out.println("Loading commands...");
            Utils.loadCommands(commandMap, commandsFile);
        }
    }

    public static boolean loadedSettingsUser() {
        return userNorm != null && userNormPass != null;
    }

    public static boolean loadedSettingsBot() {
        return userBot != null && userBotPass != null;
    }

    public static boolean loadedStreams() {
        return !channelMap.isEmpty();
    }

    public static boolean loadedCommands() {
        return !commandMap.isEmpty();
    }


    public void keyTypedEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) chatButtonActionPerformed();
    }


    public void chatButtonActionPerformed() {
        String channel = streamList.getSelectedItem().toString();
        String userInput = userChat.getText().replaceAll("\n", "");
        if (channel != null) {
            if (channel.equalsIgnoreCase("all chats")) {
                String[] channels = channelMap.toArray(new String[channelMap.size()]);
                for (String c : channels) {
                    if (c.equalsIgnoreCase("all chats")) continue;
                    if (!Utils.checkText(userInput).equals("")) {
                        viewer.sendMessage("#" + c, userInput);
                        boolean isMe = userInput.startsWith("/me");
                        if (isMe) userInput = userInput.replaceAll("/me ", "");
                        onMessage("#" + channel, viewer.getMaster(), userInput, isMe);
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

    public static String magic(final String input, final char c,
                               final int distance) {
        if (input == null) {
            throw new NullPointerException("expected: input != null");
        }
        if (distance < 2) {
            throw new IndexOutOfBoundsException("expected: distance > 1");
        }
        final StringBuilder builder = new StringBuilder(input);
        int next = -1, current = 0;
        while ((next = builder.indexOf(" ", current)) >= 0) {
            final int result = innerLoop(c, distance, builder, next, current);
            current = next + 1 + result;
        }
        innerLoop(c, distance, builder, builder.length(), current);
        return builder.toString();
    }

    private static int innerLoop(final char c, final int distance,
                                 final StringBuilder builder, int next, int current) {
        int linebreaks = 0;
        if (current + next >= distance) {
            for (int i = current + distance; i < next; i += distance) {
                builder.insert(i + linebreaks, c);

                linebreaks++;
            }
        }
        return linebreaks;
    }

    //called from IRCViewer
    public void onMessage(String channel, String sender, String message, boolean isMe) {
        if (message != null && channel != null && sender != null) {
            if (message.replaceAll(" ", "").length() > 512) return;
            message = magic(message, '\u0020', 20);
            String time = format.format(new Date(System.currentTimeMillis()));
            StyledDocument doc = chatText.getStyledDocument();
            Color c;
            if (userColMap.containsKey(sender)) {
                c = new Color(userColMap.get(sender)[0], userColMap.get(sender)[1], userColMap.get(sender)[2]);
            } else {
                c = Utils.getColor(sender.hashCode());
            }
            StyleConstants.setForeground(user, c);
            try {
                chatText.setCaretPosition(doc.getLength());
                int start = chatText.getCaretPosition();
                doc.insertString(chatText.getCaretPosition(), time, norm);
                if (Utils.isUserOp(GUIMain.viewer, channel, sender)) {
                    if (channel.contains(sender.toLowerCase())) {//broadcaster
                        insertModIcon(doc, chatText.getCaretPosition(), 1);
                    } else {//a mod
                        insertModIcon(doc, chatText.getCaretPosition(), 0);
                    }
                }
                if (sender.equalsIgnoreCase("pipe")) {//TODO change this to detect SPECIALUSER or some shit
                    insertModIcon(doc, chatText.getCaretPosition(), 2);
                }
                doc.insertString(chatText.getCaretPosition(), " " + sender + " ", user);
                if (isMe) {
                    doc.insertString(chatText.getCaretPosition(), message + "\n", message.toLowerCase().contains(viewer.getMaster()) ? color : user);
                } else {
                    doc.insertString(chatText.getCaretPosition(), "(" + channel.substring(1) + "): ", norm);
                    doc.insertString(chatText.getCaretPosition(), message + "\n", message.toLowerCase().contains(viewer.getMaster()) ? color : norm);
                }
                chatText.setCaretPosition(doc.getLength());
                doc.setParagraphAttributes(start, chatText.getCaretPosition(), line, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertModIcon(StyledDocument doc, int pos, int type) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        ImageIcon icon;
        String kind;
        switch (type) {
            case 0:
                icon = new ImageIcon(customMod);
                kind = "Mod";
                break;
            case 1:
                icon = new ImageIcon(customBroad);
                kind = "Broadcaster";
                break;
            case 2:
                icon = new ImageIcon(defaultFaceDir + File.separator + "admin.png");
                kind = "Admin";
                break;
            default:
                icon = new ImageIcon(customMod);
                kind = "Mod";
        }
        StyleConstants.setIcon(attrs, icon);
        try {
            doc.insertString(pos, " ", null);
            doc.insertString(pos + 1, kind, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Thread viewerCheck = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!shutDown) {
                viewerCount.setText(String.valueOf(viewerCount()));
                try {
                    Thread.sleep(5000);
                } catch (Exception ignored) {
                }
            }
        }
    });

    public static Pattern p = Pattern.compile("\"viewers_count\":\\s*(\\d+)");

    public static int viewerCount() {
        int count = 0;
        try {
            URL twitch = new URL("http://api.justin.tv/api/stream/summary.json?channel=" + viewer.getMaster());
            BufferedReader br = new BufferedReader(new InputStreamReader(twitch.openStream()));
            String line;
            while (!shutDown && (line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    try {
                        count = Integer.parseInt(m.group(1));
                    } catch (Exception e) {
                        count = 0;
                    }
                }
            }
            br.close();

        } catch (Exception e) {
            return 0;
        }
        return count;
    }

    public void addStreamActionPerformed() {
        GUIStreams s = new GUIStreams();
        s.setVisible(true);
    }

    public void loginsButtonActionPerformed() {
        GUILogin l = new GUILogin();
        l.setVisible(true);
    }

    public void addSoundActionPerformed() {
        GUISounds s = new GUISounds();
        s.setVisible(true);
    }

    public void addCommandActionPerformed() {
        GUIDefaults s = new GUIDefaults();
        s.setVisible(true);
    }

    public void initListener() {
        chatText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                final DocumentEvent e = event;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (e.getDocument() instanceof StyledDocument) {
                            try {
                                StyledDocument doc = (StyledDocument) e.getDocument();
                                int start = Utilities.getRowStart(chatText, Math.max(0, e.getOffset() - 1));
                                int end = Utilities.getWordEnd(chatText, e.getOffset() + e.getLength());
                                String text = doc.getText(start, end - start);
                                String[] faces = imgMap.keySet().toArray(new String[imgMap.keySet().size()]);
                                for (String s : faces) {
                                    if (text.contains(s)) {//case sensitive now
                                        int i = text.indexOf(s);
                                        while (i >= 0 && !shutDown) {
                                            final SimpleAttributeSet attrs = new SimpleAttributeSet(
                                                    doc.getCharacterElement(start + i).getAttributes());
                                            if (StyleConstants.getIcon(attrs) == null) {
                                                StyleConstants.setIcon(attrs, new ImageIcon(imgMap.get(s)));
                                                doc.remove(start + i, s.length());
                                                doc.insertString(start + i, s, attrs);
                                            }
                                            i = text.indexOf(s, i + s.length() - 1);
                                        }
                                    }
                                }
                            } catch (BadLocationException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }

            public void removeUpdate(DocumentEvent e) {
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    public void exitButtonActionPerformed() {
        shutDown = true;
        if (viewer != null && viewer.isConnected()) {
            for (String s : viewer.getChannels()) {
                viewer.doLeave(s.substring(1), false);
            }
            viewer.disconnect();
            viewer.dispose();
        }
        if (bot != null && bot.isConnected()) {
            for (String s : bot.getChannels()) {
                bot.doLeave(s.substring(1), false);
            }
            bot.disconnect();
            bot.dispose();
        }
        if (rememberBot || rememberNorm) Utils.saveAccountData();
        if (!soundMap.isEmpty()) Utils.saveSounds(soundMap, soundsFile);
        if (loadedStreams()) Utils.saveStreams(channelMap, streamsFile);
        if (!imgMap.isEmpty()) Utils.saveFaces(imgMap, imgFile);
        if (!userColMap.isEmpty()) Utils.saveUserColors(userColMap, userColFile);
        if (loadedCommands()) Utils.saveCommands(commandMap, commandsFile);
        if (viewerCheck != null && viewerCheck.isAlive()) viewerCheck.interrupt();
        dispose();
        System.exit(0);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Nick K
        chatButton = new JButton();
        addStream = new JButton();
        loginsButton = new JButton();
        addSound = new JButton();
        label1 = new JLabel();
        viewerCount = new JLabel();
        label3 = new JLabel();
        label2 = new JLabel();
        normUser = new JLabel();
        botUser = new JLabel();
        scrollPane2 = new JScrollPane();
        chatText = new JTextPane();
        addCommand = new JButton();
        streamList = new JComboBox<>();
        scrollPane1 = new JScrollPane();
        userChat = new JTextArea();
        exitButton = new JButton();

        //======== Botnak ========
        {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(680, 380));
            setName("Botnak Control Panel");
            setTitle("Botnak");
            Container BotnakContentPane = getContentPane();

            //---- chatButton ----
            chatButton.setText("Chat");
            chatButton.setFocusable(false);
            chatButton.setToolTipText("Send a chat message to all of the chats connected.");
            chatButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chatButtonActionPerformed();
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

            //---- loginsButton ----
            loginsButton.setText("Login Settings");
            loginsButton.setFocusable(false);
            loginsButton.setToolTipText("Manage the logins of both your normal account and bot account.");
            loginsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginsButtonActionPerformed();
                }
            });

            //---- addSound ----
            addSound.setText("Add Sound");
            addSound.setFocusable(false);
            addSound.setToolTipText("Add a soundfile and specify a command to activate it.");
            addSound.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addSoundActionPerformed();
                }
            });

            //---- label1 ----
            label1.setText("Viewer count: ");
            label1.setFocusable(false);
            label1.setHorizontalAlignment(SwingConstants.LEFT);
            label1.setFont(new Font("Tahoma", Font.BOLD, 16));

            //---- viewerCount ----
            viewerCount.setText("0");
            viewerCount.setFont(new Font("Tahoma", Font.BOLD, 16));
            viewerCount.setHorizontalAlignment(SwingConstants.LEFT);
            viewerCount.setFocusable(false);

            //---- label3 ----
            label3.setText("Bot Account:");

            //---- label2 ----
            label2.setText("Normal Account: ");

            //---- normUser ----
            normUser.setText("<none>");

            //---- botUser ----
            botUser.setText("<none>");

            //======== scrollPane2 ========
            {

                //---- chatText ----
                chatText.setEditable(false);
                chatText.setForeground(Color.white);
                chatText.setBackground(Color.black);
                chatText.setFont(new Font("Calibri", Font.PLAIN, 18));
                chatText.setMargin(new Insets(5, 5, 5, 5));
                scrollPane2.setViewportView(chatText);
                scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            }

            //---- addCommand ----
            addCommand.setText("Default Settings");
            addCommand.setFocusable(false);
            addCommand.setToolTipText("Opens the default settings.");
            addCommand.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addCommandActionPerformed();
                }
            });

            //---- streamList ----
            streamList.setMaximumRowCount(20);
            streamList.setModel(new DefaultComboBoxModel<>(new String[]{
                    "All Chats"
            }));
            streamList.setFocusable(false);

            //======== scrollPane1 ========
            {

                //---- userChat ----
                userChat.setFont(new Font("Consolas", Font.PLAIN, 10));
                userChat.setLineWrap(true);
                userChat.setWrapStyleWord(true);
                userChat.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }

                    public void keyPressed(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
                        keyTypedEvent(e);
                    }
                });
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane1.setViewportView(userChat);
            }

            //---- exitButton ----
            exitButton.setText("Exit");
            exitButton.setFocusable(false);
            exitButton.setToolTipText("Gracefully exit all connected streams and shutdown the program.");
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exitButtonActionPerformed();
                }
            });

            GroupLayout BotnakContentPaneLayout = new GroupLayout(BotnakContentPane);
            BotnakContentPane.setLayout(BotnakContentPaneLayout);
            BotnakContentPaneLayout.setHorizontalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addComponent(label1)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(viewerCount))
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addComponent(label3)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(botUser))
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addComponent(label2)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(normUser))
                                            .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                                    .addGroup(BotnakContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addGroup(GroupLayout.Alignment.TRAILING, BotnakContentPaneLayout.createSequentialGroup()
                                                    .addComponent(exitButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(addStream, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE))
                                            .addComponent(loginsButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                                            .addComponent(streamList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(chatButton))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                                            .addComponent(addCommand, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(addSound, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE))))
                                    .addContainerGap())
                            .addComponent(scrollPane2)
            );
            BotnakContentPaneLayout.setVerticalGroup(
                    BotnakContentPaneLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, BotnakContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(BotnakContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addGroup(GroupLayout.Alignment.TRAILING, BotnakContentPaneLayout.createSequentialGroup()
                                                    .addComponent(chatButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(streamList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(addStream)
                                                            .addComponent(exitButton))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(loginsButton)
                                                    .addGap(5, 5, 5)
                                                    .addComponent(addSound)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(addCommand)
                                                    .addGap(8, 8, 8))
                                            .addGroup(BotnakContentPaneLayout.createSequentialGroup()
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup()
                                                            .addComponent(label1)
                                                            .addComponent(viewerCount, GroupLayout.Alignment.TRAILING))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(label2)
                                                            .addComponent(normUser))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(BotnakContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(label3)
                                                            .addComponent(botUser))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
            );
            pack();
            setLocationRelativeTo(getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Nick K
    public static JButton chatButton;
    public static JButton addStream;
    public static JButton loginsButton;
    public static JButton addSound;
    public static JLabel label1;
    public static JLabel viewerCount;
    public static JLabel label3;
    public static JLabel label2;
    public static JLabel normUser;
    public static JLabel botUser;
    public static JScrollPane scrollPane2;
    public static JTextPane chatText;
    public static JButton addCommand;
    public static JComboBox<String> streamList;
    public static JScrollPane scrollPane1;
    public static JTextArea userChat;
    public static JButton exitButton;


}
