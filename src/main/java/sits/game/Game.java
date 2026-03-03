package sits.game;

import java.util.ArrayList;
import java.util.List;

import sits.observer.GameObserver;
import sits.participant.Participant;
import sits.tournament.TournamentResult;

public abstract class Game {

    private final List<GameObserver> observers = new ArrayList<>();

    public void addObserver(GameObserver o) {
        observers.add(o);
    }

    public void removeObserver(GameObserver o) {
        observers.remove(o);
    }

    protected void notifyMoveMade(MoveEvent e) {
        for (GameObserver o : observers) {
            o.onMoveMade(e);
        }
    }

    protected void notifyGameOver(GameResult e) {
        for (GameObserver o : observers) {
            o.onGameOver(e);
        }
    }

    public void fireTournamentOver(TournamentResult e) {
        for (GameObserver o : observers) {
            o.onTournamentOver(e);
        }
    }

    // Template method: stable game loop
    public final GameResult play(Participant p1, Participant p2) {
        GameHistory history = new GameHistory(p1.getName(), p2.getName());

        int roundNum = 1;
        while (!isOver(history)) {
            RoundResult rr = doRound(p1, p2, history, roundNum);

            history.addRound(rr);

            // observers get a full up-to-date history snapshot
            notifyMoveMade(new MoveEvent(rr, history));

            roundNum++;
        }

        GameResult result = computeFinalResult(history);
        notifyGameOver(result);
        return result;
    }

    // Hooks
    protected abstract RoundResult doRound(Participant p1, Participant p2, GameHistory history, int roundNum);

    protected abstract boolean isOver(GameHistory history);

    protected abstract GameResult computeFinalResult(GameHistory history);
}