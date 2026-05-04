package sits.participant.decorator;

import java.util.function.Function;
import java.util.function.Supplier;

import sits.action.Action;
import sits.game.GameHistory;
import sits.participant.Participant;

public class TremblingHandDecorator extends ParticipantDecorator {

    private final Supplier<Boolean> shouldFlip;
    private final Function<Action, Action> flipper;

    public TremblingHandDecorator(Participant wrapped,
                                  Supplier<Boolean> shouldFlip,
                                  Function<Action, Action> flipper) {
        super(wrapped);
        this.shouldFlip = shouldFlip;
        this.flipper = flipper;
    }

    @Override
    public Action chooseAction(GameHistory history) {
        Action chosen = wrapped.chooseAction(history);
        if (shouldFlip.get()) {
            return flipper.apply(chosen);
        }
        return chosen;
    }
}
