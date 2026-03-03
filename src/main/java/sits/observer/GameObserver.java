package sits.observer;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.tournament.TournamentResult;

public interface GameObserver {
    void onMoveMade(MoveEvent e);
    void onGameOver(GameResult e);
    void onTournamentOver(TournamentResult e);
}