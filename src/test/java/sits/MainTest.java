package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.app.Main;

public class MainTest {

    @Test
    void main_runsWithoutThrowing() {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }

    @Test
    void main_class_canBeConstructed() {
        assertDoesNotThrow(() -> new Main());
    }
}