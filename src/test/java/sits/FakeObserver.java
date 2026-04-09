package sits;

import java.util.ArrayList;
import java.util.List;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.observer.GameObserver;
import sits.tournament.TournamentResult;
//is not a test case by itself. It is a test helper used by observer-related tests.
// Simple test helper that records observer callbacks for assertions.
public class FakeObserver implements GameObserver {
    public final List<MoveEvent> moves = new ArrayList<>();
    public final List<GameResult> games = new ArrayList<>();
    public TournamentResult tournament = null;

    @Override
    public void onMoveMade(MoveEvent e) {
        // Keep track of each move the game reports.
        moves.add(e);
    }

    @Override
    public void onGameOver(GameResult e) {
        // Keep track of finished games.
        games.add(e);
    }

    @Override
    public void onTournamentOver(TournamentResult e) {
        // Save the final tournament result.
        tournament = e;
    }
}