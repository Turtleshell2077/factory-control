package factory;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttService implements MqttCallbackExtended{
    // $share/backend/ - общая подписка: каждое сообщение получает только одна копия backend
    private static final String TELEMETRY_TOPIC = "$share/backend/factory/+/telemetry";
    private static final String EVENT_TOPIC = "$share/backend/factory/+/event";
    private static final int MAX_ATTEMPTS = 30;
    private static final int WAIT_MS = 2000;

    private MqttClient client;
    private Database db;
    private Gson gson = new Gson();

    public MqttService(String url, Database db) throws MqttException{
        this.db = db;
        String clientId = "backend-" + System.getenv().getOrDefault("HOSTNAME", "local");
        client = new MqttClient(url, clientId, new MemoryPersistence());
        client.setCallback(this);
    }

    public void connect() throws InterruptedException{
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        for (int i = 1; i <= MAX_ATTEMPTS; i++){
            try{
                client.connect(options);
                subscribe();
                return;
            }
            catch(MqttException e){
                System.out.println("Брокер пока недоступен, попытка " + i + " из " + MAX_ATTEMPTS);
                Thread.sleep(WAIT_MS);
            }
        }
        throw new RuntimeException("Не удалось подключиться к брокеру сообщений");
    }

    private void subscribe(){
        try{
            client.subscribe(TELEMETRY_TOPIC);
            client.subscribe(EVENT_TOPIC);
            System.out.println("Подписка на телеметрию и события оформлена");
        }
        catch(MqttException e){
            System.out.println("Не удалось подписаться: " + e.getMessage());
        }
    }

    public boolean isConnected(){
        return client.isConnected();
    }

    public void sendCommand(int machineId, Command command) throws MqttException{
        String topic = "factory/" + machineId + "/command";
        MqttMessage message = new MqttMessage(gson.toJson(command).getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        client.publish(topic, message);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI){
        System.out.println("Подключение к брокеру " + serverURI + " установлено");
        // после переподключения подписки пропадают, оформляем их заново в отдельном потоке
        if (reconnect){
            new Thread(this::subscribe).start();
        }
    }

    @Override
    public void connectionLost(Throwable cause){
        System.out.println("Связь с брокером потеряна: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        // топик выглядит так: factory/3/telemetry
        try{
            String text = new String(message.getPayload(), StandardCharsets.UTF_8);
            int machineId = Integer.parseInt(topic.split("/")[1]);
            if (topic.endsWith("/telemetry")){
                Telemetry telemetry = gson.fromJson(text, Telemetry.class);
                db.updateMachine(machineId, telemetry);
            }
            else if (topic.endsWith("/event")){
                db.addEvent(machineId, "оборудование", text);
            }
        }
        catch(Exception e){
            System.out.println("Не удалось обработать сообщение из " + topic + ": " + e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
