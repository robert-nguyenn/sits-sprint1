package sits.participant.decorator;

import java.util.Random;
import java.util.function.Supplier;

public final class RandomFlipDecider implements Supplier<Boolean> {

    private final double rate;
    private final Random rng;

    public RandomFlipDecider(double rate, long seed) {
        this.rate = rate;
        this.rng = new Random(seed);
    }

    @Override
    public Boolean get() {
        return rng.nextDouble() < rate;
    }
}
