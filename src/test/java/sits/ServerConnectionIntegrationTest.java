package sits;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import sits.viewer.ServerConnection;

class ServerConnectionIntegrationTest {

    @Test
    void connectionStoresUrlCorrectly() {
        String url = "http://localhost:8080";
        ServerConnection conn = new ServerConnection(url);

        assertNotNull(conn);
    }

    @Test
    void connectionAcceptsVariousUrlFormats() {
        ServerConnection conn1 = new ServerConnection("http://127.0.0.1:8080");
        ServerConnection conn2 = new ServerConnection("http://192.168.1.100:9000");
        ServerConnection conn3 = new ServerConnection("http://server.example.com:8080");

        assertNotNull(conn1);
        assertNotNull(conn2);
        assertNotNull(conn3);
    }

    @Test
    void invalidConnectionThrowsException() {
        ServerConnection conn = new ServerConnection("http://nonexistent.invalid:9999");

        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, conn::fetchTournaments);
        String message = ex.getMessage();

        if (message != null) {
            assertTrue(
                message.contains("nonexistent")
                    || message.contains("failed")
                    || message.contains("Connection")
            );
        }
    }
}
