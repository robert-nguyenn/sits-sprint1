package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.remote.NetworkedTournament;
import sits.remote.RegistrationRequest;
import sits.remote.TournamentRegistry;
import sits.remote.TournamentServerController;
import sits.remote.TournamentStatus;
import sits.tournament.RoundRobin;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = TournamentServerHttpTest.TestApp.class
)
class TournamentServerHttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TournamentRegistry registry;

    @Test
    void serverEndpoints_workOverHttp() {
        // list registering tournaments
        ResponseEntity<String> listResponse =
                restTemplate.getForEntity("/tournaments", String.class);

        assertEquals(200, listResponse.getStatusCode().value());
        String body = Objects.requireNonNull(listResponse.getBody());
        assertTrue(body.contains("\"id\":\"ipd-register\""));

        // register a remote participant
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                "/tournaments/ipd-register/register",
                new RegistrationRequest("RemoteOne", "127.0.0.1", 9001),
                String.class
        );

        assertEquals(200, registerResponse.getStatusCode().value());
        assertEquals(1, registry.get("ipd-register").getParticipantCount());

        // start a tournament with only local players (no network calls needed)
        ResponseEntity<String> startResponse = restTemplate.postForEntity(
                "/tournaments/ipd-start/start",
                null,
                String.class
        );

        assertEquals(200, startResponse.getStatusCode().value());
        assertEquals(TournamentStatus.COMPLETED, registry.get("ipd-start").getStatus());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(TournamentServerController.class)
    static class TestApp {

        @Bean
        TournamentRegistry tournamentRegistry() {
            TournamentRegistry registry = new TournamentRegistry();

            NetworkedTournament registerTournament = new NetworkedTournament(
                    "ipd-register",
                    "IPD Register",
                    new RoundRobin(),
                    new IteratedPrisonersDilemma(1),
                    PrisonerAction::valueOf
            );
            registry.add(registerTournament);

            NetworkedTournament startTournament = new NetworkedTournament(
                    "ipd-start",
                    "IPD Start",
                    new RoundRobin(),
                    new IteratedPrisonersDilemma(1),
                    PrisonerAction::valueOf
            );
            startTournament.addLocalParticipant(new AlwaysDefect());
            startTournament.addLocalParticipant(new AlwaysCooperate());
            registry.add(startTournament);

            return registry;
        }
    }
}