package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import sits.remote.ClientApp;
import sits.remote.TournamentServerClient;

class ClientAppTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        // File under test: sits/remote/ClientApp.java
        // Method under test: ClientApp.main(String[] args)
        // When main() is called with command-line arguments, it must delegate to 
        // SpringApplication.run() to bootstrap the Spring Boot application. This test
        // verifies that main() correctly passes control to Spring's startup mechanism
        // rather than attempting its own startup logic. Without delegation, the
        // application would never start.
        
        // Simulate command-line args passed to Spring Boot app startup.
        String[] args = new String[] {"--spring.main.web-application-type=none"};

        // Mock the static SpringApplication.run(...) call so we can verify main() delegates correctly.
        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            // Fake application context returned by Spring when app starts.
            ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
            // Define expected behavior for the static run call.
            spring.when(() -> SpringApplication.run(ClientApp.class, args)).thenReturn(ctx);

            // Execute main method under test.
            ClientApp.main(args);

            // Assert: main() called SpringApplication.run exactly once with the same args.
            spring.verify(() -> SpringApplication.run(ClientApp.class, args), times(1));
        }
    }

    @Test
    void resolveLocalIpReturnsNonBlank() throws UnknownHostException {
        // File under test: sits/remote/ClientApp.java
        // Method under test: ClientApp.resolveLocalIp() [protected method]
        // When the ClientApp asks for its local IP address (to send to tournament server),
        // the resolveLocalIp() method must return a non-empty string. This is critical
        // because the tournament server needs a valid IP to reach back to this client.
        // If the IP were blank/null, the server couldn't establish a connection later.
        //
        // We don't mock the actual IP resolution because we want to verify it really
        // works on this machine. However, we use a fake/capturing tournament client
        // (not a real one) to isolate this unit test from network dependencies.
        // We access the protected method via a helper subclass wrapper because
        // resolveLocalIp() is protected, not public.
        //
        
        // Use a fake client because this test only checks IP resolution behavior.
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();

        // Create a small helper subclass so we can call the protected resolveLocalIp() method.
        ClientAppWithAccess app = new ClientAppWithAccess(fakeClient) {
            @Override
            String callResolveLocalIp() throws UnknownHostException {
                return resolveLocalIp();
            }
        };

        // Call the protected method through our helper wrapper.
        String ip = app.callResolveLocalIp();

        // Assert: resolved IP exists and is not empty.
        assertNotNull(ip);
        assertFalse(ip.isBlank());
    }

    @Test
    void onApplicationReadyRegistersUsingDetectedNetworkInfo() throws UnknownHostException {
        // ========== TESTING APP STARTUP REGISTRATION ==========
        // Name: onApplicationReadyRegistersUsingDetectedNetworkInfo
        // Purpose: Verify that ClientApp registers itself when startup finishes
        // When the app is ready, it should collect its tournamentId, participantName,
        // IP address, and port, then call register(...) with all of them.
        //
        // We fake the IP address so the test is predictable.
        // We also use CapturingTournamentClient so no real HTTP request is made.
        //
        // After calling onApplicationReady(), we check that register(...)
        // received exactly the expected values.
        
        // Fake client captures registration args instead of making network calls.
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();

        // Override IP resolution to make this test deterministic and independent of local machine network.
        ClientApp app = new ClientApp(fakeClient) {
            @Override
            protected String resolveLocalIp() {
                return "10.0.0.44";
            }
        };

        // Inject config-backed private fields directly for test setup.
        ReflectionTestUtils.setField(app, "tournamentId", "ipd-1");
        ReflectionTestUtils.setField(app, "participantName", "RemoteHuman");
        ReflectionTestUtils.setField(app, "port", 9123);

        // Simulate app-ready event handling.
        app.onApplicationReady();

        // Assert: register(...) got exactly the values we expect.
        assertEquals("ipd-1", fakeClient.tournamentId);
        assertEquals("RemoteHuman", fakeClient.name);
        assertEquals("10.0.0.44", fakeClient.ip);
        assertEquals(9123, fakeClient.port);
    }

    @Test
    void webServerReadyEventSetsPort() {
        // ========== TESTING WEB SERVER PORT CAPTURE ==========
        // Name: webServerReadyEventSetsPort
        // Purpose: Verify that ClientApp remembers the port chosen by the web server
        // Spring Boot tells the app "the web server is ready" by sending a WebServerInitializedEvent.
        // This test checks that ClientApp reads the port from that event and stores it.
        //
        // We use mocks instead of starting a real server.
        // Fake event -> gives ClientApp a fake web server
        // Fake web server -> says its port is 7777
        //
        // After calling onWebServerReady(...), we verify that the app's port field became 7777.
        
        // Create app with fake tournament client.
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();
        ClientApp app = new ClientApp(fakeClient);

        // Mock Spring Boot web-server event and embedded server.
        WebServerInitializedEvent event = mock(WebServerInitializedEvent.class);
        WebServer webServer = mock(WebServer.class);

        // When event asks for server, return our mock; when app asks for port, return 7777.
        when(event.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(7777);

        // Trigger event listener method under test.
        app.onWebServerReady(event);

        // Assert: app stored detected port into private field.
        assertEquals(7777, ReflectionTestUtils.getField(app, "port"));
    }

    @Test
    void beanFactoryMethodsCreateObjects() {
        // ========== TESTING @Bean FACTORY METHODS ==========
        // Purpose: Check that ClientApp can create the objects Spring needs at startup
        // This test calls the two @Bean methods directly and makes sure they return real objects.
        //
        // restTemplate(...) -> should create a RestTemplate object
        // tournamentServerClient(...) -> should create a TournamentServerClient object
        //
        // We are not testing real HTTP calls here.
        // We are just testing that these factory methods build the needed objects without failing.
        //
        // Spring Boot normally calls these methods automatically during startup.
        // In this test, we call them ourselves to confirm the object creation logic works.
        
        // Build app with fake client; this test checks @Bean factory methods only.
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();
        ClientApp app = new ClientApp(fakeClient);

        // Create RestTemplate via the same method Spring would call.
        RestTemplate restTemplate = app.restTemplate(new RestTemplateBuilder());
        // Create TournamentServerClient via bean factory method with explicit URL.
        TournamentServerClient built = app.tournamentServerClient("http://example:8080", restTemplate);

        // Assert: factory methods return non-null objects.
        assertNotNull(restTemplate);
        assertNotNull(built);
    }

    // ========== TEST HELPER CLASS ==========
    // Name: ClientAppWithAccess
    // Purpose: Let the test reach the protected resolveLocalIp() method
    // It extends ClientApp and gives the test a small wrapper method so we can call
    // resolveLocalIp() from the test code.
    // Normally, resolveLocalIp() is protected, so the test cannot call it directly
    // from outside the class.
    // Real ClientApp -> has the protected method, not directly accessible in the test
    // ClientAppWithAccess -> tiny test-only subclass that exposes a way to call it
    private abstract static class ClientAppWithAccess extends ClientApp {
        ClientAppWithAccess(TournamentServerClient client) {
            super(client);
        }

        abstract String callResolveLocalIp() throws UnknownHostException;
    }

    // ========== TEST DOUBLE CLASS ==========
    // Name: CapturingTournamentClient
    // Purpose: Replace real HTTP client to avoid network calls during tests
    // It extends TournamentServerClient, but instead of doing the real network behavior, it just captures the arguments passed to register(...) and stores them in fields so the test can check them later.
    // Real TournamentServerClient → would make actual HTTP calls
    // CapturingTournamentClient → fake version for tests, no real HTTP
    private static class CapturingTournamentClient extends TournamentServerClient {
        private String tournamentId;
        private String name;
        private String ip;
        private int port;

        // Provide harmless defaults required by TournamentServerClient superclass constructor.
        CapturingTournamentClient() {
            super("http://localhost:8080", new RestTemplate());
        }

        @Override
        // Capture all register call arguments instead of performing any HTTP call.
        public void register(String tournamentId, String name, String ip, int port) {
            this.tournamentId = tournamentId;
            this.name = name;
            this.ip = ip;
            this.port = port;
        }
    }
}