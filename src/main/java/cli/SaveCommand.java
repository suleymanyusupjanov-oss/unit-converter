package cli;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save", "Сохранить коллекцию в XML файл. Использование: save <имя_файла.xml>");
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        if (args.length == 0) {
            System.out.println("Ошибка: Вы не указали путь к файлу!");
            System.out.println("Пример правильного ввода: save data.xml");
            return;
        }

        String path = args[0];

        try {
            // ИСПОЛЬЗУЕМ unitManager() ТАК КАК ЭТО RECORD
            context.unitManager().saveToFile(path);

        } catch (Exception e) {
            System.out.println("Критическая ошибка при сохранении: " + e.getMessage());
        }
    }
}