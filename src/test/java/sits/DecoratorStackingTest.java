package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.participant.AlwaysCooperate;
import sits.participant.decorator.LoggingDecorator;
import sits.participant.decorator.TremblingHandDecorator;

class DecoratorStackingTest {

    private static final Function<Action, Action> FLIPPER = a ->
            a == PrisonerAction.COOPERATE ? PrisonerAction.DEFECT : PrisonerAction.COOPERATE;

    @Test
    void loggingOutsideTremblingSeesPostFlipAction() {
        // Trembling runs first, then Logging records what Game will see.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        LoggingDecorator stacked = new LoggingDecorator(
                new TremblingHandDecorator(new AlwaysCooperate(), () -> true, FLIPPER),
                new PrintStream(buffer));

        Action result = stacked.chooseAction(new GameHistory("a", "b"));

        assertEquals(PrisonerAction.DEFECT, result);
        String output = buffer.toString();
        assertTrue(output.contains("DEFECT"), "log should record the flipped action");
        assertFalse(output.contains("COOPERATE"), "log should not see the pre-flip action");
    }

    @Test
    void loggingInsideTremblingSeesPreFlipAction() {
        // Logging runs first on what AlwaysCooperate returned, then Trembling flips on the way out.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        TremblingHandDecorator stacked = new TremblingHandDecorator(
                new LoggingDecorator(new AlwaysCooperate(), new PrintStream(buffer)),
                () -> true,
                FLIPPER);

        Action result = stacked.chooseAction(new GameHistory("a", "b"));

        assertEquals(PrisonerAction.DEFECT, result);
        String output = buffer.toString();
        assertTrue(output.contains("COOPERATE"), "log should record the pre-flip action");
        assertFalse(output.contains("DEFECT"), "log should not see the post-flip action");
    }

    @Test
    void deeplyNestedDecoratorsForwardName() {
        TremblingHandDecorator stacked = new TremblingHandDecorator(
                new LoggingDecorator(
                        new TremblingHandDecorator(new AlwaysCooperate(), () -> false, FLIPPER),
                        new PrintStream(new ByteArrayOutputStream())),
                () -> false,
                FLIPPER);

        assertEquals("AlwaysCooperate", stacked.getName());
    }
}
