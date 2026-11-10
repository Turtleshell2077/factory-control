package emulator;
import java.util.Random;

public class Machine {
    public static final double ROOM_TEMPERATURE = 20.0;
    public static final double MAX_TEMPERATURE = 90.0;
    public static final double SAFE_TEMPERATURE = 50.0;

    private int id;
    private String name;
    private String state = "STOPPED";
    private double temperature = ROOM_TEMPERATURE;
    private int speed;
    private int produced;
    private Random random = new Random();

    public Machine(int id, String name, int speed){
        this.id = id;
        this.name = name;
        this.speed = speed;
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public synchronized String getState(){
        return state;
    }
    public synchronized double getTemperature(){
        return temperature;
    }
    public synchronized int getSpeed(){
        return speed;
    }
    public synchronized int getProduced(){
        return produced;
    }

    // один шаг работы оборудования, вызывается раз в 2 секунды
    // возвращает текст события или null, если ничего не случилось
    public synchronized String tick(){
        double target = ROOM_TEMPERATURE;
        if (state.equals("RUNNING")){
            target = ROOM_TEMPERATURE + speed * 0.75;
            produced += speed / 20;
        }
        temperature += (target - temperature) * 0.1 + (random.nextDouble() - 0.5);

        if (state.equals("RUNNING") && temperature > MAX_TEMPERATURE){
            state = "ALARM";
            return "Аварийная остановка: перегрев " + round(temperature) + " °C";
        }
        return null;
    }

    public synchronized String execute(String action, int value){
        switch (action){
            case "start":
                if (state.equals("ALARM")){
                    return "Запуск невозможен: сначала нужно сбросить аварию";
                }
                if (state.equals("RUNNING")){
                    return null;
                }
                state = "RUNNING";
                return "Оборудование запущено";
            case "stop":
                if (!state.equals("RUNNING")){
                    return null;
                }
                state = "STOPPED";
                return "Оборудование остановлено";
            case "reset":
                if (!state.equals("ALARM")){
                    return null;
                }
                if (temperature > SAFE_TEMPERATURE){
                    return "Сброс невозможен: температура ещё " + round(temperature) + " °C";
                }
                state = "STOPPED";
                return "Авария сброшена, оборудование готово к запуску";
            case "speed":
                speed = value;
                return "Скорость изменена на " + value + " %";
            default:
                return "Получена неизвестная команда: " + action;
        }
    }

    public synchronized Telemetry getTelemetry(){
        return new Telemetry(state, round(temperature), speed, produced);
    }

    private double round(double x){
        return Math.round(x * 10) / 10.0;
    }
}
