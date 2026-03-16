package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import sits.action.PrisonerAction;
import sits.game.GameHistory;
import sits.game.RoundResult;
import sits.remote.dto.GameHistoryDTO;
import sits.remote.dto.RoundResultDTO;

class GameHistoryDTOTest {

    @Test
    void fromGameHistoryCopiesTransportFields() {
        GameHistory history = new GameHistory("P1", "P2");
        history.addRound(new RoundResult(PrisonerAction.COOPERATE, PrisonerAction.DEFECT, 0, 5, 1));

        GameHistoryDTO dto = GameHistoryDTO.fromGameHistory(history);

        assertEquals("P1", dto.getNameP1());
        assertEquals("P2", dto.getNameP2());
        assertEquals(1, dto.getRounds().size());
        assertEquals("COOPERATE", dto.getRounds().get(0).getActionP1());
        assertEquals("DEFECT", dto.getRounds().get(0).getActionP2());
        assertEquals(0, dto.getRounds().get(0).getPayoffP1());
        assertEquals(5, dto.getRounds().get(0).getPayoffP2());
    }

    @Test
    void toGameHistoryRebuildsActionLabelsAndRounds() {
        GameHistoryDTO dto = new GameHistoryDTO(
                "A",
                "B",
                List.of(
                        new RoundResultDTO("COOPERATE", "DEFECT", 0, 5),
                        new RoundResultDTO("DEFECT", "DEFECT", 1, 1)
                )
        );

        GameHistory rebuilt = dto.toGameHistory();

        assertEquals("A", rebuilt.getP1Name());
        assertEquals("B", rebuilt.getP2Name());
        assertEquals(2, rebuilt.getRounds().size());
        assertEquals("COOPERATE", rebuilt.getRounds().get(0).getActionP1().getLabel());
        assertEquals("DEFECT", rebuilt.getRounds().get(0).getActionP2().getLabel());
        assertEquals(2, rebuilt.getRounds().get(1).getRoundNumber());
    }

    @Test
    void toGameHistoryHandlesNullRoundList() {
        GameHistoryDTO dto = new GameHistoryDTO();
        dto.setNameP1("P1");
        dto.setNameP2("P2");
        dto.setRounds(null);

        GameHistory rebuilt = dto.toGameHistory();

        assertEquals("P1", rebuilt.getP1Name());
        assertEquals("P2", rebuilt.getP2Name());
        assertTrue(rebuilt.getRounds().isEmpty());
    }
}
