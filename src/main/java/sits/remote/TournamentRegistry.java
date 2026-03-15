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
