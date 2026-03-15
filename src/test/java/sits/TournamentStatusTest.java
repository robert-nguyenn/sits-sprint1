package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.TournamentStatus;

class TournamentStatusTest {

    @Test
    void containsExpectedLifecycleValues() {
        assertEquals(TournamentStatus.REGISTERING, TournamentStatus.valueOf("REGISTERING"));
        assertEquals(TournamentStatus.RUNNING, TournamentStatus.valueOf("RUNNING"));
        assertEquals(TournamentStatus.COMPLETED, TournamentStatus.valueOf("COMPLETED"));
    }
}
