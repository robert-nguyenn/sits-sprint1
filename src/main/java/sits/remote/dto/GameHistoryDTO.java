package sits.remote.dto;

import java.util.ArrayList;
import java.util.List;

import sits.game.GameHistory;
import sits.game.RoundResult;
import sits.remote.action.StringAction;

public class GameHistoryDTO {

    private String nameP1;
    private String nameP2;
    private List<RoundResultDTO> rounds = new ArrayList<>();

    public GameHistoryDTO() {
    }

    public GameHistoryDTO(String nameP1, String nameP2, List<RoundResultDTO> rounds) {
        this.nameP1 = nameP1;
        this.nameP2 = nameP2;
        this.rounds = rounds;
    }

    public static GameHistoryDTO fromGameHistory(GameHistory h) {
        List<RoundResultDTO> dtos = new ArrayList<>();

        for (RoundResult r : h.getRounds()) {
            dtos.add(new RoundResultDTO(
                    r.getActionP1().getLabel(),
                    r.getActionP2().getLabel(),
                    r.getScoreP1(),
                    r.getScoreP2()
            ));
        }

        return new GameHistoryDTO(h.getP1Name(), h.getP2Name(), dtos);
    }

    public GameHistory toGameHistory() {
        GameHistory history = new GameHistory(nameP1, nameP2);

        if (rounds == null) {
            return history;
        }

        int roundNumber = 1;
        for (RoundResultDTO r : rounds) {
            history.addRound(new RoundResult(
                    new StringAction(r.getActionP1()),
                    new StringAction(r.getActionP2()),
                    r.getPayoffP1(),
                    r.getPayoffP2(),
                    roundNumber
            ));
            roundNumber++;
        }

        return history;
    }

    public String getNameP1() {
        return nameP1;
    }

    public void setNameP1(String nameP1) {
        this.nameP1 = nameP1;
    }

    public String getNameP2() {
        return nameP2;
    }

    public void setNameP2(String nameP2) {
        this.nameP2 = nameP2;
    }

    public List<RoundResultDTO> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundResultDTO> rounds) {
        this.rounds = rounds;
    }
}
