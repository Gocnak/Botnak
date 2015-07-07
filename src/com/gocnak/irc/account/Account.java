package com.gocnak.irc.account;

/**
 * Created by Nick on 7/16/2014.
 */
public class Account {

    private String name;
    private Oauth key;

    public Account(String name, Oauth key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public Oauth getKey() {
        return key;
    }
}