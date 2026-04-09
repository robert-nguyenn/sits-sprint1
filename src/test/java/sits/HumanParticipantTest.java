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

// Tests for the human player that reads actions from a Scanner.
class HumanParticipantTest {

    @Test
    void noInputFallsBackToCooperate() {
        // Checks that empty input does not crash and defaults to cooperate.
        Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.COOPERATE, action);
    }

    @Test
    void acceptsFullWordLowercaseInput() {
        // Checks that lowercase full-word input is accepted correctly.
        Scanner scanner = new Scanner(new ByteArrayInputStream("defect\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.DEFECT, action);
    }

    @Test
    void acceptsCooperateInput() {
        // Checks that the short C input works and the participant keeps its name.
        Scanner scanner = new Scanner(new ByteArrayInputStream("C\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.COOPERATE, action);
        assertEquals("Human", participant.getName());
    }

    @Test
    void retriesAfterInvalidInputThenAcceptsDefect() {
        // Checks that bad input is ignored and the player keeps asking until it gets a valid move.
        Scanner scanner = new Scanner(new ByteArrayInputStream("maybe\nD\n".getBytes(StandardCharsets.UTF_8)));
        HumanParticipant participant = new HumanParticipant("Human", scanner);

        var action = participant.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.DEFECT, action);
        assertDoesNotThrow(participant::reset);
    }
    
    @Test
    void oneArgConstructorSetsName() {
        // Checks the simple constructor still stores the player's name.
        HumanParticipant participant = new HumanParticipant("HumanOneArg");
        assertEquals("HumanOneArg", participant.getName());
    }
}
