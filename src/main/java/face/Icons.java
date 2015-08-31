package face;

import gui.ChatPane;
import lib.scalr.Scalr;
import util.settings.Settings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * A class which specifies special chat icons, or 'badges'.
 * This allows for easier creation of custom icons, and
 * should be easily adaptable to create custom donation
 * levels.
 *
 * @author Joseph Blackman
 * @version 4/10/2015
 */

public class Icons {

    /**
     * For a specified icon, returns the file containing the
     * image, resized to fit into the chat window.
     *
     * @param i the type of icon to get
     * @return the icon along with what type it is
     */

    public static BotnakIcon getIcon(IconEnum i, String channel) {
        ImageIcon icon = null;
        switch (i) {
            case MOD:
                icon = sizeIcon(Settings.modIcon.getValue());
                break;
            case BROADCASTER:
                icon = sizeIcon(Settings.broadIcon.getValue());
                break;
            case ADMIN:
                icon = sizeIcon(Settings.adminIcon.getValue());
                break;
            case STAFF:
                icon = sizeIcon(Settings.staffIcon.getValue());
                break;
            case TURBO:
                icon = sizeIcon(Settings.turboIcon.getValue());
                break;
            case SUBSCRIBER:
                URL subIcon = FaceManager.getSubIcon(channel);
                if (subIcon != null) {
                    icon = sizeIcon(subIcon);
                }
                break;
            case EX_SUBSCRIBER:
                URL exSubscriberIcon = FaceManager.getExSubscriberIcon(channel);
                if (exSubscriberIcon != null) {
                    icon = sizeIcon(exSubscriberIcon);
                }
                break;
            case DONOR_BASIC:
                icon = sizeIcon(ChatPane.class.getResource("/image/green.png"));
                break;
            case DONOR_LOW:
                icon = sizeIcon(ChatPane.class.getResource("/image/bronze.png"));
                break;
            case DONOR_MEDIUM:
                icon = sizeIcon(ChatPane.class.getResource("/image/silver.png"));
                break;
            case DONOR_HIGH:
                icon = sizeIcon(ChatPane.class.getResource("/image/gold.png"));
                break;
            case DONOR_INSANE:
                icon = sizeIcon(ChatPane.class.getResource("/image/diamond.png"));
                break;
            case GLOBAL_MOD:
                icon = sizeIcon(ChatPane.class.getResource("/image/globalmod.png"));
                break;
            case NONE:
                break;
            default:
                break;
        }
        return new BotnakIcon(i, icon);
    }

    /**
     * Resize an icon to match the chat font size. This has the
     * effect of allowing users to submit images of any size.
     *
     * @param image the image URL
     * @return ImageIcon the resized image
     */

    public static ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
            BufferedImage img = ImageIO.read(image);
            int size = Settings.font.getValue().getSize();
            img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size);
            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e) {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    //Wrapper class for logging purposes
    public static class BotnakIcon {
        public final IconEnum t;
        public final ImageIcon ii;

        public IconEnum getType() {
            return t;
        }

        public ImageIcon getImage() {
            return ii;
        }

        public BotnakIcon(IconEnum type, ImageIcon icon) {
            t = type;
            ii = icon;
        }
    }
}