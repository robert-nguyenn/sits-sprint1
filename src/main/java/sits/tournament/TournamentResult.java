package sits.tournament;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TournamentResult {
    private final Map<String, Integer> scores;

    public TournamentResult(Map<String, Integer> scores) {
        this.scores = new HashMap<>(scores);
    }

    public int getScore(String name) {
        return scores.getOrDefault(name, 0);
    }

    public List<String> getRankings() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // highest first

        List<String> names = new ArrayList<>();
        for (var e : list) {
            names.add(e.getKey());
        }
        return names;
    }

    public Map<String, Integer> getScoresCopy() {
        return new HashMap<>(scores);
    }
}