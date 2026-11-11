package emulator;

// команда от сервера управления из топика factory/{id}/command
public class Command {
    private String action;
    private int value;

    public String getAction(){
        return action;
    }
    public int getValue(){
        return value;
    }
}
