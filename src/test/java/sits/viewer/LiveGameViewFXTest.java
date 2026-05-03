package sits.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sits.remote.dto.MoveEventDTO;

@ExtendWith(ApplicationExtension.class)
public class LiveGameViewFXTest {

    private LiveGameController controller;
    private StreamingFakeConnection connection;
    private Stage testStage;

    @Start
    private void start(Stage stage) throws Exception {
        this.testStage = stage;
        connection = new StreamingFakeConnection();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/live_game.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setServerConnection(connection);

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("SITS Viewer - Live Game");
        stage.show();
    }

    private void watchOnFxThread(String tournamentId) {
        Platform.runLater(() -> controller.watchTournament(tournamentId));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void streamedMovesAreAppendedToTextArea(FxRobot robot) {
        watchOnFxThread("t1");

        TextArea area = robot.lookup("#moveTextArea").queryAs(TextArea.class);
        String text = area.getText();
        org.assertj.core.api.Assertions.assertThat(text)
                .contains("Connecting to tournament")
                .contains("Round 1")
                .contains("COOPERATE")
                .contains("DEFECT")
                .contains("GAME OVER")
                .contains("TOURNAMENT COMPLETE")
                .contains("Stream ended");
    }

    @Test
    void multipleMoveEventsArriveInOrder(FxRobot robot) {
        connection.scriptedMoves.add(makeMove(1, "COOPERATE", "COOPERATE", 3, 3));
        connection.scriptedMoves.add(makeMove(2, "DEFECT", "COOPERATE", 5, 0));
        connection.scriptedMoves.add(makeMove(3, "DEFECT", "DEFECT", 1, 1));

        watchOnFxThread("t1");

        TextArea area = robot.lookup("#moveTextArea").queryAs(TextArea.class);
        String text = area.getText();
        int round1 = text.indexOf("Round 1");
        int round2 = text.indexOf("Round 2");
        int round3 = text.indexOf("Round 3");
        org.assertj.core.api.Assertions.assertThat(round1).isGreaterThanOrEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(round2).isGreaterThan(round1);
        org.assertj.core.api.Assertions.assertThat(round3).isGreaterThan(round2);
    }

    @Test
    void backButtonReturnsToLobby(FxRobot robot) {
        watchOnFxThread("t1");

        // Fire the back button directly through testStage's scene rather than
        // robot.clickOn so we don't rely on TestFX cross-window targeting.
        javafx.scene.control.Button back =
                (javafx.scene.control.Button) testStage.getScene().lookup("#backButton");
        org.junit.jupiter.api.Assertions.assertNotNull(back, "backButton should be in the live scene");
        Platform.runLater(back::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // The same stage now hosts the lobby scene.
        org.junit.jupiter.api.Assertions.assertNotNull(
                testStage.getScene().lookup("#tournamentList"),
                "tournamentList should be in the lobby scene after Back is pressed");
    }

    @Test
    void backButtonCancelsAnActiveStream(FxRobot robot) {
        // Use a connection whose stream future never completes so handleBack
        // exercises the streamFuture.cancel(true) branch.
        IncompleteStreamConnection stalled = new IncompleteStreamConnection();
        Platform.runLater(() -> controller.setServerConnection(stalled));
        WaitForAsyncUtils.waitForFxEvents();

        watchOnFxThread("t1");
        org.junit.jupiter.api.Assertions.assertFalse(stalled.future.isDone(),
                "stream future should still be pending before Back is clicked");

        // Fire the back button directly through testStage's scene.
        javafx.scene.control.Button back =
                (javafx.scene.control.Button) testStage.getScene().lookup("#backButton");
        Platform.runLater(back::fire);
        WaitForAsyncUtils.waitForFxEvents();

        org.junit.jupiter.api.Assertions.assertTrue(stalled.future.isCancelled(),
                "stream future should be cancelled by handleBack");
    }

    private static final class IncompleteStreamConnection extends ServerConnection {
        volatile CompletableFuture<Void> future;

        IncompleteStreamConnection() {
            super("http://localhost:0");
        }

        @Override
        public List<TournamentInfo> fetchTournaments() {
            return List.of();
        }

        @Override
        public CompletableFuture<Void> streamMoves(String tournamentId,
                                                   Consumer<MoveEventDTO> onEvent,
                                                   Runnable onDone) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            future = f;
            return f;
        }
    }

    private MoveEventDTO makeMove(int round, String a1, String a2, int p1, int p2) {
        MoveEventDTO move = new MoveEventDTO();
        move.setType("MOVE");
        move.setRoundNumber(round);
        move.setP1Name("A");
        move.setP2Name("B");
        move.setAction1(a1);
        move.setAction2(a2);
        move.setPayoffP1(p1);
        move.setPayoffP2(p2);
        return move;
    }

    private static final class StreamingFakeConnection extends ServerConnection {
        final List<MoveEventDTO> scriptedMoves = new ArrayList<>();

        StreamingFakeConnection() {
            super("http://localhost:0");
        }

        @Override
        public List<TournamentInfo> fetchTournaments() {
            return List.of();
        }

        @Override
        public CompletableFuture<Void> streamMoves(String tournamentId,
                                                   Consumer<MoveEventDTO> onEvent,
                                                   Runnable onDone) {
            List<MoveEventDTO> events = new ArrayList<>();
            if (scriptedMoves.isEmpty()) {
                events.add(makeDefaultMove());
            } else {
                events.addAll(scriptedMoves);
            }
            events.add(makeGameOver());
            events.add(makeTournamentOver());

            Platform.runLater(() -> {
                for (MoveEventDTO event : events) {
                    onEvent.accept(event);
                }
                onDone.run();
            });

            return CompletableFuture.completedFuture(null);
        }

        private MoveEventDTO makeDefaultMove() {
            MoveEventDTO move = new MoveEventDTO();
            move.setType("MOVE");
            move.setRoundNumber(1);
            move.setP1Name("A");
            move.setP2Name("B");
            move.setAction1("COOPERATE");
            move.setAction2("DEFECT");
            move.setPayoffP1(0);
            move.setPayoffP2(5);
            return move;
        }

        private MoveEventDTO makeGameOver() {
            MoveEventDTO event = new MoveEventDTO();
            event.setType("GAME_OVER");
            event.setP1Name("A");
            event.setP2Name("B");
            event.setWinner("B");
            event.setTotalScoreP1(0);
            event.setTotalScoreP2(5);
            return event;
        }

        private MoveEventDTO makeTournamentOver() {
            MoveEventDTO event = new MoveEventDTO();
            event.setType("TOURNAMENT_OVER");
            return event;
        }
    }
}
