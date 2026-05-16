package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Unit;
import service.UnitCollectionManager;
import service.UserManager;

public class AddUnitController {

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private Button saveButton;

    private UnitCollectionManager unitManager;
    private UserManager userManager;
    private Runnable onUnitAddedCallback;

    public void setUnitManager(UnitCollectionManager manager, UserManager userMgr, Runnable callback) {
        this.unitManager = manager;
        this.userManager = userMgr;
        this.onUnitAddedCallback = callback;
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSave());
    }

    private void handleSave() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля!", Alert.AlertType.ERROR);
            return;
        }

        try {
            Unit unit = new Unit();
            unit.setCode(code);
            unit.setName(name);
            unit.setOwnerId(userManager.getCurrentUser().getId());


            unitManager.add(unit);

            if (onUnitAddedCallback != null) {
                onUnitAddedCallback.run();
            }

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            // ЭТА СТРОЧКА СПАСЕТ НАМ ЖИЗНЬ. Теперь подробная ошибка будет печататься в консоли IDEA внизу!
            e.printStackTrace();
            showAlert("Ошибка", "Произошел сбой: " + e.getClass().getSimpleName() + ". Посмотри красную консоль в IDEA!", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}