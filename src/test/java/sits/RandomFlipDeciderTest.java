package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import sits.participant.decorator.RandomFlipDecider;

class RandomFlipDeciderTest {

    @Test
    void rateOfZeroNeverReturnsTrue() {
        RandomFlipDecider decider = new RandomFlipDecider(0.0, 42L);

        for (int i = 0; i < 1000; i++) {
            assertFalse(decider.get());
        }
    }

    @Test
    void rateOfOneAlwaysReturnsTrue() {
        RandomFlipDecider decider = new RandomFlipDecider(1.0, 42L);

        for (int i = 0; i < 1000; i++) {
            assertTrue(decider.get());
        }
    }

    @Test
    void fivePercentRateProducesRoughlyFivePercentTrues() {
        RandomFlipDecider decider = new RandomFlipDecider(0.05, 42L);

        int trues = 0;
        int trials = 10_000;
        for (int i = 0; i < trials; i++) {
            if (decider.get()) {
                trues++;
            }
        }

        // Expected ~500. Wide window to keep this from flaking.
        assertTrue(trues >= 400, "expected at least 400 trues, got " + trues);
        assertTrue(trues <= 600, "expected at most 600 trues, got " + trues);
    }

    @Test
    void sameSeedProducesSameSequence() {
        RandomFlipDecider a = new RandomFlipDecider(0.5, 12345L);
        RandomFlipDecider b = new RandomFlipDecider(0.5, 12345L);

        for (int i = 0; i < 100; i++) {
            assertEquals(a.get(), b.get());
        }
    }
}
