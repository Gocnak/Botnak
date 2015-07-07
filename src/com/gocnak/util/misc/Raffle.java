package com.gocnak.util.misc;

import com.gocnak.gui.GUIMain;
import com.gocnak.util.Timer;
import com.gocnak.util.Utils;
import org.jibble.pircbot.PircBot;

import java.util.ArrayList;

/**
 * Created by Nick on 7/17/2014.
 */
public class Raffle extends Thread {

    private PircBot bot = null;
    private int time = 0;
    private int permission = 0;
    private String keyword = null;
    private String winner = null;
    private ArrayList<String> entrants = null;
    private boolean isDone = false;
    private String channel;
    private String startMessage;

    public String getKeyword() {
        return keyword;
    }

    public void addUser(String u) {
        if (!isDone) {
            if (!entrants.contains(u)) {
                entrants.add(u);
            }
        }
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    public String getWinner() {
        return winner;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public int getPermission() {
        return permission;
    }

    private Timer timer;

    public Raffle(PircBot bot, String key, int time, String channel, int permission) {
        this.bot = bot;
        this.keyword = key;
        this.time = Utils.handleInt(time);
        this.channel = channel;
        this.permission = permission;
        this.startMessage = "Raffle started! " + (permission > 2 ? "Mods have " :
                (permission > 1 ? "Subscribers and Mods have " :
                        (permission > 0 ? "Donators, Subscribers, and Mods have " : "Everybody has ")))
                + time + " seconds to type \""
                + keyword + "\" to enter!";
    }

    @Override
    public synchronized void start() {
        timer = new Timer(time);
        entrants = new ArrayList<>();
        super.start();
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown && timer.isRunning()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        //pick the user
        if (!isDone) {
            isDone = true;
            pickWinner();
        }
    }

    public boolean isDone() {
        return isDone;
    }

    private void pickWinner() {
        int size = entrants.size();
        if (size > 0) {
            winner = entrants.get(Utils.random(0, size));
            bot.sendMessage(channel, "!!! CONGRATULATIONS TO " + winner + " !!!");
        } else {
            bot.sendMessage(channel, "Nobody entered the giveaway... BibleThump");
        }
    }
}