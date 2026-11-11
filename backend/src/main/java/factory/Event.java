package factory;

// запись журнала событий для ответа /api/events
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
}
