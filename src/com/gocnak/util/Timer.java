package com.gocnak.util;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 5/30/13
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Timer {
    public long end;
    public final long start;
    public final long period;

    /**
     * Instantiates a new com.gocnak.util.Timer with a given time
     * period in milliseconds.
     *
     * @param period Time period in milliseconds.
     */
    public Timer(final long period) {
        this.period = period;
        start = System.currentTimeMillis();
        end = start + period;
    }

    /**
     * Returns the number of milliseconds elapsed since
     * the start time.
     *
     * @return The elapsed time in milliseconds.
     */
    public long getElapsed() {
        return System.currentTimeMillis() - start;
    }

    /**
     * Returns the number of milliseconds remaining
     * until the timer is up.
     *
     * @return The remaining time in milliseconds.
     */
    public long getRemaining() {
        if (isRunning()) {
            return end - System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * Returns <tt>true</tt> if this timer's time period
     * has not yet elapsed.
     *
     * @return <tt>true</tt> if the time period has not yet passed.
     */
    public boolean isRunning() {
        return System.currentTimeMillis() < end;
    }

    /**
     * Restarts this timer using its period.
     */
    public void reset() {
        end = System.currentTimeMillis() + period;
    }

    /**
     * Sets the end time of this timer to a given number of
     * milliseconds from the time it is called. This does
     * not edit the period of the timer (so will not affect
     * operation after reset).
     *
     * @param ms The number of milliseconds before the timer
     *           should stop running.
     */
    public void setEndIn(final long ms) {
        end = System.currentTimeMillis() + ms;
    }
}