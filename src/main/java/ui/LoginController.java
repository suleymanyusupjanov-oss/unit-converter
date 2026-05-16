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
}
