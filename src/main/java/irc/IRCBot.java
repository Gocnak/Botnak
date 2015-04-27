package irc;

import face.FaceManager;
import gui.GUIMain;
import irc.account.Oauth;
import irc.account.Task;
import irc.message.MessageHandler;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.User;
import sound.Sound;
import sound.SoundEngine;
import util.Response;
import util.StringArray;
import util.Utils;
import util.comm.Command;
import util.comm.ConsoleCommand;
import util.misc.Raffle;
import util.misc.Vote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

            boolean senderIsBot = sender.equalsIgnoreCase(getBot().getNick());
            boolean userIsBot = GUIMain.currentSettings.accountManager.getUserAccount().getName()
                    .equals(GUIMain.currentSettings.accountManager.getBotAccount().getName());
            //if the sender of the message is the bot, but
            //the user account is NOT the bot, just return, we don't want the bot to trigger anything
            if (senderIsBot && !userIsBot) return;

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
                            int permission = Utils.getUserPermission(u, channel);
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
                String mess = message.substring(1);
                //sound
                if (SoundEngine.getEngine().soundTrigger(trigger, sender, channel)) {
                    SoundEngine.getEngine().playSound(new Sound(SoundEngine.getEngine().getSoundMap().get(trigger)));
                }
                ConsoleCommand consoleCommand = Utils.getConsoleCommand(trigger, channel, u);
                if (consoleCommand != null) {
                    Response commandResponse = null;
                    switch (consoleCommand.getAction()) {
                        case ADD_FACE:
                            commandResponse = FaceManager.handleFace(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveFaces();
                            break;
                        case CHANGE_FACE:
                            commandResponse = FaceManager.handleFace(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveFaces();
                            break;
                        case REMOVE_FACE:
                            commandResponse = FaceManager.removeFace(first);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveFaces();
                            break;
                        case TOGGLE_FACE:
                            commandResponse = FaceManager.toggleFace(first);
                            break;
                        case ADD_SOUND:
                            commandResponse = SoundEngine.getEngine().handleSound(mess, false);
                            break;
                        case CHANGE_SOUND:
                            commandResponse = SoundEngine.getEngine().handleSound(mess, true);
                            break;
                        case REMOVE_SOUND:
                            commandResponse = SoundEngine.getEngine().removeSound(first);
                            break;
                        case SET_SOUND_DELAY:
                            commandResponse = SoundEngine.getEngine().setSoundDelay(first);
                            break;
                        case TOGGLE_SOUND:
                            boolean individualSound = split.length > 1;
                            commandResponse = SoundEngine.getEngine().toggleSound(individualSound ? first : null, individualSound);
                            break;
                        case STOP_SOUND:
                            commandResponse = SoundEngine.getEngine().stopSound(false);
                            break;
                        case STOP_ALL_SOUNDS:
                            commandResponse = SoundEngine.getEngine().stopSound(true);
                            break;
                        case SEE_SOUND_STATE:
                            commandResponse = SoundEngine.getEngine().getSoundState(first);
                            break;
                        case ADD_KEYWORD:
                            commandResponse = Utils.handleKeyword(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveKeywords();
                            break;
                        case REMOVE_KEYWORD:
                            commandResponse = Utils.handleKeyword(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveKeywords();
                            break;
                        case SET_USER_COL:
                            commandResponse = Utils.handleColor(sender, mess, u.getColor());
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveUserColors();
                            break;
                        case SET_COMMAND_PERMISSION:
                            commandResponse = Utils.setCommandPermission(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveConCommands();
                            break;
                        case ADD_TEXT_COMMAND:
                            commandResponse = Utils.addCommands(mess);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveCommands();
                            break;
                        case REMOVE_TEXT_COMMAND:
                            commandResponse = Utils.removeCommands(first);
                            if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveCommands();
                            break;
                        case ADD_DONATION:
                            commandResponse = GUIMain.currentSettings.donationManager.parseDonation(split);
                            break;
                        case SET_SUB_SOUND:
                            if (GUIMain.currentSettings.loadSubSounds()) {
                                getBot().sendMessage(channel, "Reloaded sub sounds!");
                            }
                            break;
                        case SET_SOUND_PERMISSION:
                            commandResponse = SoundEngine.getEngine().setSoundPermission(first);
                            break;
                        case SET_NAME_FACE:
                            if (first.startsWith("http")) {
                                commandResponse = FaceManager.downloadFace(first,
                                        GUIMain.currentSettings.nameFaceDir.getAbsolutePath(),
                                        Utils.setExtension(sender, ".png"), sender, FaceManager.FACE_TYPE.NAME_FACE);
                                if (commandResponse.isSuccessful()) GUIMain.currentSettings.saveNameFaces();
                            }
                            break;
                        case REMOVE_NAME_FACE:
                            if (FaceManager.nameFaceMap.containsKey(sender)) {
                                FaceManager.nameFaceMap.remove(sender);
                                GUIMain.currentSettings.saveNameFaces();
                                getBot().sendMessage(channel, "Removed face for user: " + sender + " !");
                            } else {
                                getBot().sendMessage(channel, "The user " + sender + " has no name face!");
                            }
                            break;
                        case SET_STREAM_TITLE:
                            commandResponse = Utils.setStreamStatus(key, channel, message, true);
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
                            commandResponse = Utils.setStreamStatus(key, channel, message, false);
                            break;
                        case PLAY_ADVERT:
                            if (key != null) {
                                if (key.canPlayAd()) {
                                    int length = Utils.getTime(first);
                                    if (length == -1) length = 30;
                                    if (Utils.playAdvert(key.getKey(), channel, length)) {
                                        getBot().sendMessage(channel, "Playing an ad for " + length + " seconds!");
                                        lastAd = System.currentTimeMillis();
                                    } else {
                                        getBot().sendMessage(channel, "Error playing an ad!");
                                        long diff = System.currentTimeMillis() - lastAd;
                                        if (lastAd > 0 && (diff < 480000)) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("m:ss");
                                            Date d = new Date(diff);
                                            Date toPlay = new Date(480000 - diff);
                                            getBot().sendMessage(channel, "Last ad was was only " + sdf.format(d)
                                                    + " ago! You must wait " + sdf.format(toPlay) + " to play another ad!");
                                        }
                                    }
                                } else {
                                    getBot().sendMessage(channel, "This OAuth key cannot play an advertisement!");
                                }
                            }
                            break;
                        case START_RAFFLE:
                            if (split.length > 2) {
                                String timeString = split[2];
                                int time = Utils.getTime(timeString);
                                if (time < 1) {
                                    getBot().sendMessage(channel, "Failed to start raffle, usage: !startraffle (name) (time) (permission?)");
                                    break;
                                }
                                int perm = 0;//TODO select a parameter in Settings GUI that defines the default raffle
                                if (split.length == 4) {
                                    //because right now it's just "Everyone" unless specified with the int param
                                    try {
                                        perm = Integer.parseInt(split[3]);
                                    } catch (Exception ignored) {//default to the specified value
                                    }
                                }
                                Raffle r = new Raffle(getBot(), first, time, channel, perm);
                                r.start();
                                raffles.add(r);
                                //print the blarb
                                getBot().sendMessage(channel, r.getStartMessage());
                                getBot().sendMessage(channel, "NOTE: This is a promotion from " + channel.substring(1) +
                                        ". Twitch does not sponsor or endorse broadcaster promotions and is not responsible for them.");
                            } else {
                                getBot().sendMessage(channel, "Failed to start raffle, usage: !startraffle (name) (time) (permission?)");
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
                                    break;
                                }
                            }
                            if (toRemove != null) {
                                raffles.remove(toRemove);
                            } else {
                                getBot().sendMessage(channel, "There is no such raffle \"" + first + "\" !");
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
                            commandResponse = Utils.getCurrentlyPlaying();
                            break;
                        case SHOW_UPTIME:
                            commandResponse = Utils.getUptimeString(channel.substring(1));
                            break;
                        default:
                            break;

                    }
                    if (commandResponse != null && !"".equals(commandResponse.getResponseText()))
                        getBot().sendMessage(channel, commandResponse.getResponseText());
                }
                //text command
                Command c = Utils.getCommand(trigger);
                //we check the senderIsBot here because we want to be able to call console commands,
                //but we don't want the bot to trigger its own text commands, which
                //could infinite loop (two commands calling each other over and over)
                if (c != null && !senderIsBot && c.getMessage().data.length > 0 && !c.getDelayTimer().isRunning()) {
                    //TODO add a check to see if people want it only to react in their channel
                    StringArray sa = c.getMessage();
                    if (c.hasArguments()) {
                        //build arguments if it has any
                        int argAmount = c.countArguments();
                        if ((split.length - 1) < argAmount) {
                            getBot().sendMessage(channel, "Missing command arguments! Command format: " + c.printCommand());
                            return;
                        }
                        String[] definedArguments = new String[argAmount];
                        System.arraycopy(split, 1, definedArguments, 0, argAmount);
                        sa = c.buildMessage(sa, definedArguments);
                    }
                    //send the message
                    for (String s : sa.data) {
                        getBot().sendMessage(channel, s);
                    }
                    c.getDelayTimer().reset();
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
}