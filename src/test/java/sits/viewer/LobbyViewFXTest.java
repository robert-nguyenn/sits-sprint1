package sits.viewer;

import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class LobbyViewFXTest {

    private Stage testStage;
    private MutableFakeConnection connection;
    private LobbyController controller;

    @Start
    private void start(Stage stage) throws Exception {
        this.testStage = stage;
        connection = new MutableFakeConnection();
        connection.tournaments.add(new TournamentInfo("t1", "Open Tournament", "REGISTERING"));
        connection.tournaments.add(new TournamentInfo("t2", "Live Tournament", "RUNNING"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/lobby.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setServerConnection(connection);

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("SITS Viewer - Tournament Lobby");
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Button findButton(String text) {
        for (Node n : testStage.getScene().getRoot().lookupAll(".button")) {
            if (n instanceof Button b && text.equals(b.getText())) {
                return b;
            }
        }
        throw new AssertionError("button with text '" + text + "' not found");
    }

    private void selectRow(FxRobot robot, int index) {
        ListView<TournamentInfo> list = robot.lookup("#tournamentList").queryAs(ListView.class);
        Platform.runLater(() -> list.getSelectionModel().select(index));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void pressWatch(FxRobot robot) {
        Button watch = findButton("Watch");
        Platform.runLater(watch::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void pressRefresh(FxRobot robot) {
        Button refresh = findButton("Refresh");
        Platform.runLater(refresh::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void tournamentListIsPopulatedFromServer(FxRobot robot) {
        ListView<TournamentInfo> list = robot.lookup("#tournamentList").queryAs(ListView.class);
        org.assertj.core.api.Assertions.assertThat(list.getItems()).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(list.getItems())
                .extracting(t -> t.id)
                .containsExactly("t1", "t2");
    }

    @Test
    void watchButtonStartsDisabled(FxRobot robot) {
        Button watchButton = robot.lookup("#watchButton").queryAs(Button.class);
        org.assertj.core.api.Assertions.assertThat(watchButton.isDisable()).isTrue();
    }

    @Test
    void watchButtonEnabledOnlyForRunningTournament(FxRobot robot) {
        Button watchButton = robot.lookup("#watchButton").queryAs(Button.class);

        selectRow(robot, 0);
        org.assertj.core.api.Assertions.assertThat(watchButton.isDisable())
                .as("REGISTERING tournament should keep Watch disabled")
                .isTrue();

        selectRow(robot, 1);
        org.assertj.core.api.Assertions.assertThat(watchButton.isDisable())
                .as("RUNNING tournament should enable Watch")
                .isFalse();
    }

    @Test
    void clickingWatchOnRunningTournamentNavigatesToLiveGame(FxRobot robot) {
        selectRow(robot, 1);
        pressWatch(robot);

        // The scene has been replaced with the live game view, so its text area is now reachable.
        FxAssert.verifyThat("#moveTextArea", NodeMatchers.isVisible());
        FxAssert.verifyThat("#backButton", NodeMatchers.isVisible());
    }

    @Test
    void refreshReloadsTournamentList(FxRobot robot) {
        ListView<TournamentInfo> list = robot.lookup("#tournamentList").queryAs(ListView.class);
        org.assertj.core.api.Assertions.assertThat(list.getItems()).hasSize(2);

        connection.tournaments.clear();
        connection.tournaments.add(new TournamentInfo("t3", "New Tournament", "REGISTERING"));

        pressRefresh(robot);

        org.assertj.core.api.Assertions.assertThat(list.getItems()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(list.getItems().get(0).id).isEqualTo("t3");
    }

    @Test
    void refreshFailureShowsErrorDialog(FxRobot robot) {
        connection.failNext = true;

        robot.clickOn("Refresh");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        robot.clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static final class MutableFakeConnection extends ServerConnection {
        final List<TournamentInfo> tournaments = new ArrayList<>();
        boolean failNext = false;

        MutableFakeConnection() {
            super("http://localhost:0");
        }

        @Override
        public List<TournamentInfo> fetchTournaments() throws Exception {
            if (failNext) {
                failNext = false;
                throw new Exception("simulated network failure");
            }
            return new ArrayList<>(tournaments);
        }
    }
}
