package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;

public class TitForTatTest {

    @Test
    void titForTat_cooperatesFirst_thenMirrorsDefect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(2);
        FakeObserver observer = new FakeObserver();
        game.addObserver(observer);

        game.play(new TitForTat(), new AlwaysDefect());

        assertEquals(2, observer.moves.size());
        assertEquals("COOPERATE", observer.moves.get(0).getRoundResult().getActionP1().getLabel());
        assertEquals("DEFECT", observer.moves.get(1).getRoundResult().getActionP1().getLabel());
    }
}