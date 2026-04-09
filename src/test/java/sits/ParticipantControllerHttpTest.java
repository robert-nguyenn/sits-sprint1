package sits;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.RoundResult;
import sits.participant.Participant;
import sits.remote.ParticipantController;
import sits.remote.RemoteParticipant;
import sits.remote.dto.GameHistoryDTO;
import sits.remote.dto.RoundResultDTO;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = ParticipantControllerHttpTest.TestApp.class
)
class ParticipantControllerHttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestParticipant participant;

    @LocalServerPort
    private int port;

    @Test
    void actionEndpoint_usesHistory() {
        GameHistoryDTO dto1 = new GameHistoryDTO(
                "P1",
                "P2",
                List.of(new RoundResultDTO("COOPERATE", "COOPERATE", 3, 3))
        );

        ResponseEntity<String> response1 = restTemplate.postForEntity("/action", dto1, String.class);
        assertEquals(200, response1.getStatusCode().value());
        assertEquals("DEFECT", response1.getBody());

        GameHistoryDTO dto2 = new GameHistoryDTO(
                "P1",
                "P2",
                List.of(new RoundResultDTO("DEFECT", "DEFECT", 1, 1))
        );

        ResponseEntity<String> response2 = restTemplate.postForEntity("/action", dto2, String.class);
        assertEquals(200, response2.getStatusCode().value());
        assertEquals("COOPERATE", response2.getBody());
    }

    @Test
    void resetEndpoint_callsParticipant() {
        ResponseEntity<Void> response = restTemplate.postForEntity("/reset", null, Void.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(participant.isResetCalled());
    }

    @Test
    void remoteParticipant_callsLiveHttpEndpoint() {
        String baseUrl = "http://localhost:" + port;
        RemoteParticipant remote = new RemoteParticipant(
                "Remote",
                baseUrl,
                PrisonerAction::valueOf
        );

        GameHistory history = new GameHistory("A", "B");
        history.addRound(new RoundResult(
                PrisonerAction.COOPERATE,
                PrisonerAction.COOPERATE,
                3,
                3,
                1
        ));

        Action action = remote.chooseAction(history);
        assertEquals(PrisonerAction.DEFECT, action);

        remote.reset();
        assertTrue(participant.isResetCalled());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(ParticipantController.class)
    static class TestApp {

        @Bean
        TestParticipant participant() {
            return new TestParticipant();
        }
    }

    static class TestParticipant implements Participant {

        private boolean resetCalled = false;

        @Override
        public String getName() {
            return "FlipParticipant";
        }

        @Override
        public Action chooseAction(GameHistory history) {
            if (history.getRounds().isEmpty()) {
                return PrisonerAction.COOPERATE;
            }
            String lastOpponent = history.getLastRound().getActionP2().getLabel();
            return "COOPERATE".equals(lastOpponent)
                    ? PrisonerAction.DEFECT
                    : PrisonerAction.COOPERATE;
        }

        @Override
        public void reset() {
            resetCalled = true;
        }

        boolean isResetCalled() {
            return resetCalled;
        }
    }
}