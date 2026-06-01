package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import model.Unit;
import model.ConversionRule;
import service.UnitCollectionManager;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UserManager;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Unit> unitsTableView;
    @FXML private TableView<ConversionRule> rulesTableView;
    @FXML private Button convertButton, addUnitButton, addRuleButton, saveFileButton, loadFileButton, refreshButton;

    private UnitCollectionManager unitManager;
    private ConversionRuleCollectionManager ruleManager;
    private UserManager userManager;

    // Этап 6: пункты контекстного меню — храним как поля, чтобы менять disabled по выделению
    private MenuItem delUnitMenuItem;
    private MenuItem delRuleMenuItem;

    @FXML
    public void initialize() {
        setupTables();
        setupSelection();
        setupContextMenus();
        setupActions();
    }

    private void setupTables() {
        TableColumn<Unit, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Unit, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Unit, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(data -> {
            long ownerId = data.getValue().getOwnerId();
            String login = userManager == null ? "—"
                : userManager.findById(ownerId).map(User::getLogin).orElse("—");
            return new SimpleStringProperty(login);
        });

        unitsTableView.getColumns().setAll(codeCol, nameCol, ownerCol);

        TableColumn<ConversionRule, String> toCol = new TableColumn<>("В единицу");
        toCol.setCellValueFactory(new PropertyValueFactory<>("toUnitCode"));
        TableColumn<ConversionRule, Double> factorCol = new TableColumn<>("Множитель");
        factorCol.setCellValueFactory(new PropertyValueFactory<>("factor"));
        rulesTableView.getColumns().setAll(toCol, factorCol);
    }

    private void setupSelection() {
        // При выборе юнита — обновляем правую таблицу и состояние кнопок/меню
        unitsTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                rulesTableView.setItems(FXCollections.observableArrayList(newVal.getRules()));
                boolean isOwner = isCurrentUser(newVal.getOwnerId());
                addRuleButton.setDisable(!isOwner);
                if (delUnitMenuItem != null) delUnitMenuItem.setDisable(!isOwner);
            }
        });

        // При выборе правила — disable пункта "Удалить" если правило чужое
        rulesTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (delRuleMenuItem != null) {
                boolean isOwner = newVal != null && isCurrentUser(newVal.getOwnerId());
                delRuleMenuItem.setDisable(!isOwner);
            }
        });
    }

    private boolean isCurrentUser(long ownerId) {
        return userManager != null
                && userManager.isLoggedIn()
                && userManager.getCurrentUser().getId() == ownerId;
    }

    private void setupActions() {
        // Этап 6: Refresh из БД — для актуализации кэша (units + rules + users)
        loadFileButton.setOnAction(e -> {
            try {
                userManager.loadFromDb();   // ← чтобы owner-логины новых юзеров подтягивались
                unitManager.loadFromDb();
                ruleManager.loadFromDb();
                linkRulesToUnits();
                refreshData();
                showAlert("Готово", "Данные обновлены из БД", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Ошибка БД", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        // Этап 6: каждая операция уже сохраняется в БД сразу — отдельное сохранение не нужно
        saveFileButton.setOnAction(e ->
                showAlert("Информация",
                          "БД сохраняет каждую операцию автоматически — отдельное сохранение не требуется.",
                          Alert.AlertType.INFORMATION));

        addUnitButton.setOnAction(e -> openWindow("/AddUnitWindow.fxml", "Новая единица"));
        addRuleButton.setOnAction(e -> {
            Unit u = unitsTableView.getSelectionModel().getSelectedItem();
            if (u != null) openAddRuleWindow(u);
            else showAlert("Инфо", "Выберите единицу измерения!", Alert.AlertType.WARNING);
        });

        convertButton.setOnAction(e -> handleConversion());
        refreshButton.setOnAction(e -> refreshData());
    }

    private void setupContextMenus() {
        delUnitMenuItem = new MenuItem("Удалить");
        MenuItem delUnit = delUnitMenuItem;
        delUnit.setOnAction(e -> {
            Unit s = unitsTableView.getSelectionModel().getSelectedItem();
            if (s == null) return;
            if (s.getOwnerId() != userManager.getCurrentUser().getId()) {
                showAlert("Нет прав", "Ошибка: у вас нет прав на изменение этого объекта", Alert.AlertType.ERROR);
                return;
            }
            unitManager.remove(s.getId());
            refreshData();
        });
        unitsTableView.setContextMenu(new ContextMenu(delUnit));

        delRuleMenuItem = new MenuItem("Удалить");
        MenuItem delRule = delRuleMenuItem;
        delRule.setOnAction(e -> {
            ConversionRule r = rulesTableView.getSelectionModel().getSelectedItem();
            Unit u = unitsTableView.getSelectionModel().getSelectedItem();
            if (r == null || u == null) return;
            if (r.getOwnerId() != userManager.getCurrentUser().getId()) {
                showAlert("Нет прав", "Ошибка: у вас нет прав на удаление этого правила", Alert.AlertType.ERROR);
                return;
            }
            try {
                ruleManager.remove(r.getId());          // ← Этап 6: удаление из БД
                u.getRules().remove(r);                  // ← синхронизируем UI-кэш
                rulesTableView.setItems(FXCollections.observableArrayList(u.getRules()));
            } catch (Exception ex) {
                showAlert("Ошибка БД", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        rulesTableView.setContextMenu(new ContextMenu(delRule));
    }

    private void handleConversion() {
        Unit u = unitsTableView.getSelectionModel().getSelectedItem();
        ConversionRule r = rulesTableView.getSelectionModel().getSelectedItem();
        if (u == null || r == null) return;

        TextInputDialog d = new TextInputDialog("1");
        d.setHeaderText("Конвертация " + u.getCode() + " -> " + r.getToUnitCode());
        d.showAndWait().ifPresent(val -> {
            try {
                double res = Double.parseDouble(val.replace(",", ".")) * r.getFactor();
                showAlert("Результат", "Итого: " + res, Alert.AlertType.INFORMATION);
            } catch (Exception ex) { showAlert("Ошибка", "Неверное число", Alert.AlertType.ERROR); }
        });
    }

    public void setServices(UnitCollectionManager um, ConversionRuleCollectionManager rm, ConversionService cs, UserManager userMgr) {
        this.unitManager = um;
        this.ruleManager = rm;
        this.userManager = userMgr;
        updateButtonAccess();
        refreshData();
    }

    private void updateButtonAccess() {
        boolean loggedIn = userManager != null && userManager.isLoggedIn();
        addUnitButton.setDisable(!loggedIn);
        addRuleButton.setDisable(!loggedIn);
        saveFileButton.setDisable(!loggedIn);
        loadFileButton.setDisable(!loggedIn);
    }

    private void refreshData() {
        if (unitManager != null) unitsTableView.setItems(FXCollections.observableArrayList(unitManager.getUnits()));
    }

    private void openWindow(String path, String title) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(path));
            Stage s = new Stage();
            s.setScene(new Scene(l.load()));
            if (path.contains("AddUnit")) ((AddUnitController)l.getController()).setUnitManager(unitManager, userManager, this::refreshData);
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openAddRuleWindow(Unit unit) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/AddRuleWindow.fxml"));
            Stage s = new Stage();
            s.setScene(new Scene(l.load()));
            ((AddRuleController)l.getController()).setUnit(
                    unit, userManager, ruleManager,
                    () -> rulesTableView.setItems(FXCollections.observableArrayList(unit.getRules())));
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Связывает правила из ruleManager с их юнитами (для master-detail UI). */
    private void linkRulesToUnits() {
        for (var unit : unitManager.getUnits()) {
            unit.getRules().clear();
            for (var rule : ruleManager.getRules()) {
                if (rule.getFromUnitCode().equals(unit.getCode())) {
                    unit.getRules().add(rule);
                }
            }
        }
    }

    private void showAlert(String t, String c, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}