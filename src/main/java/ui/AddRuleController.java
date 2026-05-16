package ui;

import javafx.fxml.FXML;
    import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.ConversionRule;
import model.Unit;
import service.UserManager;

public class AddRuleController {
    @FXML private TextField toUnitField, factorField;
    @FXML private Button saveButton;
    private Unit targetUnit;
    private Runnable callback;
    private UserManager userManager;

    public void setUnit(Unit u, UserManager um, Runnable cb) {
        this.targetUnit = u;
        this.userManager = um;
        this.callback = cb;
    }

    @FXML public void initialize() {
        saveButton.setOnAction(e -> {
            try {
                ConversionRule r = new ConversionRule();
                r.setFromUnitCode(targetUnit.getCode());
                r.setToUnitCode(toUnitField.getText());
                r.setFactor(Double.parseDouble(factorField.getText().replace(",", ".")));
                r.setOwnerId(userManager != null ? userManager.getCurrentUser().getId() : 0L);

                targetUnit.getRules().add(r);
                if (callback != null) callback.run();
                ((Stage)saveButton.getScene().getWindow()).close();
            } catch (Exception ex) { /* лог ошибки */ }
        });
    }
}