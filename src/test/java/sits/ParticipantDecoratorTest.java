package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.participant.AlwaysCooperate;
import sits.participant.Participant;
import sits.participant.decorator.ParticipantDecorator;

class ParticipantDecoratorTest {

    @Test
    void getNameForwardsToWrappedParticipant() {
        ParticipantDecorator decorator = new NoOpDecorator(new AlwaysCooperate());

        assertEquals("AlwaysCooperate", decorator.getName());
    }

    @Test
    void chooseActionForwardsToWrappedParticipant() {
        ParticipantDecorator decorator = new NoOpDecorator(new AlwaysCooperate());

        Action result = decorator.chooseAction(new GameHistory("a", "b"));

        assertEquals(PrisonerAction.COOPERATE, result);
    }

    @Test
    void resetForwardsToWrappedParticipant() {
        ResetTrackingParticipant tracked = new ResetTrackingParticipant();
        ParticipantDecorator decorator = new NoOpDecorator(tracked);

        assertFalse(tracked.wasReset);
        decorator.reset();
        assertTrue(tracked.wasReset);
    }

    private static final class NoOpDecorator extends ParticipantDecorator {
        NoOpDecorator(Participant wrapped) {
            super(wrapped);
        }
    }

    private static final class ResetTrackingParticipant implements Participant {
        boolean wasReset = false;

        @Override
        public String getName() {
            return "Tracked";
        }

        @Override
        public Action chooseAction(GameHistory history) {
            return PrisonerAction.COOPERATE;
        }

        @Override
        public void reset() {
            wasReset = true;
        }
    }
}
