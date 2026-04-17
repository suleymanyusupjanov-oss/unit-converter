module unit.converter {
    // Подключаем графику JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // requires java.xml;

    // Разрешаем JavaFX работать с нашим новым пакетом ui
    opens ui to javafx.fxml;
    exports ui;

    // Разрешаем JavaFX видеть твои классы из пакета model (нужно для таблиц)
    opens model to javafx.base;
    exports model;
}