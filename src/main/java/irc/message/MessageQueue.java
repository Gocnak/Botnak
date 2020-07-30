package irc.message;

import gui.ChatPane;
import gui.CombinedChatPane;
import gui.forms.GUIMain;
import sound.SoundEngine;
import util.Utils;
import util.settings.Settings;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    private static BlockingQueue<MessageWrapper> queue;

    public MessageQueue() {
        queue = new ArrayBlockingQueue<>(100, true);
        pool = Executors.newCachedThreadPool();
        start();
    }

    @Override
    public synchronized void run() {
        while (!GUIMain.shutDown) {
            try
            {
                MessageWrapper mess = queue.take();//locks for a new message, no need for Thread#sleep
                if (mess != null && mess.getLocal() != null)
                {
                    mess.print();
                }
            } catch (Exception e)
            {
                GUIMain.log(e);
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
                    switch (mess.getType()) {
                        case LOG_MESSAGE:
                            if (mess.getChannel() != null) {
                                GUIMain.getChatPane(mess.getChannel()).log(wrap, true);
                            } else if (mess.getExtra() != null) {
                                ((ChatPane) mess.getExtra()).log(wrap, true);
                            } else {
                                GUIMain.getSystemLogsPane().log(wrap, true);
                            }
                            break;
                        case NORMAL_MESSAGE:
                        case ACTION_MESSAGE:
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
                            break;
                        case SUB_NOTIFY:
                            GUIMain.getChatPane(mess.getChannel()).onSub(wrap);
                            break;
                        case BAN_NOTIFY:
                        case HOSTED_NOTIFY:
                        case HOSTING_NOTIFY:
                        case JTV_NOTIFY:
                        case RAIDED_NOTIFY:
                        case RAIDING_NOTIFY:
                            GUIMain.getChatPane(mess.getChannel()).log(wrap, false);
                            break;
                        case DONATION_NOTIFY:
                            GUIMain.getChatPane(mess.getChannel()).onDonation(wrap);
                            if (Settings.loadedDonationSounds) {
                                SoundEngine.getEngine().playSpecialSound(false);
                            }
                            break;
                        case CLEAR_TEXT:
                            wrap.addPrint(((ChatPane) mess.getExtra())::cleanupChat);
                            break;
                        case WHISPER_MESSAGE:
                            //GUIMain.getCurrentPane().onWhisper(wrap); TODO uncomment
                            break;
                        case CHEER_MESSAGE:
                            GUIMain.getChatPane(mess.getChannel()).onCheer(wrap);
                            //TODO: Update this to use a separate folder, if need be
                            int cheerAmount = (int) wrap.getLocal().getExtra();
                            if (Settings.loadedDonationSounds && Utils.isMainChannel(mess.getChannel()) && cheerAmount >= 200)
                            {
                                SoundEngine.getEngine().playSpecialSound(false);
                            }
                            break;
                        default:
                            break;
                    }
                    addToQueue(wrap);
                } catch (Exception e) {
                    GUIMain.log(e);
                }
            });
        }
    }
}