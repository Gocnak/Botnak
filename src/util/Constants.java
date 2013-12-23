package util;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.regex.Pattern;

public class Constants {

    public static final double VERSION = 2.11;

    /**
     * All users may do it
     */
    public static final int PERMISSION_ALL = 0;

    /**
     * Only mods and the person running Botnak can do it
     */
    public static final int PERMISSION_MOD = 1;

    /**
     * Only the person running Botnak can do it
     */
    public static final int PERMISSION_DEV = 2;


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
                if (ext != null) {
                    if (ext.equals("wav")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return ".wav files";
        }
    };

    public static final FileFilter pictureFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());

    public static final Pattern viewerPattern = Pattern.compile("\"viewers_count\":\\s*(\\d+)");
    public static final Pattern fileExclPattern = Pattern.compile("[\\/:\"*?<>|]");

}
