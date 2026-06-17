module unit.converter {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Этап 6: JDBC для PostgreSQL
    requires java.sql;

    // Доп. задание: прямой доступ к PGConnection/PGNotification для LISTEN/NOTIFY
    requires org.postgresql.jdbc;

    // Разрешаем JavaFX работать с пакетом ui (FXML)
    opens ui to javafx.fxml;
    exports ui;

    // Разрешаем JavaFX TableView читать model через рефлексию
    opens model to javafx.base;
    exports model;
}
