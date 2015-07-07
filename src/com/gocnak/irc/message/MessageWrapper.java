package com.gocnak.irc.message;

import com.gocnak.gui.GUIMain;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Nick on 1/14/2015.
 * <p>
 * Created for the MessageQueue class which handles messages on a com.gocnak.thread
 * to ensure better GUI performance while large amounts of text come in.
 */
public class MessageWrapper {
    private Message local;
    private ArrayList<Runnable> prints;

    public Message getLocal() {
        return local;
    }

    public MessageWrapper(Message m) {
        local = m;
        prints = new ArrayList<>();
    }

    public void addPrint(Runnable r) {
        prints.add(r);
    }

    public void print() {
        if (!prints.isEmpty()) {
            Runnable handler = () -> {
                try {
                    prints.forEach(java.lang.Runnable::run);
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            };
            if (EventQueue.isDispatchThread()) {
                handler.run();
            } else {
                try {
                    EventQueue.invokeLater(handler);
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            }
        }
    }
}