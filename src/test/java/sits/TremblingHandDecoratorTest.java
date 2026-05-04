package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.participant.AlwaysCooperate;
import sits.participant.decorator.TremblingHandDecorator;

class TremblingHandDecoratorTest {

    private static final Function<Action, Action> FLIPPER = a ->
            a == PrisonerAction.COOPERATE ? PrisonerAction.DEFECT : PrisonerAction.COOPERATE;

    @Test
    void neverFlippingReturnsWrappedAction() {
        Supplier<Boolean> neverFlip = () -> false;
        TremblingHandDecorator decorator = new TremblingHandDecorator(
                new AlwaysCooperate(), neverFlip, FLIPPER);

        for (int i = 0; i < 5; i++) {
            assertEquals(PrisonerAction.COOPERATE, decorator.chooseAction(new GameHistory("a", "b")));
        }
    }

    @Test
    void alwaysFlippingReturnsOppositeAction() {
        Supplier<Boolean> alwaysFlip = () -> true;
        TremblingHandDecorator decorator = new TremblingHandDecorator(
                new AlwaysCooperate(), alwaysFlip, FLIPPER);

        for (int i = 0; i < 5; i++) {
            assertEquals(PrisonerAction.DEFECT, decorator.chooseAction(new GameHistory("a", "b")));
        }
    }

    @Test
    void scriptedSupplierFlipsExactlyAtScheduledCalls() {
        // [COOPERATE, DEFECT, COOPERATE, DEFECT] - flips are scheduled on calls 2 and 4.
        Iterator<Boolean> script = List.of(false, true, false, true).iterator();
        TremblingHandDecorator decorator = new TremblingHandDecorator(
                new AlwaysCooperate(), script::next, FLIPPER);

        assertEquals(PrisonerAction.COOPERATE, decorator.chooseAction(new GameHistory("a", "b")));
        assertEquals(PrisonerAction.DEFECT, decorator.chooseAction(new GameHistory("a", "b")));
        assertEquals(PrisonerAction.COOPERATE, decorator.chooseAction(new GameHistory("a", "b")));
        assertEquals(PrisonerAction.DEFECT, decorator.chooseAction(new GameHistory("a", "b")));
    }

    @Test
    void getNameForwardsToWrappedDespiteFlipping() {
        TremblingHandDecorator decorator = new TremblingHandDecorator(
                new AlwaysCooperate(), () -> true, FLIPPER);

        assertEquals("AlwaysCooperate", decorator.getName());
    }
}
