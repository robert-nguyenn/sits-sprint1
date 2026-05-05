package sits.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;
import sits.remote.NetworkedTournament;
import sits.remote.TournamentRegistry;
import sits.remote.TournamentServerController;
import sits.tournament.RoundRobin;

// scanBasePackages limits component scan to this package only so we don't pull in
// ClientApp from sits.remote. The controller is brought in explicitly via @Import.
@SpringBootApplication(scanBasePackages = "sits.server")
@Import(TournamentServerController.class)
public class ServerApp {

    @Bean
    public TournamentRegistry tournamentRegistry() {
        TournamentRegistry registry = new TournamentRegistry();

        // ipd-1: cross-machine showcase. Has TFT + AC + AD locally and waits for
        // KiwiBot to register from the kiwi machine. 4 participants -> 6 pairings.
        // Anonymous subclass adds a 10-second pause before round 1 fires so the
        // viewer has time to click Watch after the start endpoint is hit.
        NetworkedTournament demo = new NetworkedTournament(
                "ipd-1",
                "Demo IPD Tournament (with KiwiBot)",
                new RoundRobin(),
                new IteratedPrisonersDilemma(15),
                PrisonerAction::valueOf
        ) {
            @Override
            public sits.tournament.TournamentResult start() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return super.start();
            }
        };
        demo.addLocalParticipant(new TitForTat());
        demo.addLocalParticipant(new AlwaysCooperate());
        demo.addLocalParticipant(new AlwaysDefect());
        demo.setDelayMs(1000);
        registry.add(demo);

        // ipd-2: all-local backup tournament. Three participants -> 3 pairings.
        // Useful if you want to demo a second tournament or as a fallback if
        // the kiwi machine is unavailable.
        NetworkedTournament localOnly = new NetworkedTournament(
                "ipd-2",
                "Local Only Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(20),
                PrisonerAction::valueOf
        ) {
            @Override
            public sits.tournament.TournamentResult start() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return super.start();
            }
        };
        localOnly.addLocalParticipant(new TitForTat());
        localOnly.addLocalParticipant(new AlwaysCooperate());
        localOnly.addLocalParticipant(new AlwaysDefect());
        localOnly.setDelayMs(1000);
        registry.add(localOnly);

        return registry;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
}
