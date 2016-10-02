package irc.account;

import gui.forms.GUIMain;
import irc.IRCBot;
import irc.IRCViewer;
import lib.pircbot.PircBot;
import lib.pircbot.PircBotConnection;
import lib.pircbot.Queue;
import util.settings.Settings;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 6/12/2014.
 */
public class AccountManager extends Thread {

    private Queue<Task> tasks;

    private ConcurrentHashMap<String, ReconnectThread> reconnectThreads;

    private Account userAccount, botAccount;

    private PircBot viewer, bot;

    public AccountManager() {
        reconnectThreads = new ConcurrentHashMap<>();
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
        if (reconnectThreads != null) {
            if (t.type == Task.Type.CONNECT || t.type == Task.Type.JOIN_CHANNEL || t.type == Task.Type.LEAVE_CHANNEL) {
                if (t.doer != null && t.doer.getConnection() != null && t.doer.getConnection().getName() != null) {
                    ReconnectThread rt = reconnectThreads.get(t.doer.getConnection().getName());
                    if (rt != null) {
                        if (t.type != Task.Type.CONNECT) {//since the reconnect thread already handles this...
                            rt.addTask(t);
                        }
                        return;
                    }
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
                            if (t.doer.getConnection() != null) {
                                ReconnectThread potential = reconnectThreads.get(t.doer.getConnection().getName());
                                if (potential != null) {
                                    potential.t.cancel();
                                    reconnectThreads.remove(t.doer.getNick());
                                }
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
                                createReconnectThread(t.doer.getConnection());
                                addTask(t);//loops back around, adds to the reconnect thread
                            }
                        }
                        break;
                    case CONNECT:
                        if (t.doer.connect()) {
                            GUIMain.log(t.message);
                        } else {
                            if (!t.doer.isConnected()) {
                                createReconnectThread(t.doer.getConnection());
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
                                createReconnectThread(t.doer.getConnection());
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

    public void createReconnectThread(PircBotConnection connection) {
        if (!Settings.autoReconnectAccounts.getValue())
        {
            GUIMain.logCurrent("Auto-reconnects disabled, please check Preferences -> Auto-Reconnect!");
            return;
        }
        if (connection == null) return;
        if (reconnectThreads.containsKey(connection.getName())) return;
        ReconnectThread rt = new ReconnectThread(connection);
        rt.start();
        reconnectThreads.put(connection.getName(), rt);
        GUIMain.logCurrent("Attempting to reconnect the account: " + connection.getBot().getNick() + " ...");
    }

    private class ReconnectThread {

        private PircBotConnection connection;
        private ArrayList<Task> cachedTasks;
        private Timer t;

        ReconnectThread(PircBotConnection toReconnect) {
            connection = toReconnect;
            cachedTasks = new ArrayList<>();
            t = new Timer();
        }

        public void start() {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (connection.connect()) {
                        reconnectThreads.remove(connection.getName());
                        cachedTasks.forEach(Settings.accountManager::addTask);
                        GUIMain.logCurrent("Successfully reconnected the account: " + connection.getBot().getNick() + " !");
                        t.cancel();
                    }
                }
            };
            t.scheduleAtFixedRate(task, 10000L, 10000L);
        }

        public void addTask(Task t) {
            cachedTasks.add(t);
        }
    }
}