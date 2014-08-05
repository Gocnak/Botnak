package irc;

import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.PircBot;

import java.util.ArrayList;

/**
 * Created by Nick on 6/12/2014.
 */
public class AccountManager extends Thread {

    ArrayList<Task> tasks = new ArrayList<>();

    private Account userAccount, botAccount;

    private PircBot viewer, bot;


    public AccountManager() {
        userAccount = null;
        botAccount = null;
        viewer = null;
        bot = null;
    }

    public void setBotAccount(Account botAccount) {
        this.botAccount = botAccount;
    }

    public void setUserAccount(Account userAccount) {
        this.userAccount = userAccount;
    }

    public Account getBotAccount() {
        return botAccount;
    }

    public Account getUserAccount() {
        return userAccount;
    }

    public synchronized void addTask(Task t) {
        tasks.add(t);
    }

    public synchronized void setViewer(PircBot viewer1) {
        viewer = viewer1;
    }

    public synchronized PircBot getBot() {
        return bot;
    }

    public synchronized PircBot getViewer() {
        return viewer;
    }

    public synchronized void setBot(PircBot bot1) {
        bot = bot1;
    }

    @Override
    public void run() {//handle connection status
        while (!GUIMain.shutDown) {
            if (tasks.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {

                }
            } else {
                Task t = tasks.get(0);
                switch (t.type) {
                    case CREATE_BOT_ACCOUNT:
                        GUIMain.bot = new IRCBot();
                        PircBot bot = new PircBot(GUIMain.bot);

                        bot.setNick(getBotAccount().getName());
                        bot.setPassword(getBotAccount().getKey().getKey());
                        bot.setMessageDelay(1500);
                        setBot(bot);
                        addTask(new Task(getBot(), Task.Type.CONNECT, "Loaded Bot: " + getBotAccount().getName() + "!"));
                        break;
                    case CREATE_VIEWER_ACCOUNT:
                        GUIMain.viewer = new IRCViewer();
                        PircBot viewer = new PircBot(GUIMain.viewer);
                        viewer.setVerbose(true);//TODO remove this
                        viewer.setNick(getUserAccount().getName());
                        viewer.setPassword(getUserAccount().getKey().getKey());
                        setViewer(viewer);
                        addTask(new Task(getViewer(), Task.Type.CONNECT, "Loaded User: " + getUserAccount().getName() + "!"));
                        break;
                    case DISCONNECT:
                        //TODO interrupt any reconnect threads
                        t.doer.disconnect();
                        t.doer.dispose();
                        break;
                    case JOIN_CHANNEL:
                        if (t.doer != null && t.doer.isConnected()) {
                            String channel = (String) t.message;
                            if (!channel.startsWith("#")) channel = "#" + channel;
                            t.doer.joinChannel(channel);
                        } else {
                            addTask(t);
                            //TODO reconnect thread
                        }
                        break;
                    case CONNECT:
                        if (t.doer.connect("irc.twitch.tv", 80)) { //TODO change the port
                            t.doer.sendRawLine("TWITCHCLIENT 3");
                            GUIMain.log(t.message);
                        } else {
                            //TODO reconnect thread
                        }
                        break;
                    case LEAVE_CHANNEL:
                        String chaan = (String) t.message;
                        if (!chaan.startsWith("#")) chaan = "#" + chaan;
                        t.doer.partChannel(chaan);
                        break;
                }
                tasks.remove(0);
            }
        }
    }


}
