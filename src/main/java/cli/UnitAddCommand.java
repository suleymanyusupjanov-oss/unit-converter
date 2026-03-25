package cli;

import main.java.model.Unit;

public class UnitAddCommand extends Command {
    public UnitAddCommand() {
        super("unit_add", "Добавить единицу измерения (интерактивно)");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.print("Код: ");
        String code = context.scanner().nextLine().trim();

        System.out.print("Название: ");
        String name = context.scanner().nextLine().trim();

        // Валидация вызовется внутри менеджера
        Unit unit = context.unitManager().createUnit(code, name, "SYSTEM");
        System.out.println("OK unit_id=" + unit.getId());
    }
}
