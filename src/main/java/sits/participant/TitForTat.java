package sits.participant;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;

public class TitForTat implements Participant {

    @Override
    public String getName() {
        return "TitForTat";
    }

    @Override
    public Action chooseAction(GameHistory history) {
        if (history.getRounds().isEmpty()) {
            return PrisonerAction.COOPERATE;
        }
        // mirror opponent's last move (p2)
        return history.getLastRound().getActionP2();
    }

    @Override
    public void reset() {
        // stateless in this version
    }
}

