package gui;

import gui.forms.GUIMain;
import irc.message.Message;
import util.Constants;
import util.misc.Donation;
import util.settings.DonationManager;
import util.settings.Settings;

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
        toAdd.setState(Settings.stMuted.getValue());
        toAdd.addItemListener(this);
        menu.add(toAdd);
        menu.addSeparator();

        Menu options = new Menu("Toggle Showing...");
        toAdd = new CheckboxMenuItem("Mentions");
        toAdd.setState(Settings.stShowMentions.getValue());
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Donations");
        toAdd.setState(Settings.stShowDonations.getValue());
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Followed Streams");
        toAdd.setState(Settings.stShowActivity.getValue());
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Followers");
        toAdd.setState(Settings.stShowNewFollowers.getValue());
        toAdd.addItemListener(this);
        options.add(toAdd);
        toAdd = new CheckboxMenuItem("Subscribers");
        toAdd.setState(Settings.stShowSubscribers.getValue());
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
            Settings.stUseSystemTray.setValue(false);
            GUIMain.log("Unable to start System Tray due to exception:");
            GUIMain.log(e);
        }
    }

    private static boolean shouldDisplay() {
        return Settings.stUseSystemTray.getValue() && !Settings.stMuted.getValue();
    }

    public static boolean shouldDisplayMentions() {
        return shouldDisplay() && Settings.stShowMentions.getValue();
    }

    public void displayMention(Message m) {
        displayMessage("Mention in " + m.getChannel(), m.getSender() + " says: " + m.getContent(), MessageType.INFO);
    }


    public static boolean shouldDisplayDonations() {
        return shouldDisplay() && Settings.stShowDonations.getValue();
    }

    public void displayDonation(Donation d) {
        displayMessage("New donation! " + DonationManager.getCurrencyFormat().format(d.getAmount())
                + " from " + d.getFromWho(), d.getNote(), MessageType.INFO);
    }


    public static boolean shouldDisplayNewFollowers() {
        return shouldDisplay() && Settings.stShowNewFollowers.getValue();
    }

    public void displayNewFollower(String name) {
        displayMessage("New follower!", name + " is now following the channel!", MessageType.INFO);
    }


    public static boolean shouldDisplayFollowedActivity() {
        return shouldDisplay() && Settings.stShowActivity.getValue();
    }

    public void displayLiveChannel(String name) {
        displayMessage("Followed stream went live!", name + " just started streaming.", MessageType.INFO);
    }


    public static boolean shouldDisplayNewSubscribers() {
        return shouldDisplay() && Settings.stShowSubscribers.getValue();
    }

    public void displaySubscriber(String content, boolean continued) {
        displayMessage(continued ? "Continued subscription!" : "New Subscriber!", content, MessageType.INFO);
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
                Settings.stMuted.setValue(bool);
            } else if (item.getLabel().startsWith("Ment")) {
                Settings.stShowMentions.setValue(bool);
            } else if (item.getLabel().startsWith("Dona")) {
                Settings.stShowDonations.setValue(bool);
            } else if (item.getLabel().startsWith("Subs")) {
                Settings.stShowSubscribers.setValue(bool);
            } else if (item.getLabel().equals("Followers")) {
                Settings.stShowNewFollowers.setValue(bool);
            } else if (item.getLabel().startsWith("Followed")) {
                Settings.stShowActivity.setValue(bool);
            }
        }
    }

    public void close() {
        SystemTray.getSystemTray().remove(this);
    }
}