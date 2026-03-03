package sits.game;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.participant.Participant;

public class IteratedPrisonersDilemma extends Game {

    private final int maxRounds;

    public IteratedPrisonersDilemma(int maxRounds) {
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be > 0");
        }
        this.maxRounds = maxRounds;
    }

    @Override
    protected RoundResult doRound(Participant p1, Participant p2, GameHistory history, int roundNum) {
        Action a1 = p1.chooseAction(history);
        Action a2 = p2.chooseAction(history);

        int[] payoff = getPayoff(a1, a2);

        return new RoundResult(a1, a2, payoff[0], payoff[1], roundNum);
    }

    @Override
    protected boolean isOver(GameHistory history) {
        return history.getRounds().size() >= maxRounds;
    }

    @Override
    protected GameResult computeFinalResult(GameHistory history) {
        int s1 = 0;
        int s2 = 0;

        for (RoundResult rr : history.getRounds()) {
            s1 += rr.getScoreP1();
            s2 += rr.getScoreP2();
        }

        String winner;
        if (s1 > s2) winner = history.getP1Name();
        else if (s2 > s1) winner = history.getP2Name();
        else winner = "TIE";

        return new GameResult(history.getP1Name(), history.getP2Name(), s1, s2, winner);
    }

    // ONLY place that knows PrisonerAction
    private int[] getPayoff(Action a1, Action a2) {
        if (a1 == PrisonerAction.COOPERATE && a2 == PrisonerAction.COOPERATE) return new int[]{3, 3};
        if (a1 == PrisonerAction.COOPERATE && a2 == PrisonerAction.DEFECT)    return new int[]{0, 5};
        if (a1 == PrisonerAction.DEFECT && a2 == PrisonerAction.COOPERATE)    return new int[]{5, 0};
        if (a1 == PrisonerAction.DEFECT && a2 == PrisonerAction.DEFECT)       return new int[]{1, 1};

        throw new IllegalArgumentException("Bad actions: " + a1.getLabel() + ", " + a2.getLabel());
    }
}