package irc;

import gui.GUIMain;
import lib.chatbot.ChatterBotSession;
import lib.chatbot.Cleverbot;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import util.Sound;
import util.StringArray;
import util.Timer;
import util.Utils;
import lib.chatbot.ChatterBot;

import java.net.ConnectException;

/**
 * //TODO
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
            GUIMain.log(e.getMessage());
        }
        botnakTimer = new Timer(chatTime);
        soundTimer = new Timer(soundTime);
        soundBackTimer = new Timer(0);
        if (GUIMain.loadedStreams()) {
            for (String s : GUIMain.channelMap) {
                doConnect(s);
            }
        }
        GUIMain.log("LOADED BOT: " + name);
        GUIMain.bot = this;
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!isConnected()) {
            try {
                connect("irc.twitch.tv", 6667, pass);
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
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
            sender = sender.toLowerCase();
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
                    User u = Utils.getUser(this, channel, sender);
                    if (u != null && u.isOp()) {
                        handleMod(channel, message.substring(1));
                    }
                    //sound
                    if (soundTrigger(content, sender, channel)) {
                        GUIMain.currentSound = GUIMain.soundMap.get(content);
                        GUIMain.currentSound.play();
                        soundTimer.reset();
                    }
                    //color
                    if (content.startsWith("setcol")) {
                        Utils.handleColor(sender, message.substring(1));
                    }
                    //reply
                    if (content.equals("ask")) {
                        if (!botnakTimer.isRunning()) {
                            talkBack(channel, sender, message.substring(4));
                        }
                    }
                    //command
                    if (Utils.commandTrigger(content)) {
                        handleMessage(channel, content);
                    }
                }
            }
        }
    }

    public void talkBack(String channel, String sender, String message) {
        if (channel != null && sender != null && message != null && session != null && chatBot != null) {
            String reply = "";
            if (sender.equals(GUIMain.viewer.getMaster())) sender = "Master";
            try {
                reply = session.think(message);
            } catch (Exception e) {
                if (e.getCause() instanceof ConnectException) {
                    session = chatBot.createSession();
                }
            }
            if (!reply.equals("")) {
                sendMessage(channel, sender + ", " + reply);
                botnakTimer.reset();
            }
        }
    }


    /**
     * Base trigger for sounds. Checks if a dev sound is not playing, if the general delay is up,
     * if the channel is yours, and if the user can even play the sound if it exists.
     *
     * @param s       Sound command's trigger/name.
     * @param send    The sender of the command.
     * @param channel Channel the command was in.
     * @return true to play the sound, else false
     */
    public boolean soundTrigger(String s, String send, String channel) {
        if (!soundBackTimer.isRunning() || (soundBackTimer.period > soundTimer.period)) {//check from a dev song
            soundBackTimer = new Timer(0);//reset the backup sound timer, and
            if (soundTimer.period > soundTime) {//check if the sound was longer (which is mostly true)
                soundTimer = new Timer(soundTime);//reset it so you don't have to
            }
        }
        if (!soundTimer.isRunning()) {//not on a delay
            if (channel.equalsIgnoreCase("#" + masterChannel)) {//is in main channel
                if (soundCheck(s, send, channel)) {//let's check the existence/permission
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
    public boolean soundCheck(String sound, String sender, String channel) {
        //set the permission
        int permission = Sound.PERMISSION_ALL;
        User u = Utils.getUser(this, channel, sender);
        if (u != null && u.isOp()) {
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
        String cont = Utils.getMessage(message);
        Timer timer = Utils.getTimer(message);
        if (!cont.equals("")) {
            if (!timer.isRunning()) {
                sendMessage(channel, cont);
                if (channel.equals(lastChannel)) {
                    if (message.equals(lastMessage) || message.equals(secondToLastMessage)) {
                        GUIMain.commandMap.put(new StringArray(new String[]{message, cont}), new Timer(timer.period));
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
        handleMod(channel, s);
    }

    public void handleMod(String channel, String s) {
        if (channel.substring(1).equals(GUIMain.viewer.getMaster())) {
            if (s.startsWith("removesound")) {
                String remove = s.split(" ")[1];
                if (GUIMain.soundMap.containsKey(remove)) {
                    GUIMain.soundMap.remove(remove);
                }
            }
            if (s.startsWith("addcommand")) {
                Utils.addCommands(s);
            }
            if (s.startsWith("removecommand")) {
                Utils.removeCommands(s.split(" ")[1]);
            }
            if (s.startsWith("changeface")) {
                Utils.handleFace(s);
            }
            if (s.startsWith("addface")) {
                Utils.handleFace(s);
            }
            if (s.startsWith("removeface")) {
                String[] split = s.split(" ");
                String toremove = split[1];
                if (GUIMain.faceMap.containsKey(toremove)) {
                    Utils.removeFace(toremove);
                }
            }
            if (s.startsWith("mod")) {
                String[] split = s.split(" ");
                String toMod = split[1];
                if (GUIMain.viewer != null) {
                    GUIMain.viewer.sendMessage(channel, ".mod " + toMod);
                }
            }
            if (s.startsWith("addsound")) {
                Utils.handleSound(s, false);
            }
            if (s.startsWith("changesound")) {
                Utils.handleSound(s, true);
            }
            if (s.startsWith("soundstate")) {
                int delay = soundTime / 1000;
                sendMessage(channel, "Sound is currently turned " + (stopSound ? "OFF" : "ON") + " with "
                        + (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds.")));
            }
            if (s.startsWith("togglesound")) {
                if (GUIMain.currentSound != null && GUIMain.currentSound.isPlaying()) {
                    GUIMain.currentSound.stop();
                }
                stopSound = !stopSound;
                sendMessage(channel, "Sound is now turned " + (stopSound ? "OFF" : "ON"));
                soundTimer = new Timer(soundTime);
                soundBackTimer = new Timer(0);//reset the backup sound timer*/
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
