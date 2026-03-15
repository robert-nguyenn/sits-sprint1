package sits.participant;

import java.util.Locale;
import java.util.Scanner;

import sits.action.Action;
import sits.action.PrisonerAction;
import sits.game.GameHistory;

public class HumanParticipant implements Participant {

    private final String name;
    private final Scanner scanner;

    public HumanParticipant(String name) {
        this(name, new Scanner(System.in));
    }

    public HumanParticipant(String name, Scanner scanner) {
        this.name = name;
        this.scanner = scanner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Action chooseAction(GameHistory history) {
        while (true) {
            System.out.println(name + ", choose action for this round:");
            System.out.println("Type C for COOPERATE or D for DEFECT.");

            if (!scanner.hasNextLine()) {
                return PrisonerAction.COOPERATE;
            }

            String input = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

            if ("C".equals(input) || "COOPERATE".equals(input)) {
                return PrisonerAction.COOPERATE;
            }
            if ("D".equals(input) || "DEFECT".equals(input)) {
                return PrisonerAction.DEFECT;
            }

            System.out.println("Invalid choice. Please enter C or D.");
        }
    }

    @Override
    public void reset() {
        // Stateless participant. Scanner remains open for the app lifetime.
    }
}
