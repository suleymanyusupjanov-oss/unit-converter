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
    @FXML private Button refreshButton;

    private UnitCollectionManager unitManager;
    private ConversionRuleCollectionManager ruleManager;
    private ConversionService conversionService;

    @FXML
    public void initialize() {
        // Настройка левого списка (Master)
        unitsListView.setCellFactory(param -> new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " - " + item.getName());
                }
            }
        });

        // Настройка правой таблицы (Detail)
        TableColumn<ConversionRule, String> toUnitCol = new TableColumn<>("В какую единицу (To)");
        toUnitCol.setCellValueFactory(new PropertyValueFactory<>("toUnitCode"));
        toUnitCol.setPrefWidth(150);

        TableColumn<ConversionRule, Double> factorCol = new TableColumn<>("Коэффициент");
        factorCol.setCellValueFactory(new PropertyValueFactory<>("factor"));
        factorCol.setPrefWidth(120);

        rulesTableView.getColumns().addAll(toUnitCol, factorCol);

        // Связь при клике (Master -> Detail)
        unitsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<ConversionRule> rules = FXCollections.observableArrayList(newValue.getRules());
                rulesTableView.setItems(rules);
            } else {
                rulesTableView.getItems().clear();
            }
        });

        // Кнопка обновления (Исправлен вызов showAlert)
        refreshButton.setOnAction(event -> {
            refreshData();
            showAlert("Успех", "Данные загружены из памяти/XML!", Alert.AlertType.INFORMATION);
        });

        // Кнопка добавления единицы
        addUnitButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUnitWindow.fxml"));
                Parent root = loader.load();

                AddUnitController addController = loader.getController();
                addController.setUnitManager(unitManager, this::refreshData);

                Stage stage = new Stage();
                stage.setTitle("Добавить единицу измерения");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                // ТА САМАЯ ОШИБКА СО СКРИНШОТА ИСПРАВЛЕНА:
                showAlert("Ошибка", "Не удалось открыть окно добавления: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });

        // === ОЖИВЛЯЕМ КНОПКУ КОНВЕРТАЦИИ ===
        convertButton.setOnAction(event -> {
            Unit selectedUnit = unitsListView.getSelectionModel().getSelectedItem();
            ConversionRule selectedRule = rulesTableView.getSelectionModel().getSelectedItem();

            if (selectedUnit == null || selectedRule == null) {
                showAlert("Внимание", "Сначала выберите единицу слева и правило конвертации справа!", Alert.AlertType.WARNING);
                return;
            }

            TextInputDialog dialog = new TextInputDialog("1.0");
            dialog.setTitle("Конвертация");
            dialog.setHeaderText("Перевод: " + selectedUnit.getCode() + " ➔ " + selectedRule.getToUnitCode());
            dialog.setContentText("Введите число для конвертации:");

            java.util.Optional<String> result = dialog.showAndWait();

            result.ifPresent(inputValue -> {
                try {
                    double value = Double.parseDouble(inputValue.replace(",", "."));
                    double convertedValue = value * selectedRule.getFactor();

                    String resultMessage = String.format("%s %s = %s %s",
                            value, selectedUnit.getCode(),
                            convertedValue, selectedRule.getToUnitCode());

                    showAlert("Результат", resultMessage, Alert.AlertType.INFORMATION);

                } catch (NumberFormatException e) {
                    showAlert("Ошибка ввода", "Пожалуйста, введите корректное число!\nВы ввели: " + inputValue, Alert.AlertType.ERROR);
                }
            });
        });
    }

    public void setServices(UnitCollectionManager um, ConversionRuleCollectionManager rm, ConversionService cs) {
        this.unitManager = um;
        this.ruleManager = rm;
        this.conversionService = cs;
        refreshData();
    }

    private void refreshData() {
        if (unitManager != null) {
            ObservableList<Unit> realUnits = FXCollections.observableArrayList(unitManager.getUnits());
            unitsListView.setItems(realUnits);
        }
    }

    // Универсальный метод для показа окон (принимает 3 параметра)
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}