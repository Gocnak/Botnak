package thread.heartbeat;

/**
 * Created by Nick on 7/8/2014.
 */
public interface HeartbeatThread {

    /**
     * The condition that the thread should call its #beat() void.
     *
     * @return True to beat, false to not beat.
     */
    public boolean shouldBeat();

    /**
     * What to do every "beat" when #shouldBeat() is true.
     */
    public void beat();

    /**
     * What to do after #beat() is called.
     */
    public void afterBeat();
}
