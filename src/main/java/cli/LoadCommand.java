package cli;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load", "Обновить in-memory кэш из PostgreSQL (этап 6).");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        try {
            context.unitManager().loadFromDb();
            System.out.println("OK: данные обновлены из БД");
        } catch (Exception e) {
            System.out.println("Ошибка БД: " + e.getMessage());
        }
    }
}