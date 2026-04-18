package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import model.Unit;
import model.ConversionRule;
import service.UnitCollectionManager;
import service.ConversionRuleCollectionManager;
import service.ConversionService;

public class MainController {

    @FXML private ListView<Unit> unitsListView;
    @FXML private TableView<ConversionRule> rulesTableView;
    @FXML private Button convertButton;
    @FXML private Button addUnitButton;
    @FXML private Button addRuleButton; // Наша новая кнопка
    @FXML private Button refreshButton;

    private UnitCollectionManager unitManager;
    private ConversionRuleCollectionManager ruleManager;
    private ConversionService conversionService;

    @FXML
    public void initialize() {
        // Настройка списков
        unitsListView.setCellFactory(param -> new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getName());
            }
        });

        TableColumn<ConversionRule, String> toUnitCol = new TableColumn<>("В какую единицу (To)");
        toUnitCol.setCellValueFactory(new PropertyValueFactory<>("toUnitCode"));
        toUnitCol.setPrefWidth(150);

        TableColumn<ConversionRule, Double> factorCol = new TableColumn<>("Коэффициент");
        factorCol.setCellValueFactory(new PropertyValueFactory<>("factor"));
        factorCol.setPrefWidth(120);

        rulesTableView.getColumns().addAll(toUnitCol, factorCol);

        // Связь при клике
        unitsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                rulesTableView.setItems(FXCollections.observableArrayList(newVal.getRules()));
            } else {
                rulesTableView.getItems().clear();
            }
        });

        refreshButton.setOnAction(e -> {
            refreshData();
            showAlert("Успех", "Данные загружены!", Alert.AlertType.INFORMATION);
        });

        addUnitButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUnitWindow.fxml"));
                Parent root = loader.load();
                AddUnitController controller = loader.getController();
                controller.setUnitManager(unitManager, this::refreshData);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // === ОЖИВЛЯЕМ КНОПКУ ДОБАВЛЕНИЯ ПРАВИЛА ===
        addRuleButton.setOnAction(e -> {
            Unit selected = unitsListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Внимание", "Сначала выберите единицу слева!", Alert.AlertType.WARNING);
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddRuleWindow.fxml"));
                Parent root = loader.load();
                AddRuleController controller = loader.getController();
                controller.setUnit(selected, () -> {
                    rulesTableView.setItems(FXCollections.observableArrayList(selected.getRules()));
                });
                Stage stage = new Stage();
                stage.setTitle("Добавить правило");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        convertButton.setOnAction(e -> {
            Unit unit = unitsListView.getSelectionModel().getSelectedItem();
            ConversionRule rule = rulesTableView.getSelectionModel().getSelectedItem();
            if (unit == null || rule == null) {
                showAlert("Внимание", "Выберите единицу и правило!", Alert.AlertType.WARNING);
                return;
            }
            TextInputDialog dialog = new TextInputDialog("1.0");
            dialog.setTitle("Конвертация");
            dialog.setHeaderText("Перевод: " + unit.getCode() + " ➔ " + rule.getToUnitCode());
            dialog.showAndWait().ifPresent(input -> {
                try {
                    double val = Double.parseDouble(input.replace(",", "."));
                    showAlert("Результат", String.format("%s %s = %s %s", val, unit.getCode(), val * rule.getFactor(), rule.getToUnitCode()), Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    showAlert("Ошибка", "Введите корректное число!", Alert.AlertType.ERROR);
                }
            });
        });
        // === КОНТЕКСТНОЕ МЕНЮ (УДАЛЕНИЕ ЕДИНИЦЫ) ===
        ContextMenu unitMenu = new ContextMenu();
        MenuItem deleteUnitItem = new MenuItem("🗑 Удалить единицу");
        deleteUnitItem.setOnAction(event -> {
            Unit selected = unitsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                unitManager.remove(selected.getId()); // Твой родной метод из UnitCollectionManager
                refreshData(); // Обновляем список на экране
                rulesTableView.getItems().clear(); // Очищаем правую таблицу
            }
        });
        unitMenu.getItems().add(deleteUnitItem);
        unitsListView.setContextMenu(unitMenu);


        // === КОНТЕКСТНОЕ МЕНЮ (УДАЛЕНИЕ ПРАВИЛА) ===
        ContextMenu ruleMenu = new ContextMenu();
        MenuItem deleteRuleItem = new MenuItem("🗑 Удалить правило");
        deleteRuleItem.setOnAction(event -> {
            Unit selectedUnit = unitsListView.getSelectionModel().getSelectedItem();
            ConversionRule selectedRule = rulesTableView.getSelectionModel().getSelectedItem();

            if (selectedUnit != null && selectedRule != null) {
                // Удаляем правило из списка самой единицы
                selectedUnit.getRules().remove(selectedRule);
                // Обновляем табличку на экране
                rulesTableView.getItems().remove(selectedRule);
            }
        });
        ruleMenu.getItems().add(deleteRuleItem);
        rulesTableView.setContextMenu(ruleMenu);
    }

    public void setServices(UnitCollectionManager um, ConversionRuleCollectionManager rm, ConversionService cs) {
        this.unitManager = um;
        this.ruleManager = rm;
        this.conversionService = cs;
        refreshData();
    }

    private void refreshData() {
        if (unitManager != null) {
            unitsListView.setItems(FXCollections.observableArrayList(unitManager.getUnits()));
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message); alert.showAndWait();
    }
}