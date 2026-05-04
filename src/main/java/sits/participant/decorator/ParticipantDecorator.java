package sits.participant.decorator;

import sits.action.Action;
import sits.game.GameHistory;
import sits.participant.Participant;

public abstract class ParticipantDecorator implements Participant {

    protected final Participant wrapped;

    protected ParticipantDecorator(Participant wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public Action chooseAction(GameHistory history) {
        return wrapped.chooseAction(history);
    }

    @Override
    public void reset() {
        wrapped.reset();
    }
}
