package sits.remote;

import java.util.Objects;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import sits.action.Action;
import sits.game.GameHistory;
import sits.participant.Participant;
import sits.remote.dto.GameHistoryDTO;

public class RemoteParticipant implements Participant {

    private final String name;
    private final String clientUrl;
    private final Function<String, Action> actionFactory;
    private final RestTemplate restTemplate;

    public RemoteParticipant(String name, String clientUrl, Function<String, Action> actionFactory) {
        this(name, clientUrl, actionFactory, new RestTemplate());
    }

    public RemoteParticipant(String name, String clientUrl, Function<String, Action> actionFactory, RestTemplate restTemplate) {
        this.name = Objects.requireNonNull(name, "name");
        this.clientUrl = Objects.requireNonNull(clientUrl, "clientUrl");
        this.actionFactory = Objects.requireNonNull(actionFactory, "actionFactory");
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Action chooseAction(GameHistory history) {
        GameHistoryDTO dto = GameHistoryDTO.fromGameHistory(history);

        String label = restTemplate.postForObject(
                clientUrl + "/action",
                dto,
                String.class
        );

        if (label == null) {
            throw new IllegalStateException("Remote participant returned null action label");
        }

        return actionFactory.apply(label);
    }

    @Override
    public void reset() {
        ResponseEntity<Void> response = restTemplate.postForEntity(clientUrl + "/reset", null, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to reset remote participant: " + name);
        }
    }
}
