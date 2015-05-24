package face;

public enum IconEnum {
    NONE(""),
    MOD("Mod"),
    BROADCASTER("Broadcaster"),
    GLOBALMOD("Global Mod"),
    ADMIN("Admin"),
    STAFF("Staff"),
    TURBO("Turbo"),
    SUBSCRIBER("Subscriber"),
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