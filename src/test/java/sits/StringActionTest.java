package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.action.StringAction;

// Tests for the simple action wrapper used in remote payloads.
class StringActionTest {

    @Test
    void defaultConstructorThenSetterWorks() {
        // Checks no-arg constructor + setter path (common in JSON deserialization).
        StringAction action = new StringAction();
        action.setLabel("COOPERATE");

        assertEquals("COOPERATE", action.getLabel());
    }

    @Test
    void storesAndReturnsLabel() {
        // Checks full constructor path and later updates through setter.
        StringAction action = new StringAction("COOPERATE");
        assertEquals("COOPERATE", action.getLabel());

        action.setLabel("DEFECT");
        assertEquals("DEFECT", action.getLabel());
    }
}
