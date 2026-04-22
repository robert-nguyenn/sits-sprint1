package sits.remote.dto;

import sits.game.GameResult;
import sits.game.MoveEvent;

public class MoveEventDTO {
    
    private String type;
    private Integer roundNumber;
    private String action1;
    private String action2;
    private Integer payoffP1;
    private Integer payoffP2;
    private String p1Name;
    private String p2Name;
    private String winner;
    private Integer totalScoreP1;
    private Integer totalScoreP2;

    public MoveEventDTO() {
    }

    private MoveEventDTO(String type, Integer roundNumber, String action1, String action2,
                         Integer payoffP1, Integer payoffP2, String p1Name, String p2Name) {
        this.type = type;
        this.roundNumber = roundNumber;
        this.action1 = action1;
        this.action2 = action2;
        this.payoffP1 = payoffP1;
        this.payoffP2 = payoffP2;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
    }
    
    private MoveEventDTO(String type, String winner, Integer totalScoreP1, Integer totalScoreP2,
                         String p1Name, String p2Name) {
        this.type = type;
        this.winner = winner;
        this.totalScoreP1 = totalScoreP1;
        this.totalScoreP2 = totalScoreP2;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
    }
    
    private MoveEventDTO(String type) {
        this.type = type;
    }
    
    public static MoveEventDTO fromMoveEvent(MoveEvent moveEvent) {
        var rr = moveEvent.getRoundResult();
        var history = moveEvent.getGameHistory();
        
        return new MoveEventDTO(
            "MOVE",
            rr.getRoundNumber(),
            rr.getActionP1().getLabel(),
            rr.getActionP2().getLabel(),
            rr.getScoreP1(),
            rr.getScoreP2(),
            history.getP1Name(),
            history.getP2Name()
        );
    }
    
    public static MoveEventDTO gameOver(GameResult gameResult) {
        return new MoveEventDTO(
            "GAME_OVER",
            gameResult.getWinner(),
            gameResult.getTotalScoreP1(),
            gameResult.getTotalScoreP2(),
            gameResult.getP1Name(),
            gameResult.getP2Name()
        );
    }
    
    public static MoveEventDTO tournamentOver() {
        return new MoveEventDTO("TOURNAMENT_OVER");
    }
    
    public String getType() { return type; }
    public Integer getRoundNumber() { return roundNumber; }
    public String getAction1() { return action1; }
    public String getAction2() { return action2; }
    public Integer getPayoffP1() { return payoffP1; }
    public Integer getPayoffP2() { return payoffP2; }
    public String getP1Name() { return p1Name; }
    public String getP2Name() { return p2Name; }
    public String getWinner() { return winner; }
    public Integer getTotalScoreP1() { return totalScoreP1; }
    public Integer getTotalScoreP2() { return totalScoreP2; }
    
    public void setType(String type) { this.type = type; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }
    public void setAction1(String action1) { this.action1 = action1; }
    public void setAction2(String action2) { this.action2 = action2; }
    public void setPayoffP1(Integer payoffP1) { this.payoffP1 = payoffP1; }
    public void setPayoffP2(Integer payoffP2) { this.payoffP2 = payoffP2; }
    public void setP1Name(String p1Name) { this.p1Name = p1Name; }
    public void setP2Name(String p2Name) { this.p2Name = p2Name; }
    public void setWinner(String winner) { this.winner = winner; }
    public void setTotalScoreP1(Integer totalScoreP1) { this.totalScoreP1 = totalScoreP1; }
    public void setTotalScoreP2(Integer totalScoreP2) { this.totalScoreP2 = totalScoreP2; }
}
