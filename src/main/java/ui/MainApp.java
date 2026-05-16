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

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        UnitCollectionManager unitManager = new UnitCollectionManager();
        ConversionRuleCollectionManager ruleManager = new ConversionRuleCollectionManager();
        ConversionService conversionService = new ConversionService(unitManager, ruleManager);

        controller.setServices(unitManager, ruleManager, conversionService);

        primaryStage.setTitle("Unit Converter Pro");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}