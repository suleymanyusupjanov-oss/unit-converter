module unit.converter {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Этап 6: JDBC для PostgreSQL
    requires java.sql;

    // Разрешаем JavaFX работать с пакетом ui (FXML)
    opens ui to javafx.fxml;
    exports ui;

    // Разрешаем JavaFX TableView читать model через рефлексию
    opens model to javafx.base;
    exports model;
}
