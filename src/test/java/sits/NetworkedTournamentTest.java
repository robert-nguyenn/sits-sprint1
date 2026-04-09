package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.remote.NetworkedTournament;
import sits.remote.RegistrationRequest;
import sits.remote.TournamentStatus;
import sits.tournament.RoundRobin;

// Tests for lifecycle, registration rules, and safety checks in NetworkedTournament.
class NetworkedTournamentTest {

    @Test
    void defaultConstructorStartsInRegisteringState() {
                // Checks default setup starts open for registration with zero participants.
        NetworkedTournament tournament = new NetworkedTournament();

        assertEquals(TournamentStatus.REGISTERING, tournament.getStatus());
        assertEquals(0, tournament.getParticipantCount());
    }

    @Test
    void canAddLocalAndRemoteParticipantsDuringRegistration() {
                // Checks both local and remote participants can join during REGISTERING state.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-1",
                "IPD Open",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysCooperate());
        tournament.addRemoteParticipant(new RegistrationRequest("RemoteA", "127.0.0.1", 9001));

        assertEquals(2, tournament.getParticipantCount());
        assertEquals(TournamentStatus.REGISTERING, tournament.getStatus());
        assertEquals("RemoteA", tournament.getParticipantsView().get(1).getName());
    }

    @Test
    void startTransitionsLifecycleAndReturnsResult() {
                // Checks start() runs the tournament and moves status to COMPLETED.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-2",
                "IPD Duel",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());

        var result = tournament.start();

        assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        assertEquals(5, result.getScore("AlwaysDefect"));
        assertEquals(0, result.getScore("AlwaysCooperate"));
    }

    @Test
    void cannotRegisterAfterTournamentStarts() {
                // Checks late registration and second start call are rejected.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-3",
                "IPD Closed",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());
        tournament.start();

        assertThrows(
                IllegalStateException.class,
                () -> tournament.addRemoteParticipant(new RegistrationRequest("Late", "127.0.0.1", 9010))
        );

        assertThrows(IllegalStateException.class, tournament::start);
    }

    @Test
    void participantsViewIsUnmodifiable() {
                // Checks external code cannot mutate the participants view directly.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-4",
                "IPD",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );
        tournament.addLocalParticipant(new AlwaysDefect());

        assertThrows(UnsupportedOperationException.class,
                () -> tournament.getParticipantsView().add(new AlwaysCooperate()));
    }

    @Test
    void addLocalParticipantRejectsNull() {
                // Checks null participant input is rejected.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-5",
                "IPD",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        assertThrows(NullPointerException.class, () -> tournament.addLocalParticipant(null));
        assertTrue(tournament.getParticipantsView().isEmpty());
    }

    @Test
    void constructorRejectsNullRequiredArguments() {
                // Checks required constructor arguments are validated.
        RoundRobin format = new RoundRobin();
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(1);

        assertThrows(NullPointerException.class,
                () -> new NetworkedTournament(null, "name", format, game, PrisonerAction::valueOf));
        assertThrows(NullPointerException.class,
                () -> new NetworkedTournament("id", null, format, game, PrisonerAction::valueOf));
        assertThrows(NullPointerException.class,
                () -> new NetworkedTournament("id", "name", null, game, PrisonerAction::valueOf));
        assertThrows(NullPointerException.class,
                () -> new NetworkedTournament("id", "name", format, null, PrisonerAction::valueOf));
        assertThrows(NullPointerException.class,
                () -> new NetworkedTournament("id", "name", format, game, null));
    }

    @Test
    void getNameReturnsConfiguredName() {
                // Checks configured tournament name is returned unchanged.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-name",
                "IPD Name Check",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        assertEquals("IPD Name Check", tournament.getName());
    }

    @Test
    void addLocalParticipantThrowsWhenTournamentNotRegistering() {
                // Checks local participant add is blocked once tournament has started.
        NetworkedTournament tournament = new NetworkedTournament(
                "ipd-locked",
                "IPD Locked",
                new RoundRobin(),
                new IteratedPrisonersDilemma(1),
                PrisonerAction::valueOf
        );

        tournament.addLocalParticipant(new AlwaysDefect());
        tournament.addLocalParticipant(new AlwaysCooperate());
        tournament.start();

        assertThrows(IllegalStateException.class,
                () -> tournament.addLocalParticipant(new AlwaysCooperate()));
    }

    @Test
    void defaultActionFactoryReturnsLabelAction() {
                // Checks default action factory builds simple label-based actions.
        NetworkedTournament tournament = new NetworkedTournament();

        @SuppressWarnings("unchecked")
        Function<String, Action> factory =
                (Function<String, Action>) ReflectionTestUtils.getField(tournament, "actionFactory");

        assertNotNull(factory);
        Action action = factory.apply("CUSTOM_LABEL");
        assertEquals("CUSTOM_LABEL", action.getLabel());
    }
}