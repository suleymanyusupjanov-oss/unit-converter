package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ConversionRule;
import model.Unit;

public class AddRuleController {

    @FXML private TextField toUnitField;
    @FXML private TextField factorField;
    @FXML private Button saveButton;

    private Unit targetUnit;
    private Runnable onRuleAddedCallback;

    public void setUnit(Unit unit, Runnable callback) {
        this.targetUnit = unit;
        this.onRuleAddedCallback = callback;
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSave());
    }

    private void handleSave() {
        String toCode = toUnitField.getText().trim();
        String factorStr = factorField.getText().trim().replace(",", "."); // Защита от запятых

        if (toCode.isEmpty() || factorStr.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля!", Alert.AlertType.ERROR);
            return;
        }

        try {
            double factor = Double.parseDouble(factorStr);
            if (factor <= 0) throw new NumberFormatException();

            // Создаем новое правило и кладем его в выбранную единицу
            ConversionRule rule = new ConversionRule();
            rule.setFromUnitCode(targetUnit.getCode());
            rule.setToUnitCode(toCode);
            rule.setFactor(factor);
            // Если у тебя ругается на эту строчку ниже, можешь просто её удалить
            // rule.setOwnerUsername("USER_UI");

            targetUnit.getRules().add(rule);

            // Обновляем правую таблицу
            if (onRuleAddedCallback != null) onRuleAddedCallback.run();

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Коэффициент должен быть положительным числом!", Alert.AlertType.ERROR);
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