package sits.viewer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentInfo {
    public String id;
    public String name;
    public String status;

    public TournamentInfo() {
    }

    public TournamentInfo(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}
