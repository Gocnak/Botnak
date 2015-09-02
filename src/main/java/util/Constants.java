package util;

import gui.WrapEditorKit;
import gui.forms.GUIMain;
import gui.listeners.*;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {

    public static final double VERSION = 2.35;

    public static final TabTitleEditListener tabListener = new TabTitleEditListener(GUIMain.channelPane);

    public static final String[] fontSizeArray = new String[]{"11", "12", "13", "14", "16", "18", "20"};

    public static final FileFilter folderFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f != null && f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Folders";
        }
    };

    public static final FileFilter wavfiles = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) return true;
                String ext = Utils.getExtension(f);
                if (ext != null && ext.equals(".wav"))
                    return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return ".wav files";
        }
    };

    public static final FileFilter pictureFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());

    public static final Pattern viewerTwitchPattern = Pattern.compile("\"viewers\":\\s*(\\d+)");
    public static final Pattern fileExclPattern = Pattern.compile("[/:\"*?<>|]");
    /**
     * The regex String for finding URLs in messages.
     * credit: TDuva
     */
    private static final String urlRegex =
            "(?i)\\b(?:(?:https?)://|www\\.)[-A-Z0-9+&@#/%=~_|$?!:,.]*[A-Z0-9+&@#/%=~_|$]";
    /**
     * The Matcher to use for finding URLs in messages.
     * credit: TDuva
     */
    public static final Matcher urlMatcher = Pattern.compile(urlRegex).matcher("");

    //Listeners
    public static final WrapEditorKit wrapEditorKit = new WrapEditorKit();
    public static final ListenerURL listenerURL = new ListenerURL();
    public static final ListenerName listenerName = new ListenerName();
    public static final ListenerFace listenerFace = new ListenerFace();
    public static final PaneMenuListener listenerPaneMenu = new PaneMenuListener();

    //Credit TDuva from his Chatty bot for some of theses, as well as the
    //"namedcolor" class idea.
    public static final NamedColor[] namedColors = {
            new NamedColor("Red", 255, 0, 0),
            new NamedColor("Blue", 0, 0, 255),
            new NamedColor("Cyan", 0, 255, 255),
            new NamedColor("Purple", 255, 0, 255),
            new NamedColor("Violet", 255, 0, 255),
            new NamedColor("Green", 0, 255, 0),
            new NamedColor("Magenta", 255, 0, 255),
            new NamedColor("Yellow", 255, 255, 0),
            new NamedColor("Orange", 255, 200, 0),
            new NamedColor("Pink", 255, 175, 175),
            new NamedColor("LightGray", 192, 192, 192),
            new NamedColor("Gray", 128, 128, 128),
            new NamedColor("FireBrick", 178, 34, 34),
            new NamedColor("Coral", 255, 127, 80),
            new NamedColor("YellowGreen", 154, 205, 50),
            new NamedColor("OrangeRed", 255, 69, 0),
            new NamedColor("SeaGreen", 46, 139, 87),
            new NamedColor("GoldenRod", 218, 165, 32),
            new NamedColor("Chocolate", 210, 105, 30),
            new NamedColor("CadetBlue", 95, 158, 160),
            new NamedColor("DodgerBlue", 30, 144, 255),
            new NamedColor("HotPink", 255, 105, 180),
            new NamedColor("BlueViolet", 138, 43, 226),
            new NamedColor("SpringGreen", 0, 255, 127)
    };
}