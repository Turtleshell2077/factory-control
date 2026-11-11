package emulator;

// то, что эмулятор раз в 2 секунды публикует в factory/{id}/telemetry
public class Telemetry {
    private String state;
    private double temperature;
    private int speed;
    private int produced;

    public Telemetry(String state, double temperature, int speed, int produced){
        this.state = state;
        this.temperature = temperature;
        this.speed = speed;
        this.produced = produced;
    }
}
