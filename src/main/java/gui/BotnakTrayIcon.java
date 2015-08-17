package gui;

import gui.forms.GUIMain;
import irc.message.Message;
import util.Constants;
import util.misc.Donation;
import util.settings.DonationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by Nick on 8/15/2015.
 * <p>
 * Tray icon support, shows events in the task bar, has toggleable states.
 */
public class BotnakTrayIcon extends TrayIcon implements ActionListener, ItemListener {

    public BotnakTrayIcon() {
        super(new ImageIcon(BotnakTrayIcon.class.getResource("/image/icon.png")).getImage());
        setImageAutoSize(true);
        String version = "Botnak v" + String.valueOf(Constants.VERSION);
        setToolTip(version);
        PopupMenu menu = new PopupMenu(version);
        menu.addActionListener(this);
        MenuItem menuItem = new MenuItem("Show/Hide Botnak");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        CheckboxMenuItem toAdd = new CheckboxMenuItem("Mute Notifications");
        toAdd.setState(GUIMain.currentSettings.stMuted);
        toAdd.addItemListener(this);
        menu.add(toAdd);
        menu.addSeparator();

        Menu options = new Menu("Toggle Showing...");
        toAdd = new CheckboxMenuItem("Mentions");
        toAdd.setState(GUIMain.currentSettings.stShowMentions);
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Donations");
        toAdd.setState(GUIMain.currentSettings.stShowDonations);
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Followed Streams");
        toAdd.setState(GUIMain.currentSettings.stShowActivity);
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Followers");
        toAdd.setState(GUIMain.currentSettings.stShowNewFollowers);
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Subscribers");
        toAdd.setState(GUIMain.currentSettings.stShowSubscribers);
        toAdd.addItemListener(this);
        options.add(toAdd);
        menu.add(options);


        menu.addSeparator();
        menuItem = new MenuItem("Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        setPopupMenu(menu);
        try {
            SystemTray.getSystemTray().add(this);
        } catch (AWTException e) {
            GUIMain.currentSettings.stUseSystemTray = false;
            GUIMain.log("Unable to start System Tray due to exception:");
            GUIMain.log(e);
        }
    }

    private static boolean shouldDisplay() {
        return GUIMain.currentSettings.stUseSystemTray && !GUIMain.currentSettings.stMuted;
    }

    public static boolean shouldDisplayMentions() {
        return shouldDisplay() && GUIMain.currentSettings.stShowMentions;
    }

    public void displayMention(Message m) {
        displayMessage("Mention in " + m.getChannel(), m.getSender() + " says: " + m.getContent(), MessageType.INFO);
    }


    public static boolean shouldDisplayDonations() {
        return shouldDisplay() && GUIMain.currentSettings.stShowDonations;
    }

    public void displayDonation(Donation d) {
        displayMessage("New donation! " + DonationManager.getCurrencyFormat().format(d.getAmount())
                + " from " + d.getFromWho(), d.getNote(), MessageType.INFO);
    }


    public static boolean shouldDisplayNewFollowers() {
        return shouldDisplay() && GUIMain.currentSettings.stShowNewFollowers;
    }

    public void displayNewFollower(String name) {
        displayMessage("New Follower!", name + " is now following the channel!", MessageType.INFO);
    }


    public static boolean shouldDisplayFollowedActivity() {
        return shouldDisplay() && GUIMain.currentSettings.stShowActivity;
    }

    public void displayLiveChannel(String name) {
        displayMessage("Followed Stream went live!", name + " just started streaming.", MessageType.INFO);
    }


    public static boolean shouldDisplayNewSubscribers() {
        return shouldDisplay() && GUIMain.currentSettings.stShowSubscribers;
    }

    public void displaySubscriber(String content, boolean continued) {
        displayMessage(continued ? "Continued Subscription!" : "New Subscriber!", content, MessageType.INFO);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof MenuItem) {
            String text = ((MenuItem) e.getSource()).getLabel();
            if (text.startsWith("Show/H")) {
                GUIMain.instance.setVisible(!GUIMain.instance.isVisible());
            } else if (text.equals("Exit")) {
                GUIMain.instance.exitButtonActionPerformed();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof CheckboxMenuItem) {
            CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();
            boolean bool = (e.getStateChange() == ItemEvent.SELECTED);
            if (item.getLabel().startsWith("Mute ")) {
                GUIMain.currentSettings.stMuted = bool;
            } else if (item.getLabel().startsWith("Ment")) {
                GUIMain.currentSettings.stShowMentions = bool;
            } else if (item.getLabel().startsWith("Dona")) {
                GUIMain.currentSettings.stShowDonations = bool;
            } else if (item.getLabel().startsWith("Subs")) {
                GUIMain.currentSettings.stShowSubscribers = bool;
            } else if (item.getLabel().equals("Followers")) {
                GUIMain.currentSettings.stShowNewFollowers = bool;
            } else if (item.getLabel().startsWith("Followed")) {
                GUIMain.currentSettings.stShowActivity = bool;
            }
        }
    }

    public void close() {
        SystemTray.getSystemTray().remove(this);
    }
}