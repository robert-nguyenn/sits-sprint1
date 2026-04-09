package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.MoveEvent;
import sits.game.RoundResult;
import sits.observer.MoveLogger;
import sits.tournament.TournamentResult;

// Tests for the logger that writes round-by-round move details to a file.
public class MoveLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void moveLogger_writesSingleMoveLine() throws Exception {
        // Checks that one move gets written in the expected text format.
        Path file = tempDir.resolve("moves.txt");
        MoveLogger logger = new MoveLogger(file.toString());

        GameHistory history = new GameHistory("P1", "P2");
        RoundResult rr = new RoundResult(
                PrisonerAction.COOPERATE,
                PrisonerAction.DEFECT,
                0,
                5,
                1
        );
        history.addRound(rr);

        logger.onMoveMade(new MoveEvent(rr, history));
        logger.onTournamentOver(new TournamentResult(Map.of("P1", 0, "P2", 5)));

        String content = Files.readString(file);

        assertTrue(content.contains("Round 1: P1=COOPERATE, P2=DEFECT | (0,5)"));
        assertTrue(content.contains("TOURNAMENT OVER"));
    }

    @Test
    void moveLogger_writesMultipleMoveLinesInOrder() throws Exception {
        // Checks that multiple moves are written and kept in the right order.
        Path file = tempDir.resolve("moves_multi.txt");
        MoveLogger logger = new MoveLogger(file.toString());

        GameHistory history = new GameHistory("A", "B");

        RoundResult rr1 = new RoundResult(
                PrisonerAction.COOPERATE,
                PrisonerAction.COOPERATE,
                3,
                3,
                1
        );
        history.addRound(rr1);
        logger.onMoveMade(new MoveEvent(rr1, history));

        RoundResult rr2 = new RoundResult(
                PrisonerAction.DEFECT,
                PrisonerAction.COOPERATE,
                5,
                0,
                2
        );
        history.addRound(rr2);
        logger.onMoveMade(new MoveEvent(rr2, history));

        logger.onTournamentOver(new TournamentResult(Map.of("A", 8, "B", 3)));

        String content = Files.readString(file);

        assertTrue(content.contains("Round 1: A=COOPERATE, B=COOPERATE | (3,3)"));
        assertTrue(content.contains("Round 2: A=DEFECT, B=COOPERATE | (5,0)"));

        int firstIndex = content.indexOf("Round 1:");
        int secondIndex = content.indexOf("Round 2:");
        assertTrue(firstIndex >= 0);
        assertTrue(secondIndex > firstIndex);
    }

    @Test
    void onGameOver_doesNotWriteGameSummary() throws Exception {
        // Checks that move logging does not suddenly write a game summary.
        Path file = tempDir.resolve("moves_no_game_summary.txt");
        MoveLogger logger = new MoveLogger(file.toString());

        logger.onGameOver(null);
        logger.onTournamentOver(new TournamentResult(Map.of()));

        String content = Files.readString(file);

        assertFalse(content.contains("Game Over:"));
        assertTrue(content.contains("TOURNAMENT OVER"));
    }

    @Test
    void constructor_throwsWhenPathIsBad() {
        // Checks that an invalid output path fails
        assertThrows(RuntimeException.class, () -> {
            new MoveLogger(".");
        });
    }

    @Test
    void onMoveMade_wrapsIOExceptionAsRuntimeException() {
        // Checks that write errors get turned into a runtime exception.
        Path file = tempDir.resolve("moves_io_write.txt");
        MoveLogger logger = new MoveLogger(file.toString());

        logger.onTournamentOver(new TournamentResult(Map.of()));

        GameHistory history = new GameHistory("P1", "P2");
        RoundResult rr = new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.DEFECT, 0, 5, 1);
        history.addRound(rr);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> logger.onMoveMade(new MoveEvent(rr, history)));
        assertTrue(ex.getMessage().contains("MoveLogger write failed"));
    }

    @Test
    void onTournamentOver_wrapsIOExceptionAsRuntimeExceptionWhenAlreadyClosed() {
        // Checks that close errors are also wrapped as runtime exceptions.
        Path file = tempDir.resolve("moves_io_close.txt");
        MoveLogger logger = new MoveLogger(file.toString());

        logger.onTournamentOver(new TournamentResult(Map.of()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logger.onTournamentOver(new TournamentResult(Map.of())));
        assertTrue(ex.getMessage().contains("MoveLogger close failed"));
    }
}