package util;

/**
 * Created to cleanup the horrendous HashMap that was the commandMap in GUIMain.
 */
public class Command {

    private Timer delayTimer;
    private int delay;
    private String trigger;
    private StringArray contents;

    public Command(String name, int delay, String... contents) {
        this.contents = new StringArray(contents);
        this.delay = Utils.handleInt(delay);
        trigger = name;
        delayTimer = new Timer(delay);
    }

    public String getTrigger() {
        return trigger;
    }

    public int getDelay() {
        return delay;
    }

    public StringArray getMessage() {
        return contents;
    }

    public Timer getDelayTimer() {
        return delayTimer;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null) {
            if (other instanceof Command) {
                return ((Command) other).contents.equals(contents) &&
                        ((Command) other).trigger.equals(trigger) &&
                        ((Command) other).delay == delay;
            }
        }
        return false;
    }
}
