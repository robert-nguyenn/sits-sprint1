package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.app.Main;

// Small tests for the app entry point.
public class MainTest {

    @Test
    void main_runsWithoutThrowing() {
        // Checks that the program can start normally with no arguments.
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }

    @Test
    void main_class_canBeConstructed() {
        // Checks that the Main class itself can be created without issues.
        assertDoesNotThrow(() -> new Main());
    }
}