package irc;

import java.time.LocalDateTime;

/**
 * Created by Nick on 11/22/2014.
 */
public class Subscriber implements Comparable<Subscriber> {

    private String name;
    private LocalDateTime started;
    private boolean isActive;
    private int streak;

    public Subscriber(String name, LocalDateTime started, boolean active, int streak) {
        this.name = name;
        this.started = started;
        this.isActive = active;
        this.streak = streak;//how many months in a row this person has subbed
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getStreak() {
        return streak;
    }

    public void resetStreak() {
        this.streak = 0;
    }

    public void incrementStreak(int num) {
        this.streak += num;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Subscriber) &&
                ((Subscriber) obj).getStarted().equals(this.getStarted()) &&
                ((Subscriber) obj).getStreak() == this.getStreak() &&
                ((Subscriber) obj).getName().equals(this.getName()) &&
                ((Subscriber) obj).isActive() == this.isActive();
    }

    @Override
    public int compareTo(Subscriber o) {
        if (this.getStarted().isAfter(o.getStarted())) {
            //this donation is newer, put the other behind
            return -1;
        }
        if (o.getStarted().equals(this.getStarted())) {
            return 0;
        } else {//the other donation is more recent
            return 1;
        }
    }
}