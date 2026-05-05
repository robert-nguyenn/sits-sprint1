package sits.viewer;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class LobbyController {
    
    @FXML
    private ListView<TournamentInfo> tournamentList;
    
    @FXML
    private Button watchButton;
    
    private ServerConnection serverConnection;

    @FXML
    private void initialize() {
        tournamentList.setCellFactory(lv -> new ListCell<TournamentInfo>() {
            @Override
            protected void updateItem(TournamentInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name + " [" + item.status + "]");
                }
            }
        });

        tournamentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            watchButton.setDisable(newVal == null
                    || !("RUNNING".equals(newVal.status) || "COMPLETED".equals(newVal.status)));
        });
        watchButton.setDisable(true);
    }

    public void setServerConnection(ServerConnection connection) {
        this.serverConnection = connection;
        loadTournaments();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadTournaments();
    }

    @FXML
    private void handleWatch(ActionEvent event) {
        TournamentInfo selected = tournamentList.getSelectionModel().getSelectedItem();
        if (selected != null
                && ("RUNNING".equals(selected.status) || "COMPLETED".equals(selected.status))) {
            watchTournament(selected);
        }
    }

    private void loadTournaments() {
        try {
            List<TournamentInfo> tournaments = serverConnection.fetchTournaments();
            tournamentList.getItems().clear();
            tournamentList.getItems().addAll(tournaments);
        } catch (Exception e) {
            showError("Failed to load tournaments: " + e.getMessage());
        }
    }

    protected void watchTournament(TournamentInfo tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/live_game.fxml"));
            Parent root = loader.load();
            
            LiveGameController controller = loader.getController();
            controller.setServerConnection(serverConnection);
            controller.watchTournament(tournament.id);

            Stage stage = (Stage) tournamentList.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("SITS Viewer - Watching " + tournament.name);
        } catch (Exception e) {
            showError("Failed to start watching: " + e.getMessage());
        }
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Viewing Error");
        alert.setContentText(message);
        displayAlert(alert);
    }

    protected void displayAlert(Alert alert) {
        alert.showAndWait();
    }
}
