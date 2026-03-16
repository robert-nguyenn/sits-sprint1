package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;

public class ObserverWritingTest {

    @Test
    void observer_receivesMoveEventsAndGameOver() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(3);
        FakeObserver observer = new FakeObserver();

        game.addObserver(observer);
        game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(3, observer.moves.size());
        assertEquals(1, observer.games.size());
        assertEquals("TIE", observer.games.get(0).getWinner());
    }

    @Test
    void observer_receivesRoundEventsInOrder() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(3);
        FakeObserver observer = new FakeObserver();

        game.addObserver(observer);
        game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(3, observer.moves.size());
        assertEquals(1, observer.moves.get(0).getRoundResult().getRoundNumber());
        assertEquals(2, observer.moves.get(1).getRoundResult().getRoundNumber());
        assertEquals(3, observer.moves.get(2).getRoundResult().getRoundNumber());
    }

    @Test
    void removedObserver_noLongerReceivesEvents() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(2);
        FakeObserver observer = new FakeObserver();

        game.addObserver(observer);
        game.removeObserver(observer);

        game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(0, observer.moves.size());
        assertEquals(0, observer.games.size());
    }
}