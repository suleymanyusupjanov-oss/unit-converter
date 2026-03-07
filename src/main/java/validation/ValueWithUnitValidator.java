package main.java.validation;

public class ValueWithUnitValidator {

    public static void validateValue(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Значение должно быть больше нуля.");
        }
    }

    public static void validateUnitCode(String unitCode) {
        if (unitCode == null || unitCode.isBlank()) {
            throw new IllegalArgumentException("Единица измерения не указана.");
        }
    }

}