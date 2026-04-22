package sits.viewer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import sits.remote.dto.MoveEventDTO;

public class ServerConnection {
    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ServerConnection(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<TournamentInfo> fetchTournaments() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tournaments"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return new ArrayList<>();
        }

        TournamentInfo[] tournaments = mapper.readValue(response.body(), TournamentInfo[].class);
        
        List<TournamentInfo> result = new ArrayList<>();
        if (tournaments != null) {
            for (TournamentInfo t : tournaments) {
                result.add(t);
            }
        }
        return result;
    }

    public CompletableFuture<Void> streamMoves(String tournamentId,
                                               Consumer<MoveEventDTO> onEvent,
                                               Runnable onDone) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tournaments/" + tournamentId + "/stream"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body()
                            .filter(line -> line.startsWith("data:"))
                            .map(line -> line.substring(5).trim())
                            .forEach(json -> {
                                try {
                                    MoveEventDTO dto = mapper.readValue(json, MoveEventDTO.class);
                                    Platform.runLater(() -> onEvent.accept(dto));
                                } catch (Exception ignored) {
                                }
                            });

                    Platform.runLater(onDone);
                });
    }
}
