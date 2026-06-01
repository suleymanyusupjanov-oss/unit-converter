package cli;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save", "(этап 6) БД сохраняет каждую операцию автоматически.");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        System.out.println("OK: БД сохраняет каждую операцию автоматически — отдельное save не требуется.");
    }
}