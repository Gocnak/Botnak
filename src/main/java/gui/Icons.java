package gui;

import face.FaceManager;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import lib.scalr.Scalr;

/**
* A class which specifies special chat icons, or 'badges'.
* This allows for easier creation of custom icons, and
* should be easily adaptable to create custom donation
* levels.
*
* @author Joseph Blackman
* @date 4/10/2015
*/

public class Icons {

    /**
     * For a specified icon, returns the file containing the
     * image, resized to fit into the chat window.
     *
     * @param Icon the icon to get
     * @return ImageIcon the icon file
     */
    
    public static ImageIcon getIcon(IconEnum i, String channel) {
    	String kind = "";
    	ImageIcon icon = null;
    	switch (i) {
    		case Mod:
    			kind = "Mod";
    			icon = sizeIcon(GUIMain.currentSettings.modIcon);
    			break;
    		case Broadcaster:
    			kind = "Broadcaster";
    			icon = sizeIcon(GUIMain.currentSettings.broadIcon);
    			break;
    		case Admin:
    			kind = "Admin";
    	        icon = sizeIcon(GUIMain.currentSettings.adminIcon);
        		break;
    		case Staff:
    			kind = "Staff";
    			icon = sizeIcon(GUIMain.currentSettings.staffIcon);
    			break;
    		case Turbo:
    			kind = "Turbo";
    			icon = sizeIcon(GUIMain.currentSettings.turboIcon);
    			break;
    		case Subscriber:
    			kind = "Subscriber";
    			URL subIcon = FaceManager.getSubIcon(channel);
    			if (subIcon != null) {
        			icon = sizeIcon(subIcon);
        		}
    			break;
    		case Donator_basic:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/green.png"));
    			break;
    		case Donator_low:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/bronze.png"));
    			break;
    		case Donator_medium:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/silver.png"));
    			break;
    		case Donator_high:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/gold.png"));
    			break;
    		case Donator_insane:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/diamond.png"));
    			break;
    		case GlobalMod:
    		    kind = "GlobalMod";
    		    icon = sizeIcon(ChatPane.class.getResource("/image/globalmod.png"));
                break;
            case None:
            default:
                break;
    	}
    	
    	try {
        	print(channel, kind, null);
        } catch (Exception e) {
        	GUIMain.log("INSERT ICON " + e.getMessage());
        }
    	
    	return icon;
    }
    
    /**
     * Resize an icon to match the chat font size. This has the
     * effect of allowing users to submit images of any size.
     *
     * @param URL the image URL
     * @return ImageIcon the resized image
     */
    
    private static ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
        		BufferedImage img = ImageIO.read(image);
        		int size = GUIMain.currentSettings.font.getSize();
        		img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size);
        		icon = new ImageIcon(img);
        		icon.getImage().flush();
        		return icon;
        } catch (Exception e) {
        		icon = new ImageIcon(image);
        }
        return icon;
    }
}