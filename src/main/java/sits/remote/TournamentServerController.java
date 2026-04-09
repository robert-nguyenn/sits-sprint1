// TournamentServerController is on the tournament server.
// It receives HTTP requests like register/start/list tournaments.
// TournamentServerClient is used by the remote app to send HTTP requests to that server.
// So one receives requests, the other sends requests.

package sits.remote;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sits.tournament.TournamentResult;

@RestController
@RequestMapping("/tournaments")
public class TournamentServerController {

    private final TournamentRegistry registry;

    public TournamentServerController(TournamentRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public List<NetworkedTournament> getTournaments() {
        return registry.listRegistering();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<String> register(@PathVariable String id, @RequestBody RegistrationRequest body) {
        NetworkedTournament tournament = registry.get(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found: " + id);
        }

        try {
            tournament.addRemoteParticipant(body);
            return ResponseEntity.ok("Registered: " + body.getName());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TournamentResult> start(@PathVariable String id) {
        NetworkedTournament tournament = registry.get(id);
        if (tournament == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            return ResponseEntity.ok(tournament.start());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
