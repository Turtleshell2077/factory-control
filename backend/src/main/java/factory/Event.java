package factory;

public class Event {
    private int id;
    private String time;
    private String machine;
    private String source;
    private String message;

    public Event(int id, String time, String machine, String source, String message){
        this.id = id;
        this.time = time;
        this.machine = machine;
        this.source = source;
        this.message = message;
    }

    public String getMachine(){
        return machine;
    }
    public String getSource(){
        return source;
    }
    public String getMessage(){
        return message;
    }
}
