package com.gocnak.sound;

import com.gocnak.gui.GUIMain;
import com.gocnak.util.Response;
import com.gocnak.util.Timer;
import com.gocnak.util.Utils;
import org.jibble.pircbot.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 12/20/13.
 */
public class SoundEngine {

    private static SoundEngine engine = null;
    private static SoundPlayer player = null;

    public static SoundEngine getEngine() {
        return engine;
    }

    private int delay = 10000;//default to 10 seconds
    private int permission = 1;//default to sub+ permission
    private boolean soundToggle = true;
    private Timer soundTimer = new Timer(delay);
    private ConcurrentHashMap<String, Sound> soundMap;
    private Stack<Sound> subStack, donationStack;
    private Sound lastSubSound, lastDonationSound;

    public static void init() {
        engine = new SoundEngine();
    }

    public SoundEngine() {
        soundMap = new ConcurrentHashMap<>();
        player = new SoundPlayer();
        subStack = new Stack<>();
        donationStack = new Stack<>();
        lastSubSound = lastDonationSound = null;
    }

    public void setDelay(int newDelay) {
        delay = newDelay;
        soundTimer = new Timer(delay);
    }

    public int getDelay() {
        return delay;
    }

    public ConcurrentHashMap<String, Sound> getSoundMap() {
        return soundMap;
    }

    public Stack<Sound> getSubStack() {
        return subStack;
    }

    public Stack<Sound> getDonationStack() {
        return donationStack;
    }

    public Timer getSoundTimer() {
        return soundTimer;
    }

    public void setShouldPlay(boolean newBool) {
        soundToggle = newBool;
    }

    public boolean shouldPlay() {
        return soundToggle;
    }

    public void setPermission(int perm) {
        permission = perm;
    }

    public int getPermission() {
        return permission;
    }

    /**
     * Adds a com.gocnak.sound to the com.gocnak.sound set.
     * This respects the "one at a time" but
     * also can allow spam if the com.gocnak.sound delay is 0
     *
     * @param s The com.gocnak.sound to add.
     */
    public void playSound(Sound s) {
        if (!soundTimer.isRunning()) {
            if (soundTimer.period == 0) {//alowing for spam
                try {
                    player.play(s.getFile(), SoundPlayer.PlayMode.Force);
                } catch (Exception ignored) {
                }
            } else {
                try {
                    player.play(s.getFile(), SoundPlayer.PlayMode.Ignore);
                } catch (Exception ignored) {
                }
                soundTimer.reset();
            }
        }
    }

    /**
     * Plays the new subscriber/donation com.gocnak.sound, overrides current ruleset for engine.
     */
    public void playSpecialSound(boolean isSub) {
        Sound s = getSpecialSound(isSub);
        try {
            player.play(s.getFile(), SoundPlayer.PlayMode.Force);
        } catch (Exception ignored) {
        }
    }

    private Sound getSpecialSound(boolean isSub) {
        if ((isSub ? subStack : donationStack).isEmpty()) {//refreshes and reshuffles
            if (isSub) GUIMain.currentSettings.loadSubSounds();
            else GUIMain.currentSettings.loadDonationSounds();
        }
        Sound sound = (isSub ? subStack : donationStack).pop();
        if (isSub) lastSubSound = sound;
        else lastDonationSound = sound;
        return sound;
    }

    public Response getLastDonationSound() {
        Response toReturn = new Response();
        if (lastDonationSound != null) {
            toReturn.setResponseText("The previous donation notification com.gocnak.sound was taken from the song: " +
                    Utils.removeExt(lastDonationSound.getFile().getName()));
        } else {
            toReturn.setResponseText("There is no previous donation com.gocnak.sound!");
        }
        return toReturn;
    }

    public Response getLastSubSound() {
        Response toReturn = new Response();
        if (lastSubSound != null) {
            toReturn.setResponseText("The previous subscriber notification com.gocnak.sound was taken from the song: " +
                    Utils.removeExt(lastSubSound.getFile().getName()));
        } else {
            toReturn.setResponseText("There is no previous donation com.gocnak.sound!");
        }
        return toReturn;
    }

    /**
     * Gets the first playing com.gocnak.sound in the queue.
     *
     * @return The first playing com.gocnak.sound.
     */
    public SoundEntry getCurrentPlayingSound() {
        Collection<SoundEntry> coll = player.getPlayingSounds();
        if (!coll.isEmpty()) {
            for (SoundEntry s : coll) {
                if (s.getClip().isRunning()) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Gets all of the current playing sounds.
     *
     * @return All of the currently playing sounds.
     */
    public Collection<SoundEntry> getCurrentPlayingSounds() {
        return player.getPlayingSounds();
    }

    public void close() {
        player.close();
    }


    private String getSoundStateString() {
        int delay = (int) getSoundTimer().period / 1000;
        String onOrOff = (shouldPlay() ? "ON" : "OFF");
        int numSound = getCurrentPlayingSounds().size();
        int permission = getPermission();
        String numSounds = (numSound > 0 ? (numSound == 1 ? "one com.gocnak.sound" : (numSound + " sounds")) : "no sounds") + " currently playing";
        String delayS = (delay < 2 ? (delay == 0 ? "no delay." : "a delay of 1 second.") : ("a delay of " + delay + " seconds."));
        String perm = Utils.getPermissionString(permission) + " can play sounds.";
        return "Sound is currently turned " + onOrOff + " with " + numSounds + " with " + delayS + " " + perm;
    }

    /**
     * Base trigger for sounds. Checks if a dev com.gocnak.sound is not playing, if the general delay is up,
     * if the channel is yours, and if the user can even play the com.gocnak.sound if it exists.
     *
     * @param s       Sound command trigger/name.
     * @param send    The sender of the command.
     * @param channel Channel the command was in.
     * @return true to play the com.gocnak.sound, else false
     */
    public boolean soundTrigger(String s, String send, String channel) {
        if (SoundEngine.getEngine().shouldPlay()) {//com.gocnak.sound not turned off
            if (channel.equalsIgnoreCase("#" + GUIMain.currentSettings.accountManager.getUserAccount().getName())) {//is in main channel
                if (soundCheck(s, send, channel)) {//let's check the existence/permission
                    return true;//HIT THAT
                }
            }
        }
        return false;
    }

    /**
     * Checks the existence of a com.gocnak.sound, and the permission of the requester.
     *
     * @param sound  Sound trigger
     * @param sender Sender of the command trigger.
     * @return false if the com.gocnak.sound is not allowed, else true if it is.
     */
    private boolean soundCheck(String sound, String sender, String channel) {
        //set the permission
        User u = GUIMain.currentSettings.channelManager.getUser(sender, true);
        int permission = Utils.getUserPermission(u, channel);
        String[] keys = soundMap.keySet().toArray(new String[soundMap.keySet().size()]);
        for (String s : keys) {
            if (s != null && s.equalsIgnoreCase(sound)) {
                Sound snd = soundMap.get(s);
                if (snd != null && snd.isEnabled()) {
                    int perm = snd.getPermission();
                    //check permission
                    if (permission >= perm && permission >= SoundEngine.getEngine().getPermission()) {
                        //descending permission, this should work; devs can play mod and all sounds, etc.
                        //this also checks to see if they can even play the com.gocnak.sound given the certain circumstance.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles the adding/changing of a com.gocnak.sound, its permission, and/or its files.
     *
     * @param s      The string from the chat to manipulate.
     * @param change True for changing a com.gocnak.sound, false for adding.
     */
    public Response handleSound(String s, boolean change) {
        Response toReturn = new Response();
        if (GUIMain.currentSettings.defaultSoundDir != null &&
                !GUIMain.currentSettings.defaultSoundDir.equals("null") &&
                !GUIMain.currentSettings.defaultSoundDir.equals("")) {
            try {
                String[] split = s.split(" ");
                String name = split[1];//both commands have this in common.
                int perm;
                if (split.length > 3) {//!add/changesound com.gocnak.sound 0 com.gocnak.sound(,maybe,more)
                    try {
                        perm = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        toReturn.setResponseText("Failed to handle com.gocnak.sound, could not parse the permission!");
                        return toReturn;
                    }
                    String files = split[3];
                    if (perm < 0 || permission > 4) {
                        toReturn.setResponseText("Failed to handle com.gocnak.sound due to a bad permission!");
                        return toReturn;
                    }
                    if (!files.contains(",")) {//isn't multiple
                        //this can be !addsound com.gocnak.sound 0 com.gocnak.sound or !changesound com.gocnak.sound 0 newsound
                        String filename = GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(files, ".wav");
                        if (Utils.areFilesGood(filename)) {
                            if (soundMap.containsKey(name)) {//they could technically change the permission here as well
                                if (!change) {//!addsound
                                    soundMap.put(name, new Sound(perm,// add it tooo it maaan
                                            Utils.addStringsToArray(soundMap.get(name).getSounds().data, filename)));
                                    toReturn.setResponseText("Successfully added the com.gocnak.sound \"" + filename + "\" to the command \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                } else {//!changesound
                                    soundMap.put(name, new Sound(perm, filename));//replace it
                                    toReturn.setResponseText("Successfully changed the com.gocnak.sound \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                }
                            } else { //*gasp* A NEW SOUND!?
                                if (!change) {//can't have !changesound act like !addsound
                                    soundMap.put(name, new Sound(perm, filename));
                                    toReturn.setResponseText("Successfully added the new com.gocnak.sound \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                } else {
                                    toReturn.setResponseText("The com.gocnak.sound \"!" + name + "\" does not exist; cannot change it!");
                                }
                            }
                        } else {
                            toReturn.setResponseText("Failed to handle com.gocnak.sound, the file \"" + filename + "\" does not exist!");
                        }
                    } else {//is multiple
                        //this can be !addsound com.gocnak.sound 0 multi,com.gocnak.sound or !changesound com.gocnak.sound 0 multi,com.gocnak.sound
                        ArrayList<String> list = new ArrayList<>();
                        String[] filesSplit = files.split(",");
                        for (String str : filesSplit) {
                            list.add(GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(str, ".wav"));
                        }             //calls the areFilesGood boolean in it (filters bad files already)
                        filesSplit = Utils.checkFiles(list.toArray(new String[list.size()]));
                        list.clear();//recycle time!
                        if (!change) { //adding sounds
                            if (soundMap.containsKey(name)) {//adding sounds, so get the old ones V
                                Collections.addAll(list, soundMap.get(name).getSounds().data);
                            }
                            Utils.checkAndAdd(list, filesSplit);//checks for repetition, will add anyway if list is empty
                            soundMap.put(name, new Sound(perm, list.toArray(new String[list.size()])));
                            toReturn.setResponseText("Successfully added multiple sounds!");
                            toReturn.wasSuccessful();
                            return toReturn;
                        } else {//!changesound, so replace it if it's in there
                            if (soundMap.containsKey(name)) {
                                soundMap.put(name, new Sound(perm, filesSplit));
                                toReturn.setResponseText("Successfully changed the com.gocnak.sound \"!" + name +
                                        "\" to have the new files!");
                                toReturn.wasSuccessful();
                            } else {
                                toReturn.setResponseText("Failed to change com.gocnak.sound, the com.gocnak.sound \"" + name + "\" does not exist!");
                            }
                        }
                    }
                } else if (split.length == 3) {//add/changesound com.gocnak.sound perm/newsound
                    if (split[2].length() == 1) {//ASSUMING it's a permission change.
                        try {
                            perm = Integer.parseInt(split[2]);//I mean come on. What com.gocnak.sound will have a 1 char name?
                            if (perm != -1 && perm >= 0 && perm < 5) {
                                if (change) {//because adding just a com.gocnak.sound name and a permission is silly
                                    soundMap.put(name, new Sound(perm, soundMap.get(name).getSounds().data));//A pretty bad one...
                                    toReturn.setResponseText("Successfully changed the com.gocnak.sound \"!" + name +
                                            "\" to have the new permission: " + perm);
                                    toReturn.wasSuccessful();
                                } else {
                                    toReturn.setResponseText("Failed to add com.gocnak.sound, cannot add a permission as a com.gocnak.sound!");
                                }
                            } else {
                                toReturn.setResponseText("Failed to change the permission, please give a permission from 0 to 4!");
                            }
                        } catch (NumberFormatException e) {//maybe it really is a 1-char-named com.gocnak.sound?
                            String test = GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(split[2], ".wav");
                            if (Utils.areFilesGood(test)) { //wow...
                                if (change) {
                                    soundMap.put(name, new Sound(soundMap.get(name).getPermission(), test));
                                    toReturn.setResponseText("Successfully changed the com.gocnak.sound for \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                } else {//adding a 1 char com.gocnak.sound that exists to the pool...
                                    soundMap.put(name, new Sound(soundMap.get(name).getPermission(),
                                            Utils.addStringsToArray(soundMap.get(name).getSounds().data, test)));
                                    toReturn.setResponseText("Successfully added the com.gocnak.sound \"" + split[2] +
                                            "\" to the command \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                }
                            } else {
                                toReturn.setResponseText("Failed to handle com.gocnak.sound, invalid permission!");
                            }
                        }
                    } else { //it's a/some new file(s) as replacement/to add!
                        if (split[2].contains(",")) {//multiple
                            String[] filesSplit = split[2].split(",");
                            ArrayList<String> list = new ArrayList<>();
                            for (String str : filesSplit) {
                                list.add(GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(str, ".wav"));
                            }             //calls the areFilesGood boolean in it (filters bad files already)
                            filesSplit = Utils.checkFiles(list.toArray(new String[list.size()]));
                            if (!change) {//!addsound soundname more,sounds
                                if (soundMap.containsKey(name)) {
                                    filesSplit = Utils.addStringsToArray(soundMap.get(name).getSounds().data, filesSplit);
                                    soundMap.put(name, new Sound(soundMap.get(name).getPermission(), filesSplit));
                                    toReturn.wasSuccessful();
                                    toReturn.setResponseText("Successfully added " + filesSplit.length +
                                            " new sounds to the com.gocnak.sound \"!" + name + "\" !");
                                } else { //use default permission
                                    soundMap.put(name, new Sound(filesSplit));
                                    toReturn.setResponseText("Successfully added " + filesSplit.length +
                                            " new sounds to the new command \"!" + name + "\" !");
                                    toReturn.wasSuccessful();
                                }
                            } else {//!changesound soundname new,sounds
                                if (soundMap.containsKey(name)) {//!changesound isn't !addsound
                                    soundMap.put(name, new Sound(soundMap.get(name).getPermission(), filesSplit));
                                    toReturn.setResponseText("Successfully changed the com.gocnak.sound \"!" + name + "\" to have the "
                                            + filesSplit.length + " new files!");
                                    toReturn.wasSuccessful();
                                } else {
                                    toReturn.setResponseText("Cannot change the com.gocnak.sound, \"" + name + "\" doesn't exist!");
                                }
                            }
                        } else {//singular
                            String test = GUIMain.currentSettings.defaultSoundDir + File.separator + Utils.setExtension(split[2], ".wav");
                            if (Utils.areFilesGood(test)) {
                                if (!change) {//!addsound com.gocnak.sound newsound
                                    if (soundMap.containsKey(name)) {//getting the old permission/files
                                        soundMap.put(name, new Sound(soundMap.get(name).getPermission(),
                                                Utils.addStringsToArray(soundMap.get(name).getSounds().data, test)));
                                        toReturn.setResponseText("Successfully added the com.gocnak.sound \"" + split[2] + "\" to the command \"!" + name + "\" !");
                                        toReturn.wasSuccessful();
                                    } else {//use default permission
                                        soundMap.put(name, new Sound(test));
                                        toReturn.setResponseText("Successfully added new com.gocnak.sound \"!" + name + "\" !");
                                        toReturn.wasSuccessful();
                                    }
                                } else { //!changesound com.gocnak.sound newsound
                                    if (soundMap.containsKey(name)) {//!changesound isn't !addsound
                                        soundMap.put(name, new Sound(soundMap.get(name).getPermission(), test));
                                        toReturn.setResponseText("Successfully changed the com.gocnak.sound \"" + name + "\" to have the new com.gocnak.sound \"" + split[2] + "\" !");
                                        toReturn.wasSuccessful();
                                    } else {
                                        toReturn.setResponseText("Cannot change com.gocnak.sound, \"" + name + "\" does not exist!");
                                    }
                                }
                            } else {
                                toReturn.setResponseText("Cannot handle com.gocnak.sound, the file \"" + split[2] + "\" does not exist!");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                toReturn.setResponseText("Failed to handle com.gocnak.sound due to Exception: " + e.getMessage());
            }
        } else {
            toReturn.setResponseText("Failed to handle com.gocnak.sound, the default com.gocnak.sound directory is null!");
        }
        return toReturn;
    }


    public Response toggleSound(String name, boolean individualSound) {
        Response toReturn = new Response();
        boolean newBool;
        if (individualSound) {
            if (soundMap.containsKey(name)) {
                Sound s = soundMap.get(name);
                newBool = !s.isEnabled();
                s.setEnabled(newBool);
                toReturn.wasSuccessful();
                toReturn.setResponseText("The com.gocnak.sound " + name + " is now turned " + (newBool ? "ON" : "OFF"));
            } else {
                toReturn.setResponseText("Cannot toggle com.gocnak.sound; the com.gocnak.sound \"" + name + "\" does not exist!");
            }
        } else {
            newBool = !shouldPlay();
            setShouldPlay(newBool);
            toReturn.wasSuccessful();
            toReturn.setResponseText("Sound is now turned " + (newBool ? "ON" : "OFF"));
        }
        return toReturn;
    }

    public Response removeSound(String name) {
        Response toReturn = new Response();
        if (!"".equals(name)) {
            if (soundMap.containsKey(name)) {
                soundMap.remove(name);
                toReturn.wasSuccessful();
                toReturn.setResponseText("Successfully removed com.gocnak.sound \"!" + name + "\" !");
            } else {
                toReturn.setResponseText("Failed to remove com.gocnak.sound, the com.gocnak.sound \"!" + name + "\" does not exist!");
            }
        } else {
            toReturn.setResponseText("Failed to remove com.gocnak.sound, no specified name!");
        }
        return toReturn;
    }

    public Response setSoundDelay(String first) {
        Response toReturn = new Response();
        if (!"".equals(first)) {
            int soundTime;
            soundTime = Utils.getTime(first);
            if (soundTime < 0) {
                toReturn.setResponseText("Failed to set com.gocnak.sound delay; could not parse the given time!");
                return toReturn;
            }
            soundTime = Utils.handleInt(soundTime);
            int delay = soundTime / 1000;
            toReturn.setResponseText("Sound delay " + (delay < 2 ? (delay == 0 ? "off." : "is now 1 second.") : ("is now " + delay + " seconds.")));
            setDelay(soundTime);
            toReturn.wasSuccessful();
        } else {
            toReturn.setResponseText("Failed to set com.gocnak.sound delay, usage: !setsound (time)");
        }
        return toReturn;
    }

    public Response setSoundPermission(String first) {
        Response toReturn = new Response();
        try {
            int perm = Integer.parseInt(first);
            if (perm > -1 && perm < 5) {
                setPermission(perm);
                toReturn.wasSuccessful();
                toReturn.setResponseText("Sound permission successfully changed to: " + Utils.getPermissionString(perm));
            } else {
                toReturn.setResponseText("Failed to set com.gocnak.sound permission, the permission must be from 0 to 4!");
            }
        } catch (Exception ignored) {
            toReturn.setResponseText("Failed to set com.gocnak.sound permission, usage: !setsoundperm (permission)");
        }
        return toReturn;
    }

    public Response stopSound(boolean all) {
        Response toReturn = new Response();
        if (all) {
            Collection<SoundEntry> coll = getCurrentPlayingSounds();
            if (!coll.isEmpty()) {
                coll.stream().forEach(SoundEntry::close);
                toReturn.setResponseText("Successfully stopped all playing sounds!");
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("There are no sounds currently playing!");
            }
        } else {//first com.gocnak.sound it can find
            SoundEntry sound = getCurrentPlayingSound();
            if (sound != null) {
                sound.close();
                toReturn.wasSuccessful();
                toReturn.setResponseText("Successfully stopped the first found playing com.gocnak.sound!");
            } else {
                toReturn.setResponseText("There are no sounds currently playing!");
            }
        }
        return toReturn;
    }

    public Response getSoundState(String name) {
        Response toReturn = new Response();
        if ("".equals(name)) {
            toReturn.setResponseText(getSoundStateString());
            toReturn.wasSuccessful();
        } else {
            if (soundMap.containsKey(name)) {
                Sound toCheck = soundMap.get(name);
                toReturn.setResponseText("The com.gocnak.sound \"!" + name + "\" is currently turned "
                        + (toCheck.isEnabled() ? "ON" : "OFF"));
                toReturn.wasSuccessful();
            } else {
                toReturn.setResponseText("The com.gocnak.sound \"!" + name + "\" does not exist!");
            }
        }
        return toReturn;
    }
}