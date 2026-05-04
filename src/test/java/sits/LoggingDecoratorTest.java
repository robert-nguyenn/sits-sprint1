package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.participant.AlwaysCooperate;
import sits.participant.AlwaysDefect;
import sits.participant.decorator.LoggingDecorator;

class LoggingDecoratorTest {

    @Test
    void logsWrappedNameAndChosenAction() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        LoggingDecorator decorator = new LoggingDecorator(
                new AlwaysCooperate(), new PrintStream(buffer));

        decorator.chooseAction(new GameHistory("a", "b"));

        String output = buffer.toString();
        assertTrue(output.contains("AlwaysCooperate"), "log should mention the wrapped name");
        assertTrue(output.contains("COOPERATE"), "log should mention the action label");
    }

    @Test
    void writesOneLinePerDecision() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        LoggingDecorator decorator = new LoggingDecorator(
                new AlwaysDefect(), new PrintStream(buffer));

        decorator.chooseAction(new GameHistory("a", "b"));
        decorator.chooseAction(new GameHistory("a", "b"));
        decorator.chooseAction(new GameHistory("a", "b"));

        long lineCount = buffer.toString().lines().count();
        assertEquals(3, lineCount);
    }

    @Test
    void returnsActionUnchanged() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        LoggingDecorator decorator = new LoggingDecorator(
                new AlwaysCooperate(), new PrintStream(buffer));

        assertEquals(PrisonerAction.COOPERATE, decorator.chooseAction(new GameHistory("a", "b")));
    }
}
