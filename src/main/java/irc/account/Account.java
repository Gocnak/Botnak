package irc.account;

/**
 * Created by Nick on 7/16/2014.
 */
public class Account {

    private String name;
    private OAuth key;

    public Account(String name, OAuth key)
    {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public OAuth getOAuth()
    {
        return key;
    }
}