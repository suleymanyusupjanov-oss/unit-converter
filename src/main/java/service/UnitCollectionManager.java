package service;

import model.Unit;
import validation.UnitValidator;

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
    public Unit createUnit(String code, String name, String ownerUsername) {

        // ВАЛИДАЦИЯ данных
        UnitValidator.validateCode(code);
        UnitValidator.validateName(name);
        UnitValidator.validateOwnerUsername(ownerUsername);

        // создаём объект с id и временем создания
        Unit unit = new Unit(generateId(), Instant.now());

        // заполняем данные объекта
        unit.setCode(code);
        unit.setName(name);
        unit.setOwnerUsername(ownerUsername);

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
}