package cli;

import model.Unit;

public class UnitListCommand extends Command {
    public UnitListCommand() { super("unit_list", "Список единиц измерения"); }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.printf("%-5s %-10s %s%n", "ID", "Code", "Name");
        for (Unit u : context.unitManager().getUnits()) {
            System.out.printf("%-5d %-10s %s%n", u.getId(), u.getCode(), u.getName());
        }
    }
}

