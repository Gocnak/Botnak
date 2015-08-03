package util.misc;

import gui.forms.GUIMain;
import util.Timer;
import util.Utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nick on 8/8/2014.
 */
public class Vote extends Thread {

    private int time;
    private Timer pollTime;
    private boolean isDone;
    private String channel;

    public boolean isDone() {
        return isDone;
    }

    private ArrayList<Option> options;

    public Vote(String channel, int time, String... options) {
        this.channel = channel;
        isDone = false;
        this.time = time;
        this.options = createOptions(options);
    }

    private ArrayList<Option> createOptions(String[] options) {
        ArrayList<Option> toReturn = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            Option o = new Option(options[i], i + 1);
            toReturn.add(o);
        }
        return toReturn;
    }

    public synchronized void addVote(String name, int option) {
        if (option > options.size() || option < 1) return;
        Option vote = getOption(name);
        if (vote != null) {//they already voted
            if (vote.compare != option) {//but now it's for a different
                vote.decrease(name);
            } else {
                return;
            }
        }
        option--;
        Option newVote = options.get(option);
        newVote.increment(name);
    }


    @Override
    public synchronized void start() {
        pollTime = new Timer(Utils.handleInt(time));
        printStart();
        super.start();
    }

    @Override
    public void run() {
        while (!GUIMain.shutDown && pollTime.isRunning()) {
            try {
                Thread.sleep(20);
            } catch (Exception ignored) {
            }
        }
        if (!isDone) {
            isDone = true;
            printResults();
        }
    }

    @Override
    public void interrupt() {
        isDone = true;
        super.interrupt();
    }

    /**
     * Gets an Option that the user is in, otherwise null.
     *
     * @param user The user to check.
     * @return The option they voted for, otherwise null.
     */
    private synchronized Option getOption(String user) {
        for (Option o : options) {
            if (o.count > 0) {
                for (String voter : o.voters) {
                    if (voter.equalsIgnoreCase(user)) {
                        return o;
                    }
                }
            }
        }
        return null;
    }

    class Option implements Comparable<Option> {
        String name;
        ArrayList<String> voters;
        int count = 0;
        int compare;

        Option(String name, int compareIndex) {
            this.name = name;
            compare = compareIndex;
            voters = new ArrayList<>();
        }

        void increment(String name) {
            count++;
            voters.add(name);
        }

        void decrease(String name) {
            count--;
            voters.remove(name);
        }

        @Override
        public int compareTo(Option o) {
            if (o.count > this.count) {
                return -1;
            } else if (o.count == this.count) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public void printStart() {
        GUIMain.currentSettings.accountManager.getBot().sendMessage(channel, "Vote started! " +
                "Everybody has " + time + " seconds to vote! To vote, type \"!vote #\" where # represents your choice!");
        for (int i = 0; i < options.size(); i++) {
            GUIMain.currentSettings.accountManager.getBot().sendMessage(channel, (i + 1) + ": " + options.get(i).name);
        }
    }

    public void printResults() {
        String[] results = getResults();
        for (String s : results) {
            GUIMain.currentSettings.accountManager.getBot().sendMessage(channel, s);
        }
    }

    public String[] getResults() {
        ArrayList<String> resultStrings = new ArrayList<>();
        resultStrings.add("The results to the poll are:");
        ArrayList<Option> results = getSortedOptions();
        int totalVotes = 0;
        for (Option o : results) {
            totalVotes += o.count;
        }
        if (totalVotes > 0) {
            for (Option o : results) {
                resultStrings.add(o.name + ": " + o.count + "/" + totalVotes + " (" + (int) (((double) o.count / (double) totalVotes) * 100) + "%)");
            }
        } else {
            resultStrings.add("Nobody voted for anything. BibleThump");
        }
        return resultStrings.toArray(new String[resultStrings.size()]);
    }

    private ArrayList<Option> getSortedOptions() {
        ArrayList<Option> results = new ArrayList<>();
        options.forEach(results::add);
        Collections.sort(results);//sort into ascending based on votes
        Collections.reverse(results);//make it descending
        return results;
    }
}