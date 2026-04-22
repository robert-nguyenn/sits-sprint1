package sits.viewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

class LobbyControllerTest {

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
    void watchButtonEnabledOnlyForRunning() throws Exception {
        LobbyController controller = new LobbyController();
        ListView<TournamentInfo> list = new ListView<>();
        Button watchButton = new Button();

        runOnFxThreadAndWait(() -> {
            setField(controller, "tournamentList", list);
            setField(controller, "watchButton", watchButton);
            invokeInitialize(controller);

            list.getItems().addAll(
                new TournamentInfo("t1", "Tournament 1", "REGISTERING"),
                new TournamentInfo("t2", "Tournament 2", "RUNNING")
            );

            list.getSelectionModel().select(0);
            assertTrue(watchButton.isDisable());

            list.getSelectionModel().select(1);
            assertFalse(watchButton.isDisable());
        });
    }

    @Test
    void setServerConnectionLoadsTournaments() throws Exception {
        TestLobbyController controller = new TestLobbyController();
        ListView<TournamentInfo> list = new ListView<>();
        Button watchButton = new Button();

        runOnFxThreadAndWait(() -> {
            setField(controller, "tournamentList", list);
            setField(controller, "watchButton", watchButton);
            invokeInitialize(controller);

            controller.setServerConnection(new FakeServerConnection());
            assertEquals(2, list.getItems().size());
        });
    }

    @Test
    void handleWatchCallsWatchTournamentForRunning() throws Exception {
        TestLobbyController controller = new TestLobbyController();
        ListView<TournamentInfo> list = new ListView<>();
        Button watchButton = new Button();

        runOnFxThreadAndWait(() -> {
            setField(controller, "tournamentList", list);
            setField(controller, "watchButton", watchButton);
            invokeInitialize(controller);

            list.getItems().addAll(
                new TournamentInfo("t1", "Tournament 1", "REGISTERING"),
                new TournamentInfo("t2", "Tournament 2", "RUNNING")
            );
            list.getSelectionModel().select(1);
        });

        runOnFxThreadAndWait(() -> invokeHandleWatch(controller));

        assertTrue(controller.watchCalled);
        assertEquals("t2", controller.watchedId);
    }

    @Test
    void handleWatchIgnoresNonRunning() throws Exception {
        TestLobbyController controller = new TestLobbyController();
        ListView<TournamentInfo> list = new ListView<>();
        Button watchButton = new Button();

        runOnFxThreadAndWait(() -> {
            setField(controller, "tournamentList", list);
            setField(controller, "watchButton", watchButton);
            invokeInitialize(controller);

            list.getItems().add(new TournamentInfo("t1", "Tournament 1", "REGISTERING"));
            list.getSelectionModel().select(0);
        });

        runOnFxThreadAndWait(() -> invokeHandleWatch(controller));

        assertFalse(controller.watchCalled);
    }

    @Test
    void handleRefreshShowsErrorOnFailure() throws Exception {
        TestLobbyController controller = new TestLobbyController();
        ListView<TournamentInfo> list = new ListView<>();
        Button watchButton = new Button();

        runOnFxThreadAndWait(() -> {
            setField(controller, "tournamentList", list);
            setField(controller, "watchButton", watchButton);
            invokeInitialize(controller);
            controller.setServerConnection(new FailingServerConnection());
        });

        runOnFxThreadAndWait(() -> invokeHandleRefresh(controller));

        assertTrue(controller.errorMessage.startsWith("Failed to load tournaments:"));
    }

    private static void invokeInitialize(LobbyController controller) {
        try {
            Method method = LobbyController.class.getDeclaredMethod("initialize");
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeHandleWatch(LobbyController controller) {
        try {
            Method method = LobbyController.class.getDeclaredMethod("handleWatch", javafx.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new javafx.event.ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeHandleRefresh(LobbyController controller) {
        try {
            Method method = LobbyController.class.getDeclaredMethod("handleRefresh", javafx.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new javafx.event.ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = LobbyController.class.getDeclaredField(name);
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

    private static final class FakeServerConnection extends ServerConnection {
        FakeServerConnection() {
            super("http://localhost:8080");
        }

        @Override
        public List<TournamentInfo> fetchTournaments() {
            return List.of(
                new TournamentInfo("a", "Alpha", "REGISTERING"),
                new TournamentInfo("b", "Beta", "RUNNING")
            );
        }
    }

    private static final class FailingServerConnection extends ServerConnection {
        FailingServerConnection() {
            super("http://localhost:8080");
        }

        @Override
        public List<TournamentInfo> fetchTournaments() throws Exception {
            throw new Exception("boom");
        }
    }

    private static final class TestLobbyController extends LobbyController {
        private boolean watchCalled;
        private String watchedId;
        private String errorMessage;

        @Override
        protected void watchTournament(TournamentInfo tournament) {
            this.watchCalled = true;
            this.watchedId = tournament.id;
        }

        @Override
        protected void displayAlert(javafx.scene.control.Alert alert) {
            this.errorMessage = alert.getContentText();
        }
    }
}
