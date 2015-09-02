package util.settings;

import gui.forms.GUIMain;

import java.io.*;

/**
 * Created by Nick on 8/31/2015.
 * <p>
 * This class is created for all the other Settings in the Settings class,
 * where the saving and loading are standardized.
 */
public abstract class AbstractFileSave {

    public abstract void handleLineLoad(String line);

    public abstract void handleLineSave(PrintWriter pw);

    public abstract File getFile();

    public void save() {
        try (PrintWriter pw = new PrintWriter(getFile())) {
            handleLineSave(pw);
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }

    public void load() {
        try (FileInputStream fis = new FileInputStream(getFile());
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                handleLineLoad(line);
            }
        } catch (Exception e) {
            GUIMain.log(e);
        }
    }
}