package sits;

import java.util.ArrayList;
import java.util.List;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.observer.GameObserver;
import sits.tournament.TournamentResult;

public class FakeObserver implements GameObserver {
    public final List<MoveEvent> moves = new ArrayList<>();
    public final List<GameResult> games = new ArrayList<>();
    public TournamentResult tournament = null;

    @Override
    public void onMoveMade(MoveEvent e) {
        moves.add(e);
    }

    @Override
    public void onGameOver(GameResult e) {
        games.add(e);
    }

    @Override
    public void onTournamentOver(TournamentResult e) {
        tournament = e;
    }
}