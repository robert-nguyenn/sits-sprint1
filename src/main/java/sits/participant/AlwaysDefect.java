package sits.participant;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;

public class AlwaysDefect implements Participant {

    @Override
    public String getName() {
        return "AlwaysDefect";
    }

    @Override
    public Action chooseAction(GameHistory history) {
        return PrisonerAction.DEFECT;
    }

    @Override
    public void reset() {
        // nothing to reset (stateless)
    }
}