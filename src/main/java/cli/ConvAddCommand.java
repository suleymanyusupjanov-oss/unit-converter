package cli;

import main.java.model.ConversionRule;

public class ConvAddCommand extends Command {
    public ConvAddCommand() {
        super("conv_add", "Добавить правило конвертации (интерактивно)");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.print("Из (unit code): ");
        String fromCode = context.scanner().nextLine().trim();

        System.out.print("В (unit code): ");
        String toCode = context.scanner().nextLine().trim();

        System.out.print("Коэффициент (умножить): ");
        String factorStr = context.scanner().nextLine().trim();

        try {
            double factor = Double.parseDouble(factorStr);
            ConversionRule rule = context.ruleManager().createRule(fromCode, toCode, factor, "SYSTEM");
            System.out.println("OK rule_id=" + rule.getId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Коэффициент должен быть числом.");
        }
        }
    }
