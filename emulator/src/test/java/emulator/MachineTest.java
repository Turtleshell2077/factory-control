package emulator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MachineTest {
    @Test
    public void newMachineIsStopped(){
        Machine m = new Machine(1, "Станок", 50);
        assertEquals("STOPPED", m.getState());
        m.execute("start", 0);
        assertEquals("RUNNING", m.getState());
    }

    @Test
    public void stoppedMachineDoesNotProduce(){
        Machine m = new Machine(1, "Станок", 50);
        for (int i = 0; i < 10; i++){
            m.tick();
        }
        assertEquals(0, m.getProduced());
    }

    @Test
    public void runningMachineProduces(){
        Machine m = new Machine(1, "Станок", 60);
        m.execute("start", 0);
        for (int i = 0; i < 5; i++){
            m.tick();
        }
        assertEquals(15, m.getProduced());
    }

    @Test
    public void overheatTurnsOnAlarm(){
        Machine m = new Machine(1, "Станок", 100);
        m.execute("start", 0);
        String event = null;
        for (int i = 0; i < 200 && event == null; i++){
            event = m.tick();
        }
        assertEquals("ALARM", m.getState());
        assertTrue(event.contains("перегрев"));
    }

    @Test
    public void cannotStartInAlarm(){
        Machine m = new Machine(1, "Станок", 100);
        m.execute("start", 0);
        for (int i = 0; i < 200; i++){
            m.tick();
        }
        String answer = m.execute("start", 0);
        assertEquals("ALARM", m.getState());
        assertTrue(answer.contains("невозможен"));
    }

    @Test
    public void resetOnlyAfterCooling(){
        Machine m = new Machine(1, "Станок", 100);
        m.execute("start", 0);
        for (int i = 0; i < 200 && !m.getState().equals("ALARM"); i++){
            m.tick();
        }
        m.execute("reset", 0);
        assertEquals("ALARM", m.getState());

        for (int i = 0; i < 200; i++){
            m.tick();
        }
        m.execute("reset", 0);
        assertEquals("STOPPED", m.getState());
    }

    @Test
    public void speedCommandChangesSpeed(){
        Machine m = new Machine(1, "Станок", 50);
        m.execute("speed", 80);
        assertEquals(80, m.getSpeed());
    }
}
