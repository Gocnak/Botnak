package irc.account;

/**
 * Created by Nick on 7/16/2014.
 */
public class OAuth
{
    private String key;
    private boolean canSetTitle, canReadFollowed, canReadSubscribers, canPlayAd = false;

    public OAuth(String key, boolean canSetTitle, boolean canPlayAd, boolean canReadSubscribers, boolean followed)
    {
        this.key = key;
        this.canPlayAd = canPlayAd;
        this.canSetTitle = canSetTitle;
        this.canReadSubscribers = canReadSubscribers;
        this.canReadFollowed = followed;
    }

    public String getKey() {
        return key;
    }

    public boolean canReadSubscribers() {
        return canReadSubscribers;
    }

    public void setCanReadSubscribers(boolean canReadSubscribers) {
        this.canReadSubscribers = canReadSubscribers;
    }

    public boolean canPlayAd() {
        return canPlayAd;
    }

    public boolean canSetTitle() {
        return canSetTitle;
    }

    public boolean canReadFollowed() {
        return canReadFollowed;
    }
}