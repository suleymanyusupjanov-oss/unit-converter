package cli;

import model.ValueWithUnit;

public class ConvConvertCommand extends Command {
    public ConvConvertCommand() {
        super("conv_convert", "Конвертировать значение <value> <fromCode> <toCode>");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Использование: conv_convert <value> <fromCode> <toCode>");
        }
        try {
            double value = Double.parseDouble(args[0]);
            String fromCode = args[1];
            String toCode = args[2];

            ValueWithUnit input = new ValueWithUnit(fromCode, value);
            ValueWithUnit result = context.conversionService().convert(input, toCode);

            System.out.println("result: " + result.getValue() + " " + result.getUnitCode());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Значение должно быть числом.");
        }
        }
    }
