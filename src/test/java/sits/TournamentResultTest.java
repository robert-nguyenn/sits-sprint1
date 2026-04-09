package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import sits.tournament.TournamentResult;

// Tests for score lookup, ranking order, and defensive-copy behavior.
public class TournamentResultTest {

    @Test
    void getScore_returnsStoredValues() {
    // Checks that stored scores are returned correctly.
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
        // Checks that unknown names default to 0 instead of failing.
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3
        ));

        assertEquals(0, result.getScore("Missing"));
    }

    @Test
    void getRankings_returnsHighestFirst() {
        // Checks that rankings are sorted from highest score to lowest.
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
        // Checks that editing the returned map does not change internal state.
        //         getScoresCopy() should return a new map, not the real internal map.
        // So when test code changes the returned map (copy.put("Alice", 999)), it should NOT change the real score inside TournamentResult.
        // Final assert checks that:
        // internal score is still 7, not 999.
        TournamentResult result = new TournamentResult(Map.of(
                "Alice", 7,
                "Bob", 3
        ));

        Map<String, Integer> copy = result.getScoresCopy();
        copy.put("Alice", 999);

        assertEquals(7, result.getScore("Alice"));
    }
}