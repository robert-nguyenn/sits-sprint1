package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.dto.RoundResultDTO;

// Tests for the round-result DTO used in remote payloads.
class RoundResultDTOTest {

    @Test
    void defaultConstructorAndSettersWork() {
        // Checks that the empty constructor + setters store values correctly.
        RoundResultDTO dto = new RoundResultDTO();

        dto.setActionP1("COOPERATE");
        dto.setActionP2("DEFECT");
        dto.setPayoffP1(0);
        dto.setPayoffP2(5);

        assertEquals("COOPERATE", dto.getActionP1());
        assertEquals("DEFECT", dto.getActionP2());
        assertEquals(0, dto.getPayoffP1());
        assertEquals(5, dto.getPayoffP2());
    }

    @Test
    void constructorAndAccessorsWork() {
        // Checks that the full constructor and getters work, and fields can still be updated.
        RoundResultDTO dto = new RoundResultDTO("COOPERATE", "DEFECT", 0, 5);

        assertEquals("COOPERATE", dto.getActionP1());
        assertEquals("DEFECT", dto.getActionP2());
        assertEquals(0, dto.getPayoffP1());
        assertEquals(5, dto.getPayoffP2());

        dto.setActionP1("DEFECT");
        dto.setActionP2("COOPERATE");
        dto.setPayoffP1(5);
        dto.setPayoffP2(0);

        assertEquals("DEFECT", dto.getActionP1());
        assertEquals("COOPERATE", dto.getActionP2());
        assertEquals(5, dto.getPayoffP1());
        assertEquals(0, dto.getPayoffP2());
    }
}
