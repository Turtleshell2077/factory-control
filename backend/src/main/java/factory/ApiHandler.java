package factory;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ApiHandler implements HttpHandler{
    private static final int EVENTS_LIMIT = 20;

    private Database db;
    private MqttService mqtt;
    private Gson gson = new Gson();

    public ApiHandler(Database db, MqttService mqtt){
        this.db = db;
        this.mqtt = mqtt;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try{
            if (method.equals("GET") && path.equals("/api/health")){
                handleHealth(exchange);
            }
            else if (method.equals("GET") && path.equals("/api/machines")){
                sendJson(exchange, 200, gson.toJson(db.getMachines()));
            }
            else if (method.equals("GET") && path.equals("/api/events")){
                sendJson(exchange, 200, gson.toJson(db.getEvents(EVENTS_LIMIT)));
            }
            else if (method.equals("POST") && path.endsWith("/command")){
                handleCommand(exchange, path);
            }
            else{
                sendJson(exchange, 404, "{\"error\":\"Адрес не найден\"}");
            }
        }
        catch(Exception e){
            System.out.println("Ошибка при обработке " + method + " " + path + ": " + e.getMessage());
            sendJson(exchange, 500, "{\"error\":\"Внутренняя ошибка сервера\"}");
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException{
        boolean dbOk = db.isAlive();
        boolean mqttOk = mqtt.isConnected();
        String json = "{\"database\":" + dbOk + ",\"broker\":" + mqttOk + "}";
        if (dbOk && mqttOk){
            sendJson(exchange, 200, json);
        }
        else{
            sendJson(exchange, 503, json);
        }
    }

    // POST /api/machines/3/command, в теле {"action":"speed","value":70}
    private void handleCommand(HttpExchange exchange, String path) throws Exception{
        int machineId = getMachineId(path);
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Command command = gson.fromJson(body, Command.class);

        if (command == null || !command.isValid()){
            sendJson(exchange, 400, "{\"error\":\"Неверная команда\"}");
            return;
        }
        if (machineId < 0 || !db.machineExists(machineId)){
            sendJson(exchange, 404, "{\"error\":\"Оборудование не найдено\"}");
            return;
        }
        mqtt.sendCommand(machineId, command);
        db.addEvent(machineId, "оператор", command.describe());
        sendJson(exchange, 200, "{\"result\":\"Команда отправлена\"}");
    }

    // из /api/machines/3/command достаем 3, если не получилось - возвращаем -1
    public static int getMachineId(String path){
        String[] parts = path.split("/");
        if (parts.length != 5 || !parts[2].equals("machines") || !parts[4].equals("command")){
            return -1;
        }
        try{
            return Integer.parseInt(parts[3]);
        }
        catch(NumberFormatException e){
            return -1;
        }
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException{
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()){
            out.write(bytes);
        }
    }
}
