package factory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CommandTest {
    @Test
    public void simpleCommandsAreValid(){
        assertTrue(new Command("start", 0).isValid());
        assertTrue(new Command("stop", 0).isValid());
        assertTrue(new Command("reset", 0).isValid());
    }

    @Test
    public void unknownCommandIsNotValid(){
        assertFalse(new Command("explode", 0).isValid());
        assertFalse(new Command(null, 0).isValid());
    }

    @Test
    public void speedMustBeFromZeroToHundred(){
        assertTrue(new Command("speed", 0).isValid());
        assertTrue(new Command("speed", 100).isValid());
        assertFalse(new Command("speed", -5).isValid());
        assertFalse(new Command("speed", 150).isValid());
    }

    @Test
    public void describeSpeedCommand(){
        assertEquals("Оператор задал скорость 70 %", new Command("speed", 70).describe());
    }

    @Test
    public void machineIdFromPath(){
        assertEquals(3, ApiHandler.getMachineId("/api/machines/3/command"));
        assertEquals(-1, ApiHandler.getMachineId("/api/machines/abc/command"));
        assertEquals(-1, ApiHandler.getMachineId("/api/events"));
    }
}
