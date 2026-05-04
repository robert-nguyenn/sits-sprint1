package sits.participant.decorator;

import java.io.PrintStream;

import sits.action.Action;
import sits.game.GameHistory;
import sits.participant.Participant;

public class LoggingDecorator extends ParticipantDecorator {

    private final PrintStream out;

    public LoggingDecorator(Participant wrapped, PrintStream out) {
        super(wrapped);
        this.out = out;
    }

    @Override
    public Action chooseAction(GameHistory history) {
        Action chosen = wrapped.chooseAction(history);
        out.printf("[%s] chose %s%n", wrapped.getName(), chosen.getLabel());
        return chosen;
    }
}
