package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class TournamentServerControllerViewerTest {

    private TournamentServerController controller;
    private TournamentRegistry registry;
    private NetworkedTournament tournament;

    @BeforeEach
    void setup() {
        registry = new TournamentRegistry();
        controller = new TournamentServerController(registry);

        tournament = new NetworkedTournament(
                "ipd-1",
                "Test Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());

        registry.add(tournament);
    }

    @Test
    void getTournamentsReturnsRegisteringAndRunningTournaments() {
        NetworkedTournament runningTournament = new NetworkedTournament(
                "ipd-2",
                "Running Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                runningTournament,
                "status",
                TournamentStatus.RUNNING
        );
        registry.add(runningTournament);

        var tournaments = controller.getTournaments();

        assertEquals(2, tournaments.size());
        assertTrue(tournaments.stream().anyMatch(t -> "ipd-1".equals(t.getId())));
        assertTrue(tournaments.stream().anyMatch(t -> "ipd-2".equals(t.getId())));
    }

    @Test
    void registerAddsRemoteParticipant() {
        RegistrationRequest request = new RegistrationRequest("RemoteBot", "127.0.0.1", 9001);

        var response = controller.register("ipd-1", request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(3, tournament.getParticipantCount());
    }

    @Test
    void startReturnsAcceptedImmediately() {
        long startTime = System.currentTimeMillis();
        var response = controller.start("ipd-1");
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(202, response.getStatusCode().value()); // 202 Accepted
        assertTrue(elapsed < 1000, "Start should return immediately (< 1 second)");
    }

    @Test
    void startWithUnknownTournamentReturnsNotFound() {
        var response = controller.start("nonexistent");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getStreamerEmitterReturnsValidEmitter() {
        var emitter = controller.streamMoves("ipd-1");

        assertNotNull(emitter);
    }

    @Test
    void getStreamerEmitterThrowsForUnknownTournament() {
        assertThrows(IllegalArgumentException.class, () -> controller.streamMoves("nonexistent"));
    }

    @Test
    void broadcasterIsNotNullAfterCreation() {
        assertNotNull(tournament.getBroadcaster());
    }
}
