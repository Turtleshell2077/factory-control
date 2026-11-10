package factory;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public void main() throws Exception{
        String dbUrl = getEnv("DB_URL", "jdbc:postgresql://db:5432/factory");
        String dbUser = getEnv("DB_USER", "factory");
        String dbPassword = getEnv("DB_PASSWORD", "factory123");
        String mqttUrl = getEnv("MQTT_URL", "tcp://broker:1883");
        int port = Integer.parseInt(getEnv("PORT", "8080"));

        Database db = new Database(dbUrl, dbUser, dbPassword);
        db.connect();

        MqttService mqtt = new MqttService(mqttUrl, db);
        mqtt.connect();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler(db, mqtt));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Сервер управления запущен на порту " + port);
    }

    private String getEnv(String name, String defaultValue){
        String value = System.getenv(name);
        if (value == null || value.isEmpty()){
            return defaultValue;
        }
        return value;
    }
}
