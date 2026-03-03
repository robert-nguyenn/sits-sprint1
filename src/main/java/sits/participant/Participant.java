package sits.participant;

import sits.action.Action;
import sits.game.GameHistory;

public interface Participant {
    String getName();

    Action chooseAction(GameHistory history);

    void reset();
}