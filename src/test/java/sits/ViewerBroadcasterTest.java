package sits;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.game.RoundResult;
import sits.observer.ViewerBroadcaster;
import sits.tournament.TournamentResult;

class ViewerBroadcasterTest {

    private ViewerBroadcaster broadcaster;
    private SseEmitter mockEmitter;

    @BeforeEach
    void setup() {
        broadcaster = new ViewerBroadcaster(0); // No delay for testing
        mockEmitter = mock(SseEmitter.class);
    }

    @Test
    void addEmitterRegistersViewer() throws IOException {
        broadcaster.addEmitter(mockEmitter);
        // Test passes if no exception thrown

        // Verify by sending a move
        GameHistory history = new GameHistory("A", "B");
        RoundResult rr = new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.COOPERATE, 3, 3, 1);
        MoveEvent event = new MoveEvent(rr, history);

        broadcaster.onMoveMade(event);

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void onMoveMadeSendsEventToAllEmitters() throws IOException {
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        broadcaster.addEmitter(emitter1);
        broadcaster.addEmitter(emitter2);

        GameHistory history = new GameHistory("P1", "P2");
        RoundResult rr = new RoundResult(PrisonerAction.DEFECT, PrisonerAction.COOPERATE, 5, 0, 1);
        MoveEvent event = new MoveEvent(rr, history);

        broadcaster.onMoveMade(event);

        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void onGameOverSendsEventToEmitters() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        broadcaster.addEmitter(emitter);

        GameResult result = new GameResult("P1", "P2", 15, 10, "P1");
        broadcaster.onGameOver(result);

        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void onTournamentOverCompletesAllEmitters() throws IOException {
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        broadcaster.addEmitter(emitter1);
        broadcaster.addEmitter(emitter2);

        Map<String, Integer> scores = new HashMap<>();
        scores.put("P1", 20);
        scores.put("P2", 10);
        TournamentResult result = new TournamentResult(scores);

        broadcaster.onTournamentOver(result);

        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter1, times(1)).complete();
        verify(emitter2, times(1)).complete();
    }

    @Test
    void disconnectedEmitterIsRemoved() throws IOException {
        SseEmitter workingEmitter = mock(SseEmitter.class);
        SseEmitter brokenEmitter = mock(SseEmitter.class);

        broadcaster.addEmitter(workingEmitter);
        broadcaster.addEmitter(brokenEmitter);

        // Make broken emitter throw IOException
        doThrow(new IOException("Disconnected")).when(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));

        GameHistory history = new GameHistory("A", "B");
        RoundResult rr = new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.COOPERATE, 3, 3, 1);
        MoveEvent event = new MoveEvent(rr, history);

        broadcaster.onMoveMade(event);

        // Second move should only go to working emitter
        broadcaster.onMoveMade(event);

        verify(workingEmitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
        verify(brokenEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void broadcasterWithDelayPausesAfterSend() {
        ViewerBroadcaster delayedBroadcaster = new ViewerBroadcaster(50); // 50ms delay
        SseEmitter emitter = mock(SseEmitter.class);
        delayedBroadcaster.addEmitter(emitter);

        GameHistory history = new GameHistory("A", "B");
        RoundResult rr = new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.COOPERATE, 3, 3, 1);
        MoveEvent event = new MoveEvent(rr, history);

        long startTime = System.currentTimeMillis();
        delayedBroadcaster.onMoveMade(event);
        long elapsed = System.currentTimeMillis() - startTime;

        assertTrue(elapsed >= 50, "Broadcaster should have slept for at least 50ms");
    }
}
