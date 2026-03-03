package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;
import sits.tournament.RoundRobin;

public class RoundRobinTest {

    @Test
    void roundRobin_threePlayers_runsThreeMatches_andFiresTournamentOver() {
        var game = new IteratedPrisonersDilemma(1); // 1 round per match to keep counts simple
        var obs = new FakeObserver();
        game.addObserver(obs);

        var rr = new RoundRobin();
        rr.run(List.of(new TitForTat(), new AlwaysDefect(), new AlwaysCooperate()), game);

        // 3 matches, 1 round each => 3 move events total
        assertEquals(3, obs.moves.size());
        assertNotNull(obs.tournament);
        assertTrue(obs.tournament.getScoresCopy().containsKey("AlwaysDefect"));
    }
}