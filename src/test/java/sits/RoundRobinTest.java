package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;
import sits.tournament.RoundRobin;
import sits.tournament.TournamentResult;

// Tests for the round-robin tournament format end-to-end behavior.
public class RoundRobinTest {

    @Test
    void roundRobin_runsAllMatches_andFiresTournamentOver() {
        // Checks that all pairings run, observers get events, and scores/rankings are correct.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(1);
        FakeObserver observer = new FakeObserver();
        game.addObserver(observer);

        RoundRobin rr = new RoundRobin();
        TournamentResult result = rr.run(
                List.of(new TitForTat(), new AlwaysDefect(), new AlwaysCooperate()),
                game
        );

        assertEquals(3, observer.moves.size());
        assertEquals(3, observer.games.size());
        assertNotNull(observer.tournament);

        assertEquals(10, result.getScore("AlwaysDefect"));
        assertEquals(3, result.getScore("TitForTat"));
        assertEquals(3, result.getScore("AlwaysCooperate"));
        assertEquals("AlwaysDefect", result.getRankings().get(0));
    }
}