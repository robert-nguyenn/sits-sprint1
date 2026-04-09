package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.RoundResult;

// Tests for the game history object that stores rounds and player names.
public class GameHistoryTest {

    @Test
    void getLastRound_throwsWhenEmpty() {
        // Checks that asking for the last round before any rounds exist fails fast.
        GameHistory history = new GameHistory("P1", "P2");

        assertThrows(IllegalStateException.class, history::getLastRound);
    }

    @Test
    void addRound_andGetLastRound_work() {
        // Checks that rounds are stored in order and the latest one can be read back.
        GameHistory history = new GameHistory("P1", "P2");

        RoundResult first = new RoundResult(
                PrisonerAction.COOPERATE,
                PrisonerAction.DEFECT,
                0,
                5,
                1
        );

        RoundResult second = new RoundResult(
                PrisonerAction.DEFECT,
                PrisonerAction.DEFECT,
                1,
                1,
                2
        );

        history.addRound(first);
        history.addRound(second);

        assertEquals(2, history.getRounds().size());
        assertSame(second, history.getLastRound());
        assertEquals("P1", history.getP1Name());
        assertEquals("P2", history.getP2Name());
    }

    @Test
    void getRounds_returnsUnmodifiableList() {
        // Checks that callers can read the round list but cannot change it directly.
        GameHistory history = new GameHistory("P1", "P2");

        assertThrows(UnsupportedOperationException.class, () -> {
            history.getRounds().add(
                    new RoundResult(
                            PrisonerAction.COOPERATE,
                            PrisonerAction.COOPERATE,
                            3,
                            3,
                            1
                    )
            );
        });
    }
}