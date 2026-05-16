package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Unit;
import model.ConversionRule;
import service.UnitCollectionManager;
import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UserManager;
import java.io.File;
import java.util.Optional;

public class MainController {

    @FXML private ListView<Unit> unitsListView;
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
        unitsListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Unit item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getCode() + " - " + item.getName());
            }
        });
        TableColumn<ConversionRule, String> toCol = new TableColumn<>("В единицу");
        toCol.setCellValueFactory(new PropertyValueFactory<>("toUnitCode"));
        TableColumn<ConversionRule, Double> factorCol = new TableColumn<>("Множитель");
        factorCol.setCellValueFactory(new PropertyValueFactory<>("factor"));
        rulesTableView.getColumns().setAll(toCol, factorCol);
    }

    private void setupSelection() {
        unitsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
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
            chooser.setInitialFileName("my_units.xml");
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
            Unit u = unitsListView.getSelectionModel().getSelectedItem();
            if (u != null) openAddRuleWindow(u);
            else showAlert("Инфо", "Выберите единицу измерения!", Alert.AlertType.WARNING);
        });

        convertButton.setOnAction(e -> handleConversion());
        refreshButton.setOnAction(e -> refreshData());
    }

    private void setupContextMenus() {
        MenuItem delUnit = new MenuItem("Удалить");
        delUnit.setOnAction(e -> {
            Unit s = unitsListView.getSelectionModel().getSelectedItem();
            if (s != null) { unitManager.remove(s.getId()); refreshData(); }
        });
        unitsListView.setContextMenu(new ContextMenu(delUnit));
    }

    private void handleConversion() {
        Unit u = unitsListView.getSelectionModel().getSelectedItem();
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
        if (unitManager != null) unitsListView.setItems(FXCollections.observableArrayList(unitManager.getUnits()));
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
            ((AddRuleController)l.getController()).setUnit(unit, () -> rulesTableView.setItems(FXCollections.observableArrayList(unit.getRules())));
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String t, String c, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}