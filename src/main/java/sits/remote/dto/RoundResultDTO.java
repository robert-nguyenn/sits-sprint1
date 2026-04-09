// RoundResultDTO.java is the network/JSON version of one round result.

// Stores action labels as strings (p1 and p2).
// Stores round payoffs/scores.
// Used inside GameHistoryDTO.java for HTTP transfer.
// So: RoundResult = internal game object, RoundResultDTO = transport object.




package sits.remote.dto;

public class RoundResultDTO {

    private String actionP1;
    private String actionP2;
    private int payoffP1;
    private int payoffP2;

    public RoundResultDTO() {
    }

    public RoundResultDTO(String actionP1, String actionP2, int payoffP1, int payoffP2) {
        this.actionP1 = actionP1;
        this.actionP2 = actionP2;
        this.payoffP1 = payoffP1;
        this.payoffP2 = payoffP2;
    }

    public String getActionP1() {
        return actionP1;
    }

    public void setActionP1(String actionP1) {
        this.actionP1 = actionP1;
    }

    public String getActionP2() {
        return actionP2;
    }

    public void setActionP2(String actionP2) {
        this.actionP2 = actionP2;
    }

    public int getPayoffP1() {
        return payoffP1;
    }

    public void setPayoffP1(int payoffP1) {
        this.payoffP1 = payoffP1;
    }

    public int getPayoffP2() {
        return payoffP2;
    }

    public void setPayoffP2(int payoffP2) {
        this.payoffP2 = payoffP2;
    }
}
