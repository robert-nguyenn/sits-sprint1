package sits.viewer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectController {
    
    @FXML
    private TextField ipTextField;
    
    @FXML
    private TextField portTextField;

    @FXML
    private void handleConnect(ActionEvent event) {
        String ip = ipTextField.getText().trim();
        String portStr = portTextField.getText().trim();

        if (ip.isEmpty() || portStr.isEmpty()) {
            showError("Please enter both IP and port");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            showError("Port must be a valid number");
            return;
        }

        if (port < 1 || port > 65535) {
            showError("Port must be between 1 and 65535");
            return;
        }

        try {
            String serverUrl = "http://" + ip + ":" + port;
            ServerConnection connection = createConnection(serverUrl);
            navigateToLobby(connection);
        } catch (Exception e) {
            showError("Failed to connect: " + e.getMessage());
        }
    }

    protected ServerConnection createConnection(String serverUrl) {
        return new ServerConnection(serverUrl);
    }

    protected void navigateToLobby(ServerConnection connection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/lobby.fxml"));
            Parent root = loader.load();
            
            LobbyController controller = loader.getController();
            controller.setServerConnection(connection);

            Stage stage = (Stage) ipTextField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("SITS Viewer - Tournament Lobby");
        } catch (Exception e) {
            showError("Failed to load lobby: " + e.getMessage());
        }
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Connection Error");
        alert.setContentText(message);
        displayAlert(alert);
    }

    protected void displayAlert(Alert alert) {
        alert.showAndWait();
    }
}
