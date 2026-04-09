package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.TournamentStatus;

// Tests the enum values used for tournament lifecycle states.
class TournamentStatusTest {

    @Test
    void containsExpectedLifecycleValues() {
        // Checks that all expected status names map to valid enum constants.
        assertEquals(TournamentStatus.REGISTERING, TournamentStatus.valueOf("REGISTERING"));
        assertEquals(TournamentStatus.RUNNING, TournamentStatus.valueOf("RUNNING"));
        assertEquals(TournamentStatus.COMPLETED, TournamentStatus.valueOf("COMPLETED"));
    }
}
