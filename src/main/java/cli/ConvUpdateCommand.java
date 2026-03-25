package cli;

import java.util.Arrays;

public class ConvUpdateCommand extends Command {
    public ConvUpdateCommand() {
        super("conv_update", "Изменить коэффициент правила <rule_id> factor=value");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length < 2) throw new IllegalArgumentException("Использование: conv_update <id> factor=0.5");

        try {
            long id = Long.parseLong(args[0]);
            String rest = String.join("", Arrays.copyOfRange(args, 1, args.length));
            String[] parts = rest.split("=");

            if (parts.length < 2 || !parts[0].trim().equals("factor")) {
                throw new IllegalArgumentException("Можно обновить только поле factor (формат: factor=0.5)");
            }

            double newFactor = Double.parseDouble(parts[1].trim());
            context.ruleManager().update(id, newFactor); // Вызов метода обновления из Этапа 1
            System.out.println("OK");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID и коэффициент должны быть числами");
        }
    }
}
