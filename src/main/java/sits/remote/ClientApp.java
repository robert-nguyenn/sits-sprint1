package sits.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ClientApp {

    private int port;

    @Value("${tournament.id:ipd-1}")
    private String tournamentId;

    @Value("${participant.name:RemoteParticipant}")
    private String participantName;

    private final TournamentServerClient client;

    public ClientApp(TournamentServerClient client) {
        this.client = client;
    }

    @EventListener
    public void onWebServerReady(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws UnknownHostException {
        String ip = resolveLocalIp();
        client.register(tournamentId, participantName, ip, port);
    }

    protected String resolveLocalIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public TournamentServerClient tournamentServerClient(
            @Value("${tournament.server.url:http://localhost:8080}") String serverUrl,
            RestTemplate restTemplate
    ) {
        return new TournamentServerClient(serverUrl, restTemplate);
    }

    public static void main(String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }
}
