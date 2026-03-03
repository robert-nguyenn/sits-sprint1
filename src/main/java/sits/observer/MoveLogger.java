package sits.observer;

import java.io.FileWriter;
import java.io.IOException;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.game.RoundResult;
import sits.tournament.TournamentResult;

public class MoveLogger implements GameObserver {

    private final FileWriter writer;

    public MoveLogger(String filePath) {
        try {
            this.writer = new FileWriter(filePath, false);
        } catch (IOException e) {
            throw new RuntimeException("Could not open MoveLogger file: " + filePath, e);
        }
    }

    @Override
    public void onMoveMade(MoveEvent e) {
        RoundResult rr = e.getRoundResult();
        try {
            writer.write("Round " + rr.getRoundNumber()
                    + ": " + e.getGameHistory().getP1Name() + "=" + rr.getActionP1().getLabel()
                    + ", " + e.getGameHistory().getP2Name() + "=" + rr.getActionP2().getLabel()
                    + " | (" + rr.getScoreP1() + "," + rr.getScoreP2() + ")\n");
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("MoveLogger write failed", ex);
        }
    }

    @Override
    public void onGameOver(GameResult e) {
        // this logger is for moves only
    }

    @Override
    public void onTournamentOver(TournamentResult e) {
        try {
            writer.write("TOURNAMENT OVER\n");
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException("MoveLogger close failed", ex);
        }
    }
}