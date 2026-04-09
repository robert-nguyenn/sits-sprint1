// is a small event object sent to observers after each round.

// It carries:

// the latest RoundResult
// the current GameHistory


package sits.game;

public class MoveEvent {
    private final RoundResult roundResult;
    private final GameHistory gameHistory;

    public MoveEvent(RoundResult roundResult, GameHistory gameHistory) {
        this.roundResult = roundResult;
        this.gameHistory = gameHistory;
    }

    public RoundResult getRoundResult() { return roundResult; }
    public GameHistory getGameHistory() { return gameHistory; }
}