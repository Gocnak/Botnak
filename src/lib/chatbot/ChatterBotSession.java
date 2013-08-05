package lib.chatbot;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 8/1/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatterBotSession {

    ChatterBotThought think(ChatterBotThought thought) throws Exception;

    String think(String text) throws Exception;
}
