package lib.pircbot.org.jibble.pircbot;

/**
 * Created by Nick on 12/30/13.
 */
public class SubcriberListener extends Thread {

    private Channel channel = null;
    private String userName = null;
    private boolean isVerified = false;

    public SubcriberListener(String user) {
        this.userName = user;
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public synchronized void setChannel(Channel c) {
        channel = c;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void run() {
        while (channel == null) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
        }
        isVerified = true;
    }
}
