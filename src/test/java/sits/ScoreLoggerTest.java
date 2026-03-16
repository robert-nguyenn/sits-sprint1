package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sits.game.GameResult;
import sits.observer.ScoreLogger;
import sits.tournament.TournamentResult;

public class ScoreLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void scoreLogger_writesGameOverAndTournamentResults() throws Exception {
        Path file = tempDir.resolve("scores.txt");
        ScoreLogger logger = new ScoreLogger(file.toString());

        GameResult result = new GameResult("A", "B", 10, 4, "A");
        logger.onGameOver(result);

        TournamentResult tournament = new TournamentResult(Map.of(
                "A", 10,
                "B", 4
        ));
        logger.onTournamentOver(tournament);

        String content = Files.readString(file);

        assertTrue(content.contains("Game Over: A vs B total (10,4) winner=A"));
        assertTrue(content.contains("Final Rankings:"));
        assertTrue(content.contains("A: 10"));
        assertTrue(content.contains("B: 4"));
    }

    @Test
    void scoreLogger_writesMultipleGameResults() throws Exception {
        Path file = tempDir.resolve("scores_multi.txt");
        ScoreLogger logger = new ScoreLogger(file.toString());

        logger.onGameOver(new GameResult("A", "B", 6, 2, "A"));
        logger.onGameOver(new GameResult("A", "C", 3, 3, "TIE"));
        logger.onTournamentOver(new TournamentResult(Map.of(
                "A", 9,
                "B", 2,
                "C", 3
        )));

        String content = Files.readString(file);

        assertTrue(content.contains("Game Over: A vs B total (6,2) winner=A"));
        assertTrue(content.contains("Game Over: A vs C total (3,3) winner=TIE"));
        assertTrue(content.contains("Final Rankings:"));
    }

    @Test
    void onMoveMade_doesNotWriteMoveLines() throws Exception {
        Path file = tempDir.resolve("scores_no_moves.txt");
        ScoreLogger logger = new ScoreLogger(file.toString());

        logger.onMoveMade(null);
        logger.onTournamentOver(new TournamentResult(Map.of()));

        String content = Files.readString(file);

        assertFalse(content.contains("Round 1:"));
        assertTrue(content.contains("Final Rankings:"));
    }

    @Test
    void constructor_throwsWhenPathIsBad() {
        assertThrows(RuntimeException.class, () -> {
            new ScoreLogger(".");
        });
    }

    @Test
    void onGameOver_wrapsIOExceptionAsRuntimeException() {
        Path file = tempDir.resolve("scores_io_write.txt");
        ScoreLogger logger = new ScoreLogger(file.toString());

        logger.onTournamentOver(new TournamentResult(Map.of()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logger.onGameOver(new GameResult("A", "B", 1, 2, "B")));
        assertTrue(ex.getMessage().contains("ScoreLogger write failed"));
    }

    @Test
    void onTournamentOver_wrapsIOExceptionAsRuntimeExceptionWhenAlreadyClosed() {
        Path file = tempDir.resolve("scores_io_close.txt");
        ScoreLogger logger = new ScoreLogger(file.toString());

        logger.onTournamentOver(new TournamentResult(Map.of()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logger.onTournamentOver(new TournamentResult(Map.of())));
        assertTrue(ex.getMessage().contains("ScoreLogger close failed"));
    }
}