package irc;

import gui.GUIMain;
import lib.chatbot.ChatterBot;
import lib.chatbot.ChatterBotSession;
import lib.chatbot.Cleverbot;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import sound.Sound;
import sound.SoundEngine;
import sound.SoundThread;
import util.*;

import java.rmi.ConnectException;
import java.util.Set;

public class IRCBot extends PircBot {

    public String getMasterChannel() {
        return masterChannel;
    }

    //Sounds
    public static boolean shouldTalk = true;
    public static boolean stopSound = false;
    public static String masterChannel;
    public ChatterBot chatBot;
    public ChatterBotSession session;
    public static Timer botnakTimer;

    public IRCBot(String user, String password) {
        if (GUIMain.currentSettings.bot == null) {
            GUIMain.currentSettings.bot = new Settings.Account(user, password);
        }
        setName(user);
        setLogin(user);
        if (GUIMain.viewer != null) masterChannel = GUIMain.viewer.getMaster();
        GUIMain.updateTitle();
        try {
            chatBot = Cleverbot.create();
            session = chatBot.createSession();
            connect("irc.twitch.tv", 6667, password);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        botnakTimer = new Timer(5000);
        if (GUIMain.loadedStreams()) {
            for (String s : GUIMain.channelSet) {
                doConnect(s);
            }
        }
        GUIMain.log("Loaded Bot: " + user + "!");
        GUIMain.bot = this;
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        if (!isConnected()) {
            try {
                connect("irc.twitch.tv", 6667, GUIMain.currentSettings.bot.getAccountPass());
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else {
            joinChannel(channelName);
            GUIMain.channelsToReplyIn.put(channelName, shouldTalk);
            if (!GUIMain.channelSet.contains(channel)) GUIMain.channelSet.add(channel);
        }
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     * @param forget  If true, will remove the channel from the channel list.
     */
    public void doLeave(String channel, boolean forget) {
        String channelName = "#" + channel;
        if (Utils.isInChannel(this, channelName)) {
            partChannel(channelName);
            GUIMain.channelsToReplyIn.remove(channelName);
        }
        if (forget) {
            if (GUIMain.channelSet.contains(channel)) {
                GUIMain.channelSet.remove(channel);
            }
        }
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget True if you are logging out, false if shutting down.
     */
    public void close(boolean forget) {
        GUIMain.log("Logging out of bot: " + GUIMain.currentSettings.bot.getAccountName());
        for (String s : getChannels()) {
            doLeave(s.substring(1), false);
        }
        disconnect();
        dispose();
        if (forget) {
            GUIMain.currentSettings.bot = null;
        }
        GUIMain.bot = null;
    }


    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (message != null && channel != null && sender != null && GUIMain.viewer != null) {
            sender = sender.toLowerCase();
            String low = message.toLowerCase();
            //un-shorten short URLs
            if (low.contains("bit.ly/") || low.contains("tinyurl.com/") || low.contains("goo.gl")) {
                String[] split = message.split(" ");
                for (String s : split) {
                    if (s.contains("http") && (s.contains("bit.ly/") || s.contains("tinyurl.com/") || s.contains("goo.gl"))) {
                        sendMessage(channel, Utils.getLongURL(s));
                    }
                }
            }
            //commands
            if (message.startsWith("!")) {
                String content = message.substring(1).split(" ")[0].toLowerCase();
                /*//dev
                if (sender.equals(GUIMain.viewer.getMaster())) {
                    handleDev(channel, message.substring(1));
                }*/
                //mod
                User u = Utils.getUser(this, channel, sender);
                if (u != null && (u.isOp() || u.isAdmin() || u.isStaff())
                        && !sender.equals(GUIMain.viewer.getMaster())) {
                    handleMod(channel, message.substring(1));
                }
                //sound
                if (soundTrigger(content, sender, channel)) {
                    SoundEngine.getEngine().addSound(new Sound(GUIMain.soundMap.get(content)));
                }
                //reply
                if (content.equals("ask")) {
                    if (shouldTalk) {
                        if (!botnakTimer.isRunning()) {
                            talkBack(channel, sender, message.substring(4));
                        }
                    }
                }
                ConsoleCommand consoleCommand = Utils.getConsoleCommand(content, this, channel, sender);
                if (consoleCommand != null) {
                    String mess = message.substring(1);
                    switch (consoleCommand.getAction()) {
                        case ADD_FACE:
                            Utils.handleFace(mess);
                            break;
                        case CHANGE_FACE:
                            Utils.handleFace(mess);
                            break;
                        case REMOVE_FACE:
                            String[] split = mess.split(" ");
                            String toremove = split[1];
                            if (GUIMain.faceMap.containsKey(toremove)) {
                                Utils.removeFace(toremove);
                            }
                            break;
                        case TOGGLE_FACE:
                            Utils.toggleFace(mess.split(" ")[1]);
                            break;
                        case ADD_SOUND:
                            Utils.handleSound(mess, false);
                            break;
                        case CHANGE_SOUND:
                            Utils.handleSound(mess, true);
                            break;
                        case REMOVE_SOUND:
                            String remove = mess.split(" ")[1];
                            if (GUIMain.soundMap.containsKey(remove)) {
                                GUIMain.soundMap.remove(remove);
                            }
                            break;
                        case SET_SOUND_DELAY:
                            int soundTime;
                            try {
                                soundTime = Integer.parseInt(mess.split(" ")[1]);
                            } catch (Exception e) {
                                return;
                            }
                            soundTime = Utils.handleInt(soundTime);
                            int delay = soundTime / 1000;
                            sendMessage(channel, "Sound delay " + (delay < 2 ? (delay == 0 ? "off." : "is now 1 second.") : ("is now " + delay + " seconds.")));
                            SoundEngine.getEngine().setDelay(soundTime);
                            break;
                        case TOGGLE_SOUND:
                            SoundEngine.getEngine().setShouldPlay(!SoundEngine.getEngine().shouldPlay());
                            sendMessage(channel, "Sound is now turned " + (SoundEngine.getEngine().shouldPlay() ? "ON" : "OFF"));
                            break;
                        case TOGGLE_REPLY:
                            if (mess.split(" ").length == 2) {//specific
                                String chan = mess.split(" ")[1];
                                if (!chan.contains("#")) chan = "#" + chan;
                                Set<String> set = GUIMain.channelsToReplyIn.keySet();
                                for (String str : set) {
                                    if (str.equalsIgnoreCase(chan)) {
                                        boolean current = GUIMain.channelsToReplyIn.get(str);
                                        GUIMain.channelsToReplyIn.put(str, !current);
                                        sendMessage(channel, "Replying is now " + (!current ? "on" : "off") + " for the channel " + str);
                                        break;
                                    }
                                }
                            } else if (mess.split(" ").length == 1) {//turn it on/off for all channels
                                shouldTalk = !shouldTalk;
                                Set<String> set = GUIMain.channelsToReplyIn.keySet();
                                for (String str1 : set) {
                                    GUIMain.channelsToReplyIn.put(str1, shouldTalk);
                                }
                                sendMessage(channel, "Replying is now " + (shouldTalk ? "ON." : "OFF."));
                            }
                            break;
                        case STOP_SOUND:
                            sendMessage(channel, "Stopping the first sound...");
                            SoundThread sound = SoundEngine.getEngine().getCurrentPlayingSound();
                            if (sound != null) {
                                sound.interrupt();
                            }
                            break;
                        case STOP_ALL_SOUNDS:
                            sendMessage(channel, "Stopping all currently playing sounds...");
                            SoundThread[] array = SoundEngine.getEngine().getCurrentPlayingSounds();
                            if (array != null && array.length > 0) {
                                for (SoundThread soun : array) {
                                    soun.interrupt();
                                }
                            }
                            array = null;
                            break;
                        case MOD_USER:
                            String[] splitBySpace = mess.split(" ");
                            String toMod = splitBySpace[1];
                            GUIMain.viewer.sendMessage(channel, ".mod " + toMod);
                            break;
                        case ADD_KEYWORD:
                            Utils.handleKeyword(mess);
                            break;
                        case REMOVE_KEYWORD:
                            Utils.handleKeyword(mess);
                            break;
                        case SET_USER_COL:
                            Utils.handleColor(sender, mess);
                            break;
                        case SET_COMMAND_PERMISSION:
                            Utils.setCommandPermission(mess);
                            break;
                        case ADD_TEXT_COMMAND:
                            Utils.addCommands(mess);
                            break;
                        case REMOVE_TEXT_COMMAND:
                            Utils.removeCommands(mess.split(" ")[1]);
                            break;
                    }
                }
                //text command
                Command c = Utils.getCommand(content);
                if (c != null) {
                    handleCommand(channel, c);
                }
            }
        }
    }

    public void talkBack(String channel, String sender, String message) {
        if (channel != null && sender != null && message != null && session != null && chatBot != null) {
            if (GUIMain.channelsToReplyIn.containsKey(channel)) {
                if (!botnakTimer.isRunning() && GUIMain.channelsToReplyIn.get(channel)) {
                    String reply = "";
                    if (GUIMain.viewer != null && sender.equals(GUIMain.viewer.getMaster())) sender = "Master";
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
        }
    }


    /**
     * Base trigger for sounds. Checks if a dev sound is not playing, if the general delay is up,
     * if the channel is yours, and if the user can even play the sound if it exists.
     *
     * @param s       Sound command trigger/name.
     * @param send    The sender of the command.
     * @param channel Channel the command was in.
     * @return true to play the sound, else false
     */
    public boolean soundTrigger(String s, String send, String channel) {
        if (SoundEngine.getEngine().shouldPlay()) {//sound not turned off
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
        int permission = Constants.PERMISSION_ALL;
        User u = Utils.getUser(this, channel, sender);
        if (u != null && (u.isOp() || u.isAdmin() || u.isStaff())) {
            permission = Constants.PERMISSION_MOD;
        }
        if (GUIMain.viewer != null && GUIMain.viewer.getMaster().equals(sender)) {
            permission = Constants.PERMISSION_DEV;
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

    public void handleCommand(String channel, Command c) {
        if (c.getMessage().data.length != 0) {
            if (!c.getDelayTimer().isRunning()) {
                for (String s : c.getMessage().data) {
                    sendMessage(channel, s);
                }
                c.getDelayTimer().reset();
            }
        }
    }

    /*public void handleDev(String channel, String s) {
        //TODO think of some more dev commands later, this is pretty bare now
        handleMod(channel, s);
    }*/

    public void handleMod(String channel, String s) {
        if (GUIMain.viewer == null) return;
        if (channel.substring(1).equals(GUIMain.viewer.getMaster())) {
            if (s.startsWith("soundstate")) {
                int delay = (int) SoundEngine.getEngine().getSoundTimer().period / 1000;
                String onOrOff = (stopSound ? "OFF" : "ON");
                int numSound = SoundEngine.getEngine().getCurrentPlayingSounds().length;
                String numSounds = (numSound > 0 ? (numSound == 1 ? "one sound" : (numSound + " sounds")) : "no sounds") + " currently playing";
                String delayS = (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds."));
                sendMessage(channel, "Sound is currently turned " + onOrOff + " with " + numSounds + " with " + delayS);
            }
        }
    }
}
