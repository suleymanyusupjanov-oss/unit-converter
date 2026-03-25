package cli;

import main.java.model.Unit;
import java.util.Arrays;

public class UnitUpdateCommand extends Command {
    public UnitUpdateCommand() {
        super("unit_update", "Изменить единицу <unit_id> field=value");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length < 2) throw new IllegalArgumentException("Ожидается <unit_id> field=value");

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть числом");
        }

        // Собираем оставшиеся аргументы обратно в строку (на случай пробелов в value)
        String restArgs = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String[] parts = restArgs.split("=", 2);
        if (parts.length < 2) throw new IllegalArgumentException("Формат должен быть field=value");

        String field = parts[0].trim();
        String value = parts[1].trim();

        Unit existingUnit = context.unitManager().getById(id);
        if (existingUnit == null) throw new IllegalArgumentException("Единица с таким ID не найдена");

        if (field.equals("code")) {
            context.unitManager().update(id, value, existingUnit.getName());
        } else if (field.equals("name")) {
            context.unitManager().update(id, existingUnit.getCode(), value);
        } else {
            throw new IllegalArgumentException("Неизвестное поле; доступно: code, name");
        }
        System.out.println("OK");
    }
}
