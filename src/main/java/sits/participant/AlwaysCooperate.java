package sits.participant;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;

public class AlwaysCooperate implements Participant {

    @Override
    public String getName() {
        return "AlwaysCooperate";
    }

    @Override
    public Action chooseAction(GameHistory history) {
        return PrisonerAction.COOPERATE;
    }

    @Override
    public void reset() {
        // nothing to reset (stateless)
    }
}