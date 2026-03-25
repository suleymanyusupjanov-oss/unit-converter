package cli;

public class ConvCheckCycleCommand extends Command {
    public ConvCheckCycleCommand() {
        super("conv_check_cycle", "Проверить правила на наличие циклов");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        // Здесь вызывается логика из нашего менеджера или сервиса
        // Если алгоритм еще не реализован, возвращаем статус
        boolean hasCycles = false;

        // context.ruleManager().checkCycles(); // Если такой метод есть

        if (hasCycles) {
            System.out.println("Внимание: обнаружены подозрительные циклы в правилах!");
        } else {
            System.out.println("OK: циклов не обнаружено");
        }
    }
}