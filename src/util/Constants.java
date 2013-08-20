package util;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.regex.Pattern;

public interface Constants {

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

    public static final FileFilter pictureFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());

    public static final Pattern viewerPattern = Pattern.compile("\"viewers_count\":\\s*(\\d+)");
    public static final Pattern fileExclPattern = Pattern.compile("[\\/:\"*?<>|]");

}
