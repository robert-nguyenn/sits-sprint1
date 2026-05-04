package sits.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import sits.action.PrisonerAction;
import sits.game.IteratedPrisonersDilemma;
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

        // Anonymous subclass adds a 10-second pause before round 1 fires, giving
        // the viewer time to click Watch after the start endpoint is hit.
        NetworkedTournament demo = new NetworkedTournament(
                "ipd-1",
                "Demo IPD Tournament",
                new RoundRobin(),
                new IteratedPrisonersDilemma(30),
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
        demo.addLocalParticipant(new AlwaysDefect());
        demo.setDelayMs(2000);
        registry.add(demo);

        return registry;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
}
