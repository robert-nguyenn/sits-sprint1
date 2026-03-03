package sits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import sits.game.IteratedPrisonersDilemma;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;

public class IteratedPrisonersDilemmaTest {

    @Test
    void cooperateVsCooperate_is33EachRound() {
        var game = new IteratedPrisonersDilemma(5);
        var res = game.play(new AlwaysCooperate(), new AlwaysCooperate());

        assertEquals(15, res.getTotalScoreP1());
        assertEquals(15, res.getTotalScoreP2());
        assertEquals("TIE", res.getWinner());
    }

    @Test
    void cooperateVsDefect_is05EachRound() {
        var game = new IteratedPrisonersDilemma(4);
        var res = game.play(new AlwaysCooperate(), new AlwaysDefect());

        assertEquals(0, res.getTotalScoreP1());
        assertEquals(20, res.getTotalScoreP2());
        assertEquals("AlwaysDefect", res.getWinner());
    }
}