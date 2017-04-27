package face;

import gui.ChatPane;
import gui.forms.GUIMain;
import lib.scalr.Scalr;
import util.AnimatedGifEncoder;
import util.GifDecoder;
import util.Utils;
import util.settings.Settings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

public class Icons
{

    /**
     * For a specified icon, returns the file containing the
     * image, resized to fit into the chat window.
     *
     * @param i the type of icon to get
     * @return the icon along with what type it is
     */

    public static BotnakIcon getIcon(IconEnum i, String channel)
    {
        ImageIcon icon = null;
        switch (i)
        {
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
            case PRIME:
                icon = sizeIcon(ChatPane.class.getResource("/image/prime.png"));
                break;
            case VERIFIED:
                icon = sizeIcon(ChatPane.class.getResource("/image/verified.png"));
                break;
            case SUBSCRIBER:
                URL subIcon = FaceManager.getSubIcon(channel);
                if (subIcon != null)
                {
                    icon = sizeIcon(subIcon);
                }
                break;
            case EX_SUBSCRIBER:
                URL exSubscriberIcon = FaceManager.getExSubscriberIcon(channel);
                if (exSubscriberIcon != null)
                {
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
            case CHEER_BIT_AMT_RED:
                icon = sizeGifIcon(ChatPane.class.getResource("/image/bits_red.gif"), i.name());
                break;
            case CHEER_BIT_AMT_BLUE:
                icon = sizeGifIcon(ChatPane.class.getResource("/image/bits_blue.gif"), i.name());
                break;
            case CHEER_BIT_AMT_GREEN:
                icon = sizeGifIcon(ChatPane.class.getResource("/image/bits_green.gif"), i.name());
                break;
            case CHEER_BIT_AMT_PURPLE:
                icon = sizeGifIcon(ChatPane.class.getResource("/image/bits_purple.gif"), i.name());
                break;
            case CHEER_BIT_AMT_GRAY:
                icon = sizeGifIcon(ChatPane.class.getResource("/image/bits_gray.gif"), i.name());
                break;
            case CHEER_1_99:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_gray.png"));
                break;
            case CHEER_100_999:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_purple.png"));
                break;
            case CHEER_1K_4K:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_green.png"));
                break;
            case CHEER_5K_9K:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_blue.png"));
                break;
            case CHEER_10K_99K:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_red.png"));
                break;
            case CHEER_100K:
                icon = sizeIcon(ChatPane.class.getResource("/image/bits_tier_orange.png"));
                break;
            case NONE:
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

    public static ImageIcon sizeIcon(URL image)
    {
        ImageIcon icon;
        try
        {
            BufferedImage img = ImageIO.read(image);
            int size = Settings.font.getValue().getSize();
            img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size);
            icon = new ImageIcon(img);
            icon.getImage().flush();
            return icon;
        } catch (Exception e)
        {
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public static ImageIcon sizeGifIcon(URL image, String name)
    {
        ImageIcon icon;
        File temp = new File(Settings.tmpDir + File.separator + Utils.setExtension(name, ".gif"));
        //Only size this if we haven't already
        if (temp.exists())
        {
            icon = new ImageIcon(temp.getAbsolutePath());
        } else
        {
            try
            {
                InputStream is = image.openStream();
                GifDecoder decoder = new GifDecoder();
                int status = decoder.read(is);
                if (status == 0)
                {
                    AnimatedGifEncoder age = new AnimatedGifEncoder();
                    age.setRepeat(0);
                    age.setQuality(1);
                    FileOutputStream fos = new FileOutputStream(temp);
                    age.start(fos);
                    int n = decoder.getFrameCount();
                    int size = Settings.font.getValue().getSize();
                    for (int i = 0; i < n; i++)
                    {
                        BufferedImage scaled = Scalr.resize(decoder.getFrame(i), Scalr.Method.ULTRA_QUALITY, size, size);
                        age.addFrame(scaled);
                        if (decoder.getDelay(i) == 0)
                        {
                            age.setDelay(100);
                        } else
                        {
                            age.setDelay(decoder.getDelay(i));
                        }
                    }

                    age.finish();
                    fos.close();

                    icon = new ImageIcon(temp.getAbsolutePath());
                } else
                {
                    GUIMain.log("sizeGifIcon failed to read the input gif! Status: " + status);
                    icon = new ImageIcon(image);
                }
                is.close();
            } catch (Exception e)
            {
                icon = new ImageIcon(image);
            }
        }
        return icon;
    }

    //Wrapper class for logging purposes
    public static class BotnakIcon
    {
        public final IconEnum t;
        public final ImageIcon ii;

        public IconEnum getType()
        {
            return t;
        }

        public ImageIcon getImage()
        {
            return ii;
        }

        public BotnakIcon(IconEnum type, ImageIcon icon)
        {
            t = type;
            ii = icon;
        }
    }
}