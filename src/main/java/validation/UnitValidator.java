package validation;

public class UnitValidator {

    public static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Код единицы измерения не может быть пустым");
        }

        if (code.length() > 16) {
            throw new IllegalArgumentException("Код единицы измерения не может быть длиннее 16 символов");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название единицы измерения не может быть пустым");
        }

        if (name.length() > 64) {
            throw new IllegalArgumentException("Название единицы измерения не может быть длиннее 64 символов");
        }
    }

    public static void validateOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
    }
}