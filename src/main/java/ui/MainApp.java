package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UnitCollectionManager;

public class MainApp extends Application {

    private UnitCollectionManager unitManager; // Вынесли наверх, чтобы был доступен при закрытии

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        // 1. Создаем сервисы
        unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        // === МАГИЯ ЗАГРУЗКИ ===
        // Пытаемся загрузить старые данные при запуске программы
        try {
            unitManager.loadFromFile("data.xml");
            System.out.println("Данные успешно загружены из файла.");
        } catch (Exception e) {
            System.out.println("Файл данных не найден или пуст. Начинаем с чистого листа.");
        }

        // 2. Передаем сервисы (уже с данными!) в контроллер
        controller.setServices(unitManager, ruleManager, conversionService);

        // === МАГИЯ СОХРАНЕНИЯ ===
        // Говорим программе: "Когда пользователь нажмет на крестик, сохрани всё в файл!"
        primaryStage.setOnCloseRequest(event -> {
            try {
                unitManager.saveToFile("data.xml");
                System.out.println("Данные успешно сохранены перед выходом.");
            } catch (Exception e) {
                System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            }
        });

        // 3. Настраиваем и показываем окно
        primaryStage.setTitle("Unit Converter - Финал");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}