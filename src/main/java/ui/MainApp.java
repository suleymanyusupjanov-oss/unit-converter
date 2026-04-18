package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import service.UnitCollectionManager;
import service.ConversionRuleCollectionManager;
import service.ConversionService;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ищем наш "чертеж" окна в папке ресурсов
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));

        // Задаем размер окна 800 на 600
        Scene scene = new Scene(loader.load(), 800, 600);

        // 1. Достаем контроллер из загруженного окна
        MainController controller = loader.getController();

        // 2. Создаем реальные сервисы
        UnitCollectionManager unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        // 3. Передаем эти сервисы в окно
        controller.setServices(unitManager, ruleManager, conversionService);

        // Настраиваем заголовок
        primaryStage.setTitle("Unit Converter - Этап 4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args); // Команда на запуск графики
    }
}
