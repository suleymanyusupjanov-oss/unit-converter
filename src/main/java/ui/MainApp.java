package ui;

import config.DbConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UnitCollectionManager;
import service.UserManager;
import storage.DbChangeListener;
import storage.DbStorage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // === Этап 6: подключение к PostgreSQL ===
        DbStorage db;
        try {
            db = new DbStorage(DbConfig.load());
            db.connect();
        } catch (Exception e) {
            showFatalDbError(e);
            return;
        }

        UserManager userManager = new UserManager(db);
        userManager.loadFromDb();

        // Показываем окно входа
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/LoginWindow.fxml"));
        Stage loginStage = new Stage();
        loginStage.setTitle("Вход");
        loginStage.setScene(new Scene(loginLoader.load()));
        loginStage.initModality(Modality.APPLICATION_MODAL);
        LoginController loginController = loginLoader.getController();
        loginController.setUserManager(userManager);
        loginStage.showAndWait();

        // Этап 6: регистрация уже сохранила пользователя в БД — XML save больше не нужен.

        // Если не вошёл — не открываем главное окно
        if (!loginController.isLoginSuccessful()) {
            db.close();
            return;
        }

        // Открываем главное окно
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        UnitCollectionManager unitManager = new UnitCollectionManager(db);
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager(db);
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        // Этап 6: загрузка из БД в in-memory кэш
        unitManager.loadFromDb();
        ruleManager.loadFromDb();

        // Связываем правила с их юнитами по коду И владельцу (для master-detail UI).
        // Owner-проверка важна: иначе правила одного пользователя подмешались бы
        // к одноимённой единице другого пользователя.
        for (var unit : unitManager.getUnits()) {
            unit.getRules().clear();
            for (var rule : ruleManager.getRules()) {
                if (rule.getFromUnitCode().equals(unit.getCode())
                        && rule.getOwnerId() == unit.getOwnerId()) {
                    unit.getRules().add(rule);
                }
            }
        }

        controller.setServices(unitManager, ruleManager, conversionService, userManager);

        primaryStage.setTitle("Unit Converter Pro — " + userManager.getCurrentUser().getLogin());
        primaryStage.setScene(new Scene(root));

        // Доп. задание (вариант 3): real-time синхронизация между клиентами.
        // Слушатель в фоне ждёт уведомлений БД (LISTEN/NOTIFY) и при изменении
        // данных другим клиентом просит контроллер перечитать таблицы.
        DbChangeListener changeListener = new DbChangeListener(
                DbConfig.load(),
                () -> Platform.runLater(controller::onExternalChange));
        changeListener.start();

        // Этап 6: каждая операция сохраняется в БД сразу — авто-сохранение при закрытии не нужно.
        primaryStage.setOnCloseRequest(e -> {
            changeListener.stop();   // останавливаем фоновый поток-слушатель
            db.close();
        });

        primaryStage.show();
    }

    /** Показывает фатальную ошибку подключения к БД и закрывает приложение. */
    private void showFatalDbError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Не удалось подключиться к базе данных");
        alert.setHeaderText("Подключение к PostgreSQL не удалось");
        alert.setContentText(
                "Причина: " + e.getMessage() + "\n\n" +
                "Проверьте:\n" +
                "  • Запущен ли PostgreSQL\n" +
                "  • Правильны ли настройки в db.properties\n" +
                "    (URL, пользователь, пароль)\n" +
                "  • Применена ли schema.sql"
        );
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}