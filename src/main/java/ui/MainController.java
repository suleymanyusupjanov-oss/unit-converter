package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import model.Unit;
import model.ConversionRule;
import service.UnitCollectionManager;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UserManager;
import java.io.File;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Unit> unitsTableView;
    @FXML private TableView<ConversionRule> rulesTableView;
    @FXML private Button convertButton, addUnitButton, addRuleButton, saveFileButton, loadFileButton, refreshButton;

    private UnitCollectionManager unitManager;
    private UserManager userManager;

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
        unitsTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) rulesTableView.setItems(FXCollections.observableArrayList(newVal.getRules()));
        });
    }

    private void setupActions() {
        // ЗАГРУЗКА ЧЕРЕЗ ОКНО
        loadFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
            File file = chooser.showOpenDialog(loadFileButton.getScene().getWindow());
            if (file != null) {
                try {
                    unitManager.loadFromFile(file.getAbsolutePath());
                    refreshData();
                    showAlert("Успех", "Загружено из " + file.getName(), Alert.AlertType.INFORMATION);
                } catch (Exception ex) { showAlert("Ошибка", ex.getMessage(), Alert.AlertType.ERROR); }
            }
        });

        // СОХРАНЕНИЕ ЧЕРЕЗ ОКНО
        saveFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("data.xml");
            File file = chooser.showSaveDialog(saveFileButton.getScene().getWindow());
            if (file != null) {
                try {
                    unitManager.saveToFile(file.getAbsolutePath());
                    showAlert("Готово", "Файл сохранен!", Alert.AlertType.INFORMATION);
                } catch (Exception ex) { showAlert("Ошибка", ex.getMessage(), Alert.AlertType.ERROR); }
            }
        });

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
        MenuItem delUnit = new MenuItem("Удалить");
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

        MenuItem delRule = new MenuItem("Удалить");
        delRule.setOnAction(e -> {
            ConversionRule r = rulesTableView.getSelectionModel().getSelectedItem();
            Unit u = unitsTableView.getSelectionModel().getSelectedItem();
            if (r == null || u == null) return;
            if (r.getOwnerId() != userManager.getCurrentUser().getId()) {
                showAlert("Нет прав", "Ошибка: у вас нет прав на удаление этого правила", Alert.AlertType.ERROR);
                return;
            }
            u.getRules().remove(r);
            rulesTableView.setItems(FXCollections.observableArrayList(u.getRules()));
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
            ((AddRuleController)l.getController()).setUnit(unit, userManager, () -> rulesTableView.setItems(FXCollections.observableArrayList(unit.getRules())));
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String t, String c, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}