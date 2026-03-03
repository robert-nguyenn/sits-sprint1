package sits.observer;

import java.io.FileWriter;
import java.io.IOException;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.tournament.TournamentResult;

public class ScoreLogger implements GameObserver {

    private final FileWriter writer;

    public ScoreLogger(String filePath) {
        try {
            this.writer = new FileWriter(filePath, false);
        } catch (IOException e) {
            throw new RuntimeException("Could not open ScoreLogger file: " + filePath, e);
        }
    }

    @Override
    public void onMoveMade(MoveEvent e) {
        // separate stream
    }

    @Override
    public void onGameOver(GameResult e) {
        try {
            writer.write("Game Over: " + e.getP1Name() + " vs " + e.getP2Name()
                    + " total (" + e.getTotalScoreP1() + "," + e.getTotalScoreP2()
                    + ") winner=" + e.getWinner() + "\n");
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("ScoreLogger write failed", ex);
        }
    }

    @Override
    public void onTournamentOver(TournamentResult e) {
        try {
            writer.write("Final Rankings:\n");
            for (String name : e.getRankings()) {
                writer.write(name + ": " + e.getScore(name) + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException("ScoreLogger close failed", ex);
        }
    }
}