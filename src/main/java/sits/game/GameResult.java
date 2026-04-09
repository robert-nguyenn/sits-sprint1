// Final summary after match ends.
// Contains total scores and winner.
// No per-round details.

package sits.game;

public class GameResult {
    private final String p1Name;
    private final String p2Name;
    private final int totalScoreP1;
    private final int totalScoreP2;
    private final String winner; // or "TIE"

    public GameResult(String p1Name, String p2Name, int totalScoreP1, int totalScoreP2, String winner) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.totalScoreP1 = totalScoreP1;
        this.totalScoreP2 = totalScoreP2;
        this.winner = winner;
    }

    public String getP1Name() { return p1Name; }
    public String getP2Name() { return p2Name; }
    public int getTotalScoreP1() { return totalScoreP1; }
    public int getTotalScoreP2() { return totalScoreP2; }
    public String getWinner() { return winner; }
}