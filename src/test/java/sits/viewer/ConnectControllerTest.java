package sits.viewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

class ConnectControllerTest {

    @BeforeAll
    static void initFx() throws Exception {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // JavaFX toolkit already initialized
        }
    }

    @Test
    void validInputNavigatesToLobby() throws Exception {
        TestConnectController controller = new TestConnectController();

        runOnFxThreadAndWait(() -> {
            TextField ip = new TextField("127.0.0.1");
            TextField port = new TextField("8080");
            setField(controller, "ipTextField", ip);
            setField(controller, "portTextField", port);
        });

        runOnFxThreadAndWait(() -> invokeHandleConnect(controller));

        assertTrue(controller.navigateCalled);
        assertEquals("http://127.0.0.1:8080", controller.lastServerUrl);
        assertNull(controller.errorMessage);
    }

    @Test
    void emptyInputShowsError() throws Exception {
        TestConnectController controller = new TestConnectController();

        runOnFxThreadAndWait(() -> {
            TextField ip = new TextField("");
            TextField port = new TextField("");
            setField(controller, "ipTextField", ip);
            setField(controller, "portTextField", port);
        });

        runOnFxThreadAndWait(() -> invokeHandleConnect(controller));

        assertEquals("Please enter both IP and port", controller.errorMessage);
    }

    @Test
    void invalidPortShowsError() throws Exception {
        TestConnectController controller = new TestConnectController();

        runOnFxThreadAndWait(() -> {
            TextField ip = new TextField("127.0.0.1");
            TextField port = new TextField("not-a-number");
            setField(controller, "ipTextField", ip);
            setField(controller, "portTextField", port);
        });

        runOnFxThreadAndWait(() -> invokeHandleConnect(controller));

        assertEquals("Port must be a valid number", controller.errorMessage);
    }

    @Test
    void outOfRangePortShowsError() throws Exception {
        TestConnectController controller = new TestConnectController();

        runOnFxThreadAndWait(() -> {
            TextField ip = new TextField("127.0.0.1");
            TextField port = new TextField("70000");
            setField(controller, "ipTextField", ip);
            setField(controller, "portTextField", port);
        });

        runOnFxThreadAndWait(() -> invokeHandleConnect(controller));

        assertEquals("Port must be between 1 and 65535", controller.errorMessage);
    }

    @Test
    void createConnectionFailureShowsError() throws Exception {
        TestConnectController controller = new TestConnectController();
        controller.throwOnCreate = true;

        runOnFxThreadAndWait(() -> {
            TextField ip = new TextField("127.0.0.1");
            TextField port = new TextField("8080");
            setField(controller, "ipTextField", ip);
            setField(controller, "portTextField", port);
        });

        runOnFxThreadAndWait(() -> invokeHandleConnect(controller));

        assertTrue(controller.errorMessage.startsWith("Failed to connect:"));
    }

    private static void invokeHandleConnect(ConnectController controller) {
        try {
            Method method = ConnectController.class.getDeclaredMethod("handleConnect", ActionEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = ConnectController.class.getDeclaredField(name);
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

    private static final class TestConnectController extends ConnectController {
        private boolean navigateCalled;
        private String lastServerUrl;
        private String errorMessage;
        private boolean throwOnCreate;

        @Override
        protected ServerConnection createConnection(String serverUrl) {
            this.lastServerUrl = serverUrl;
            if (throwOnCreate) {
                throw new RuntimeException("boom");
            }
            return new ServerConnection(serverUrl);
        }

        @Override
        protected void navigateToLobby(ServerConnection connection) {
            this.navigateCalled = true;
        }

        @Override
        protected void displayAlert(javafx.scene.control.Alert alert) {
            this.errorMessage = alert.getContentText();
        }
    }
}
