package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ConversionRule;
import model.Unit;
import service.ConversionRuleCollectionManager;
import service.UserManager;

public class AddRuleController {
    @FXML private TextField toUnitField, factorField;
    @FXML private Button saveButton;
    private Unit targetUnit;
    private Runnable callback;
    private UserManager userManager;
    private ConversionRuleCollectionManager ruleManager;

    public void setUnit(Unit u, UserManager um, ConversionRuleCollectionManager rm, Runnable cb) {
        this.targetUnit = u;
        this.userManager = um;
        this.ruleManager = rm;
        this.callback = cb;
    }

    @FXML public void initialize() {
        saveButton.setOnAction(e -> {
            try {
                String toCode = toUnitField.getText();
                double factor = Double.parseDouble(factorField.getText().replace(",", "."));
                long ownerId = userManager != null ? userManager.getCurrentUser().getId() : 0L;

                // Этап 6: правило идёт через менеджер → БД + in-memory кэш
                ConversionRule r = ruleManager.createRule(
                        targetUnit.getCode(), toCode, factor, ownerId);

                // Для master-detail UI: тоже кладём в локальный список юнита
                targetUnit.getRules().add(r);

                if (callback != null) callback.run();
                ((Stage)saveButton.getScene().getWindow()).close();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Не удалось добавить правило: " + ex.getMessage()).showAndWait();
            }
        });
    }
}