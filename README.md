# Unit Converter Pro

## Описание

JavaFX-приложение на Java для управления единицами измерения и правилами их конверсии.

**6 этап** заменяет файловое хранилище (XML) на **PostgreSQL через JDBC**:
- Пользователи, единицы и правила хранятся в реляционной БД
- ID генерируется БД через `SERIAL`
- Связь `1:N` между `users` и `units`/`conversion_rules` через `FOREIGN KEY`
- Конфигурация подключения — в файле или переменных окружения (не в коде)
- Каждая операция синхронно пишется в БД и в in-memory кэш
- Кнопка **Refresh** подтягивает свежие данные (для multi-window сценария)

Стек:
- Java 17
- Maven
- JavaFX 17
- **PostgreSQL** + **JDBC драйвер** (`org.postgresql:postgresql:42.7.4`)
- SHA-256 для паролей через `MessageDigest`

---

## Требования

- Java 17+
- Maven 3.6+
- **PostgreSQL 14+** (рекомендуется 18, как у автора)

Проверка:

    java -version
    mvn -v
    psql --version

---

## Установка и настройка БД (Этап 6)

### 1. Установить PostgreSQL

Скачать с https://www.postgresql.org/download/ и установить стандартным установщиком.
Запомнить пароль для пользователя `postgres`. Порт по умолчанию: `5432`.

### 2. Применить схему БД

Открыть **pgAdmin** → правый клик на БД `postgres` → **Query Tool** →
вставить содержимое `schema.sql` (из корня проекта) → нажать **Execute** (молния).

Создадутся три таблицы:
- `users` — логины и хеши паролей
- `units` — единицы измерения
- `conversion_rules` — правила конверсии

### 3. Настроить подключение

Создать файл `db.properties` в корне проекта (он в `.gitignore`):

```properties
db.url=jdbc:postgresql://localhost:5432/postgres
db.user=postgres
db.password=ВАШ_ПАРОЛЬ
```

Альтернатива: переменные окружения `DB_URL`, `DB_USER`, `DB_PASSWORD`
(имеют приоритет над файлом).

---

## Сборка и запуск

### Через Maven

```bash
mvn package
java -jar target/unit-converter-1.0-jar-with-dependencies.jar
```

### Через `mvn exec` (без сборки jar)

```bash
mvn exec:java "-Dexec.mainClass=ui.Launcher"
```

### Несколько окон параллельно

Просто запустить команду в разных терминалах — каждое окно имеет своё
JDBC-соединение, общая БД синхронизирует данные. Кнопка **Refresh**
подтягивает изменения от других окон.

---

## Работа с приложением

### Первый запуск

1. Открывается окно входа
2. Регистрация — логин и пароль сохраняются в таблицу `users` (пароль хешируется SHA-256)
3. Вход — открывается главное окно с твоим логином в заголовке

### Главное окно

- Список единиц (левая таблица) и правил (правая таблица)
- **Add Unit** / **Add Rule** — добавление (синхронно в БД)
- **Контекстное меню "Удалить"** — для своих объектов (на чужих — disabled)
- **Refresh** — подтянуть свежие данные из БД (для multi-window)
- **Save** (рудимент) — показывает что БД сохраняет автоматически

### Права доступа (этап 5+6)

- Все пользователи **видят** общую коллекцию
- Изменять/удалять может только **владелец** (по `ownerId` → таблица `users`)
- Кнопки Add Rule и пункты "Удалить" для чужих строк **выключены**
- В таблице есть колонка **Owner** с логином владельца

---

## Структура проекта

```
src/main/java/
├── cli/                  командный интерпретатор (исторически с 1 этапа)
├── config/
│   └── DbConfig.java     загрузка настроек подключения
├── model/
│   ├── User.java
│   ├── Unit.java
│   ├── ConversionRule.java
│   └── ValueWithUnit.java
├── service/
│   ├── UserManager.java                    register/login через DbStorage
│   ├── UnitCollectionManager.java          CRUD единиц через DbStorage
│   ├── ConversionRuleCollectionManager.java CRUD правил через DbStorage
│   └── ConversionService.java
├── storage/
│   ├── DbStorage.java    единый класс доступа к БД (вариант 2 ЛР3)
│   └── DbErrors.java     маппинг SQLState → понятные сообщения
├── ui/                   JavaFX контроллеры и FXML
└── validation/

schema.sql                схема БД (этап 6)
db.properties             настройки подключения (не в git)
```

---

## Архитектура (этап 6)

```
MainApp.start()
  │
  ├─ DbStorage(DbConfig.load()) → connect()
  │     └─ если ошибка → диалог "Не удалось подключиться к БД"
  │
  ├─ UserManager(db).loadFromDb()
  ├─ LoginWindow (модальное)
  │
  ├─ UnitCollectionManager(db).loadFromDb()
  ├─ ConversionRuleCollectionManager(db).loadFromDb()
  ├─ linkRulesToUnits()  ← связываем правила с юнитами для master-detail UI
  │
  └─ primaryStage.setOnCloseRequest(e -> db.close())
```

### Поток операции add/update/remove

```
UI → менеджер → DbStorage (PreparedStatement, SERIAL → RETURNING id)
                       │
                       └─ при успехе → синхронизация in-memory кэша
                       └─ при ошибке → DbErrors.humanize(SQLException) → Alert
```

---

## Безопасность

- Пароли — SHA-256, не plain
- Все запросы — `PreparedStatement` (защита от SQL injection)
- `db.properties` в `.gitignore`
- Пароль БД допускается через env var `DB_PASSWORD`
