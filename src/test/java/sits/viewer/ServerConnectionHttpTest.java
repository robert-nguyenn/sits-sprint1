package sits.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;
import sits.remote.dto.MoveEventDTO;

class ServerConnectionHttpTest {

    private HttpServer server;
    private String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // JavaFX toolkit already initialized
        }
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        baseUrl = "http://127.0.0.1:" + port;

        server.createContext("/tournaments", exchange -> {
            String body = "[{\"id\":\"t1\",\"name\":\"Test\",\"status\":\"RUNNING\"}]";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.createContext("/tournaments/t1/stream", exchange -> {
            MoveEventDTO dto = new MoveEventDTO();
            dto.setType("MOVE");
            dto.setRoundNumber(1);
            dto.setP1Name("P1");
            dto.setP2Name("P2");
            dto.setAction1("COOPERATE");
            dto.setAction2("DEFECT");
            dto.setPayoffP1(0);
            dto.setPayoffP2(5);

            String json = mapper.writeValueAsString(dto);
            String sse = "data: " + json + "\n\n";
            byte[] bytes = sse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchTournamentsReturnsList() throws Exception {
        ServerConnection conn = new ServerConnection(baseUrl);
        List<TournamentInfo> list = conn.fetchTournaments();

        assertEquals(1, list.size());
        assertEquals("t1", list.get(0).id);
    }

    @Test
    void streamMovesReceivesEventAndDone() throws Exception {
        ServerConnection conn = new ServerConnection(baseUrl);
        CountDownLatch eventLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(1);
        AtomicReference<MoveEventDTO> received = new AtomicReference<>();

        conn.streamMoves("t1", dto -> {
            received.set(dto);
            eventLatch.countDown();
        }, doneLatch::countDown);

        assertTrue(eventLatch.await(3, TimeUnit.SECONDS));
        assertTrue(doneLatch.await(3, TimeUnit.SECONDS));
        assertEquals("MOVE", received.get().getType());
    }
}
