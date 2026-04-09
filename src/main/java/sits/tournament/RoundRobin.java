// This is one actual tournament format.
// Round-robin means:
// every participant plays every other participant once


package sits.tournament;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sits.game.Game;
import sits.game.GameResult;
import sits.participant.Participant;

public class RoundRobin implements TournamentFormat {

    @Override
    public TournamentResult run(List<Participant> participants, Game game) {
        Map<String, Integer> totals = new HashMap<>();

        // init scores for everyone
        for (Participant p : participants) {
            totals.put(p.getName(), 0);
        }

        // all unique pairs
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                Participant p1 = participants.get(i);
                Participant p2 = participants.get(j);

                // no state bleed between matches
                p1.reset();
                p2.reset();

                GameResult gr = game.play(p1, p2);

                totals.put(gr.getP1Name(), totals.get(gr.getP1Name()) + gr.getTotalScoreP1());
                totals.put(gr.getP2Name(), totals.get(gr.getP2Name()) + gr.getTotalScoreP2());
            }
        }

        TournamentResult tr = new TournamentResult(totals);

        // let observers do end-of-tournament stuff
        game.fireTournamentOver(tr);

        return tr;
    }
}