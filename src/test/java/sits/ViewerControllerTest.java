package sits;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import sits.viewer.ConnectController;
import sits.viewer.LiveGameController;
import sits.viewer.LobbyController;

class ConnectControllerTest {

    @Test
    void controllerCanBeInstantiated() {
        ConnectController controller = new ConnectController();

        assertNotNull(controller);
    }
}

class LobbyControllerTest {

    @Test
    void controllerCanBeInstantiated() {
        LobbyController controller = new LobbyController();

        assertNotNull(controller);
    }
}

class LiveGameControllerTest {

    @Test
    void controllerCanBeInstantiated() {
        LiveGameController controller = new LiveGameController();

        assertNotNull(controller);
    }
}
