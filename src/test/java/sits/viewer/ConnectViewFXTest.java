package sits.viewer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/connect.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("SITS Viewer - Connect");
        stage.show();
    }

    private void clearTextField(FxRobot robot, String selector) {
        TextField tf = robot.lookup(selector).queryAs(TextField.class);
        Platform.runLater(tf::clear);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void enterText(FxRobot robot, String selector, String text) {
        clearTextField(robot, selector);
        robot.clickOn(selector);
        robot.write(text);
    }

    private void pressConnect(FxRobot robot) {
        robot.clickOn("Connect");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void dismissAlert(FxRobot robot) {
        robot.clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void validInputCreatesConnectionAndNavigatesToLobby(FxRobot robot) {
        enterText(robot, "#ipTextField", "127.0.0.1");
        enterText(robot, "#portTextField", String.valueOf(serverPort));
        pressConnect(robot);

        // Lobby scene is now active. Its tournament list must be on screen.
        Assertions.assertThat(robot.lookup("#tournamentList").queryAs(ListView.class)).isNotNull();
        FxAssert.verifyThat("#tournamentList", NodeMatchers.isVisible());
    }

    @Test
    void emptyInputShowsErrorDialog(FxRobot robot) {
        enterText(robot, "#ipTextField", "");
        enterText(robot, "#portTextField", "");

        // showAndWait blocks the FX thread, so issue the click without waiting first.
        robot.clickOn("Connect");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        dismissAlert(robot);

        // Still on the connect screen because navigation never ran.
        FxAssert.verifyThat("#ipTextField", NodeMatchers.isVisible());
    }

    @Test
    void nonNumericPortShowsErrorDialog(FxRobot robot) {
        enterText(robot, "#ipTextField", "127.0.0.1");
        enterText(robot, "#portTextField", "not-a-number");

        robot.clickOn("Connect");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        dismissAlert(robot);

        FxAssert.verifyThat("#portTextField", NodeMatchers.isVisible());
    }

    @Test
    void portOutOfRangeShowsErrorDialog(FxRobot robot) {
        enterText(robot, "#ipTextField", "127.0.0.1");
        enterText(robot, "#portTextField", "70000");

        robot.clickOn("Connect");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        dismissAlert(robot);

        FxAssert.verifyThat("#portTextField", NodeMatchers.isVisible());
    }
}
