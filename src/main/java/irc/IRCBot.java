package irc;

import face.Face;
import face.FaceManager;
import gui.forms.GUIMain;
import irc.account.OAuth;
import irc.account.Task;
import irc.message.Message;
import irc.message.MessageHandler;
import irc.message.MessageQueue;
import lib.pircbot.PircBot;
import lib.pircbot.User;
import sound.Sound;
import sound.SoundEngine;
import thread.ThreadEngine;
import util.APIRequests;
import util.Response;
import util.StringArray;
import util.Utils;
import util.comm.Command;
import util.comm.ConsoleCommand;
import util.misc.Raffle;
import util.misc.Vote;
import util.settings.Settings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class IRCBot extends MessageHandler {

    public PircBot getBot() {
        return Settings.accountManager.getBot();
    }

    public List<String> winners;
    public List<Raffle> raffles;

    private Vote poll;
    private long lastAd;

    public IRCBot() {
        raffles = new ArrayList<>();
        winners = new ArrayList<>();
        poll = null;
        lastAd = -1;
    }

    @Override
    public void onConnect() {
        GUIMain.channelSet.forEach(this::doConnect);
        GUIMain.updateTitle(null);
    }

    public void doConnect(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        Settings.accountManager.addTask(new Task(getBot(), Task.Type.JOIN_CHANNEL, channel));
    }

    /**
     * Leaves a channel and if specified, removes the channel from the
     * channel list.
     *
     * @param channel The channel name to leave (# not included).
     */
    public void doLeave(String channel) {
        if (!channel.startsWith("#")) channel = "#" + channel;
        Settings.accountManager.addTask(new Task(getBot(), Task.Type.LEAVE_CHANNEL, channel));
    }

    /**
     * Disconnects from all chats and disposes of the bot.
     *
     * @param forget True if you are logging out, false if shutting down.
     */
    public void close(boolean forget) {
        GUIMain.log("Logging out of bot: " + Settings.accountManager.getBotAccount().getName());
        Settings.accountManager.addTask(new Task(getBot(), Task.Type.DISCONNECT, null));
        if (forget) {
            Settings.accountManager.setBotAccount(null);
        }
        GUIMain.bot = null;
    }

    public void onDisconnect() {
        if (!GUIMain.shutDown && getBot() != null) {
            GUIMain.logCurrent("Detected a disconnection for the account: " + getBot().getNick());
            if (Settings.autoReconnectAccounts.getValue())
                Settings.accountManager.createReconnectThread(getBot().getConnection());
            else {
                GUIMain.logCurrent("Auto-reconnects disabled, please check Preferences -> Auto-Reconnect!");
            }
        }
    }

    @Override
    public void onJTVMessage(String channel, String line, Map<String, String> tags)
    {
        if (tags.containsValue("msg_banned") || tags.containsValue("msg_timedout"))
        {
            MessageQueue.addMessage(new Message().setChannel(channel).setType(Message.MessageType.JTV_NOTIFY)
                    .setContent(getBot().getNick() + " is " + line.substring(8)));
        }
    }

    @Override
    public void onMessage(String channel, long senderID, String message)
    {
        if (message != null && channel != null && Settings.accountManager.getViewer() != null)
        {
            String botnakUserName = Settings.accountManager.getUserAccount().getName();
            User senderUser = Settings.channelManager.getUser(senderID, false);
            if (senderUser == null)
            {
                GUIMain.logCurrent("THE SENDER FOR THE MESSAGE IS NULL!");
                return;
            }

            String sender = senderUser.getLowerNick();

            if (!channel.contains(botnakUserName.toLowerCase())) {//in other channels
                int replyType = Settings.botReplyType.getValue();
                if (replyType == 0) return;
                //0 = reply to nobody (just spectate), 1 = reply to just the Botnak user, 2 = reply to everyone
                if (replyType == 1 && !sender.equalsIgnoreCase(botnakUserName)) return;
            }

            boolean senderIsBot = sender.equalsIgnoreCase(getBot().getNick());
            boolean userIsBot = botnakUserName.equalsIgnoreCase(Settings.accountManager.getBotAccount().getName());
            //if the sender of the message is the bot, but
            //the user account is NOT the bot, just return, we don't want the bot to trigger anything
            if (senderIsBot && !userIsBot) return;

            //raffles
            if (!raffles.isEmpty()) {
                if (!winners.contains(senderUser.getNick().toLowerCase()))
                {
                    for (Raffle r : raffles) {
                        if (r.isDone()) {
                            continue;
                        }
                        String key = r.getKeyword();
                        if (message.contains(key)) {
                            r.addUser(senderUser, channel); // Handles filtering permissions
                        }
                    }
                }
                ArrayList<Raffle> toRemove = new ArrayList<>();
                raffles.stream().filter(Raffle::isDone).forEach(r -> {
                    winners.add(r.getWinner());
                    toRemove.add(r);
                });
                if (!toRemove.isEmpty()) {
                    raffles.removeAll(toRemove);
                    toRemove.clear();
                }
            }

            OAuth key = Settings.accountManager.getUserAccount().getOAuth();
            String[] split = message.split(" ");

            //URL Checking
            boolean ytVidDetail = Settings.botShowYTVideoDetails.getValue();
            boolean twitchVOD = Settings.botShowTwitchVODDetails.getValue();
            boolean unshortenURLs = Settings.botUnshortenURLs.getValue();
            if (ytVidDetail || twitchVOD || unshortenURLs) {
                ThreadEngine.submit(() -> {
                    int count = 0;
                    for (String part : split) {
                        if (count > 1) break;//only allowing 2 requests here; don't want spam
                        if (part.startsWith("http") || part.startsWith("www")) {
                            if (ytVidDetail && (part.contains("youtu.be") || part.contains("youtube.com/watch")
                                    || part.contains("youtube.com/v") || part.contains("youtube.com/embed/"))) {
                                getBot().sendMessage(channel, APIRequests.YouTube.getVideoData(part).getResponseText());
                                count++;
                            } else if (unshortenURLs && (part.contains("bit.ly") ||
                                    part.contains("tinyurl") || part.contains("goo.gl"))) {
                                getBot().sendMessage(channel, APIRequests.UnshortenIt.getUnshortened(part).getResponseText());
                                count++;
                            } else if (twitchVOD && part.contains("twitch.tv/videos/"))
                            {
                                getBot().sendMessage(channel, APIRequests.Twitch.getTitleOfVOD(part).getResponseText());
                                count++;
                            }
                        }
                    }
                });
            }
            String first = "";
            if (split.length > 1) first = split[1];
            //commands
            if (message.startsWith("!")) {
                String trigger = message.substring(1).split(" ")[0].toLowerCase();
                String mess = message.substring(1);
                //sound
                if (SoundEngine.getEngine().soundTrigger(trigger, senderUser, channel)) {
                    SoundEngine.getEngine().playSound(new Sound(SoundEngine.getEngine().getSoundMap().get(trigger)));
                }
                ConsoleCommand consoleCommand = Utils.getConsoleCommand(trigger, channel, senderUser);
                if (consoleCommand != null) {
                    Response commandResponse = null;
                    switch (consoleCommand.getAction()) {
                        case ADD_FACE:
                        case CHANGE_FACE:
                            commandResponse = FaceManager.handleFace(mess);
                            if (commandResponse.isSuccessful()) Settings.FACES.save();
                            break;
                        case REMOVE_FACE:
                            commandResponse = FaceManager.removeFace(first);
                            if (commandResponse.isSuccessful()) Settings.FACES.save();
                            break;
                        case TOGGLE_FACE:
                            commandResponse = FaceManager.toggleFace(first);
                            break;
                        case ADD_SOUND:
                        case CHANGE_SOUND:
                            commandResponse = SoundEngine.getEngine().handleSound(mess, consoleCommand.getAction() == ConsoleCommand.Action.CHANGE_SOUND);
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
                        case STOP_ALL_SOUNDS:
                            commandResponse = SoundEngine.getEngine().stopSound(consoleCommand.getAction() == ConsoleCommand.Action.STOP_ALL_SOUNDS);
                            break;
                        case SEE_SOUND_STATE:
                            commandResponse = SoundEngine.getEngine().getSoundState(first);
                            break;
                        case ADD_KEYWORD:
                        case REMOVE_KEYWORD:
                            commandResponse = Utils.handleKeyword(mess);
                            if (commandResponse.isSuccessful()) Settings.KEYWORDS.save();
                            break;
                        case SET_USER_COL:
                            commandResponse = Utils.handleColor(senderUser, mess, senderUser.getColor());
                            if (commandResponse.isSuccessful()) Settings.USER_COLORS.save();
                            break;
                        case SET_COMMAND_PERMISSION:
                            commandResponse = Utils.setCommandPermission(mess);
                            if (commandResponse.isSuccessful()) Settings.saveConCommands();
                            break;
                        case ADD_TEXT_COMMAND:
                            commandResponse = Utils.addCommands(mess);
                            if (commandResponse.isSuccessful()) Settings.COMMANDS.save();
                            break;
                        case REMOVE_TEXT_COMMAND:
                            commandResponse = Utils.removeCommands(first);
                            if (commandResponse.isSuccessful()) Settings.COMMANDS.save();
                            break;
                        case ADD_DONATION:
                            commandResponse = Settings.donationManager.parseDonation(split);
                            break;
                        case SET_SOUND_PERMISSION:
                            commandResponse = SoundEngine.getEngine().setSoundPermission(first);
                            break;
                        case SET_NAME_FACE:
                            if (first.startsWith("http")) {
                                commandResponse = FaceManager.downloadFace(first,
                                        Settings.nameFaceDir.getAbsolutePath(),
                                        Utils.setExtension(String.valueOf(senderID), ".png"), sender, FaceManager.FACE_TYPE.NAME_FACE);
                            }
                            break;
                        case REMOVE_NAME_FACE:
                            if (FaceManager.nameFaceMap.containsKey(senderID))
                            {
                                try {
                                    Face f = FaceManager.nameFaceMap.remove(senderID);
                                    if (f != null && new File(f.getFilePath()).delete())
                                        getBot().sendMessage(channel, "Removed face for user: " + sender + " !");
                                } catch (Exception e) {
                                    getBot().sendMessage(channel, "Name face for user " + sender +
                                            " could not be removed due to an exception!");
                                }
                            } else {
                                getBot().sendMessage(channel, "The user " + sender + " has no name face!");
                            }
                            break;
                        case SET_STREAM_TITLE:
                            commandResponse = APIRequests.Twitch.setStreamStatus(key, channel, message, true);
                            if (commandResponse.isSuccessful()) {
                                if (GUIMain.statusGUI != null && GUIMain.statusGUI.isVisible()) {
                                    GUIMain.statusGUI.updateStatusComponents();
                                }
                            }
                            break;
                        case SEE_STREAM_TITLE:
                            String title = APIRequests.Twitch.getTitleOfStream(channel);
                            if (!"".equals(title)) {
                                getBot().sendMessage(channel, "The title of the stream is: " + title);
                            } else {
                                getBot().sendMessage(channel, "The stream currently has no title!");
                            }
                            break;
                        case SEE_STREAM_GAME:
                            String game = APIRequests.Twitch.getGameOfStream(channel);
                            if ("".equals(game)) {
                                getBot().sendMessage(channel, "The streamer is currently not playing a game!");
                            } else {
                                getBot().sendMessage(channel, "The current game is: " + game);
                            }
                            break;
                        case SET_STREAM_GAME:
                            commandResponse = APIRequests.Twitch.setStreamStatus(key, channel, message, false);
                            if (commandResponse.isSuccessful()) {
                                if (GUIMain.statusGUI != null && GUIMain.statusGUI.isVisible()) {
                                    GUIMain.statusGUI.updateStatusComponents();
                                }
                            }
                            break;
                        case PLAY_ADVERT:
                            if (key != null) {
                                commandResponse = playAdvert(key, first, channel);
                            }
                            break;
                        case START_RAFFLE:
                            if (split.length > 2) {
                                String timeString = split[2];
                                int time = Utils.getTime(timeString);
                                if (time < 1) {
                                    getBot().sendMessage(channel, "Failed to start raffle, usage: !startraffle (key) (time) (permission?)");
                                    break;
                                }
                                int perm = 0;//TODO select a parameter in Settings GUI that defines the default raffle
                                if (split.length == 4) {
                                    //because right now it's just "Everyone" unless specified with the int param
                                    try {
                                        perm = Integer.parseInt(split[3]);
                                        perm = Utils.capNumber(0, 3, perm);
                                    } catch (Exception ignored) {//default to the specified value
                                    }
                                }
                                Raffle r = new Raffle(getBot(), first, time, channel, perm);
                                startRaffle(r);
                                updateRaffleGUI(r, true);
                            } else {
                                getBot().sendMessage(channel, "Failed to start raffle, usage: !startraffle (key) (time) (permission?)");
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
                            Raffle toRemove = stopRaffle(first);
                            if (toRemove != null) {
                                sendStopRaffleMessage(toRemove);
                                updateRaffleGUI(toRemove, false);
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
                                if (poll.isDone())
                                    poll.printResults();
                                else
                                    getBot().sendMessage(channel, "The poll is still running! Cancel it before seeing the results!");
                            } else {
                                getBot().sendMessage(channel, "There never was a poll!");
                            }
                            break;
                        case CANCEL_POLL:
                            if (poll != null) {
                                if (poll.isDone()) {
                                    getBot().sendMessage(channel, "The poll is already finished!");
                                } else {
                                    stopPoll();
                                    getBot().sendMessage(channel, "The poll has been stopped!");
                                }
                            } else {
                                getBot().sendMessage(channel, "There is no current poll!");
                            }
                            break;
                        case VOTE_POLL:
                            if (poll != null) {
                                if (!poll.isDone()) {
                                    try {
                                        int option = Integer.parseInt(first);
                                        poll.addVote(sender, option);
                                    } catch (Exception e)
                                    {
                                        GUIMain.log(e);
                                    }
                                }
                            }
                            break;
                        case NOW_PLAYING:
                            commandResponse = APIRequests.LastFM.getCurrentlyPlaying();
                            break;
                        case SHOW_UPTIME:
                            commandResponse = APIRequests.Twitch.getUptimeString(channel.substring(1));
                            break;
                        case SEE_PREV_SOUND_DON:
                            if (Settings.botShowPreviousDonSound.getValue()) {
                                if (Settings.loadedDonationSounds)
                                    commandResponse = SoundEngine.getEngine().getLastDonationSound();
                            }
                            break;
                        case SEE_PREV_SOUND_SUB:
                            if (Settings.botShowPreviousSubSound.getValue()) {
                                if (Settings.loadedSubSounds)
                                    commandResponse = SoundEngine.getEngine().getLastSubSound();
                            }
                            break;
                        case SEE_OR_SET_REPLY_TYPE:
                            commandResponse = parseReplyType(first, botnakUserName);
                            break;
                        case SEE_OR_SET_VOLUME:
                            if ("".equals(first)) {
                                getBot().sendMessage(channel, "The current Sound volume is " + String.format("%.1f", Settings.soundVolumeGain.getValue()));
                            } else {
                                try {
                                    Float volume = Float.parseFloat(first);
                                    volume = Utils.capNumber(0F, 100F, volume);
                                    Settings.soundVolumeGain.setValue(volume);
                                    getBot().sendMessage(channel, "The Sound volume was successfully set to " + String.format("%.1f", Settings.soundVolumeGain.getValue()));
                                } catch (Exception e) {
                                    getBot().sendMessage(channel, "Failed to change Sound volume! Usage: \"!volume (number)\"");
                                }
                            }
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
    public void createPoll(String channel, String message)
    {
        if (message.contains("]")) {//because what's the point of a poll with one option?
            int first = message.indexOf(" ") + 1;
            int second = message.indexOf(" ", first) + 1;
            String[] split = message.split(" ");
            int time = Utils.getTime(split[1]);
            if (time > 0) {
                startPoll(new Vote(channel, time, message.substring(second).split("]")));
            }
        }
    }

    public boolean pollExists()
    {
        return poll != null;
    }

    public boolean isPollRunning()
    {
        return pollExists() && !poll.isDone() && poll.isAlive();
    }

    public Vote getPoll()
    {
        return poll;
    }

    public void startPoll(Vote v)
    {
        // Actually start the poll
        poll = v;
        poll.start();

        // Update the GUI, if there is one
        if (GUIMain.voteGUI != null)
            GUIMain.voteGUI.addPoll(poll);
    }

    public void stopPoll()
    {
        if (poll == null) return;
        poll.interrupt();

        if (GUIMain.voteGUI != null)
            GUIMain.voteGUI.pollEnded(poll);
    }

    private void updateRaffleGUI(Raffle r, boolean add)
    {
        if (GUIMain.raffleGUI != null && GUIMain.raffleGUI.isVisible())
        {
            if (add)
            {
                GUIMain.raffleGUI.addRaffle(r);
            } else // removed
            {
                GUIMain.raffleGUI.removeRaffle(r);
            }
        }
    }


    public Raffle stopRaffle(String keyword)
    {
        for (Raffle r : raffles)
        {
            if (r.getKeyword().equalsIgnoreCase(keyword))
            {
                r.setDone(true);
                r.interrupt();
                return r;
            }
        }
        return null;
    }

    public void sendStopRaffleMessage(Raffle r)
    {
        getBot().sendMessage(r.getChannel(), "The raffle with key \"" + r.getKeyword() + "\" has been stopped!");
    }

    public void startRaffle(Raffle toStart)
    {
        toStart.start();
        raffles.add(toStart);
        //print the blarb
        getBot().sendMessage(toStart.getChannel(), toStart.getStartMessage());
        getBot().sendMessage(toStart.getChannel(), "NOTE: This is a promotion from " + toStart.getChannel().substring(1) +
                ". Twitch does not sponsor or endorse broadcaster promotions and is not responsible for them.");
    }

    public Response playAdvert(OAuth key, String first, String channel)
    {
        Response r = new Response();
        if (key.canPlayAd()) {
            int length = Utils.getTime(first);
            if (length == -1) length = 30;
            if (APIRequests.Twitch.playAdvert(key.getKey(), channel, length)) {
                r.wasSuccessful();
                r.setResponseText("Playing an ad for " + length + " seconds!");
                lastAd = System.currentTimeMillis();
            } else {
                r.setResponseText("Error playing an ad!");
                long diff = System.currentTimeMillis() - lastAd;
                if (lastAd > 0 && (diff < 480000)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("m:ss");
                    Date d = new Date(diff);
                    Date toPlay = new Date(480000 - diff);
                    r.setResponseText("Error playing advertisement! Last ad was was only " + sdf.format(d)
                            + " ago! You must wait " + sdf.format(toPlay) + " to play another ad!");
                }
            }
        } else {
            r.setResponseText("The current User OAuth key cannot play an advertisement!");
        }
        return r;
    }

    public Response parseReplyType(String first, String botnakUser) {
        Response toReturn = new Response();
        try {
            if (!"".equals(first)) {
                int perm = Integer.parseInt(first);
                perm = Utils.capNumber(0, 2, perm);
                Settings.botReplyType.setValue(perm);
                toReturn.setResponseText("Successfully changed the bot reply type (for other channels) to: " + getReplyType(perm, botnakUser));
            } else {
                toReturn.setResponseText("Current bot reply type for other channels is: " +
                        getReplyType(Settings.botReplyType.getValue(), botnakUser));
            }
        } catch (Exception ignored) {
            toReturn.setResponseText("Failed to set bot reply type due to an exception!");
        }
        return toReturn;
    }

    private String getReplyType(int perm, String botnakUser) {
        if (perm > 1) {
            return "Reply to everybody (" + perm + ")";
        } else if (perm > 0) {
            return "Reply to just " + botnakUser + " (" + perm + ")";
        } else {
            return "Reply to nobody (" + perm + ")";
        }
    }
}