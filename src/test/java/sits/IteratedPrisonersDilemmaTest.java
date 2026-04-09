package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.game.GameHistory;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.Participant;

// Tests for the Prisoner's Dilemma game rules and scoring.
public class IteratedPrisonersDilemmaTest {

    @Test
    void cooperateVsCooperate_scoresAreCorrect() {
        // Checks the normal mutual-cooperation payoff.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(5);

        var result = game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(15, result.getTotalScoreP1());
        assertEquals(15, result.getTotalScoreP2());
        assertEquals("TIE", result.getWinner());
    }

    @Test
    void cooperateVsDefect_scoresAreCorrect() {
        // Checks the case where player 1 cooperates and player 2 defects.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(4);

        var result = game.play(new AlwaysCooperate(), new AlwaysDefect());

        assertEquals(0, result.getTotalScoreP1());
        assertEquals(20, result.getTotalScoreP2());
        assertEquals("AlwaysDefect", result.getWinner());
    }

    @Test
    void defectVsCooperate_scoresAreCorrect() {
        // Checks the mirrored case where player 1 defects and player 2 cooperates.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(4);

        var result = game.play(new AlwaysDefect(), new AlwaysCooperate());

        assertEquals(20, result.getTotalScoreP1());
        assertEquals(0, result.getTotalScoreP2());
        assertEquals("AlwaysDefect", result.getWinner());
    }

    @Test
    void defectVsDefect_scoresAreCorrect() {
        // Checks the mutual-defection payoff.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(3);

        var result = game.play(new AlwaysDefect(), new AlwaysDefect());

        assertEquals(3, result.getTotalScoreP1());
        assertEquals(3, result.getTotalScoreP2());
        assertEquals("TIE", result.getWinner());
    }

    @Test
    void constructor_rejectsZeroOrNegativeRounds() {
        // Checks that the game refuses invalid round counts.
        assertThrows(IllegalArgumentException.class, () -> new IteratedPrisonersDilemma(0));
        assertThrows(IllegalArgumentException.class, () -> new IteratedPrisonersDilemma(-2));
    }

    @Test
    void badActionType_throwsException() {
        // Checks that the game rejects actions it does not understand.
        Participant badParticipant = new Participant() {
            @Override
            public String getName() {
                return "BadParticipant";
            }

            @Override
            public Action chooseAction(GameHistory history) {
                return new Action() {
                    @Override
                    public String getLabel() {
                        return "BAD";
                    }
                };
            }

            @Override
            public void reset() {
            }
        };

        badParticipant.reset();

        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> game.play(badParticipant, new AlwaysCooperate())
        );

        assertTrue(ex.getMessage().contains("Bad actions"));
    }
}