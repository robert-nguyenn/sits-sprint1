package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.remote.NetworkedTournament;
import sits.remote.RegistrationRequest;
import sits.remote.TournamentRegistry;
import sits.remote.TournamentServerController;
import sits.tournament.RoundRobin;

class TournamentServerControllerTest {

    @Test
    void supportsListRegisterStartFlow() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-1",
                "IPD",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );
        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());

        TournamentRegistry registry = new TournamentRegistry();
        registry.add(tournament);

        TournamentServerController controller = new TournamentServerController(registry);

        assertEquals(1, controller.getTournaments().size());
        assertEquals(HttpStatus.OK, controller.start("ipd-1").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.start("ipd-1").getStatusCode());
    }

    @Test
    void registerWorksForOpenTournament() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-2",
                "IPD",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        TournamentRegistry registry = new TournamentRegistry();
        registry.add(tournament);

        TournamentServerController controller = new TournamentServerController(registry);

        assertEquals(HttpStatus.OK, controller.register("ipd-2", new RegistrationRequest("Remote", "127.0.0.1", 9020)).getStatusCode());
    }

    @Test
    void returnsNotFoundForUnknownTournament() {
        TournamentServerController controller = new TournamentServerController(new TournamentRegistry());

        assertEquals(HttpStatus.NOT_FOUND, controller.register("missing", new RegistrationRequest("A", "127.0.0.1", 1)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.start("missing").getStatusCode());
    }

    @Test
    void registerReturnsBadRequestWhenTournamentAlreadyCompleted() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-3",
                "IPD",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );
        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());
        tournament.start();

        TournamentRegistry registry = new TournamentRegistry();
        registry.add(tournament);

        TournamentServerController controller = new TournamentServerController(registry);
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.register("ipd-3", new RegistrationRequest("Late", "127.0.0.1", 9999)).getStatusCode());
    }
}
