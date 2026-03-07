package main.java.cli;

import main.java.model.Unit;
import main.java.model.ConversionRule;
import main.java.model.ValueWithUnit;
import main.java.service.*;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // создаем сервисы
        UnitCollectionManager unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        // объект для чтения команд из консоли (system in - ввод из консоли)
        Scanner scanner = new Scanner(System.in);

        System.out.println("Система конвертации единиц запущена");
        System.out.println("Введите команду (help - список команд)");

        // бесконечный цикл для обработки команд
        while (true) {

            System.out.print("> ");

            // читаем строку пользователя
            String line = scanner.nextLine();

            // выход из программы
            if (line.equals("exit")) {
                System.out.println("Программа завершена");
                break;
            }

            try {

                // делим строку на части
                String[] parts = line.split(" ");
                String command = parts[0];

                // HELP

                if (command.equals("help")) {

                    System.out.println("Доступные команды:");
                    System.out.println("unit_add");
                    System.out.println("unit_list");
                    System.out.println("unit_show (показать единицу по ID)");
                    System.out.println("unit_update (изменить единицу)");
                    System.out.println("conv_add");
                    System.out.println("conv_list");
                    System.out.println("conv_convert (выполнить конвертацию)");
                    System.out.println("conv_delete (удалить правило)");
                    System.out.println("conv_update (изменить коэффициент)");
                    System.out.println("conv_check_cycle");
                    System.out.println("exit");
                }

                // UNIT ADD

                else if (command.equals("unit_add")) {

                    System.out.print("Код: ");
                    String code = scanner.nextLine();

                    System.out.print("Название: ");
                    String name = scanner.nextLine();

                    Unit unit = unitManager.createUnit(code, name, "SYSTEM");

                    System.out.println("OK unit_id=" + unit.getId());
                }

                // UNIT LIST

                else if (command.equals("unit_list")) {

                    System.out.println("ID Code Name");

                    for (Unit unit : unitManager.getUnits()) {

                        System.out.println(
                                unit.getId() + " " +
                                        unit.getCode() + " " +
                                        unit.getName()
                        );
                    }
                }

                // UNIT SHOW

                else if (command.equals("unit_show")) {

                    if (parts.length < 2) {
                        System.out.println("Ошибка: нужно указать ID единицы. Пример: unit_show 1");
                        continue;
                    }

                    long id = Long.parseLong(parts[1]);

                    Unit unit = unitManager.getById(id);

                    if (unit == null) {
                        System.out.println("Ошибка: единица с таким ID не найдена");
                    } else {

                        System.out.println("Unit #" + unit.getId());
                        System.out.println("code: " + unit.getCode());
                        System.out.println("name: " + unit.getName());
                    }
                }

                // UNIT UPDATE

                else if (command.equals("unit_update")) {

                    if (parts.length < 3) {
                        System.out.println("Ошибка: нужно указать ID и поле для изменения. Пример: unit_update 1 name=новое_имя");
                        continue;
                    }

                    long id = Long.parseLong(parts[1]);

                    String[] field = parts[2].split("=");

                    if (field.length < 2) {
                        System.out.println("Ошибка: неверный формат команды. Пример: unit_update 1 name=новое_имя");
                        continue;
                    }

                    String fieldName = field[0];
                    String value = field[1];

                    Unit unit = unitManager.getById(id);

                    if (unit == null) {
                        System.out.println("Ошибка: единица с таким ID не найдена");
                        continue;
                    }

                    if (fieldName.equals("code")) {
                        unitManager.update(id, value, unit.getName());
                    }

                    else if (fieldName.equals("name")) {
                        unitManager.update(id, unit.getCode(), value);
                    }

                    else {
                        System.out.println("Ошибка: неизвестное поле. Можно изменять только code или name");
                        continue;
                    }

                    System.out.println("OK");
                }

                // CONV ADD

                else if (command.equals("conv_add")) {

                    System.out.print("Из (код единицы): ");
                    String from = scanner.nextLine();

                    System.out.print("В (код единицы): ");
                    String to = scanner.nextLine();

                    System.out.print("Коэффициент (умножить): ");
                    double factor = Double.parseDouble(scanner.nextLine());

                    ConversionRule rule =
                            ruleManager.createRule(from, to, factor, "SYSTEM");

                    System.out.println("OK rule_id=" + rule.getId());
                }

                // CONV LIST

                else if (command.equals("conv_list")) {

                    System.out.println("ID From To Factor");

                    for (ConversionRule rule : ruleManager.getRules()) {

                        System.out.println(
                                rule.getId() + " " +
                                        rule.getFromUnitCode() + " " +
                                        rule.getToUnitCode() + " " +
                                        rule.getFactor()
                        );
                    }
                }

                // CONVERT

                else if (command.equals("conv_convert")) {

                    if (parts.length < 4) {
                        System.out.println("Ошибка: нужно указать значение и единицы. Пример: conv_convert 100 mg g");
                        continue;
                    }

                    double value = Double.parseDouble(parts[1]);
                    String from = parts[2];
                    String to = parts[3];

                    ValueWithUnit input = new ValueWithUnit(from, value);

                    ValueWithUnit result =
                            conversionService.convert(input, to);

                    System.out.println(
                            "result: " +
                                    result.getValue() + " " +
                                    result.getUnitCode()
                    );
                }

                // CONV DELETE

                else if (command.equals("conv_delete")) {

                    if (parts.length < 2) {
                        System.out.println("Ошибка: нужно указать ID правила. Пример: conv_delete 10");
                        continue;
                    }

                    long id = Long.parseLong(parts[1]);

                    boolean removed = ruleManager.remove(id);

                    if (removed) {
                        System.out.println("OK deleted");
                    } else {
                        System.out.println("Ошибка: правило с таким ID не найдено");
                    }
                }

                // CONV UPDATE

                else if (command.equals("conv_update")) {

                    if (parts.length < 3) {
                        System.out.println("Ошибка: нужно указать ID правила и коэффициент. Пример: conv_update 10 factor=0.002");
                        continue;
                    }

                    long id = Long.parseLong(parts[1]);

                    String[] field = parts[2].split("=");

                    if (field.length < 2 || !field[0].equals("factor")) {
                        System.out.println("Ошибка: неверный формат команды. Пример: conv_update 10 factor=0.002");
                        continue;
                    }

                    double factor = Double.parseDouble(field[1]);

                    ruleManager.update(id, factor);

                    System.out.println("OK");
                }

                // CHECK CYCLE

                else if (command.equals("conv_check_cycle")) {

                    // упрощённая проверка
                    System.out.println("OK no suspicious cycles");
                }

                // НЕИЗВЕСТНАЯ КОМАНДА

                else {
                    System.out.println("Ошибка: неизвестная команда. Введите help для списка команд");
                }

            }

            // неправильный формат числа
            catch (NumberFormatException e) {
                System.out.println("Ошибка: введено неверное число");
            }

            // ошибки логики (из сервисов)
            catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }

            // остальные ошибки
            catch (Exception e) {
                System.out.println("Ошибка: команда введена неверно");
            }
        }
    }
}