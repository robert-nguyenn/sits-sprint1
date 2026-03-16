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
        String[] args = new String[] {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
            spring.when(() -> SpringApplication.run(ClientApp.class, args)).thenReturn(ctx);

            ClientApp.main(args);

            spring.verify(() -> SpringApplication.run(ClientApp.class, args), times(1));
        }
    }

    @Test
    void resolveLocalIpReturnsNonBlank() throws UnknownHostException {
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();
        ClientAppWithAccess app = new ClientAppWithAccess(fakeClient) {
            @Override
            String callResolveLocalIp() throws UnknownHostException {
                return resolveLocalIp();
            }
        };

        String ip = app.callResolveLocalIp();

        assertNotNull(ip);
        assertFalse(ip.isBlank());
    }

    @Test
    void onApplicationReadyRegistersUsingDetectedNetworkInfo() throws UnknownHostException {
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();

        ClientApp app = new ClientApp(fakeClient) {
            @Override
            protected String resolveLocalIp() {
                return "10.0.0.44";
            }
        };

        ReflectionTestUtils.setField(app, "tournamentId", "ipd-1");
        ReflectionTestUtils.setField(app, "participantName", "RemoteHuman");
        ReflectionTestUtils.setField(app, "port", 9123);

        app.onApplicationReady();

        assertEquals("ipd-1", fakeClient.tournamentId);
        assertEquals("RemoteHuman", fakeClient.name);
        assertEquals("10.0.0.44", fakeClient.ip);
        assertEquals(9123, fakeClient.port);
    }

    @Test
    void webServerReadyEventSetsPort() {
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();
        ClientApp app = new ClientApp(fakeClient);

        WebServerInitializedEvent event = mock(WebServerInitializedEvent.class);
        WebServer webServer = mock(WebServer.class);
        when(event.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(7777);

        app.onWebServerReady(event);

        assertEquals(7777, ReflectionTestUtils.getField(app, "port"));
    }

    @Test
    void beanFactoryMethodsCreateObjects() {
        CapturingTournamentClient fakeClient = new CapturingTournamentClient();
        ClientApp app = new ClientApp(fakeClient);

        RestTemplate restTemplate = app.restTemplate(new RestTemplateBuilder());
        TournamentServerClient built = app.tournamentServerClient("http://example:8080", restTemplate);

        assertNotNull(restTemplate);
        assertNotNull(built);
    }

    private abstract static class ClientAppWithAccess extends ClientApp {
        ClientAppWithAccess(TournamentServerClient client) {
            super(client);
        }

        abstract String callResolveLocalIp() throws UnknownHostException;
    }

    private static class CapturingTournamentClient extends TournamentServerClient {
        private String tournamentId;
        private String name;
        private String ip;
        private int port;

        CapturingTournamentClient() {
            super("http://localhost:8080", new RestTemplate());
        }

        @Override
        public void register(String tournamentId, String name, String ip, int port) {
            this.tournamentId = tournamentId;
            this.name = name;
            this.ip = ip;
            this.port = port;
        }
    }
}