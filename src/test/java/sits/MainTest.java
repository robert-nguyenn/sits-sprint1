package sits;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

import sits.app.Main;

public class MainTest {

    @Test
    void main_runsWithoutThrowing() {
        assertDoesNotThrow(() -> new Main());
    }

    @Test
    void main_class_canBeConstructed() {
        assertDoesNotThrow(() -> new Main());
    }
}