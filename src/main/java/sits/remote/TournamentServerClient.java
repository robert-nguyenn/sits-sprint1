package sits.remote;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TournamentServerClient {

    private final String serverUrl;
    private final RestTemplate restTemplate;

    public TournamentServerClient(String serverUrl) {
        this(serverUrl, new RestTemplate());
    }

    public TournamentServerClient(String serverUrl, RestTemplate restTemplate) {
        this.serverUrl = serverUrl;
        this.restTemplate = restTemplate;
    }

    public List<NetworkedTournament> listTournaments() {
        ResponseEntity<NetworkedTournament[]> response = restTemplate.exchange(
                serverUrl + "/tournaments",
                HttpMethod.GET,
                null,
                NetworkedTournament[].class
        );

        NetworkedTournament[] body = response.getBody();
        if (body == null) {
            return List.of();
        }

        return Arrays.asList(body);
    }

    public void register(String tournamentId, String name, String ip, int port) {
        RegistrationRequest request = new RegistrationRequest(name, ip, port);

        RequestEntity<RegistrationRequest> entity = RequestEntity
                .post(serverUrl + "/tournaments/" + tournamentId + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request);

        restTemplate.exchange(entity, String.class);
    }

    public void start(String tournamentId) {
        HttpEntity<String> entity = new HttpEntity<>(null);
        restTemplate.exchange(
                serverUrl + "/tournaments/" + tournamentId + "/start",
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}
