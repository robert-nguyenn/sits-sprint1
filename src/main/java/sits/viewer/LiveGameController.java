package sits.viewer;

import java.util.concurrent.CompletableFuture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sits.remote.dto.MoveEventDTO;

public class LiveGameController {
    
    @FXML
    private TextArea moveTextArea;
    
    @FXML
    private Button backButton;
    
    private ServerConnection serverConnection;
    private CompletableFuture<Void> streamFuture;

    @FXML
    private void initialize() {
        moveTextArea.setWrapText(true);
        moveTextArea.setEditable(false);
    }

    public void setServerConnection(ServerConnection connection) {
        this.serverConnection = connection;
    }

    public void watchTournament(String tournamentId) {
        moveTextArea.clear();
        moveTextArea.appendText("Connecting to tournament...\n");

        streamFuture = serverConnection.streamMoves(
                tournamentId,
                this::onMoveReceived,
                this::onStreamComplete
        );
    }

    private void onMoveReceived(MoveEventDTO event) {
        if ("MOVE".equals(event.getType())) {
            String moveText = String.format(
                    "Round %d: %s [%s] vs %s [%s] | Scores: (%d, %d)\n",
                    event.getRoundNumber(),
                    event.getP1Name(),
                    event.getAction1(),
                    event.getP2Name(),
                    event.getAction2(),
                    event.getPayoffP1(),
                    event.getPayoffP2()
            );
            moveTextArea.appendText(moveText);
        } else if ("GAME_OVER".equals(event.getType())) {
            String gameOverText = String.format(
                    "\n=== GAME OVER ===\n%s vs %s\nWinner: %s (Scores: %d vs %d)\n\n",
                    event.getP1Name(),
                    event.getP2Name(),
                    event.getWinner(),
                    event.getTotalScoreP1(),
                    event.getTotalScoreP2()
            );
            moveTextArea.appendText(gameOverText);
        } else if ("TOURNAMENT_OVER".equals(event.getType())) {
            moveTextArea.appendText("\n*** TOURNAMENT COMPLETE ***\n");
        }
    }

    private void onStreamComplete() {
        moveTextArea.appendText("\nStream ended.\n");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Cancel the stream if still running
        if (streamFuture != null && !streamFuture.isDone()) {
            streamFuture.cancel(true);
        }

        try {
            navigateToLobby();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void navigateToLobby() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/lobby.fxml"));
        Parent root = loader.load();

        LobbyController controller = loader.getController();
        controller.setServerConnection(serverConnection);

        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("SITS Viewer - Tournament Lobby");
    }
}
