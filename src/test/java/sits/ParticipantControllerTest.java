package sits;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import sits.participant.AlwaysDefect;
import sits.remote.ParticipantController;
import sits.remote.dto.GameHistoryDTO;
import sits.remote.dto.RoundResultDTO;

// Tests for the HTTP controller that exposes a participant over the network.
class ParticipantControllerTest {

    @Test
    void delegatesToHostedParticipant() {
        // Checks that the controller forwards calls to the participant it wraps.
        ParticipantController controller = new ParticipantController(new AlwaysDefect());

        GameHistoryDTO dto = new GameHistoryDTO(
                "A",
                "B",
                List.of(new RoundResultDTO("COOPERATE", "DEFECT", 0, 5))
        );

        assertEquals("AlwaysDefect", controller.getName());
        assertEquals("DEFECT", controller.getAction(dto));
        assertDoesNotThrow(controller::reset);
    }
}
