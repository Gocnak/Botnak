package thread.heartbeat;

/**
 * Created by Nick on 7/8/2014.
 */
public abstract class HeartbeatThread extends Heartbeat {

    /**
     * The condition that the thread should call its #beat() void.
     *
     * @return True to beat, false to not beat.
     */
    public abstract boolean shouldBeat();

    /**
     * What to do every "beat" when #shouldBeat() is true.
     */
    public abstract void beat();

    /**
     * What to do after #beat() is called.
     */
    public abstract void afterBeat();
}
