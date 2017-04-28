package util.misc;

import gui.forms.GUIMain;
import lib.pircbot.PircBot;
import lib.pircbot.User;
import util.Permissions;
import util.Timer;
import util.Utils;

import java.util.ArrayList;

/**
 * Created by Nick on 7/17/2014.
 */
public class Raffle extends Thread {

    private PircBot bot = null;
    private int time = 0;
    private String keyword = null;
    private String winner = null;
    private ArrayList<String> entrants = null;
    private ArrayList<Permissions.Permission> requiredPermissions, denyPerms;
    private boolean isDone = false;
    private String channel;
    private String startMessage;

    public String getKeyword() {
        return keyword;
    }

    public void addUser(User u, String channel)
    {
        if (!isDone && !entrants.contains(u.getDisplayName()))
        {
            ArrayList<Permissions.Permission> userPerms = Permissions.getUserPermissions(u, channel);
            if (userPerms.stream().anyMatch(p -> requiredPermissions.contains(p))
                    && userPerms.stream().noneMatch(p -> denyPerms.contains(p)))
                entrants.add(u.getDisplayName());
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

    public String getChannel()
    {
        return channel;
    }

    public int getNumberEntrants()
    {
        return entrants != null ? entrants.size() : 0;
    }

    private Timer timer;

    // Created from text command
    public Raffle(PircBot bot, String key, int time, String channel, int permission) {
        this.bot = bot;
        this.keyword = key;
        this.time = Utils.handleInt(time);
        this.channel = channel;
        determinePermission(permission);
        determineStartMessage();
    }

    // Created from the GUI
    public Raffle(PircBot bot, String key, int time, String channel, ArrayList<Permissions.Permission> allowPerms,
                  ArrayList<Permissions.Permission> denyPerms)
    {
        this.bot = bot;
        this.keyword = key;
        this.time = Utils.handleInt(time);
        this.channel = channel;
        this.requiredPermissions = allowPerms;
        this.denyPerms = denyPerms;
        determineStartMessage();
    }

    // Used by the chat command to make the allow/deny automatically
    private void determinePermission(int givenPermission)
    {
        requiredPermissions = new ArrayList<>();
        if (givenPermission <= 0)
            requiredPermissions.add(Permissions.Permission.VIEWER);
        else
        {
            switch (givenPermission)
            {
                default:
                case 1:
                    requiredPermissions.add(Permissions.Permission.SUBSCRIBER);
                case 2:
                    requiredPermissions.add(Permissions.Permission.DONOR);
                case 3:
                    requiredPermissions.add(Permissions.Permission.MODERATOR);
                    break;
            }
        }

        // No denying it, it's gonna happen
        denyPerms = new ArrayList<>();
    }

    private void determineStartMessage()
    {
        this.startMessage = "Raffle started! Who can enter: ";

        StringBuilder stanSB = new StringBuilder();
        // Two cases when the VIEWER perm is in required: 1. everybody else is excluded, or everybody can enter
        if (requiredPermissions.contains(Permissions.Permission.VIEWER) && denyPerms.isEmpty())
        {
            startMessage += "Everybody!";
        } else
        {
            for (Permissions.Permission p : requiredPermissions)
            {
                stanSB.append(p.toString().substring(0, 1));
                stanSB.append(p.toString().substring(1).toLowerCase());
                stanSB.append("s, ");
            }
            startMessage += stanSB.substring(0, stanSB.length() - 2) + ".";
        }

        if (!denyPerms.isEmpty())
        {
            stanSB = new StringBuilder(" || Who CANNOT ENTER: ");
            for (Permissions.Permission p : denyPerms)
            {
                stanSB.append(p.toString().substring(0, 1));
                stanSB.append(p.toString().substring(1).toLowerCase());
                stanSB.append("s, ");
            }

            startMessage += stanSB.substring(0, stanSB.length() - 2) + ".";
        }

        startMessage += String.format(" Time limit: %d seconds!", time / 1000);

        startMessage += " Keyword to enter: \"" + keyword + "\" !";
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
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }

            updateGUI();
        }
        //pick the user
        if (!isDone) {
            isDone = true;
            pickWinner();
        }
    }

    private void updateGUI()
    {
        if (GUIMain.raffleGUI != null && GUIMain.raffleGUI.isVisible())
            GUIMain.raffleGUI.updateRaffle(this);
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
            winner = "Nobody :(";
        }

        updateGUI();
    }
}