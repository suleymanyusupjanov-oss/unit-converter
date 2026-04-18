package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Unit;
import service.UnitCollectionManager;

public class AddUnitController {
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private Button saveButton;

    private UnitCollectionManager unitManager;
    private Runnable onUnitAddedCallback; // для обновления главного окна

    // Метод для передачи менеджера из главного окна
    public void setUnitManager(UnitCollectionManager unitManager, Runnable onUnitAddedCallback) {
        this.unitManager = unitManager;
        this.onUnitAddedCallback = onUnitAddedCallback;
    }
    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSave());
    }
    private void handleSave() {
        //считываем что ввел пользователь
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        try {
            // 2. Отдаем данные менеджеру.
            // Если данные плохие, менеджер прервет работу и выбросит ошибку прямо отсюда.
            unitManager.createUnit(code, name, "USER_UI");

            // 3. Если код дошел сюда, значит валидация прошла успешно! Обновляем список.
            if (onUnitAddedCallback != null) {
                onUnitAddedCallback.run();
            }
            // 4. Закрываем окно
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {

            // Если UnitValidator выбросил IllegalArgumentException,
            // мы берем его текст (e.getMessage()) и показываем во всплывающем окне
            showAlert("Ошибка данных", e.getMessage(), Alert.AlertType.ERROR);

        } catch (Exception e) {
            // Отлов любых других непредвиденных ошибок (на всякий случай)
            showAlert("Системная ошибка", e.getMessage(), Alert.AlertType.ERROR);
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
