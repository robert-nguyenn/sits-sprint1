package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameResult;
import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.TitForTat;
import sits.participant.decorator.RandomFlipDecider;
import sits.participant.decorator.TremblingHandDecorator;

class TournamentWithDecoratorTest {

    private static final Function<Action, Action> FLIPPER = a ->
            a == PrisonerAction.COOPERATE ? PrisonerAction.DEFECT : PrisonerAction.COOPERATE;

    @Test
    void noisyTitForTatScoresDifferentlyThanCleanAgainstAlwaysDefect() {
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(50);

        GameResult clean = game.play(new TitForTat(), new AlwaysDefect());

        TremblingHandDecorator noisyTFT = new TremblingHandDecorator(
                new TitForTat(),
                new RandomFlipDecider(0.20, 42L),
                FLIPPER);
        GameResult noisy = game.play(noisyTFT, new AlwaysDefect());

        assertNotEquals(clean.getTotalScoreP1(), noisy.getTotalScoreP1(),
                "noisy TitForTat should score differently than the clean version");
    }

    @Test
    void scriptedFlipOnFirstRoundChangesFinalScores() {
        // First-round flip changes TFT's opening from COOPERATE to DEFECT against AlwaysCooperate.
        // After that the script returns false, so TFT mirrors normally for the rest of the match.
        IteratedPrisonersDilemma game = new IteratedPrisonersDilemma(5);

        Iterator<Boolean> script = List.of(true, false, false, false, false).iterator();
        TremblingHandDecorator firstRoundFlipper = new TremblingHandDecorator(
                new TitForTat(), script::next, FLIPPER);

        GameResult result = game.play(firstRoundFlipper, new AlwaysCooperate());

        // Round 1: TFT picks COOPERATE, flipped to DEFECT, opp COOPERATE -> (5, 0)
        // Rounds 2-5: TFT mirrors opp's COOPERATE, no flip, opp COOPERATE -> (3, 3) each
        assertEquals(5 + 3 * 4, result.getTotalScoreP1());
        assertEquals(0 + 3 * 4, result.getTotalScoreP2());
    }
}
