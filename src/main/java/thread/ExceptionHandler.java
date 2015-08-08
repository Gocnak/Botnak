package thread;

import gui.forms.GUIMain;

/**
 * Created by Nick on 8/6/2015.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        GUIMain.log("Uncaught exception from thread: " + t.toString());
        GUIMain.log(e);
    }
}