module unit.converter {
    // 1. Подключаем графику JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // 2. Разрешения для работы с XML (Jackson с 3 этапа)
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;

    // 3. Разрешаем JavaFX работать с нашим новым пакетом ui
    opens ui to javafx.fxml;
    exports ui;

    //4. Разрешаем таблицам на экране И библиотеке Jackson читать данные из model
    opens model to javafx.base, com.fasterxml.jackson.databind;
    exports model;
}