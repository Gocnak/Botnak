package face;

public enum IconEnum {
    NONE(""),
    MOD("Mod"),
    BROADCASTER("Broadcaster"),
    GLOBAL_MOD("Global Mod"),
    ADMIN("Admin"),
    STAFF("Staff"),
    TURBO("Turbo"),
    SUBSCRIBER("Subscriber"),
    EX_SUBSCRIBER("Ex-Subscriber"),
    //These are what show when people do the actual cheering
    CHEER_BIT_AMT_RED("Red cheer"), // >= 10000
    CHEER_BIT_AMT_BLUE("Blue cheer"),// ( >= 5000 )
    CHEER_BIT_AMT_GREEN("Green cheer"),// ( >= 1000 )
    CHEER_BIT_AMT_PURPLE("Purple cheer"),// ( >= 100 )
    CHEER_BIT_AMT_GRAY("Gray cheer"),// ( > 1 )
    //These show next to their name, how much they cheered
    CHEER_1_99("Gray Cheerer"),
    CHEER_100_999("Purple Cheerer"),
    CHEER_1K_4K("Green Cheerer"),
    CHEER_5K_9K("Blue Cheerer"),
    CHEER_10K_99K("Red Cheerer"),
    CHEER_100K("Orange Cheerleader"),
    //How much people have donated to a third party site
    DONOR_BASIC("Donor"),
    DONOR_LOW("Donor"),
    DONOR_MEDIUM("Donor"),
    DONOR_HIGH("Donor"),
    DONOR_INSANE("Donor");

    public final String type;
    IconEnum(String type) {
        this.type = type;
    }
}