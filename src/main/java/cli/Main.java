package cli;

import main.java.service.ConversionRuleCollectionManager;
import main.java.service.ConversionService;
import main.java.service.UnitCollectionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Инициализация сервисов этапа 1
        UnitCollectionManager unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        Scanner scanner = new Scanner(System.in);
        CommandContext context = new CommandContext(scanner, unitManager, ruleManager, conversionService);

        // Регистрация команд
        Map<String, Command> commands = new HashMap<>();
        register(commands, new HelpCommand(commands));
        register(commands, new ExitCommand());
        register(commands, new UnitAddCommand());
        register(commands, new UnitListCommand());
        register(commands, new UnitShowCommand());
        register(commands, new ConvAddCommand());
        register(commands, new ConvListCommand());
        register(commands, new ConvConvertCommand());
        register(commands, new ConvDeleteCommand());
        register(commands, new UnitUpdateCommand());
        register(commands, new ConvUpdateCommand());
        register(commands, new ConvCheckCycleCommand());

        System.out.println("Система конвертации единиц запущена.");
        System.out.println("Введите help для просмотра списка команд.");

        // Главный цикл интерпретатора
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String commandName = parts[0];
            String[] commandArgs = Arrays.copyOfRange(parts, 1, parts.length);

            Command command = commands.get(commandName);
            if (command == null) {
                System.out.println("Ошибка: неизвестная команда. Введите help для списка доступных команд.");
                continue;
            }
            try {
                command.execute(commandArgs, context);
            } catch (IllegalArgumentException e) {
                // Перехватываем ошибки валидации
                System.out.println("Ошибка валидации: " + e.getMessage());
            } catch (Exception e) {
                // Перехватываем прочие непредвиденные ошибки (NumberFormatException и т.д.)
                System.out.println("Ошибка выполнения: " + e.getMessage());
            }

        }
    }

    private static void register(Map<String, Command> map, Command command) {
        map.put(command.getName(), command);
    }
}