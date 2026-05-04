// ClientApp runs on the remote player machine.
// RemoteParticipant runs on the tournament server side.
// ClientApp exposes endpoints.
// RemoteParticipant calls those endpoints.
// So ClientApp is the actual remote app, while RemoteParticipant is the server's proxy for that remote player.


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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import sits.participant.AlwaysCooperate;
import sits.participant.Participant;

@SpringBootApplication
public class ClientApp {

    private int port;

    @Value("${tournament.id:ipd-1}")
    private String tournamentId;

    @Value("${participant.name:RemoteParticipant}")
    private String participantName;

    private final TournamentServerClient client;

    // @Lazy breaks the self-cycle: TournamentServerClient is defined as a @Bean inside
    // this same class, so eager constructor injection would cycle on itself.
    public ClientApp(@Lazy TournamentServerClient client) {
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

    // ParticipantController is on the classpath and needs a Participant bean to inject.
    @Bean
    public Participant participant() {
        return new AlwaysCooperate();
    }

    // TournamentServerController is also on the classpath and needs a TournamentRegistry.
    // The client doesn't host any tournaments, so an empty registry is enough to satisfy wiring.
    @Bean
    public TournamentRegistry tournamentRegistry() {
        return new TournamentRegistry();
    }

    public static void main(String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }
}
