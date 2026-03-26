# Unit Converter

## Описание

Консольное приложение на Java для управления единицами измерения и
правилами их конверсии.

Программа позволяет: - добавлять единицы измерения - просматривать
список единиц - создавать правила конверсии - выполнять преобразование
значений между единицами - обновлять и удалять правила

Проект реализован на: - Java 17 - Maven - архитектуре с разделением на
пакеты (model, service, cli, validation)

------------------------------------------------------------------------

## Требования

Перед запуском убедитесь, что установлены:

-   Java 17
-   Maven

Проверка:

    java -version
    mvn -v

------------------------------------------------------------------------

## Сборка проекта

В корне проекта выполнить:

    mvn package

После сборки появится папка:

    target

Внутри будет создан файл:

    unit-converter-1.0.jar

------------------------------------------------------------------------

## Запуск программы

Запуск выполняется из терминала:

   java -cp target/unit-converter-1.0.jar main.java.cli.Main

После запуска появится приглашение для ввода команд.

------------------------------------------------------------------------

## Команды

### Добавление единицы

    unit_add

Программа запросит:

    Код
    Название

Пример:

    > unit_add
    Код: mg/L
    Название: milligrams per liter
    OK unit_id=1

------------------------------------------------------------------------

### Список единиц

    unit_list

Пример вывода:

    ID Code Name
    1 mg/L milligrams per liter
    2 g/L grams per liter

------------------------------------------------------------------------

### Просмотр единицы

    unit_show <unit_id>

Пример:

    unit_show 1

------------------------------------------------------------------------

### Обновление единицы

    unit_update <unit_id> field=value

Поля:

    code
    name

Пример:

    unit_update 1 name=milligram

------------------------------------------------------------------------

### Добавление правила конверсии

    conv_add

Программа запросит:

    Из (unit code)
    В (unit code)
    Коэффициент

Пример:

    > conv_add
    Из: mg
    В: g
    Коэффициент: 0.001
    OK rule_id=1

------------------------------------------------------------------------

### Список правил

    conv_list

Пример:

    ID From To Factor
    1 mg g 0.001

------------------------------------------------------------------------

### Конвертация

    conv_convert <value> <fromCode> <toCode>

Пример:

    conv_convert 1000 mg g
    result: 1 g

------------------------------------------------------------------------

### Удаление правила

    conv_delete <rule_id>

Пример:

    conv_delete 1
    OK deleted

------------------------------------------------------------------------

### Обновление правила

    conv_update <rule_id> factor=value

Пример:

    conv_update 1 factor=0.002

------------------------------------------------------------------------

### Проверка циклов

    conv_check_cycle

Команда выполняет упрощённую проверку наличия подозрительных циклов.

------------------------------------------------------------------------

## Структура проекта

    src
├── main
│   ├── java
│   │   ├── cli
│   │   │   ├── Command.java
│   │   │   ├── CommandContext.java
│   │   │   ├── ConvAddCommand.java
│   │   │   ├── ConvCheckCycleCommand.java
│   │   │   ├── ConvConvertCommand.java
│   │   │   ├── ConvDeleteCommand.java
│   │   │   ├── ConvListCommand.java
│   │   │   ├── ConvUpdateCommand.java
│   │   │   ├── ExitCommand.java
│   │   │   ├── HelpCommand.java
│   │   │   ├── Main.java
│   │   │   ├── UnitAddCommand.java
│   │   │   ├── UnitListCommand.java
│   │   │   ├── UnitShowCommand.java
│   │   │   └── UnitUpdateCommand.java
│   │   └── model
│   │       ├── Unit.java
│   │       ├── ConversionRule.java
│   │       ├── ValueWithUnit.java
│   │       └── service
│   │           ├── UnitCollectionManager.java
│   │           ├── ConversionRuleController.java
│   │           ├── ConversionService.java
│   │           └── validation
│   │               ├── UnitValidator.java
│   │               ├── ConversionRuleValidator.java
│   │               └── ValueWithUnitValidator.java
