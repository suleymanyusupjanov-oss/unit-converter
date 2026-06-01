package service;

import model.ConversionRule;
import storage.DbErrors;
import storage.DbStorage;
import validation.ConversionRuleValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversionRuleCollectionManager {

    // Класс доступа к БД (этап 6).
    private final DbStorage db;

    // In-memory кэш всех правил. Синхронизируется с БД.
    private final ArrayList<ConversionRule> ruleCollection = new ArrayList<>();

    public ConversionRuleCollectionManager(DbStorage db) {
        this.db = db;
    }


    // СОЗДАНИЕ ПРАВИЛА
    public ConversionRule createRule(String fromUnitCode, String toUnitCode, double factor, long ownerId) {

        // ВАЛИДАЦИЯ данных
        ConversionRuleValidator.validateFromUnitCode(fromUnitCode);
        ConversionRuleValidator.validateToUnitCode(toUnitCode);
        ConversionRuleValidator.validateFactor(factor);

        ConversionRule rule = new ConversionRule();
        rule.setFromUnitCode(fromUnitCode);
        rule.setToUnitCode(toUnitCode);
        rule.setFactor(factor);
        rule.setOwnerId(ownerId);

        try {
            db.insertRule(rule); // id/created_at/updated_at проставит БД
        } catch (SQLException e) {
            throw new RuntimeException("Добавление правила: " + DbErrors.humanize(e), e);
        }

        ruleCollection.add(rule);
        return rule;
    }


    // LIST по ТЗ
    public List<ConversionRule> getRules() {

        return Collections.unmodifiableList(ruleCollection);
    }


    // ПОИСК ПО ID

    public ConversionRule getById(long id) {
        for (ConversionRule r : ruleCollection) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }


    // ПОИСК ПРАВИЛА КОНВЕРСИИ

    public ConversionRule findRule(String fromCode, String toCode) {

        for (ConversionRule r : ruleCollection) {

            if (r.getFromUnitCode().equals(fromCode) &&
                    r.getToUnitCode().equals(toCode)) {

                return r;
            }
        }

        return null;
    }


    // ОБНОВЛЕНИЕ ПРАВИЛА — только factor. БД проставит новый updated_at.
    public void update(long id, double factor) {

        ConversionRule rule = getById(id);
        if (rule == null) {
            throw new IllegalArgumentException("Правило с таким id не найдено");
        }

        ConversionRuleValidator.validateFactor(factor);
        rule.setFactor(factor);

        try {
            db.updateRule(rule); // запишет новый updated_at обратно в объект
        } catch (SQLException e) {
            throw new RuntimeException("Обновление правила: " + DbErrors.humanize(e), e);
        }
    }


    // УДАЛЕНИЕ — сначала из БД, потом из in-memory кэша
    public boolean remove(long id) {

        ConversionRule rule = getById(id);
        if (rule == null) return false;

        try {
            db.deleteRule(id);
        } catch (SQLException e) {
            throw new RuntimeException("Удаление правила: " + DbErrors.humanize(e), e);
        }
        ruleCollection.remove(rule);
        return true;
    }

    /** Загружает все правила из БД в in-memory кэш. Вызывать при старте. */
    public void loadFromDb() {
        try {
            ruleCollection.clear();
            ruleCollection.addAll(db.findAllRules());
        } catch (SQLException e) {
            throw new RuntimeException("Загрузка правил: " + DbErrors.humanize(e), e);
        }
    }
}