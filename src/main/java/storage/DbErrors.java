package storage;

import java.sql.SQLException;

/**
 * Маппинг SQL-ошибок PostgreSQL в человеко-понятные сообщения.
 * Коды SQLState: https://www.postgresql.org/docs/current/errcodes-appendix.html
 */
public final class DbErrors {

    private DbErrors() {}

    /** Возвращает понятное сообщение для пользователя по SQLException. */
    public static String humanize(SQLException e) {
        String state = e.getSQLState();
        if (state == null) return "Ошибка БД: " + e.getMessage();

        return switch (state) {
            case "23505" -> "Уже существует запись с такими значениями (нарушено условие уникальности).";
            case "23503" -> "Связанные данные мешают операции (нарушена ссылочная целостность).";
            case "23502" -> "Не заполнено обязательное поле.";
            case "23514" -> "Значение не прошло проверку (CHECK).";
            case "08001", "08006", "08003", "08000" ->
                    "Нет соединения с PostgreSQL. Проверьте что сервер запущен и db.properties корректен.";
            default -> "Ошибка БД [" + state + "]: " + e.getMessage();
        };
    }
}
