package sits.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHistory {
    private final List<RoundResult> rounds = new ArrayList<>();
    private final String p1Name;
    private final String p2Name;

    public GameHistory(String p1Name, String p2Name) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
    }

    public void addRound(RoundResult rr) {
        rounds.add(rr);
    }

    public List<RoundResult> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    public RoundResult getLastRound() {
        if (rounds.isEmpty()) {
            throw new IllegalStateException("No rounds yet");
        }
        return rounds.get(rounds.size() - 1);
    }

    public String getP1Name() { return p1Name; }
    public String getP2Name() { return p2Name; }
}