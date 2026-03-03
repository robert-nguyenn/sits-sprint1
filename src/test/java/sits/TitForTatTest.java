package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;

public class TitForTatTest {

    @Test
    void titForTat_cooperatesFirst_thenMirrors() {
        var game = new IteratedPrisonersDilemma(2);
        var obs = new FakeObserver();
        game.addObserver(obs);

        game.play(new TitForTat(), new AlwaysDefect());

        assertEquals(2, obs.moves.size());
        assertEquals("COOPERATE", obs.moves.get(0).getRoundResult().getActionP1().getLabel());
        assertEquals("DEFECT", obs.moves.get(1).getRoundResult().getActionP1().getLabel());
    }
}