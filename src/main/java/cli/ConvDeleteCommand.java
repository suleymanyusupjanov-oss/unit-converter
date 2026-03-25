package cli;

public class ConvDeleteCommand extends Command {
    public ConvDeleteCommand() {
        super("conv_delete", "Удалить правило конвертации <rule_id>");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length == 0) throw new IllegalArgumentException("Укажите ID правила");

        try {
            long id = Long.parseLong(args[0]);
            boolean removed = context.ruleManager().remove(id); // Предполагаем наличие метода remove
            if (removed) {
                System.out.println("OK");
            } else {
                System.out.println("Ошибка: правило с таким ID не найдено");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть числом");
        }
    }
}