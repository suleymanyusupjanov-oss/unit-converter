package service;
import model.ConversionRule;
import model.Unit;
import storage.XmlStorage;
import validation.UnitValidator;
import validation.ConversionRuleValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnitCollectionManager {

    // Коллекция, где хранятся все Unit
    // final означает, что ссылка на список не может измениться
    private final ArrayList<Unit> unitCollection = new ArrayList<>();


    // Счетчик для генерации уникального id
    // каждый новый объект получит следующий номер
    private long nextId = 1;


    // Метод генерации id
    // возвращает текущий id и увеличивает счетчик
    private long generateId() {
        return nextId++;
    }


    // СОЗДАНИЕ ЕДИНИЦЫ
    // Метод создаёт новую единицу измерения
    // принимает данные от пользователя
    public Unit createUnit(String code, String name, long ownerId) {

        // ВАЛИДАЦИЯ данных
        UnitValidator.validateCode(code);
        UnitValidator.validateName(name);

        // создаём объект с id и временем создания
        Unit unit = new Unit(generateId(), Instant.now());

        // заполняем данные объекта
        unit.setCode(code);
        unit.setName(name);
        unit.setOwnerId(ownerId);

        // устанавливаем время последнего изменения
        unit.setUpdatedAt(Instant.now());

        // добавляем объект в коллекцию
        unitCollection.add(unit);

        // возвращаем созданный объект
        return unit;
    }


    // возвращает список всех единиц
    public List<Unit> getUnits() {

        // unmodifiableList делает список только для чтения
        // внешний код не сможет изменить коллекцию
        return Collections.unmodifiableList(unitCollection);
    }


    // ищет единицу по уникальному id
    public Unit getById(long id) {

        // перебираем все элементы коллекции
        for (Unit u : unitCollection) {

            // сравниваем id
            if (u.getId() == id) {
                return u;
            }
        }

        // если объект не найден
        return null;
    }


    // ищет единицу по коду (mg, g, kg)
    public Unit findByCode(String code) {

        // перебираем все объекты
        for (Unit u : unitCollection) {

            if (u.getCode().equals(code)) {
                return u;
            }
        }

        return null;
    }


    // ОБНОВЛЕНИЕ ЕДИНИЦЫ
    // обновляет код и название единицы
    public void update(long id, String code, String name) {

        Unit unit = getById(id);

        if (unit == null) {
            throw new IllegalArgumentException("Единица с таким id не найдена");
        }

        // валидация новых данных
        UnitValidator.validateCode(code);
        UnitValidator.validateName(name);

        // обновляем данные
        unit.setCode(code);
        unit.setName(name);

        // обновляем время последнего изменения
        unit.setUpdatedAt(Instant.now());
    }


    // УДАЛЕНИЕ
    // удаляет единицу по id
    public boolean remove(long id) {

        Unit unit = getById(id);

        if (unit != null) {

            unitCollection.remove(unit);

            return true;
        }

        // если объект не найден
        return false;
    }

    // СОХРАНЕНИЕ В ФАЙЛ
    // Передает текущую коллекцию в XmlStorage для записи на диск
    public void saveToFile(String path) throws Exception {

        // Создаем нашего помощника по работе с XML
        storage.XmlStorage xmlStorage = new storage.XmlStorage();

        // Отдаем ему команду сохранить нашу коллекцию по указанному пути
        xmlStorage.save(this.unitCollection, path);

        System.out.println("Данные успешно сохранены в файл: " + path);
    }

    // ЗАГРУЗКА ИЗ ФАЙЛА
    public void loadFromFile(String path) {
        try {
            XmlStorage xmlStorage = new XmlStorage();
            List<Unit> loadedUnits = xmlStorage.load(path);

            // Если из файла пришла пустота - просто прерываемся, чтобы не затереть то, что есть
            if (loadedUnits == null) {
                System.out.println("Файл пуст или вернул null.");
                return;
            }

            // Твоя валидация (оставляем как было)
            for (Unit unit : loadedUnits) {
                validation.UnitValidator.validateCode(unit.getCode());
                validation.UnitValidator.validateName(unit.getName());
                for (model.ConversionRule rule : unit.getRules()) {
                    validation.ConversionRuleValidator.validateFromUnitCode(rule.getFromUnitCode());
                    validation.ConversionRuleValidator.validateToUnitCode(rule.getToUnitCode());
                    validation.ConversionRuleValidator.validateFactor(rule.getFactor());
                }
            }

            // Если дошли сюда — данные 100% валидны. Заменяем коллекцию.
            this.unitCollection.clear();
            this.unitCollection.addAll(loadedUnits);

            // === БЕЗОПАСНЫЙ СДВИГ ID ===
            // Ищем самый большой ID среди загруженных файлов
            long maxId = 0;
            for (Unit u : loadedUnits) {
                if (u.getId() > maxId) {
                    maxId = u.getId();
                }
            }
            // Ставим счетчик на 1 больше максимального
            this.nextId = maxId + 1;

            System.out.println("Успех! Загружено единиц: " + loadedUnits.size() + ". Следующий свободный ID: " + nextId);

        } catch (Exception e) {
            // ТЕПЕРЬ ОШИБКА БУДЕТ КРАСНОЙ И ПОНЯТНОЙ В КОНСОЛИ
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА ЗАГРУЗКИ ИЗ ФАЙЛА:");
            e.printStackTrace();
        }
    }

    public void add(model.Unit unit) {
        // Присваиваем уникальный ID и увеличиваем счетчик
        unit.setId(this.nextId);
        this.nextId++;

        // Добавляем во внутреннюю (незащищенную) коллекцию
        this.unitCollection.add(unit);
    }
}