package emulator;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

// эмулятор оборудования цеха: вместо настоящих станков с контроллерами
public class Main implements MqttCallbackExtended{
    private static final String COMMAND_TOPIC = "factory/+/command";
    private static final int TICK_MS = 2000;
    private static final int MAX_ATTEMPTS = 30;

    private ArrayList<Machine> machines = new ArrayList<>();
    private MqttClient client;
    private Gson gson = new Gson();

    public void main() throws Exception{
        String mqttUrl = System.getenv().getOrDefault("MQTT_URL", "tcp://broker:1883");

        machines.add(new Machine(1, "Токарный станок ЧПУ №1", 60));
        machines.add(new Machine(2, "Токарный станок ЧПУ №2", 70));
        machines.add(new Machine(3, "Фрезерный станок", 50));
        machines.add(new Machine(4, "Конвейер сборки", 40));
        for (int i = 0; i < 3; i++){
            machines.get(i).execute("start", 0);
        }

        client = new MqttClient(mqttUrl, "emulator", new MemoryPersistence());
        client.setCallback(this);
        connect();
        System.out.println("Эмулятор запущен, оборудования: " + machines.size());

        while (true){
            for (Machine m : machines){
                String event = m.tick();
                publish("factory/" + m.getId() + "/telemetry", gson.toJson(m.getTelemetry()));
                if (event != null){
                    System.out.println(m.getName() + ": " + event);
                    publish("factory/" + m.getId() + "/event", event);
                }
            }
            Thread.sleep(TICK_MS);
        }
    }

    private void connect() throws InterruptedException{
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
                Thread.sleep(TICK_MS);
            }
        }
        throw new RuntimeException("Не удалось подключиться к брокеру сообщений");
    }

    private void subscribe(){
        try{
            client.subscribe(COMMAND_TOPIC);
        }
        catch(MqttException e){
            System.out.println("Не удалось подписаться на команды: " + e.getMessage());
        }
    }

    private void publish(String topic, String text){
        try{
            client.publish(topic, new MqttMessage(text.getBytes(StandardCharsets.UTF_8)));
        }
        catch(MqttException e){
            System.out.println("Не удалось отправить сообщение в " + topic + ": " + e.getMessage());
        }
    }

    private Machine findMachine(int id){
        for (Machine m : machines){
            if (m.getId() == id){
                return m;
            }
        }
        return null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        try{
            int id = Integer.parseInt(topic.split("/")[1]);
            Machine machine = findMachine(id);
            if (machine == null){
                System.out.println("Команда для неизвестного оборудования: " + topic);
                return;
            }
            Command command = gson.fromJson(new String(message.getPayload(), StandardCharsets.UTF_8), Command.class);
            String event = machine.execute(command.getAction(), command.getValue());
            System.out.println(machine.getName() + ": получена команда " + command.getAction());
            if (event != null){
                publish("factory/" + id + "/event", event);
            }
        }
        catch(Exception e){
            System.out.println("Не удалось обработать команду из " + topic + ": " + e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI){
        System.out.println("Подключение к брокеру " + serverURI + " установлено");
        if (reconnect){
            new Thread(this::subscribe).start();
        }
    }

    @Override
    public void connectionLost(Throwable cause){
        System.out.println("Связь с брокером потеряна: " + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
