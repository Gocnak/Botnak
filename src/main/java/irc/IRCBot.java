package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import sound.Sound;
import sound.SoundEngine;
import sound.SoundEntry;
import util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class IRCBot extends MessageHandler {

    public String getMasterChannel() {
        return masterChannel;
    }

    //Sounds
    public static boolean shouldTalk = true;
    public static String masterChannel;
    private PircBot bot;

    public PircBot getBot() {
        return bot;
    }

    public IRCBot(String user, String password) {
        if (GUIMain.currentSettings.bot == null) {
            GUIMain.currentSettings.bot = new Settings.Account(user, password);
        }
        bot = new PircBot(this);
        bot.setNick(user);
        if (GUIMain.viewer != null) masterChannel = GUIMain.viewer.getMaster();
        GUIMain.updateTitle(null);
        try {
            bot.connect("irc.twitch.tv", 6667, password);
        } catch (Exception e) {
            GUIMain.log(e.getMessage());
        }
        bot.sendRawLineViaQueue("TWITCHCLIENT 3");
        bot.setMessageDelay(3000);
        if (GUIMain.loadedStreams()) {
            for (String s : GUIMain.channelSet) {
                doConnect(s);
            }
        }
        GUIMain.log("Loaded Bot: " + user + "!");
        GUIMain.bot = this;
    }

    public void doConnect(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        if (!bot.isConnected()) {
            try {
                bot.connect("irc.twitch.tv", 6667, GUIMain.currentSettings.bot.getAccountPass());
            } catch (Exception e) {
                GUIMain.log(e.getMessage());
            }
        } else {
            if (!bot.isInChannel(channel)) {
                bot.joinChannel(channel);
                if (!GUIMain.channelSet.contains(channel.substring(1))) GUIMain.channelSet.add(channel.substring(1));
            }
        }
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     */
    public void doLeave(String channel) {
        String channelName = (channel.startsWith("#") ? channel : "#" + channel);
        if (bot.isInChannel(channelName)) {
            bot.partChannel(channelName);
        }
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget True if you are logging out, false if shutting down.
     */
    public void close(boolean forget) {
        GUIMain.log("Logging out of bot: " + GUIMain.currentSettings.bot.getAccountName());
        for (String s : bot.getChannels()) {
            doLeave(s.substring(1));
        }
        bot.disconnect();
        bot.dispose();
        if (forget) {
            GUIMain.currentSettings.bot = null;
        }
        bot = null;
        GUIMain.bot = null;
    }

    public void onDisconnect() {
        //TODO create and run the reconnect listener thread if (!GUIMain.shutdown)
    }

    @Override
    public void onMessage(String channel, String sender, String message) {
        if (message != null && channel != null && sender != null && GUIMain.viewer != null) {
            sender = sender.toLowerCase();
            if (sender.equalsIgnoreCase(getBot().getNick())) return;
            //commands
            if (message.startsWith("!")) {
                String content = message.substring(1).split(" ")[0].toLowerCase();
                /*//dev
                if (sender.equals(GUIMain.viewer.getMaster())) {
                    handleDev(channel, message.substring(1));
                }*/
                //mod
                User u = bot.getUser(channel, sender);
                if (u != null && (u.isOp() || u.isAdmin() || u.isStaff())) {
                    handleMod(channel, message.substring(1));
                }
                //sound
                if (soundTrigger(content, sender, channel)) {
                    SoundEngine.getEngine().addSound(new Sound(GUIMain.soundMap.get(content)));
                }
                ConsoleCommand consoleCommand = Utils.getConsoleCommand(content, channel, sender);
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
                            bot.sendMessage(channel, "Sound delay " + (delay < 2 ? (delay == 0 ? "off." : "is now 1 second.") : ("is now " + delay + " seconds.")));
                            SoundEngine.getEngine().setDelay(soundTime);
                            break;
                        case TOGGLE_SOUND:
                            String[] check = mess.split(" ");
                            if (check.length > 1) {
                                String soundName = check[1];
                                if (GUIMain.soundMap.containsKey(soundName)) {
                                    Sound sound = GUIMain.soundMap.get(soundName);
                                    sound.setEnabled(!sound.isEnabled());
                                    bot.sendMessage(channel, "The sound " + soundName + " is now turned " + (sound.isEnabled() ? "ON" : "OFF"));
                                }
                            } else {
                                SoundEngine.getEngine().setShouldPlay(!SoundEngine.getEngine().shouldPlay());
                                bot.sendMessage(channel, "Sound is now turned " + (SoundEngine.getEngine().shouldPlay() ? "ON" : "OFF"));
                            }
                            break;
                        case STOP_SOUND:
                            SoundEntry sound = SoundEngine.getEngine().getCurrentPlayingSound();
                            if (sound != null) {
                                bot.sendMessage(channel, "Stopping the first sound...");
                                sound.close();
                            }
                            break;
                        case STOP_ALL_SOUNDS:
                            Collection<SoundEntry> coll = SoundEngine.getEngine().getCurrentPlayingSounds();
                            if (coll.size() > 0) {
                                bot.sendMessage(channel, "Stopping all currently playing sounds...");
                                for (SoundEntry soun : coll) {
                                    soun.close();
                                }
                            }
                            break;
                        case MOD_USER:
                            String[] splitBySpace = mess.split(" ");
                            String toMod = splitBySpace[1];
                            GUIMain.viewer.getViewer().sendMessage(channel, ".mod " + toMod);
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
                        case ADD_DONATION:
                            double amount;
                            try {
                                amount = Double.parseDouble(mess.split(" ")[2]);
                            } catch (Exception ignored) {
                                return;
                            }
                            String donator = mess.split(" ")[1];
                            Donator don = Utils.getDonator(donator);
                            if (don == null) {
                                don = new Donator(donator, amount);
                                GUIMain.donators.add(don);
                            } else {
                                don.addDonated(amount);
                            }
                            GUIMain.currentSettings.saveDonators();
                            break;
                        case SET_SUB_SOUND:
                            String toParse = mess.split(" ")[1];
                            String[] toRead = toParse.split(",");
                            ArrayList<String> ar = new ArrayList<>();
                            for (String s : toRead) {
                                String filename = GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(s, ".wav");
                                if (Utils.areFilesGood(filename)) {
                                    ar.add(filename);
                                }
                            }
                            if (!ar.isEmpty()) {
                                GUIMain.currentSettings.subSound = new Sound(15, ar.toArray(new String[ar.size()]));
                                bot.sendMessage(channel, "Sub Sound Set!");
                            }
                            break;
                        case SET_SOUND_PERMISSION:
                            try {
                                int perm;
                                perm = Integer.parseInt(mess.split(" ")[1]);
                                if (perm > -1 && perm < 5) {
                                    SoundEngine.getEngine().setPermission(perm);
                                }
                            } catch (Exception ignored) {
                            }
                            break;
                        case SET_NAME_FACE:
                            String URL = mess.split(" ")[1];
                            if (URL.startsWith("http")) {
                                Utils.downloadFace(URL, GUIMain.currentSettings.nameFaceDir.getAbsolutePath(),
                                        Utils.setExtension(sender, ".png"), sender, 2);
                            }
                            break;
                        case REMOVE_NAME_FACE:
                            if (GUIMain.nameFaceMap.containsKey(sender)) {
                                GUIMain.nameFaceMap.remove(sender);
                            }
                            break;
                        case SET_STREAM_TITLE:


                            break;
                        case SET_STREAM_GAME:


                            break;
                        case PLAY_ADVERT:

                            break;
                    }
                }
                //text command
                Command c = Utils.getCommand(content);
                if (c != null) {
                    //TODO add a check to see if people want it only to react in their channel
                    handleCommand(channel, c);
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
        User u = bot.getUser(channel, sender);
        if (u != null && u.isSubscriber()) {
            permission = Constants.PERMISSION_SUB;
        }
        Donator d = Utils.getDonator(sender);
        if (d != null) {
            if (d.getDonated() >= 5.00) {
                permission = Constants.PERMISSION_DONOR;
            }
        }
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
                if (snd != null && snd.isEnabled()) {
                    int perm = snd.getPermission();
                    //check permission
                    if (permission >= perm && permission >= SoundEngine.getEngine().getPermission()) {
                        //descending permission, this should work; devs can play mod and all sounds, etc.
                        //this also checks to see if they can even play the sound given the certain circumstance.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //TODO overhaul this shit
    public void handleCommand(String channel, Command c) {
        if (c.getMessage().data.length != 0) {
            if (!c.getDelayTimer().isRunning()) {
                for (String s : c.getMessage().data) {
                    bot.sendMessage(channel, s);
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
                String[] split = s.split(" ");
                if (split.length == 1) {
                    int delay = (int) SoundEngine.getEngine().getSoundTimer().period / 1000;
                    String onOrOff = (SoundEngine.getEngine().shouldPlay() ? "ON" : "OFF");
                    int numSound = SoundEngine.getEngine().getCurrentPlayingSounds().size();
                    int permission = SoundEngine.getEngine().getPermission();
                    String numSounds = (numSound > 0 ? (numSound == 1 ? "one sound" : (numSound + " sounds")) : "no sounds") + " currently playing";
                    String delayS = (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds."));
                    String perm = (permission > 0 ? (permission > 1 ? (permission > 2 ? (permission > 3 ?
                            "Only the Broadcaster" :
                            "Only Mods and the Broadcaster") :
                            "Donators, Mods, and the Broadcaster") :
                            "Subscribers, Donators, Mods, and the Broadcaster") :
                            "Everyone")
                            + " can play sounds.";
                    bot.sendMessage(channel, "Sound is currently turned " + onOrOff + " with " + numSounds + " with " + delayS + " " + perm);
                } else if (split.length == 2) {
                    String soundName = split[1];
                    if (GUIMain.soundMap.containsKey(soundName)) {
                        Sound toCheck = GUIMain.soundMap.get(soundName);
                        bot.sendMessage(channel, "The sound " + soundName + " is currently turned " + (toCheck.isEnabled() ? "ON" : "OFF"));
                    }
                }
            }
        }
    }
}
