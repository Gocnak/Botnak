package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import sound.Sound;
import sound.SoundEngine;
import sound.SoundEntry;
import util.Command;
import util.ConsoleCommand;
import util.Constants;
import util.Utils;
import util.misc.Raffle;
import util.misc.Vote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class IRCBot extends MessageHandler {

    public PircBot getBot() {
        return GUIMain.currentSettings.accountManager.getBot();
    }

    public ArrayList<String> winners;
    public ArrayList<Raffle> raffles;

    private static Vote poll;
    private long lastAd;

    public IRCBot() {
        raffles = new ArrayList<>();
        winners = new ArrayList<>();
        poll = null;
        lastAd = -1;
    }

    @Override
    public void onConnect() {
        //TODO do people want it to follow?
        if (GUIMain.currentSettings.accountManager.getUserAccount() != null)
            doConnect(GUIMain.currentSettings.accountManager.getUserAccount().getName());
        GUIMain.updateTitle(null);
    }

    public void doConnect(String channel) {
        String channelName = "#" + channel;
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getBot(), Task.Type.JOIN_CHANNEL, channelName));
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     */
    public void doLeave(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getBot(), Task.Type.LEAVE_CHANNEL, channel));
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget True if you are logging out, false if shutting down.
     */
    public void close(boolean forget) {
        GUIMain.log("Logging out of bot: " + GUIMain.currentSettings.accountManager.getBotAccount().getName());
        GUIMain.currentSettings.accountManager.addTask(
                new Task(GUIMain.currentSettings.accountManager.getBot(), Task.Type.DISCONNECT, null));
        if (forget) {
            GUIMain.currentSettings.accountManager.setBotAccount(null);
        }
        GUIMain.bot = null;
    }

    public void onDisconnect() {
        //TODO create and run the reconnect listener thread if (!GUIMain.shutdown)
    }

    @Override
    public void onMessage(String channel, String sender, String message) {
        if (message != null && channel != null && sender != null && GUIMain.currentSettings.accountManager.getViewer() != null) {
            sender = sender.toLowerCase();
            if (sender.equalsIgnoreCase(getBot().getNick())) return;

            //raffles
            User u = GUIMain.currentSettings.channelManager.getUser(sender, true);
            if (!raffles.isEmpty()) {
                if (!winners.contains(u.getNick().toLowerCase())) {
                    for (Raffle r : raffles) {
                        if (r.isDone()) {
                            continue;
                        }
                        String key = r.getKeyword();
                        if (message.contains(key)) {
                            int permBase = r.getPermission();
                            int permission = Constants.PERMISSION_ALL;
                            if (u.isSubscriber(channel)) {
                                permission = Constants.PERMISSION_SUB;
                            }
                            if (u.isDonator()) {
                                if (u.getDonated() >= 2.50) {
                                    permission = Constants.PERMISSION_DONOR;
                                }
                            }
                            if ((u.isOp(channel) || u.isAdmin() || u.isStaff())) {
                                permission = Constants.PERMISSION_MOD;
                            }
                            if (GUIMain.currentSettings.accountManager.getUserAccount() != null &&
                                    GUIMain.currentSettings.accountManager.getUserAccount().getName().equalsIgnoreCase(sender)) {
                                permission = Constants.PERMISSION_DEV;
                            }
                            if (permission >= permBase) {
                                r.addUser(u.getNick());
                            }
                        }
                    }
                }
                ArrayList<Raffle> toRemove = new ArrayList<>();
                raffles.stream().filter(Raffle::isDone).forEach(r -> {
                    winners.add(r.getWinner());
                    toRemove.add(r);
                });
                if (!toRemove.isEmpty()) {
                    toRemove.forEach(raffles::remove);
                    toRemove.clear();
                }
            }

            Oauth key = GUIMain.currentSettings.accountManager.getUserAccount().getKey();
            String[] split = message.split(" ");
            String first = "";
            if (split.length > 1) first = split[1];
            //commands
            if (message.startsWith("!")) {
                String trigger = message.substring(1).split(" ")[0].toLowerCase();
                /*//dev
                if (sender.equals(GUIMain.viewer.getMaster())) {
                    handleDev(channel, message.substring(1));
                }*/
                //mod
                if (u.isOp(channel) || u.isAdmin() || u.isStaff()) {
                    handleMod(channel, message.substring(1));
                }
                //sound
                if (soundTrigger(trigger, sender, channel)) {
                    SoundEngine.getEngine().addSound(new Sound(GUIMain.soundMap.get(trigger)));
                }
                ConsoleCommand consoleCommand = Utils.getConsoleCommand(trigger, channel, u);
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
                            if (GUIMain.faceMap.containsKey(first)) {
                                Utils.removeFace(first);
                            }
                            break;
                        case TOGGLE_FACE:
                            Utils.toggleFace(first);
                            break;
                        case ADD_SOUND:
                            Utils.handleSound(mess, false);
                            break;
                        case CHANGE_SOUND:
                            Utils.handleSound(mess, true);
                            break;
                        case REMOVE_SOUND:
                            if (GUIMain.soundMap.containsKey(first)) {
                                GUIMain.soundMap.remove(first);
                            }
                            break;
                        case SET_SOUND_DELAY:
                            int soundTime;
                            try {
                                soundTime = Integer.parseInt(first);
                            } catch (Exception e) {
                                return;
                            }
                            if (soundTime < 0) return;
                            soundTime = Utils.handleInt(soundTime);
                            int delay = soundTime / 1000;
                            getBot().sendMessage(channel, "Sound delay " + (delay < 2 ? (delay == 0 ? "off." : "is now 1 second.") : ("is now " + delay + " seconds.")));
                            SoundEngine.getEngine().setDelay(soundTime);
                            break;
                        case TOGGLE_SOUND:
                            if (split.length > 1) {
                                if (GUIMain.soundMap.containsKey(first)) {
                                    Sound sound = GUIMain.soundMap.get(first);
                                    sound.setEnabled(!sound.isEnabled());
                                    getBot().sendMessage(channel, "The sound " + first + " is now turned " + (sound.isEnabled() ? "ON" : "OFF"));
                                }
                            } else {
                                SoundEngine.getEngine().setShouldPlay(!SoundEngine.getEngine().shouldPlay());
                                getBot().sendMessage(channel, "Sound is now turned " + (SoundEngine.getEngine().shouldPlay() ? "ON" : "OFF"));
                            }
                            break;
                        case STOP_SOUND:
                            SoundEntry sound = SoundEngine.getEngine().getCurrentPlayingSound();
                            if (sound != null) {
                                getBot().sendMessage(channel, "Stopping the first sound...");
                                sound.close();
                            }
                            break;
                        case STOP_ALL_SOUNDS:
                            Collection<SoundEntry> coll = SoundEngine.getEngine().getCurrentPlayingSounds();
                            if (coll.size() > 0) {
                                getBot().sendMessage(channel, "Stopping all currently playing sounds...");
                                for (SoundEntry soun : coll) {
                                    soun.close();
                                }
                            }
                            break;
                        case MOD_USER:
                            GUIMain.currentSettings.accountManager.getViewer().sendMessage(channel, ".mod " + first);
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
                            GUIMain.currentSettings.saveConCommands();
                            break;
                        case ADD_TEXT_COMMAND:
                            Utils.addCommands(mess);
                            GUIMain.currentSettings.saveCommands();
                            break;
                        case REMOVE_TEXT_COMMAND:
                            Utils.removeCommands(first);
                            GUIMain.currentSettings.saveCommands();
                            break;
                        case ADD_DONATION:
                            double amount;
                            try {
                                amount = Double.parseDouble(split[2]);
                            } catch (Exception ignored) {
                                return;
                            }
                            Donator don = Utils.getDonator(first);
                            if (don == null) {
                                don = new Donator(first, amount);
                                GUIMain.donators.add(don);
                            } else {
                                don.addDonated(amount);
                            }
                            GUIMain.currentSettings.saveDonators();
                            break;
                        case SET_SUB_SOUND:
                            if (GUIMain.currentSettings.loadSubSounds()) {
                                getBot().sendMessage(channel, "Reloaded sub sounds!");
                            }
                            break;
                        case SET_SOUND_PERMISSION:
                            try {
                                int perm;
                                perm = Integer.parseInt(first);
                                if (perm > -1 && perm < 5) {
                                    SoundEngine.getEngine().setPermission(perm);
                                }
                            } catch (Exception ignored) {
                            }
                            break;
                        case SET_NAME_FACE:
                            if (first.startsWith("http")) {
                                Utils.downloadFace(first, GUIMain.currentSettings.nameFaceDir.getAbsolutePath(),
                                        Utils.setExtension(sender, ".png"), sender, 2);
                                GUIMain.currentSettings.saveNameFaces();
                            }
                            break;
                        case REMOVE_NAME_FACE:
                            if (GUIMain.nameFaceMap.containsKey(sender)) {
                                GUIMain.nameFaceMap.remove(sender);
                                GUIMain.currentSettings.saveNameFaces();
                            }
                            break;
                        case SET_STREAM_TITLE:
                            if (key != null) {
                                if (key.canSetTitle()) {
                                    String title = message.substring(message.indexOf(" ") + 1);
                                    if (Utils.setTitleOfStream(key.getKey(), channel, title)) {
                                        getBot().sendMessage(channel, "Title successfully updated to: " + title);
                                    } else {
                                        getBot().sendMessage(channel, "Error setting the title of the stream!");
                                    }
                                }
                            }
                            break;
                        case SEE_STREAM_TITLE:
                            String title = Utils.getTitleOfStream(channel);
                            if (!"".equals(title)) {
                                getBot().sendMessage(channel, "The title of the stream is: " + title);
                            }
                            break;
                        case SEE_STREAM_GAME:
                            String game = Utils.getGameOfStream(channel);
                            if ("".equals(game)) {
                                getBot().sendMessage(channel, "The streamer is currently not playing a game!");
                            } else {
                                getBot().sendMessage(channel, "The current game is: " + game);
                            }
                            break;
                        case SET_STREAM_GAME:
                            if (key != null) {
                                if (key.canSetTitle()) {
                                    String newGame = message.substring(message.indexOf(" ") + 1);
                                    if (Utils.setGameOfStream(key.getKey(), channel, newGame)) {
                                        getBot().sendMessage(channel, "The game has been set to: " + newGame);
                                    } else {
                                        getBot().sendMessage(channel, "Error in setting the game of the stream!");
                                    }
                                }
                            }
                            break;
                        case PLAY_ADVERT:
                            if (key != null) {
                                if (key.canPlayAd()) {
                                    try {
                                        int length = Utils.getTime(first);
                                        if (Utils.playAdvert(key.getKey(), channel, length)) {
                                            getBot().sendMessage(channel, "Playing an ad for " + length + " seconds!");
                                            lastAd = System.currentTimeMillis();
                                        } else {
                                            getBot().sendMessage(channel, "Error playing an ad!");
                                            if (lastAd > 0 && ((System.currentTimeMillis() - lastAd) < 480000)) {
                                                SimpleDateFormat sdf = new SimpleDateFormat("m:ss");
                                                Date d = new Date(System.currentTimeMillis() - lastAd);
                                                getBot().sendMessage(channel, "Last ad was was only " + sdf.format(d) + " ago!");
                                            }
                                        }
                                    } catch (Exception e) {
                                        GUIMain.log(e.getMessage());
                                    }
                                }
                            }
                            break;
                        case START_RAFFLE:
                            try {
                                String timeString = split[2];
                                try {
                                    int time = Utils.getTime(timeString);
                                    if (time < 1) break;
                                    int perm = 0;
                                    //TODO select a parameter in Settings GUI that defines the default raffle
                                    if (split.length == 4) {
                                        //because right now it just "Everyone" unless specified with the int param
                                        perm = Integer.parseInt(split[3]);
                                    }

                                    Raffle r = new Raffle(getBot(), first, time, channel, perm);
                                    r.start();
                                    raffles.add(r);
                                    //print the blarb
                                    getBot().sendMessage(channel, r.getStartMessage());
                                    getBot().sendMessage(channel, "NOTE: This is a promotion from " + channel.substring(1) +
                                            ". Twitch does not sponsor or endorse broadcaster promotions and is not responsible for them.");
                                } catch (Exception e) {
                                    break;
                                }
                            } catch (Exception e) {
                                GUIMain.log(e.getMessage());
                            }

                            break;
                        case ADD_RAFFLE_WINNER:
                            if (!winners.contains(first)) {
                                winners.add(first);
                                getBot().sendMessage(channel, "The user " + first + " has been added to the winners pool!");
                            } else {
                                getBot().sendMessage(channel, "The user " + first + " is already in the winners pool!");
                            }
                            break;
                        case STOP_RAFFLE:
                            Raffle toRemove = null;
                            for (Raffle r : raffles) {
                                if (r.getKeyword().equalsIgnoreCase(first)) {
                                    r.setDone(true);
                                    r.interrupt();
                                    toRemove = r;
                                    getBot().sendMessage(channel, "The raffle with key " + first + " has been stopped!");
                                }
                            }
                            if (toRemove != null) {
                                raffles.remove(toRemove);
                            }
                            break;
                        case REMOVE_RAFFLE_WINNER:
                            if (winners.contains(first)) {
                                if (winners.remove(first)) {
                                    getBot().sendMessage(channel, "The user " + first + " was removed from the winners pool!");
                                }
                            } else {
                                getBot().sendMessage(channel, "The user " + first + " is not in the winners pool!");
                            }
                            break;
                        case SEE_WINNERS:
                            if (!winners.isEmpty()) {
                                StringBuilder stanSB = new StringBuilder();
                                stanSB.append("The current raffle winners are: ");
                                for (String name : winners) {
                                    stanSB.append(name);
                                    stanSB.append(", ");
                                }
                                getBot().sendMessage(channel, stanSB.toString().substring(0, stanSB.length() - 2) + " .");
                            } else {
                                getBot().sendMessage(channel, "There are no recorded winners!");
                            }
                            break;
                        case START_POLL:
                            if (poll != null) {
                                if (poll.isDone()) {
                                    createPoll(channel, message);
                                } else {
                                    getBot().sendMessage(channel, "Cannot start a poll with one currently running!");
                                }
                            } else {
                                createPoll(channel, message);
                            }
                            break;
                        case POLL_RESULT:
                            if (poll != null) {
                                poll.printResults();
                            } else {
                                getBot().sendMessage(channel, "There never was a poll!");
                            }
                            break;
                        case CANCEL_POLL:
                            if (poll != null) {
                                if (poll.isDone()) {
                                    getBot().sendMessage(channel, "The poll is already finished!");
                                } else {
                                    poll.interrupt();
                                    getBot().sendMessage(channel, "The poll has been stopped.");
                                }
                            } else {
                                getBot().sendMessage(channel, "There is no current poll!");
                            }
                            break;
                        case VOTE_POLL:
                            if (poll != null) {
                                if (!poll.isDone()) {
                                    int option;
                                    try {
                                        option = Integer.parseInt(first);
                                    } catch (Exception e) {
                                        break;
                                    }
                                    poll.addVote(sender, option);
                                }
                            }
                            break;
                        case NOW_PLAYING:
                            String song = Utils.getCurrentlyPlaying();
                            if ("".equals(song)) {
                                getBot().sendMessage(channel, "There is no currently playing song!");
                            } else {
                                getBot().sendMessage(channel, "The current song is: " + song);
                            }
                            break;
                        default:
                            break;

                    }
                }
                //text command
                Command c = Utils.getCommand(trigger);
                if (c != null) {
                    //TODO add a check to see if people want it only to react in their channel
                    handleCommand(channel, c);
                }
            }
        }
    }

    //!startpoll time options
    public void createPoll(String channel, String message) {
        if (message.contains("]")) {//because what's the point of a poll with one option?
            int first = message.indexOf(" ") + 1;
            int second = message.indexOf(" ", first) + 1;
            String[] split = message.split(" ");
            int time = Utils.getTime(split[1]);
            if (time > 0) {
                poll = new Vote(channel, time, message.substring(second).split("\\]"));
                poll.start();
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
            if (channel.equalsIgnoreCase("#" + GUIMain.currentSettings.accountManager.getUserAccount().getName())) {//is in main channel
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
        User u = GUIMain.currentSettings.channelManager.getUser(sender, true);
        if (u.isSubscriber(channel)) {
            permission = Constants.PERMISSION_SUB;
        }
        if (u.isDonator()) {
            if (u.getDonated() >= 2.50) {
                permission = Constants.PERMISSION_DONOR;
            }
        }
        if (u.isOp(channel) || u.isAdmin() || u.isStaff()) {
            permission = Constants.PERMISSION_MOD;
        }
        if (GUIMain.currentSettings.accountManager.getUserAccount() != null &&
                GUIMain.currentSettings.accountManager.getUserAccount().getName().equalsIgnoreCase(sender)) {
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

    public void handleCommand(String channel, Command c) {
        if (c.getMessage().data.length != 0) {
            if (!c.getDelayTimer().isRunning()) {
                for (String s : c.getMessage().data) {
                    getBot().sendMessage(channel, s);
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
        if (GUIMain.currentSettings.accountManager.getUserAccount() == null) return;
        if (channel.substring(1).equalsIgnoreCase(GUIMain.currentSettings.accountManager.getUserAccount().getName())) {
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
                    getBot().sendMessage(channel, "Sound is currently turned " + onOrOff + " with " + numSounds + " with " + delayS + " " + perm);
                } else if (split.length == 2) {
                    String soundName = split[1];
                    if (GUIMain.soundMap.containsKey(soundName)) {
                        Sound toCheck = GUIMain.soundMap.get(soundName);
                        getBot().sendMessage(channel, "The sound " + soundName + " is currently turned " + (toCheck.isEnabled() ? "ON" : "OFF"));
                    }
                }
            }
        }
    }
}
