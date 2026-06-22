package service;
import model.Unit;
import storage.DbErrors;
import storage.DbStorage;
import validation.UnitValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnitCollectionManager {

    // Класс доступа к БД (этап 6).
    private final DbStorage db;

    // In-memory кэш всех Unit. Синхронизируется с БД.
    private final ArrayList<Unit> unitCollection = new ArrayList<>();

    public UnitCollectionManager(DbStorage db) {
        this.db = db;
    }


    // СОЗДАНИЕ ЕДИНИЦЫ
    public Unit createUnit(String code, String name, long ownerId) {

        // ВАЛИДАЦИЯ данных
        UnitValidator.validateCode(code);
        UnitValidator.validateName(name);

        // запрет дубликата кода у одного владельца (на уровне БД — UNIQUE(code, owner_id))
        for (Unit existing : unitCollection) {
            if (existing.getOwnerId() == ownerId && existing.getCode().equals(code)) {
                throw new IllegalArgumentException("Такая единица уже существует");
            }
        }

        // создаём объект, id/created_at/updated_at проставит БД
        Unit unit = new Unit();
        unit.setCode(code);
        unit.setName(name);
        unit.setOwnerId(ownerId);

        try {
            db.insertUnit(unit);
        } catch (SQLException e) {
            throw new RuntimeException("Добавление единицы: " + DbErrors.humanize(e), e);
        }

        // синхронизируем in-memory кэш
        unitCollection.add(unit);
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


    // ОБНОВЛЕНИЕ ЕДИНИЦЫ — code/name. БД проставит новый updated_at.
    public void update(long id, String code, String name) {

        Unit unit = getById(id);

        if (unit == null) {
            throw new IllegalArgumentException("Единица с таким id не найдена");
        }

        UnitValidator.validateCode(code);
        UnitValidator.validateName(name);

        // запрет дубликата кода у того же владельца (кроме самой этой единицы)
        for (Unit existing : unitCollection) {
            if (existing.getId() != id && existing.getOwnerId() == unit.getOwnerId()
                    && existing.getCode().equals(code)) {
                throw new IllegalArgumentException("Такая единица уже существует");
            }
        }

        unit.setCode(code);
        unit.setName(name);

        try {
            db.updateUnit(unit); // запишет новый updated_at обратно в объект
        } catch (SQLException e) {
            throw new RuntimeException("Обновление единицы: " + DbErrors.humanize(e), e);
        }
    }


    // УДАЛЕНИЕ — сначала из БД, потом из in-memory кэша
    public boolean remove(long id) {

        Unit unit = getById(id);
        if (unit == null) return false;

        try {
            db.deleteUnit(id);
        } catch (SQLException e) {
            throw new RuntimeException("Удаление единицы: " + DbErrors.humanize(e), e);
        }
        unitCollection.remove(unit);
        return true;
    }

    /** Загружает все юниты из БД в in-memory кэш. Вызывать при старте. */
    public void loadFromDb() {
        try {
            unitCollection.clear();
            unitCollection.addAll(db.findAllUnits());
        } catch (SQLException e) {
            throw new RuntimeException("Загрузка единиц: " + DbErrors.humanize(e), e);
        }
    }
}