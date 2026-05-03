package sits.viewer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

@ExtendWith(ApplicationExtension.class)
public class ConnectViewFXTest {

    private static HttpServer server;
    private static int serverPort;

    @BeforeAll
    static void startBackingServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        serverPort = server.getAddress().getPort();

        server.createContext("/tournaments", exchange -> {
            String body = "[]";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.start();
    }

    @AfterAll
    static void stopBackingServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private Stage testStage;

    @Start
    private void start(Stage stage) throws Exception {
        this.testStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/connect.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("SITS Viewer - Connect");
        stage.show();
    }

    private void setIpAndPort(String ip, String port) {
        TextField ipField = (TextField) testStage.getScene().lookup("#ipTextField");
        TextField portField = (TextField) testStage.getScene().lookup("#portTextField");
        Platform.runLater(() -> {
            ipField.setText(ip);
            portField.setText(port);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void fireConnectButton() {
        Button connect = findButton(testStage.getScene().getRoot(), "Connect");
        Platform.runLater(connect::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Button findButton(Node root, String text) {
        for (Node n : root.lookupAll(".button")) {
            if (n instanceof Button b && text.equals(b.getText())) {
                return b;
            }
        }
        throw new AssertionError("button with text '" + text + "' not found");
    }

    private DialogPane findOpenDialog() {
        for (Window w : Window.getWindows()) {
            if (!w.isShowing() || w.getScene() == null) continue;
            Node root = w.getScene().getRoot();
            if (root instanceof DialogPane dp) return dp;
        }
        return null;
    }

    private DialogPane waitForOpenDialog() {
        for (int i = 0; i < 30; i++) {
            DialogPane dp = findOpenDialog();
            if (dp != null) return dp;
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    private void fireConnectAsync() {
        // Fire without waitForFxEvents because the click handler calls
        // alert.showAndWait() which blocks the FX thread until OK is clicked.
        Button connect = findButton(testStage.getScene().getRoot(), "Connect");
        Platform.runLater(connect::fire);
    }

    private void dismissOpenDialog() {
        // Find the dialog's OK button directly. robot.clickOn("OK") searches by
        // visible text and can fail if the alert button isn't picked up by the
        // text search across windows.
        DialogPane dialog = findOpenDialog();
        if (dialog == null) return;
        Button okButton = (Button) dialog.lookupButton(javafx.scene.control.ButtonType.OK);
        Platform.runLater(okButton::fire);
        // Poll until the dialog actually goes away (showAndWait returns and the
        // dialog window is hidden).
        for (int i = 0; i < 30; i++) {
            if (findOpenDialog() == null) return;
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
    }

    @Test
    void validInputCreatesConnectionAndNavigatesToLobby(FxRobot robot) {
        setIpAndPort("127.0.0.1", String.valueOf(serverPort));
        fireConnectButton();

        org.junit.jupiter.api.Assertions.assertEquals(
                "SITS Viewer - Tournament Lobby", testStage.getTitle());

        ListView<?> tournamentList = (ListView<?>) testStage.getScene().lookup("#tournamentList");
        org.junit.jupiter.api.Assertions.assertNotNull(tournamentList,
                "tournamentList should exist in the lobby scene after navigation");
        org.junit.jupiter.api.Assertions.assertTrue(tournamentList.isVisible(),
                "tournamentList should be visible after navigation");
    }

    @Test
    void emptyInputShowsErrorDialog(FxRobot robot) {
        setIpAndPort("", "");
        fireConnectAsync();

        DialogPane dialog = waitForOpenDialog();
        org.junit.jupiter.api.Assertions.assertNotNull(dialog, "error dialog should be open");
        org.junit.jupiter.api.Assertions.assertTrue(
                dialog.getContentText().contains("IP and port"),
                "expected validation message about IP and port, got: " + dialog.getContentText());

        dismissOpenDialog();

        // Connect screen still visible because navigation never ran.
        org.junit.jupiter.api.Assertions.assertEquals(
                "SITS Viewer - Connect", testStage.getTitle());
    }

    @Test
    void nonNumericPortShowsErrorDialog(FxRobot robot) {
        setIpAndPort("127.0.0.1", "not-a-number");
        fireConnectAsync();

        DialogPane dialog = waitForOpenDialog();
        org.junit.jupiter.api.Assertions.assertNotNull(dialog, "error dialog should be open");
        org.junit.jupiter.api.Assertions.assertTrue(
                dialog.getContentText().contains("valid number"),
                "expected message about valid number, got: " + dialog.getContentText());

        dismissOpenDialog();

        org.junit.jupiter.api.Assertions.assertEquals(
                "SITS Viewer - Connect", testStage.getTitle());
    }

    @Test
    void portOutOfRangeShowsErrorDialog(FxRobot robot) {
        setIpAndPort("127.0.0.1", "70000");
        fireConnectAsync();

        DialogPane dialog = waitForOpenDialog();
        org.junit.jupiter.api.Assertions.assertNotNull(dialog, "error dialog should be open");
        org.junit.jupiter.api.Assertions.assertTrue(
                dialog.getContentText().contains("between 1 and 65535"),
                "expected message about valid range, got: " + dialog.getContentText());

        dismissOpenDialog();

        org.junit.jupiter.api.Assertions.assertEquals(
                "SITS Viewer - Connect", testStage.getTitle());
    }
}
