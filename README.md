# Unit Converter Pro

## Описание

JavaFX-приложение на Java для управления единицами измерения и правилами их конверсии.

**5 этап** добавляет систему авторизации пользователей: регистрация, вход по логину и паролю (SHA-256), хранение данных пользователей в `users.xml`, автозагрузка коллекции после входа, автосохранение при закрытии, разграничение прав на удаление объектов (удалять может только владелец).

Реализован на:
- Java 17
- Maven
- JavaFX 17
- Jackson XML (XmlMapper + JavaTimeModule)
- SHA-256 через `MessageDigest`

---

## Требования

- Java 17+
- Maven 3.6+

Проверка:

    java -version
    mvn -v

---

## Сборка и запуск

```bash
mvn package
java --module-path "<путь к JavaFX lib>" --add-modules javafx.controls,javafx.fxml -jar target/unit-converter-1.0.jar
```

Или через Launcher (если настроен манифест):

```bash
mvn package
java -jar target/unit-converter-1.0.jar
```

---

## Работа с приложением (5 этап)

### Первый запуск

1. Открывается окно входа.
2. Введите логин и пароль → нажмите **Зарегистрироваться**.
3. После регистрации нажмите **Войти**.
4. Открывается главное окно с вашим именем в заголовке.

### Следующие запуски

1. Вводите логин и пароль → **Войти**.
2. Данные коллекции (`data.xml`) загружаются автоматически.
3. При закрытии окна коллекция сохраняется автоматически.

### Права доступа

- Все пользователи **видят** общую коллекцию.
- Удалить единицу измерения может только её **владелец** (тот, кто её добавил).
- При попытке удалить чужой объект выводится сообщение «Нет прав».

---

## Файлы данных

| Файл | Содержимое |
|------|-----------|
| `data.xml` | Единицы измерения и правила конверсии |
| `users.xml` | Пользователи (логины + хеши паролей) |

Оба файла создаются автоматически в рабочей директории. В `.gitignore` они исключены.

---

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
    │   │   │   ├── LoadCommand.java
    │   │   │   ├── Main.java
    │   │   │   ├── SaveCommand.java
    │   │   │   ├── UnitAddCommand.java
    │   │   │   ├── UnitListCommand.java
    │   │   │   ├── UnitShowCommand.java
    │   │   │   └── UnitUpdateCommand.java
    │   │   ├── model
    │   │   │   ├── ConversionRule.java   ← ownerId вместо ownerUsername
    │   │   │   ├── DataWrapper.java
    │   │   │   ├── Unit.java             ← ownerId вместо ownerUsername
    │   │   │   ├── User.java             ← НОВЫЙ: доменный класс пользователя
    │   │   │   └── ValueWithUnit.java
    │   │   ├── service
    │   │   │   ├── ConversionRuleCollectionManager.java
    │   │   │   ├── ConversionService.java
    │   │   │   ├── UnitCollectionManager.java
    │   │   │   └── UserManager.java      ← НОВЫЙ: управление пользователями и сессией
    │   │   ├── storage
    │   │   │   ├── UserXmlStorage.java   ← НОВЫЙ: сохранение/загрузка users.xml
    │   │   │   └── XmlStorage.java
    │   │   ├── ui
    │   │   │   ├── AddRuleController.java
    │   │   │   ├── AddUnitController.java ← принимает UserManager
    │   │   │   ├── Launcher.java
    │   │   │   ├── LoginController.java  ← НОВЫЙ: контроллер окна входа
    │   │   │   ├── MainApp.java          ← модальное окно входа, авто-load/save
    │   │   │   └── MainController.java   ← проверка прав при удалении
    │   │   └── validation
    │   │       ├── ConversionRuleValidator.java
    │   │       ├── UnitValidator.java
    │   │       └── ValueWithUnitValidator.java
    │   └── resources
    │       ├── AddRuleWindow.fxml
    │       ├── AddUnitWindow.fxml
    │       ├── LoginWindow.fxml          ← НОВЫЙ: форма входа/регистрации
    │       └── MainWindow.fxml

---

## Архитектура авторизации (5 этап)

```
MainApp.start()
  │
  ├─ UserXmlStorage.load("users.xml")   → загрузить список пользователей
  ├─ LoginWindow (модальное окно)        → блокирует главный поток
  │     └─ LoginController              → вызывает userManager.login() / .register()
  ├─ UserXmlStorage.save("users.xml")   → сохранить (мог зарегистрироваться новый)
  │
  ├─ если не вошёл → выход
  │
  ├─ UnitCollectionManager.loadFromFile("data.xml")  → автозагрузка данных
  ├─ MainController.setServices(..., userManager)    → передача сервисов в UI
  └─ primaryStage.setOnCloseRequest(...)             → автосохранение data.xml
```

---

## Пароли и безопасность

Пароль никогда не хранится в открытом виде. При регистрации:

1. Вызывается `User.hashPassword(password)`.
2. Внутри: `MessageDigest.getInstance("SHA-256")` → байты → hex-строка.
3. В `users.xml` записывается только хеш (64 символа hex).

При входе тот же алгоритм применяется к введённому паролю и результат сравнивается с сохранённым хешем.
