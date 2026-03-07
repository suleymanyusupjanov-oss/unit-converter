package main.java.service;

import main.java.model.ConversionRule;
import main.java.validation.ConversionRuleValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversionRuleCollectionManager {

    // коллекция правил конверсии
    private final ArrayList<ConversionRule> ruleCollection = new ArrayList<>();


    // счетчик id
    private long nextId = 1;

    // генерация id
    private long generateId() {
        return nextId++;
    }


    // СОЗДАНИЕ ПРАВИЛА

    public ConversionRule createRule(String fromUnitCode, String toUnitCode, double factor, String ownerUsername) {

        // ВАЛИДАЦИЯ данных
        ConversionRuleValidator.validateFromUnitCode(fromUnitCode);
        ConversionRuleValidator.validateToUnitCode(toUnitCode);
        ConversionRuleValidator.validateFactor(factor);
        ConversionRuleValidator.validateOwnerUsername(ownerUsername);

        ConversionRule rule = new ConversionRule(generateId(), Instant.now());

        // заполняем данные
        rule.setFromUnitCode(fromUnitCode);
        rule.setToUnitCode(toUnitCode);
        rule.setFactor(factor);
        rule.setOwnerUsername(ownerUsername);

        // время последнего изменения
        rule.setUpdatedAt(Instant.now());

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


    // ОБНОВЛЕНИЕ ПРАВИЛА

    public void update(long id, double factor) {

        ConversionRule rule = getById(id);

        if (rule == null) {
            throw new IllegalArgumentException("Правило с таким id не найдено");
        }

        // валидация коэффициента
        ConversionRuleValidator.validateFactor(factor);

        rule.setFactor(factor);

        rule.setUpdatedAt(Instant.now());
    }


    // УДАЛЕНИЕ

    public boolean remove(long id) {

        ConversionRule rule = getById(id);

        if (rule != null) {

            ruleCollection.remove(rule);

            return true;
        }

        return false;
    }
}