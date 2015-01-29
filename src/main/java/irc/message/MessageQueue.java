package irc.message;

import gui.CombinedChatPane;
import gui.GUIMain;
import lib.pircbot.org.jibble.pircbot.Queue;
import sound.SoundEngine;

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

    public static synchronized void addToQueue(MessageWrapper mw) {
        queue.add(mw);
    }

    public static void addMessage(Message mess) {
        if (mess != null && mess.getType() != null) {
            pool.execute(() -> {
                MessageWrapper wrap = new MessageWrapper(mess);
                try {//try catch for security, if one message fails, we still want to receive messages
                    if (mess.getType() == Message.MessageType.LOG_MESSAGE) {
                        GUIMain.chatPanes.get("System Logs").log(wrap, true);
                    } else if (mess.getType() == Message.MessageType.NORMAL_MESSAGE ||
                            mess.getType() == Message.MessageType.ACTION_MESSAGE) {
                        if (!GUIMain.combinedChatPanes.isEmpty()) {
                            for (CombinedChatPane cc : GUIMain.combinedChatPanes) {
                                for (String chan : cc.getChannels()) {
                                    if (mess.getChannel().substring(1).equalsIgnoreCase(chan)) {
                                        //TODO a "don't show channel source" setting?
                                        cc.onMessage(wrap, true);
                                        break;
                                    }
                                }
                            }
                        }
                        if (GUIMain.chatPanes.get(mess.getChannel().substring(1)) != null)
                            GUIMain.chatPanes.get(mess.getChannel().substring(1)).onMessage(wrap, false);
                    } else if (mess.getType() == Message.MessageType.SUB_NOTIFY) {
                        String channel = mess.getChannel().substring(1);
                        GUIMain.chatPanes.get(channel).onSub(wrap);
                        if (channel.equalsIgnoreCase(GUIMain.currentSettings.accountManager.getUserAccount().getName())) {
                            if (GUIMain.currentSettings.subSound != null)
                                SoundEngine.getEngine().playSpecialSound(true);
                        }
                    } else if (mess.getType() == Message.MessageType.BAN_NOTIFY ||
                            mess.getType() == Message.MessageType.HOSTED_NOTIFY ||
                            mess.getType() == Message.MessageType.HOSTING_NOTIFY ||
                            mess.getType() == Message.MessageType.JTV_NOTIFY) {
                        GUIMain.chatPanes.get(mess.getChannel()).log(wrap, false);
                    } else if (mess.getType() == Message.MessageType.DONATION_NOTIFY) {
                        GUIMain.chatPanes.get(mess.getChannel()).onDonation(wrap);
                        if (GUIMain.currentSettings.donationSound != null) {
                            SoundEngine.getEngine().playSpecialSound(false);
                        }
                    }
                    addToQueue(wrap);
                } catch (Exception e) {
                    GUIMain.log(e.getMessage());
                }
            });
        }
    }
}