package gui;

import java.net.URL;
import javax.imageio.ImageIO;
import lib.scalr.Scalr;

public enum IconEnum {
	None,
	Mod,
	Broadcaster,
	Admin,
	Staff,
	Turbo,
	Subscriber,
	Donator_basic,
	Donator_low,
	Donator_medium,
	Donator_high,
	Donator_insane,
	GlobalMod
}

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
    
    public static ImageIcon getIcon (IconEnum i) {
    	String kind = "";
    	ImageIcon icon = null;
    	switch (i) {
    		case IconEnum.Mod:
    			kind = "Mod";
    			icon = sizeIcon(GUIMain.currentSettings.modIcon);
    			break;
    		case IconEnum.Broadcaster:
    			kind = "Broadcaster";
    			icon = sizeIcon(GUIMain.currentSettings.broadIcon);
    			break;
    		case IconEnum.Admin:
    			kind = "Admin";
    	        icon = sizeIcon(GUIMain.currentSettings.adminIcon);
        		break;
    		case IconEnum.Staff:
    			kind = "Staff";
    			icon = sizeIcon(GUIMain.currentSettings.staffIcon);
    			break;
    		case IconEnum.Turbo:
    			kind = "Turbo";
    			icon = sizeIcon(GUIMain.currentSettings.turboIcon);
    			break;
    		case IconEnum.Subscriber:
    			kind = "Subscriber";
    			URL subIcon = FaceManager.getSubIcon(channel);
    			if (subIcon != null) {
        			icon = sizeIcon(subIcon);
        		}
    			break;
    		case IconEnum.Donator_basic:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/green.png"));
    			break;
    		case IconEnum.Donator_medium:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/bronze.png"));
    			break;
    		case IconEnum.Donator_medium:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/silver.png"));
    			break;
    		case IconEnum.Donator_high:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/gold.png"));
    			break;
    		case IconEnum.Donator_insane:
    			kind = "Donator";
    			icon = sizeIcon(ChatPane.class.getResource("/image/diamond.png"));
    			break;
    		case IconEnum.GlobalMod:
    		    kind = "GlobalMod";
    		    icon = sizeIcon(ChatPane.class.getResource("/image/globalmod.png"));
                break;
            case IconEnum.None:
            default:
                break;
    	}
    	
    	try {
    		print(m, " ", null);
    		print(m, kind, attrs);
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
    
    private ImageIcon sizeIcon(URL image) {
        ImageIcon icon;
        try {
        		BufferedImage img = ImageIO.read(image);
        		int size = GUIMain.currentSettings.font.getSize();
        		img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, size, size);
        		icon = new ImageIcon(img);
        		IconEnum.getImage().flush();
        		return icon;
        } catch (Exception e) {
        		icon = new ImageIcon(image);
        }
        return icon;
    }
}