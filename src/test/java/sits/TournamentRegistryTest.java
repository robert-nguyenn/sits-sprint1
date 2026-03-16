package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.remote.NetworkedTournament;
import sits.remote.TournamentRegistry;
import sits.tournament.RoundRobin;

class TournamentRegistryTest {

    @Test
    void listsOnlyTournamentsInRegistrationPhase() {
        NetworkedTournament openTournament = new NetworkedTournament(
                "open",
                "Open",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        NetworkedTournament completedTournament = new NetworkedTournament(
                "done",
                "Done",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );
        completedTournament.addLocalParticipant(new AlwaysDefect());
        completedTournament.addLocalParticipant(new AlwaysCooperate());
        completedTournament.start();

        TournamentRegistry registry = new TournamentRegistry();
        registry.add(openTournament);
        registry.add(completedTournament);

        assertEquals(1, registry.listRegistering().size());
        assertEquals("open", registry.listRegistering().get(0).getId());
        assertEquals(openTournament, registry.get("open"));
    }
}
