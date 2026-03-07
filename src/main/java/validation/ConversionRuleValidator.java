package main.java.validation;

public class ConversionRuleValidator {

    public static void validateFromUnitCode(String fromUnitCode) {
        if (fromUnitCode == null || fromUnitCode.isBlank()) {
            throw new IllegalArgumentException("Код исходной единицы измерения не может быть пустым");
        }
    }

    public static void validateToUnitCode(String toUnitCode) {
        if (toUnitCode == null || toUnitCode.isBlank()) {
            throw new IllegalArgumentException("Код целевой единицы измерения не может быть пустым");
        }
    }

    public static void validateFactor(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Коэффициент преобразования должен быть больше нуля");
        }
    }

    public static void validateOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
    }
}