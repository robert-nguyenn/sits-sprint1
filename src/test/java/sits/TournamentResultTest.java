package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import sits.tournament.TournamentResult;

public class TournamentResultTest {

    @Test
    void getScore_returnsStoredValues() {
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3,
                "Cara", 5
        ));

        assertEquals(7, result.getScore("Alice"));
        assertEquals(3, result.getScore("Bob"));
        assertEquals(5, result.getScore("Cara"));
    }

    @Test
    void getScore_returnsZeroForMissingName() {
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3
        ));

        assertEquals(0, result.getScore("Missing"));
    }

    @Test
    void getRankings_returnsHighestFirst() {
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3,
                "Cara", 5
        ));

        List<String> rankings = result.getRankings();

        assertEquals("Alice", rankings.get(0));
        assertEquals("Cara", rankings.get(1));
        assertEquals("Bob", rankings.get(2));
    }

    @Test
    void getScoresCopy_returnsDefensiveCopy() {
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3
        ));

        Map<String, Integer> copy = result.getScoresCopy();
        copy.put("Alice", 999);

        assertEquals(7, result.getScore("Alice"));
    }
}