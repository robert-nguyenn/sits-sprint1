package sits.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import sits.action.Action;
import sits.game.Game;
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

    public NetworkedTournament() {
        this.participants = new ArrayList<>();
        this.status = TournamentStatus.REGISTERING;
        this.actionFactory = label -> () -> label;
    }

    public NetworkedTournament(String id, String name, TournamentFormat format, Game game, Function<String, Action> actionFactory) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.format = Objects.requireNonNull(format, "format");
        this.game = Objects.requireNonNull(game, "game");
        this.actionFactory = Objects.requireNonNull(actionFactory, "actionFactory");
        this.participants = new ArrayList<>();
        this.status = TournamentStatus.REGISTERING;
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
        TournamentResult result = format.run(participants, game);
        status = TournamentStatus.COMPLETED;
        return result;
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public List<Participant> getParticipantsView() {
        return Collections.unmodifiableList(participants);
    }
}
