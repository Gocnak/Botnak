package com.gocnak.irc.message;

import com.gocnak.gui.ChatPane;
import com.gocnak.gui.CombinedChatPane;
import com.gocnak.gui.GUIMain;
import com.gocnak.sound.SoundEngine;
import org.jibble.pircbot.Queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 1/14/2015.
 * <p>
 * Created to aid in the faster response time of the GUI while large
 * quantities of messages come in, using the Pircbot Queue.
 */
public class MessageQueue extends Thread {

    private static ExecutorService pool;
    private static Queue<MessageWrapper> queue;

    public MessageQueue() {
        queue = new Queue<>(100);
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public synchronized void run() {
        while (!GUIMain.shutDown) {
            MessageWrapper mess = queue.next();//locks for a new message, no need for Thread#sleep
            if (mess != null && mess.getLocal() != null) {
                mess.print();
            }
        }
    }

    private static synchronized void addToQueue(MessageWrapper mw) {
        queue.add(mw);
    }

    public static void addMessage(Message mess) {
        if (mess != null && mess.getType() != null) {
            pool.execute(() -> {
                MessageWrapper wrap = new MessageWrapper(mess);
                try {//try catch for security, if one message fails, we still want to receive messages
                    if (mess.getType() == Message.MessageType.LOG_MESSAGE) {
                        if (mess.getChannel() != null)
                            GUIMain.getChatPane(mess.getChannel()).log(wrap, true);
                        else GUIMain.getSystemLogsPane().log(wrap, true);
                    } else if (mess.getType() == Message.MessageType.NORMAL_MESSAGE ||
                            mess.getType() == Message.MessageType.ACTION_MESSAGE) {
                        if (!GUIMain.combinedChatPanes.isEmpty()) {
                            for (CombinedChatPane cc : GUIMain.combinedChatPanes) {
                                for (String chan : cc.getChannels()) {
                                    if (mess.getChannel().substring(1).equalsIgnoreCase(chan)) {
                                        cc.onMessage(wrap, true);
                                        break;
                                    }
                                }
                            }
                        }
                        GUIMain.getChatPane(mess.getChannel()).onMessage(wrap, false);
                    } else if (mess.getType() == Message.MessageType.SUB_NOTIFY) {
                        GUIMain.getChatPane(mess.getChannel()).onSub(wrap);
                    } else if (mess.getType() == Message.MessageType.BAN_NOTIFY ||
                            mess.getType() == Message.MessageType.HOSTED_NOTIFY ||
                            mess.getType() == Message.MessageType.HOSTING_NOTIFY ||
                            mess.getType() == Message.MessageType.JTV_NOTIFY) {
                        GUIMain.getChatPane(mess.getChannel()).log(wrap, false);
                    } else if (mess.getType() == Message.MessageType.DONATION_NOTIFY) {
                        GUIMain.getChatPane(mess.getChannel()).onDonation(wrap);
                        if (GUIMain.currentSettings.loadedDonationSounds) {
                            SoundEngine.getEngine().playSpecialSound(false);
                        }
                    } else if (mess.getType() == Message.MessageType.CLEAR_TEXT) {
                        wrap.addPrint(((ChatPane) mess.getExtra())::cleanupChat);
                    }
                    addToQueue(wrap);
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            });
        }
    }
}