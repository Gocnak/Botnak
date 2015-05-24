package irc.account;

import lib.pircbot.org.jibble.pircbot.PircBot;

/**
 * Created by Nick on 7/16/2014.
 */
public class Task {

    Type type;
    Object message;
    PircBot doer;

    public Task(PircBot botToDoTask, Type type, Object message) {
        doer = botToDoTask;
        this.type = type;
        this.message = message;
    }

    public enum Type {
        CREATE_VIEWER_ACCOUNT,
        CREATE_BOT_ACCOUNT,
        CONNECT,
        JOIN_CHANNEL,
        LEAVE_CHANNEL,
        DISCONNECT
    }
}