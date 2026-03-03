package sits.game;

import sits.action.Action;

public class RoundResult {
    private final Action actionP1;
    private final Action actionP2;
    private final int scoreP1;
    private final int scoreP2;
    private final int roundNumber;

    public RoundResult(Action actionP1, Action actionP2, int scoreP1, int scoreP2, int roundNumber) {
        this.actionP1 = actionP1;
        this.actionP2 = actionP2;
        this.scoreP1 = scoreP1;
        this.scoreP2 = scoreP2;
        this.roundNumber = roundNumber;
    }

    public Action getActionP1() { return actionP1; }
    public Action getActionP2() { return actionP2; }
    public int getScoreP1() { return scoreP1; }
    public int getScoreP2() { return scoreP2; }
    public int getRoundNumber() { return roundNumber; }
}