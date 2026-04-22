package sits.viewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = loadRoot();

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("SITS Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    protected Parent loadRoot() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sits/viewer/connect.fxml"));
        return loader.load();
    }
}
