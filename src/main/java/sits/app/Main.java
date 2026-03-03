package sits.app;

import java.util.List;

import sits.game.IteratedPrisonersDilemma;
import sits.observer.MoveLogger;
import sits.observer.ScoreLogger;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.Participant;
import sits.participant.TitForTat;
import sits.tournament.RoundRobin;
import sits.tournament.TournamentResult;

public class Main {
    public static void main(String[] args) {
        var game = new IteratedPrisonersDilemma(10);

        game.addObserver(new MoveLogger("target/moves.txt"));
        game.addObserver(new ScoreLogger("target/scores.txt"));

        List<Participant> participants = List.of(
                new TitForTat(),
                new AlwaysDefect(),
                new AlwaysCooperate()
        );

        var rr = new RoundRobin();
        TournamentResult result = rr.run(participants, game);

        System.out.println("Rankings: " + result.getRankings());
        for (String name : result.getRankings()) {
            System.out.println(name + " = " + result.getScore(name));
        }
    }
}