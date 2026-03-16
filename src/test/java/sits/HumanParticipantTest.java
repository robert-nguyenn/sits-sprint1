package sits;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.participant.HumanParticipant;

class HumanParticipantTest {

    @Test
    void noInputFallsBackToCooperate() {
        Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.COOPERATE, action);
    }

    @Test
    void acceptsFullWordLowercaseInput() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("defect\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.DEFECT, action);
    }

    @Test
    void acceptsCooperateInput() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("C\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.COOPERATE, action);
        assertEquals("Human", participant.getName());
    }

    @Test
    void retriesAfterInvalidInputThenAcceptsDefect() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("maybe\nD\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.DEFECT, action);
        assertDoesNotThrow(participant::reset);
    }
    
    @Test
    void oneArgConstructorSetsName() {
        HumanParticipant participant = new HumanParticipant("HumanOneArg");
        assertEquals("HumanOneArg", participant.getName());
    }
}
