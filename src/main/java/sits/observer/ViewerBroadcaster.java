package sits.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.remote.dto.MoveEventDTO;
import sits.tournament.TournamentResult;

public class ViewerBroadcaster implements GameObserver {

    private final CopyOnWriteArrayList<SseEmitter> emitters;
    private final CopyOnWriteArrayList<String> eventHistory;
    private volatile boolean tournamentOver;
    private final long delayMs;
    private final ObjectMapper mapper;

    public ViewerBroadcaster(long delayMs) {
        this.emitters = new CopyOnWriteArrayList<>();
        this.eventHistory = new CopyOnWriteArrayList<>();
        this.tournamentOver = false;
        this.delayMs = delayMs;
        this.mapper = new ObjectMapper();
    }

    public void addEmitter(SseEmitter emitter) {
        // Replay all past events so a viewer that re-connects mid-tournament
        // (or joins after the tournament finished) sees the full history.
        for (String json : eventHistory) {
            try {
                emitter.send(SseEmitter.event().data(json));
            } catch (IOException e) {
                return;
            }
        }
        if (tournamentOver) {
            try {
                emitter.complete();
            } catch (Exception e) {
                // already disconnected
            }
            return;
        }
        emitters.add(emitter);
    }

    @Override
    public void onMoveMade(MoveEvent event) {
        MoveEventDTO dto = MoveEventDTO.fromMoveEvent(event);
        sendToAllViewers(dto);

        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onGameOver(GameResult result) {
        MoveEventDTO dto = MoveEventDTO.gameOver(result);
        sendToAllViewers(dto);
    }

    @Override
    public void onTournamentOver(TournamentResult result) {
        MoveEventDTO dto = MoveEventDTO.tournamentOver();
        sendToAllViewers(dto);
        tournamentOver = true;
        completeAllEmitters();
    }

    private void sendToAllViewers(MoveEventDTO dto) {
        String json;
        try {
            json = mapper.writeValueAsString(dto);
        } catch (Exception e) {
            // DTO fields are all basic types, this should never happen
            return;
        }

        eventHistory.add(json);

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(json));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        // safe with CopyOnWriteArrayList
        emitters.removeAll(deadEmitters);
    }

    private void completeAllEmitters() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.complete();
            } catch (Exception e) {
                // Already disconnected or completion failed
            }
        }
        emitters.clear();
    }
}
