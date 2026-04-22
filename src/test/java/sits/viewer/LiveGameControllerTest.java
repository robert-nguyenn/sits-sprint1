package sits.viewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import sits.remote.dto.MoveEventDTO;

class LiveGameControllerTest {

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // JavaFX toolkit already initialized
        }
    }

    @Test
    void watchTournamentAppendsEvents() throws Exception {
        LiveGameController controller = new LiveGameController();
        TextArea area = new TextArea();

        runOnFxThreadAndWait(() -> {
            setField(controller, "moveTextArea", area);
            invokeInitialize(controller);
            controller.setServerConnection(new FakeServerConnection());
            controller.watchTournament("t1");
        });

        String text = area.getText();
        assertTrue(text.contains("Connecting to tournament"));
        assertTrue(text.contains("Round 1"));
        assertTrue(text.contains("GAME OVER"));
        assertTrue(text.contains("TOURNAMENT COMPLETE"));
        assertTrue(text.contains("Stream ended"));
    }

    @Test
    void handleBackCancelsStreamAndNavigates() throws Exception {
        TestLiveGameController controller = new TestLiveGameController();
        CompletableFuture<Void> future = new CompletableFuture<>();

        runOnFxThreadAndWait(() -> {
            setField(controller, "streamFuture", future);
            invokeHandleBack(controller);
        });

        assertTrue(future.isCancelled());
        assertTrue(controller.navigateCalled);
    }

    private static void invokeInitialize(LiveGameController controller) {
        try {
            Method method = LiveGameController.class.getDeclaredMethod("initialize");
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = LiveGameController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX task timed out");
        }
    }

    private static void invokeHandleBack(LiveGameController controller) {
        try {
            Method method = LiveGameController.class.getDeclaredMethod("handleBack", javafx.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new javafx.event.ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class FakeServerConnection extends ServerConnection {
        FakeServerConnection() {
            super("http://localhost:8080");
        }

        @Override
        public CompletableFuture<Void> streamMoves(String tournamentId,
                                                   Consumer<MoveEventDTO> onEvent,
                                                   Runnable onDone) {
            MoveEventDTO move = new MoveEventDTO();
            move.setType("MOVE");
            move.setRoundNumber(1);
            move.setP1Name("A");
            move.setP2Name("B");
            move.setAction1("COOPERATE");
            move.setAction2("DEFECT");
            move.setPayoffP1(0);
            move.setPayoffP2(5);
            onEvent.accept(move);

            MoveEventDTO gameOver = new MoveEventDTO();
            gameOver.setType("GAME_OVER");
            gameOver.setP1Name("A");
            gameOver.setP2Name("B");
            gameOver.setWinner("B");
            gameOver.setTotalScoreP1(0);
            gameOver.setTotalScoreP2(5);
            onEvent.accept(gameOver);

            MoveEventDTO tournamentOver = new MoveEventDTO();
            tournamentOver.setType("TOURNAMENT_OVER");
            onEvent.accept(tournamentOver);

            onDone.run();
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class TestLiveGameController extends LiveGameController {
        private boolean navigateCalled;

        @Override
        protected void navigateToLobby() {
            this.navigateCalled = true;
        }
    }
}
