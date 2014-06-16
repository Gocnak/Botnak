import gui.GUIMain;
import gui.GUIUpdate;
import util.Settings;

import javax.swing.*;
import java.awt.*;

public class Boot {

    public static void main(final String[] args) {

        /* Thread-safe initialization */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                /*NativeLibrary.addSearchPath(
                        RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");
                Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
                LibXUtil.initialise();
                JFrame frame = new JFrame("vlcj Tutorial");



                frame.setLocation(100, 100);
                frame.setSize(1050, 600);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
                EmbeddedMediaPlayerComponent player = new EmbeddedMediaPlayerComponent();
                frame.setContentPane(player);

                player.getMediaPlayer().playMedia("http://video13.fra01.hls.twitch.tv/hls83/mistermonopoli_8256897888_55574828/medium/index.m3u8?token=id=3663937107345189582,bid=8256897888,exp=1390108937,node=video13-1.fra01.hls.justin.tv,nname=video13.fra01,fmt=medium&sig=b9f5bd6c1f64ca111bbd4468c38cd62dc8533531&",
                        "network-caching=5000");*/
                setLookAndFeel();
                GUIMain g = new GUIMain();
                g.setVisible(true);
                if (GUIUpdate.checkForUpdate()) {
                    GUIUpdate gu = new GUIUpdate();
                    gu.setVisible(true);
                }
            }

            /**
             * Tries to set the swing look and feel.
             * All relevant exceptions are caught.
             */
            private void setLookAndFeel() {
                try {
                    Settings.loadLAF();
                    UIManager.setLookAndFeel(Settings.lookAndFeel);
                } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
