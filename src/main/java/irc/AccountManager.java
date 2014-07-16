package irc;

import lib.pircbot.org.jibble.pircbot.PircBot;

/**
 * Created by Nick on 6/12/2014.
 */
public class AccountManager {


    private PircBot viewer, bot;


    public AccountManager() {
        viewer = null;
        bot = null;
    }

    public void setViewer(PircBot viewer1) {
        viewer = viewer1;
    }

    public PircBot getBot() {
        return bot;
    }

    public PircBot getViewer() {
        return viewer;
    }

    public void setBot(PircBot bot1) {
        bot = bot1;
    }


}
