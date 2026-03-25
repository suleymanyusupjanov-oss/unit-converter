package cli;

import main.java.model.ConversionRule;

public class ConvListCommand extends Command {
    public ConvListCommand() {
        super("conv_list", "Список всех правил конвертации");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.printf("%-5s %-10s %-10s %s%n", "ID", "From", "To", "Factor");
        for (ConversionRule rule : context.ruleManager().getRules()) {
            System.out.printf("%-5d %-10s %-10s %.4f%n",
                    rule.getId(), rule.getFromUnitCode(), rule.getToUnitCode(), rule.getFactor());
        }
    }
}
