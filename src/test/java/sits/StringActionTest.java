package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.action.StringAction;

class StringActionTest {

    @Test
    void defaultConstructorThenSetterWorks() {
        StringAction action = new StringAction();
        action.setLabel("COOPERATE");

        assertEquals("COOPERATE", action.getLabel());
    }

    @Test
    void storesAndReturnsLabel() {
        StringAction action = new StringAction("COOPERATE");
        assertEquals("COOPERATE", action.getLabel());

        action.setLabel("DEFECT");
        assertEquals("DEFECT", action.getLabel());
    }
}
