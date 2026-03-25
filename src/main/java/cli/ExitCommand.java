package cli;

public class ExitCommand extends Command {
    public ExitCommand() { super("exit", "Выход из программы"); }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.println("Завершение работы...");
        System.exit(0);
    }
}
