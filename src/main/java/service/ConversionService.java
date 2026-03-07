package main.java.service;

import main.java.model.ConversionRule;
import main.java.model.ValueWithUnit;
import main.java.validation.ValueWithUnitValidator;
import main.java.validation.ConversionRuleValidator;

public class ConversionService {

    // сервис для работы с единицами
    private final UnitCollectionManager unitManager;

    // сервис для работы с правилами конверсии
    private final ConversionRuleCollectionManager ruleManager;


    // конструктор
    // передача сервисов в conversionService как параметры конструктора
    public ConversionService(UnitCollectionManager unitManager, ConversionRuleCollectionManager ruleManager) {

        // сохраняем переданные сервисы (параметры конструктора) в поля класса
        this.unitManager = unitManager;
        this.ruleManager = ruleManager;
    }


    // МЕТОД КОНВЕРТАЦИИ
    public ValueWithUnit convert(ValueWithUnit input, String targetUnitCode) {

        // ВАЛИДАЦИЯ входных данных
        ValueWithUnitValidator.validateValue(input.getValue());
        ValueWithUnitValidator.validateUnitCode(input.getUnitCode());
        ConversionRuleValidator.validateToUnitCode(targetUnitCode);


        // проверяем существует ли исходная единица
        if (unitManager.findByCode(input.getUnitCode()) == null) {
            throw new IllegalArgumentException("Исходная единица не найдена");
        }


        // проверяем существует ли целевая единица
        if (unitManager.findByCode(targetUnitCode) == null) {
            throw new IllegalArgumentException("Целевая единица не найдена");
        }


        // ищем правило конверсии
        ConversionRule rule = ruleManager.findRule(input.getUnitCode(), targetUnitCode);


        // если правило не найдено
        if (rule == null) {
            throw new IllegalArgumentException("Правило конверсии не найдено");
        }


        // применяем формулу из ТЗ
        double resultValue = input.getValue() * rule.getFactor();


        // создаём объект результата через конструктор
        ValueWithUnit result = new ValueWithUnit(targetUnitCode, resultValue);


        return result;
    }
}