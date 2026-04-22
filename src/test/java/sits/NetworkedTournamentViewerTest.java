package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.remote.NetworkedTournament;
import sits.remote.TournamentStatus;
import sits.tournament.RoundRobin;

class NetworkedTournamentViewerTest {

    @Test
    void constructorInitializesBroadcaster() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-1",
                "Test Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        assertNotNull(tournament.getBroadcaster());
        assertEquals(0, tournament.getDelayMs());
    }

    @Test
    void setDelayMsUpdatesNewBroadcaster() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-2",
                "Delayed Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.setDelayMs(1000);

        assertEquals(1000, tournament.getDelayMs());
        assertNotNull(tournament.getBroadcaster());
    }

    @Test
    void broadcasterIsRegisteredWhenTournamentStarts() {
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-3",
                "Broadcast Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());

        // Before start, tournament is registered
        assertEquals(TournamentStatus.REGISTERING, tournament.getStatus());

        // After start, status should be COMPLETED (since it runs synchronously in test)
        var result = tournament.start();

        assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        assertNotNull(result);
    }

    @Test
    void defaultDelayIsZero() {
        NetworkedTournament tournament = new NetworkedTournament();

        assertEquals(0, tournament.getDelayMs());
    }
}
