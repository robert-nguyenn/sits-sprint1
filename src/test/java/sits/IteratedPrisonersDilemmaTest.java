package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.game.GameHistory;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.Participant;

public class IteratedPrisonersDilemmaTest {

    @Test
    void cooperateVsCooperate_scoresAreCorrect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(5);

        var result = game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(15, result.getTotalScoreP1());
        assertEquals(15, result.getTotalScoreP2());
        assertEquals("TIE", result.getWinner());
    }

    @Test
    void cooperateVsDefect_scoresAreCorrect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(4);

        var result = game.play(new AlwaysCooperate(), new AlwaysDefect());

        assertEquals(0, result.getTotalScoreP1());
        assertEquals(20, result.getTotalScoreP2());
        assertEquals("AlwaysDefect", result.getWinner());
    }

    @Test
    void defectVsCooperate_scoresAreCorrect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(4);

        var result = game.play(new AlwaysDefect(), new AlwaysCooperate());

        assertEquals(20, result.getTotalScoreP1());
        assertEquals(0, result.getTotalScoreP2());
        assertEquals("AlwaysDefect", result.getWinner());
    }

    @Test
    void defectVsDefect_scoresAreCorrect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(3);

        var result = game.play(new AlwaysDefect(), new AlwaysDefect());

        assertEquals(3, result.getTotalScoreP1());
        assertEquals(3, result.getTotalScoreP2());
        assertEquals("TIE", result.getWinner());
    }

    @Test
    void constructor_rejectsZeroOrNegativeRounds() {
        assertThrows(IllegalArgumentException.class, () -> new IteratedPrisonersDilemma(0));
        assertThrows(IllegalArgumentException.class, () -> new IteratedPrisonersDilemma(-2));
    }

    @Test
    void badActionType_throwsException() {
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