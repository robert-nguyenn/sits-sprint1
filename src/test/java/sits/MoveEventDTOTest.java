package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.GameResult;
import sits.game.MoveEvent;
import sits.game.RoundResult;
import sits.remote.dto.MoveEventDTO;

class MoveEventDTOTest {

    @Test
    void fromMoveEventCreatesValidMoveDTO() {
        GameHistory history = new GameHistory("Player1", "Player2");
        RoundResult rr = new RoundResult(
                PrisonerAction.COOPERATE,
                PrisonerAction.DEFECT,
                0,
                5,
                1
        );
        MoveEvent moveEvent = new MoveEvent(rr, history);

        MoveEventDTO dto = MoveEventDTO.fromMoveEvent(moveEvent);

        assertEquals("MOVE", dto.getType());
        assertEquals(1, dto.getRoundNumber());
        assertEquals("COOPERATE", dto.getAction1());
        assertEquals("DEFECT", dto.getAction2());
        assertEquals(0, dto.getPayoffP1());
        assertEquals(5, dto.getPayoffP2());
        assertEquals("Player1", dto.getP1Name());
        assertEquals("Player2", dto.getP2Name());
    }

    @Test
    void gameOverCreatesValidGameOverDTO() {
        GameResult result = new GameResult("Player1", "Player2", 15, 10, "Player1");

        MoveEventDTO dto = MoveEventDTO.gameOver(result);

        assertEquals("GAME_OVER", dto.getType());
        assertEquals("Player1", dto.getWinner());
        assertEquals(15, dto.getTotalScoreP1());
        assertEquals(10, dto.getTotalScoreP2());
        assertEquals("Player1", dto.getP1Name());
        assertEquals("Player2", dto.getP2Name());
    }

    @Test
    void tournamentOverCreatesValidTournamentOverDTO() {
        MoveEventDTO dto = MoveEventDTO.tournamentOver();

        assertEquals("TOURNAMENT_OVER", dto.getType());
        assertNull(dto.getRoundNumber());
        assertNull(dto.getWinner());
    }

    @Test
    void dtoSupportsNullFieldsForInactiveEventTypes() {
        GameHistory history = new GameHistory("A", "B");
        RoundResult rr = new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.COOPERATE, 3, 3, 1);
        MoveEvent moveEvent = new MoveEvent(rr, history);

        MoveEventDTO dto = MoveEventDTO.fromMoveEvent(moveEvent);

        assertNull(dto.getWinner());
        assertNull(dto.getTotalScoreP1());
    }

    @Test
    void dtoCanBeSerializedAndDeserialized() {
        GameHistory history = new GameHistory("Alice", "Bob");
        RoundResult rr = new RoundResult(PrisonerAction.DEFECT, PrisonerAction.COOPERATE, 5, 0, 2);
        MoveEvent moveEvent = new MoveEvent(rr, history);

        MoveEventDTO original = MoveEventDTO.fromMoveEvent(moveEvent);

        MoveEventDTO copy = new MoveEventDTO();
        copy.setType(original.getType());
        copy.setRoundNumber(original.getRoundNumber());
        copy.setAction1(original.getAction1());
        copy.setAction2(original.getAction2());
        copy.setPayoffP1(original.getPayoffP1());
        copy.setPayoffP2(original.getPayoffP2());
        copy.setP1Name(original.getP1Name());
        copy.setP2Name(original.getP2Name());

        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getRoundNumber(), copy.getRoundNumber());
        assertEquals(original.getAction1(), copy.getAction1());
    }

    @Test
    void gameOverWithTieWinner() {
        GameResult result = new GameResult("Player1", "Player2", 10, 10, "TIE");

        MoveEventDTO dto = MoveEventDTO.gameOver(result);

        assertEquals("TIE", dto.getWinner());
        assertEquals(10, dto.getTotalScoreP1());
        assertEquals(10, dto.getTotalScoreP2());
    }
}
