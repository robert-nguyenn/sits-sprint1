package sits;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import sits.remote.NetworkedTournament;
import sits.remote.TournamentServerClient;

// Tests for the HTTP client that talks to the tournament server.
class TournamentServerClientTest {

    @Test
    void listTournamentsParsesServerResponse() {
        // Checks that JSON response from /tournaments is parsed into objects correctly.
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:8080/tournaments"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess(
                      "[{\"id\":\"ipd-1\",\"name\":\"IPD\",\"status\":\"REGISTERING\"}]",
                      MediaType.APPLICATION_JSON
              ));

        TournamentServerClient client = new TournamentServerClient("http://localhost:8080", restTemplate);

        var tournaments = client.listTournaments();

        assertEquals(1, tournaments.size());
        assertEquals("ipd-1", tournaments.get(0).getId());
        server.verify();
    }

    @Test
    void registerAndStartCallExpectedEndpoints() {
        // Checks that register/start send POST requests to the expected endpoints.
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:8080/tournaments/ipd-1/register"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(content().string(containsString("\"name\":\"RemoteA\"")))
              .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        server.expect(requestTo("http://localhost:8080/tournaments/ipd-1/start"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withSuccess("started", MediaType.TEXT_PLAIN));

        TournamentServerClient client = new TournamentServerClient("http://localhost:8080", restTemplate);

        assertDoesNotThrow(() -> client.register("ipd-1", "RemoteA", "127.0.0.1", 9020));
        assertDoesNotThrow(() -> client.start("ipd-1"));

        server.verify();
    }

    @Test
    void listTournamentsReturnsEmptyWhenResponseBodyIsNull() {
        // Checks null response body is handled safely as an empty list.
        RestTemplate mockTemplate = mock(RestTemplate.class);
        when(mockTemplate.exchange(
                eq("http://localhost:8080/tournaments"),
                eq(HttpMethod.GET),
                isNull(),
                eq(NetworkedTournament[].class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        TournamentServerClient client = new TournamentServerClient("http://localhost:8080", mockTemplate);
        assertTrue(client.listTournaments().isEmpty());
    }
    
    @Test
    void oneArgConstructorCreatesClient() {
        // Checks the convenience constructor creates a usable instance.
        TournamentServerClient client = new TournamentServerClient("http://localhost:8080");
        assertDoesNotThrow(() -> client.toString()); // just ensure object constructed
    }
}
