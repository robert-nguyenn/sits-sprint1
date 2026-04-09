// RegistrationRequest is just the data sent when a remote player registers.
// It contains name, IP, and port.
// TournamentRegistry is the server’s in-memory storage of tournaments.
// It keeps track of tournaments currently known to the server.
// So one is request data, the other is server storage.

package sits.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentRegistry {

    private final Map<String, NetworkedTournament> tournaments = new ConcurrentHashMap<>();

    public void add(NetworkedTournament tournament) {
        tournaments.put(tournament.getId(), tournament);
    }

    public NetworkedTournament get(String id) {
        return tournaments.get(id);
    }

    public List<NetworkedTournament> listRegistering() {
        List<NetworkedTournament> open = new ArrayList<>();

        for (NetworkedTournament tournament : tournaments.values()) {
            if (tournament.getStatus() == TournamentStatus.REGISTERING) {
                open.add(tournament);
            }
        }

        return open;
    }
}
