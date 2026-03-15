package sits;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.remote.RemoteParticipant;

class RemoteParticipantTest {

    @Test
    void getNameDoesNotRequireNetworkCall() {
        RestTemplate restTemplate = new RestTemplate();
        RemoteParticipant remote = new RemoteParticipant(
                "RemoteOne",
                "http://127.0.0.1:7000",
                PrisonerAction::valueOf,
                restTemplate
        );

        assertEquals("RemoteOne", remote.getName());
    }

    @Test
    void chooseActionPerformsRoundTripAndMapsLabel() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://127.0.0.1:7001/action"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withSuccess("DEFECT", MediaType.TEXT_PLAIN));

        RemoteParticipant remote = new RemoteParticipant(
                "RemoteOne",
                "http://127.0.0.1:7001",
                PrisonerAction::valueOf,
                restTemplate
        );

        var action = remote.chooseAction(new GameHistory("P1", "P2"));

        assertEquals(PrisonerAction.DEFECT, action);
        server.verify();
    }

    @Test
    void resetCallsRemoteResetEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://127.0.0.1:7002/reset"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withNoContent());

        RemoteParticipant remote = new RemoteParticipant(
                "RemoteOne",
                "http://127.0.0.1:7002",
                PrisonerAction::valueOf,
                restTemplate
        );

        assertDoesNotThrow(remote::reset);
        server.verify();
    }

    @Test
    void chooseActionThrowsWhenRemoteReturnsNullLabel() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://127.0.0.1:7003/action"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withNoContent());

        RemoteParticipant remote = new RemoteParticipant(
                "RemoteOne",
                "http://127.0.0.1:7003",
                PrisonerAction::valueOf,
                restTemplate
        );

        assertThrows(IllegalStateException.class, () -> remote.chooseAction(new GameHistory("P1", "P2")));
        server.verify();
    }

    @Test
    void resetThrowsWhenRemoteReturnsNon2xx() {
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
                return ResponseEntity.badRequest().build();
            }
        };

        RemoteParticipant remote = new RemoteParticipant(
                "RemoteOne",
                "http://127.0.0.1:7004",
                PrisonerAction::valueOf,
                restTemplate
        );

        assertThrows(IllegalStateException.class, remote::reset);
    }
}
