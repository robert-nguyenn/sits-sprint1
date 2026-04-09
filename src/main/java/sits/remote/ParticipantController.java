// ParticipantController is inside the remote app.
// It exposes the participant’s name, action, and reset over HTTP.
// RemoteParticipant is on the server and calls those endpoints.
// So ParticipantController is the HTTP face of the real participant.

package sits.remote;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import sits.action.Action;
import sits.game.GameHistory;
import sits.participant.Participant;
import sits.remote.dto.GameHistoryDTO;

@RestController
public class ParticipantController {

    private final Participant participant;

    public ParticipantController(Participant participant) {
        this.participant = participant;
    }

    @GetMapping("/name")
    public String getName() {
        return participant.getName();
    }

    @PostMapping("/action")
    public String getAction(@RequestBody GameHistoryDTO dto) {
        GameHistory history = dto.toGameHistory();
        Action action = participant.chooseAction(history);
        return action.getLabel();
    }

    @PostMapping("/reset")
    public void reset() {
        participant.reset();
    }
}
