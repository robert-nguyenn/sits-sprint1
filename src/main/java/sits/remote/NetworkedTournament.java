package sits.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import sits.action.Action;
import sits.game.Game;
import sits.observer.ViewerBroadcaster;
import sits.participant.Participant;
import sits.tournament.TournamentFormat;
import sits.tournament.TournamentResult;

public class NetworkedTournament {

    private String id;
    private String name;

    @JsonIgnore
    private TournamentFormat format;

    @JsonIgnore
    private Game game;

    @JsonIgnore
    private List<Participant> participants;

    private TournamentStatus status;

    @JsonIgnore
    private Function<String, Action> actionFactory;

    @JsonIgnore
    private ViewerBroadcaster broadcaster;

    @JsonIgnore
    private long delayMs;

    public NetworkedTournament() {
        this.participants = new ArrayList<>();
        this.status = TournamentStatus.REGISTERING;
        this.actionFactory = label -> () -> label;
        this.delayMs = 0;
        this.broadcaster = new ViewerBroadcaster(delayMs);
    }

    public NetworkedTournament(String id, String name, TournamentFormat format, Game game, Function<String, Action> actionFactory) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.format = Objects.requireNonNull(format, "format");
        this.game = Objects.requireNonNull(game, "game");
        this.actionFactory = Objects.requireNonNull(actionFactory, "actionFactory");
        this.participants = new ArrayList<>();
        this.status = TournamentStatus.REGISTERING;
        this.delayMs = 0;
        this.broadcaster = new ViewerBroadcaster(delayMs);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void addLocalParticipant(Participant participant) {
        if (status != TournamentStatus.REGISTERING) {
            throw new IllegalStateException("Cannot add participant after registration phase");
        }
        participants.add(Objects.requireNonNull(participant));
    }

    public void addRemoteParticipant(RegistrationRequest req) {
        if (status != TournamentStatus.REGISTERING) {
            throw new IllegalStateException("Cannot register after tournament has started");
        }

        String clientUrl = "http://" + req.getIp() + ":" + req.getPort();
        participants.add(new RemoteParticipant(req.getName(), clientUrl, actionFactory));
    }

    public TournamentResult start() {
        if (status != TournamentStatus.REGISTERING) {
            throw new IllegalStateException("Tournament is not in registration phase");
        }

        status = TournamentStatus.RUNNING;
        game.addObserver(broadcaster);

        // try-finally so a failed remote call (e.g. a stale participant whose
        // client process died) still flips status to COMPLETED instead of
        // leaving the tournament stuck in RUNNING forever.
        try {
            return format.run(participants, game);
        } finally {
            status = TournamentStatus.COMPLETED;
        }
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public List<Participant> getParticipantsView() {
        return Collections.unmodifiableList(participants);
    }

    public ViewerBroadcaster getBroadcaster() {
        return broadcaster;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
        this.broadcaster = new ViewerBroadcaster(delayMs);
    }

    public long getDelayMs() {
        return delayMs;
    }
}
