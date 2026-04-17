package cli;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load", "Загрузить коллекцию из XML файла. Использование: load <имя_файла.xml>");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length == 0) {
            System.out.println("Ошибка: Вы не указали путь к файлу!");
            System.out.println("Пример правильного ввода: load data.xml");
            return;
        }

        String path = args[0];

        // ИСПОЛЬЗУЕМ unitManager() ТАК КАК ЭТО RECORD
        context.unitManager().loadFromFile(path);
    }
}