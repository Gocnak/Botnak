package irc.account;

import gui.forms.GUIMain;
import irc.IRCBot;
import irc.IRCViewer;
import lib.pircbot.org.jibble.pircbot.PircBot;
import lib.pircbot.org.jibble.pircbot.Queue;
import util.Timer;
import util.settings.Settings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nick on 6/12/2014.
 */
public class AccountManager extends Thread {

    private Queue<Task> tasks;

    private HashMap<String, ReconnectThread> reconnectThreads;

    private Account userAccount, botAccount;

    private PircBot viewer, bot;

    public AccountManager() {
        reconnectThreads = new HashMap<>();
        tasks = new Queue<>();
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
        if (t.type == Task.Type.CONNECT || t.type == Task.Type.JOIN_CHANNEL || t.type == Task.Type.LEAVE_CHANNEL) {
            if (t.doer != null) {
                ReconnectThread rt = reconnectThreads.get(t.doer.getNick());
                if (rt != null) {
                    if (t.type != Task.Type.CONNECT) {//since the reconnect thread already handles this...
                        rt.addTask(t);
                    }
                    return;
                }
            }
        }
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
            Task t = tasks.next();
            if (t != null) {
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
                        if (t.doer != null) {
                            ReconnectThread potential = reconnectThreads.get(t.doer.getNick());
                            if (potential != null) {
                                potential.interrupt();
                                reconnectThreads.remove(t.doer.getNick());
                            }
                            t.doer.disconnect();
                            t.doer.dispose();
                        }
                        break;
                    case JOIN_CHANNEL:
                        if (t.doer != null) {
                            if (t.doer.isConnected()) {
                                String channel = (String) t.message;
                                if (!channel.startsWith("#")) channel = "#" + channel;
                                t.doer.joinChannel(channel);
                            } else {
                                createReconnectThread(t.doer);
                                addTask(t);//loops back around, adds to the reconnect thread
                            }
                        }
                        break;
                    case CONNECT:
                        if (t.doer.connect("irc.twitch.tv", 6667)) {
                            GUIMain.log(t.message);
                        } else {
                            if (!t.doer.isConnected()) {
                                createReconnectThread(t.doer);
                            }
                        }
                        break;
                    case LEAVE_CHANNEL:
                        if (t.doer != null) {
                            if (t.doer.isConnected()) {
                                String chaan = (String) t.message;
                                if (!chaan.startsWith("#")) chaan = "#" + chaan;
                                t.doer.partChannel(chaan);
                            } else {
                                createReconnectThread(t.doer);
                                addTask(t);//loops back around, adds to the reconnect thread
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void createReconnectThread(PircBot b) {
        if (!Settings.autoReconnectAccounts.getValue()) return;
        if (reconnectThreads.get(b.getNick()) != null) return;
        ReconnectThread rt = new ReconnectThread(b);
        rt.start();
        reconnectThreads.put(b.getNick(), rt);
        GUIMain.logCurrent("Detected a disconnection, attempting to reconnect the account: " + b.getNick() + " !");
    }

    private class ReconnectThread extends Thread {

        private PircBot doer;
        private ArrayList<Task> cachedTasks;
        private Timer t;
        private boolean isDone = false;

        ReconnectThread(PircBot toReconnect) {
            this.doer = toReconnect;
            cachedTasks = new ArrayList<>();
        }

        @Override
        public synchronized void start() {
            t = new Timer(10000);
            super.start();
        }

        public void addTask(Task t) {
            cachedTasks.add(t);
        }

        @Override
        public void run() {
            while (!isDone && !GUIMain.shutDown) {
                while (t.isRunning()) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception ignored) {
                    }
                }
                if (doer.connect("irc.twitch.tv", 6667)) {
                    isDone = true;
                } else {
                    t.reset();
                }
            }
            if (isDone) {
                reconnectThreads.remove(doer.getNick());
                cachedTasks.forEach(Settings.accountManager::addTask);
                GUIMain.logCurrent("Successfully reconnected the account: " + doer.getNick() + " !");
            }
        }
    }
}