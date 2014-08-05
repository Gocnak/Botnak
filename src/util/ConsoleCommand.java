package util;


/**
 * This class is the container for all of the default
 * Botnak commands, like !mod and !addface, etc.
 * <p/>
 * This was created to change permissions of the aforementioned
 * commands to either full permission classes (all mods, or everyone)
 * or to specific people in the chat, regardless of status in the chat.
 * <p/>
 * >> If you want more commands added, you will have to hard-code them! <<
 * <p/>
 * The reason this is called *Console*Command is that these commands
 * directly change something about Botnak, or require specific code.
 * Text Commands are different.
 */
public class ConsoleCommand {

    public Action action;
    public String[] certainPermission;
    public int classPermission;
    public String trigger;

    public static enum Action { //one for each
        ADD_FACE,
        CHANGE_FACE,
        REMOVE_FACE,
        TOGGLE_FACE,
        ADD_SOUND,
        CHANGE_SOUND,
        REMOVE_SOUND,
        SET_SOUND_DELAY,
        TOGGLE_SOUND,
        STOP_SOUND,
        STOP_ALL_SOUNDS,
        MOD_USER,
        ADD_KEYWORD,
        REMOVE_KEYWORD,
        SET_USER_COL,
        SET_COMMAND_PERMISSION,
        ADD_TEXT_COMMAND,
        REMOVE_TEXT_COMMAND,
        ADD_DONATION,
        SET_SUB_SOUND,
        SET_SOUND_PERMISSION,
        SET_NAME_FACE,
        REMOVE_NAME_FACE,
        SET_STREAM_TITLE,
        SET_STREAM_GAME,
        PLAY_ADVERT,
        START_RAFFLE,
        ADD_RAFFLE_WINNER,
        REMOVE_RAFFLE_WINNER,
        STOP_RAFFLE,
        SEE_WINNERS
    }

    /**
     * Creates a command that modifies Botnak directly. All of this is internal.
     *
     * @param trigger           The name of the command; what comes after the !
     * @param act               The action of the console command.
     * @param classPerm         The class permission (@see Constants.PERMISSION_ s)
     * @param certainPermission The certain users able to use the command.
     */
    public ConsoleCommand(String trigger, Action act, int classPerm, String[] certainPermission) {
        action = act;
        this.trigger = trigger;
        classPermission = classPerm;
        this.certainPermission = certainPermission;
    }

    public Action getAction() {
        return action;
    }

    public String getTrigger() {
        return trigger;
    }

    public int getClassPermission() {
        return classPermission;
    }

    public String[] getCertainPermissions() {
        return certainPermission;
    }

    public void setClassPermission(int newInt) {
        classPermission = newInt;
    }

    public void setCertainPermission(String... newPerm) {
        certainPermission = newPerm;
    }


}
