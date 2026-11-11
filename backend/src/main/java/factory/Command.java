package factory;

// команда оператора, приходит из браузера в виде {"action":"speed","value":70}
public class Command {
    public static final int MIN_SPEED = 0;
    public static final int MAX_SPEED = 100;

    private String action;
    private int value;

    public Command(String action, int value){
        this.action = action;
        this.value = value;
    }

    public boolean isValid(){
        if (action == null){
            return false;
        }
        if (action.equals("speed")){
            return value >= MIN_SPEED && value <= MAX_SPEED;
        }
        return action.equals("start") || action.equals("stop") || action.equals("reset");
    }

    // текст для журнала событий
    public String describe(){
        switch (action){
            case "start":
                return "Оператор отправил команду: пуск";
            case "stop":
                return "Оператор отправил команду: стоп";
            case "reset":
                return "Оператор отправил команду: сброс аварии";
            case "speed":
                return "Оператор задал скорость " + value + " %";
            default:
                return "Неизвестная команда";
        }
    }
}
