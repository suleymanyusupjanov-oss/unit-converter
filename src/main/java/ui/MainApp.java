package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UnitCollectionManager;
import service.UserManager;
import storage.UserXmlStorage;

public class MainApp extends Application {

    private static final String USERS_FILE = "users.xml";
    private static final String DATA_FILE = "data.xml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        UserManager userManager = new UserManager();
        UserXmlStorage userStorage = new UserXmlStorage();

        // Загружаем пользователей из файла при старте
        try {
            userManager.setUsers(userStorage.load(USERS_FILE));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить пользователей: " + e.getMessage());
        }

        // Показываем окно входа
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/LoginWindow.fxml"));
        Stage loginStage = new Stage();
        loginStage.setTitle("Вход");
        loginStage.setScene(new Scene(loginLoader.load()));
        loginStage.initModality(Modality.APPLICATION_MODAL);
        LoginController loginController = loginLoader.getController();
        loginController.setUserManager(userManager);
        loginStage.showAndWait();

        // Сохраняем пользователей (мог зарегистрироваться новый)
        try {
            userStorage.save(userManager.getUsers(), USERS_FILE);
        } catch (Exception e) {
            System.err.println("Не удалось сохранить пользователей: " + e.getMessage());
        }

        // Если не вошёл — не открываем главное окно
        if (!loginController.isLoginSuccessful()) return;

        // Открываем главное окно
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        UnitCollectionManager unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        controller.setServices(unitManager, ruleManager, conversionService, userManager);

        primaryStage.setTitle("Unit Converter Pro — " + userManager.getCurrentUser().getLogin());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}