package cli;
import model.Unit;

public class UnitShowCommand extends Command {
    public UnitShowCommand() {
        super("unit_show", "Показать информацию о единице <unit_id>");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length == 0) throw new IllegalArgumentException("Укажите ID единицы");

        try {
            long id = Long.parseLong(args[0]);
            Unit unit = context.unitManager().getById(id);
            if (unit == null) {
                System.out.println("Единица с таким ID не найдена.");
            } else {
                System.out.println("ID: " + unit.getId() + ", Code: " + unit.getCode() + ", Name: " + unit.getName());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть числом.");
        }
    }
}