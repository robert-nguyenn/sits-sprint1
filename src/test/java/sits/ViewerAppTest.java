package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sits.viewer.ViewerApp;

class ViewerAppTest {

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
    void startSetsSceneAndTitle() throws Exception {
        TestViewerApp app = new TestViewerApp();

        runOnFxThreadAndWait(() -> {
            try {
                Stage stage = new Stage();
                app.start(stage);

                Scene scene = stage.getScene();
                assertNotNull(scene);
                assertEquals("SITS Viewer", stage.getTitle());
                assertEquals(400.0, scene.getWidth());
                assertEquals(200.0, scene.getHeight());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

    private static final class TestViewerApp extends ViewerApp {
        @Override
        protected Parent loadRoot() {
            return new Pane();
        }
    }
}
