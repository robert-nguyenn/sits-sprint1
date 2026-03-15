package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;

public class ObserverWiringTest {

    @Test
    void observer_getsMoveEventsAndGameOver() {
        var game = new IteratedPrisonersDilemma(3);
        var obs = new FakeObserver();
        game.addObserver(obs);

        game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(3, obs.moves.size());
        assertEquals(1, obs.games.size());
    }
}