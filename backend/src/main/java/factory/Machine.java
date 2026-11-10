package factory;

public class Machine {
    private int id;
    private String name;
    private String type;
    private String state;
    private double temperature;
    private int speed;
    private int produced;
    private String updatedAt;

    public Machine(int id, String name, String type, String state, double temperature, int speed, int produced, String updatedAt){
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = state;
        this.temperature = temperature;
        this.speed = speed;
        this.produced = produced;
        this.updatedAt = updatedAt;
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getState(){
        return state;
    }
    public double getTemperature(){
        return temperature;
    }
    public int getSpeed(){
        return speed;
    }
    public int getProduced(){
        return produced;
    }
}
