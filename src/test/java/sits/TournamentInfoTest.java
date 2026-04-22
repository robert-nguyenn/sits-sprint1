package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import sits.viewer.TournamentInfo;

class TournamentInfoTest {

    @Test
    void constructorSetsFields() {
        TournamentInfo info = new TournamentInfo("ipd-1", "My Tournament", "RUNNING");

        assertEquals("ipd-1", info.id);
        assertEquals("My Tournament", info.name);
        assertEquals("RUNNING", info.status);
    }

    @Test
    void noArgConstructorInitializes() {
        TournamentInfo info = new TournamentInfo();

        assertNull(info.id);
        assertNull(info.name);
        assertNull(info.status);
    }

    @Test
    void toStringFormatting() {
        TournamentInfo info = new TournamentInfo("ipd-2", "Tournament 2", "REGISTERING");

        String result = info.toString();

        assertTrue(result.contains("Tournament 2"));
        assertTrue(result.contains("REGISTERING"));
    }

    @Test
    void fieldsMutable() {
        TournamentInfo info = new TournamentInfo("id1", "name1", "RUNNING");

        info.id = "id2";
        info.name = "name2";
        info.status = "COMPLETED";

        assertEquals("id2", info.id);
        assertEquals("name2", info.name);
        assertEquals("COMPLETED", info.status);
    }
}
