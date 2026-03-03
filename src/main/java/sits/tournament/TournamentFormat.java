package sits.tournament;

import java.util.List;

import sits.game.Game;
import sits.participant.Participant;

public interface TournamentFormat {
    TournamentResult run(List<Participant> participants, Game game);
}