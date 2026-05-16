package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserManager;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private UserManager userManager;
    private boolean loginSuccessful = false;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        try {
            userManager.login(login, password);
            loginSuccessful = true;
            ((Stage) loginButton.getScene().getWindow()).close();
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void handleRegister() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        try {
            userManager.register(login, password);
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Регистрация успешна! Теперь войдите.");
        } catch (Exception ex) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText(ex.getMessage());
        }
    }
}
