package sits.remote.action;

import sits.action.Action;

public class StringAction implements Action {

    private String label;

    public StringAction() {
    }

    public StringAction(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
