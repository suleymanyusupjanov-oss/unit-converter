package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ищем наш "чертеж" окна в папке ресурсов
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));

        // Задаем размер окна 800 на 600
        Scene scene = new Scene(loader.load(), 800, 600);

        // Настраиваем заголовок
        primaryStage.setTitle("Unit Converter - Этап 4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args); // Команда на запуск графики
    }
}
