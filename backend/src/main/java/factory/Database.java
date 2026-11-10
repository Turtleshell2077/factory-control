package factory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
    private static final int MAX_ATTEMPTS = 30;
    private static final int WAIT_MS = 2000;

    private String url;
    private String user;
    private String password;
    private Connection connection;

    public Database(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // база может подняться позже сервера, поэтому пробуем несколько раз
    public void connect() throws InterruptedException{
        for (int i = 1; i <= MAX_ATTEMPTS; i++){
            try{
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Подключение к базе данных установлено");
                return;
            }
            catch(SQLException e){
                System.out.println("База данных пока недоступна, попытка " + i + " из " + MAX_ATTEMPTS);
                Thread.sleep(WAIT_MS);
            }
        }
        throw new RuntimeException("Не удалось подключиться к базе данных");
    }

    private void checkConnection() throws SQLException{
        if (connection == null || !connection.isValid(2)){
            System.out.println("Соединение с базой потеряно, подключаемся заново");
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public synchronized boolean isAlive(){
        try{
            checkConnection();
            return true;
        }
        catch(SQLException e){
            return false;
        }
    }

    public synchronized ArrayList<Machine> getMachines() throws SQLException{
        checkConnection();
        ArrayList<Machine> machines = new ArrayList<>();
        String sql = "SELECT id, name, type, state, temperature, speed, produced, updated_at FROM machines ORDER BY id";
        try (PreparedStatement st = connection.prepareStatement(sql)){
            ResultSet rs = st.executeQuery();
            while (rs.next()){
                machines.add(new Machine(rs.getInt("id"), rs.getString("name"), rs.getString("type"),
                        rs.getString("state"), rs.getDouble("temperature"), rs.getInt("speed"),
                        rs.getInt("produced"), rs.getTimestamp("updated_at").toInstant().toString()));
            }
        }
        return machines;
    }

    public synchronized boolean machineExists(int id) throws SQLException{
        checkConnection();
        try (PreparedStatement st = connection.prepareStatement("SELECT id FROM machines WHERE id = ?")){
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            return rs.next();
        }
    }

    public synchronized void updateMachine(int id, Telemetry t) throws SQLException{
        checkConnection();
        String sql = "UPDATE machines SET state = ?, temperature = ?, speed = ?, produced = ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)){
            st.setString(1, t.getState());
            st.setDouble(2, t.getTemperature());
            st.setInt(3, t.getSpeed());
            st.setInt(4, t.getProduced());
            st.setInt(5, id);
            st.executeUpdate();
        }
    }

    public synchronized void addEvent(int machineId, String source, String message) throws SQLException{
        checkConnection();
        String sql = "INSERT INTO events (machine_id, source, message) VALUES (?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)){
            st.setInt(1, machineId);
            st.setString(2, source);
            st.setString(3, message);
            st.executeUpdate();
        }
    }

    public synchronized ArrayList<Event> getEvents(int limit) throws SQLException{
        checkConnection();
        ArrayList<Event> events = new ArrayList<>();
        String sql = "SELECT e.id, e.created_at, m.name, e.source, e.message FROM events e "
                + "JOIN machines m ON m.id = e.machine_id ORDER BY e.id DESC LIMIT ?";
        try (PreparedStatement st = connection.prepareStatement(sql)){
            st.setInt(1, limit);
            ResultSet rs = st.executeQuery();
            while (rs.next()){
                events.add(new Event(rs.getInt("id"), rs.getTimestamp("created_at").toInstant().toString(),
                        rs.getString("name"), rs.getString("source"), rs.getString("message")));
            }
        }
        return events;
    }
}
