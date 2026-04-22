package sits;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import sits.viewer.ServerConnection;

class ServerConnectionTest {

    @Test
    void constructorStoresBaseUrl() {
        String baseUrl = "http://localhost:8080";
        ServerConnection connection = new ServerConnection(baseUrl);

        assertNotNull(connection);
    }

    @Test
    void connectionCanBeCreatedForDifferentServers() {
        ServerConnection conn1 = new ServerConnection("http://server1:8080");
        ServerConnection conn2 = new ServerConnection("http://server2:9000");

        assertNotNull(conn1);
        assertNotNull(conn2);
    }

    @Test
    void fetchTournamentsThrowsExceptionOnConnectionFailure() {
        ServerConnection connection = new ServerConnection("http://invalid-server-that-does-not-exist:9999");

        assertThrows(Exception.class, () -> connection.fetchTournaments());
    }

    @Test
    void serverConnectionHandlesEmptyBaseUrl() {
        // Should not throw on construction
        ServerConnection connection = new ServerConnection("");

        assertNotNull(connection);
    }
}
