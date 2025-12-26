package com.unical.pixelflow.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.unical.pixelflow.view.SceneHandler;
import com.unical.pixelflow.components.ComboPasswordField;
import com.unical.pixelflow.model.DBObjects.dbUser;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.utility.FieldAnimationUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class  LoginController {

    @FXML
    private Label LabelRegister;

    @FXML
    private Label errorLabel;

    @FXML
    private Label labelPassword;

    @FXML
    private Label labelUsername;

    @FXML
    private Button loginButton;

    @FXML
    private ComboPasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private TextField usernameField;

    @FXML
    private ImageView logoImageView;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @FXML
    void ChangeToRegister(ActionEvent event) {
        try {
            SceneHandler loginScreen = SceneHandler.getInstance((Stage) registerButton.getScene().getWindow());
            loginScreen.showRegisterScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void CheckLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.password.get();

        Map<Node, String> validationErrors = new LinkedHashMap<>();

        if (username.isEmpty() && (password == null || password.isEmpty())) {
            validationErrors.put(usernameField, "login.error.missing");
            validationErrors.put(passwordField, "login.error.missing");
        } else {
            if (username.isEmpty()) {
                validationErrors.put(usernameField, "login.error.username");
            }
            if (password == null || password.isEmpty()) {
                validationErrors.put(passwordField, "login.error.password");
            }
        }

        handleValidationErrors(validationErrors);
        if (!validationErrors.isEmpty()) return;

        Task<Boolean> loginTask = DataPF.pfDatabase.checkPassword(username, password);
        executorService.submit(loginTask);

        loginTask.setOnSucceeded(e -> {
            boolean isAuthenticated = loginTask.getValue();
            if (isAuthenticated) {
                handleSuccessfulLogin(username);
            } else {
                handleFailedLogin();
            }
        });

        loginTask.setOnFailed(e -> {
            showError("login.error.generic");
            animateErrorFields(usernameField, passwordField);
        });
    }

    private void handleSuccessfulLogin(String username) {
        Task<dbUser> userTask = DataPF.pfDatabase.getUser(username);
        executorService.submit(userTask);

        userTask.setOnSucceeded(e -> {
            DataPF.dbUser = userTask.getValue();
            SceneHandler.getInstance((Stage) loginButton.getScene().getWindow()).startPixeFlow();
        });

        userTask.setOnFailed(e -> {
            showError("login.error.generic");
        });
    }

    private void handleFailedLogin() {
        showError("login.error.invalid");
        animateErrorFields(usernameField, passwordField);
    }

    public static void animateErrorFields(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                FieldAnimationUtils.animateFieldError((TextField) field);
            } else if (field instanceof ComboPasswordField) {
                FieldAnimationUtils.animateFieldError((ComboPasswordField) field);
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + field.getClass().getName());
            }
        }
    }

    private void handleValidationErrors(Map<Node, String> validationErrors) {
        if (!validationErrors.isEmpty()) {
            validationErrors.forEach((field, errorKey) -> {
                showError(errorKey);
                if (field instanceof TextField) {
                    FieldAnimationUtils.animateFieldError((TextField) field);
                } else if (field instanceof ComboPasswordField) {
                    FieldAnimationUtils.animateFieldError((ComboPasswordField) field);
                }
            });
        }
    }



    private void showError(String message) {
        errorLabel.textProperty().bind(DataPF.lsp.lsb(message));
        Timeline fadeInTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> errorLabel.setOpacity(0)),
                new KeyFrame(Duration.seconds(0.2), e -> errorLabel.setOpacity(1))
        );
        fadeInTimeline.setCycleCount(1);
        fadeInTimeline.play();
    }

    @FXML
    private void initialize() {
        DataPF.esm.register(executorService);

        try {
            String path = "/com/unical/pixelflow/assets/logo/LogoPixelFlow.png";
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Errore caricamento logo register: " + e.getMessage());
        }

        FieldAnimationUtils.disableContextMenu(
                usernameField
        );

        loginButton.textProperty().bind(DataPF.lsp.lsb("login.logintext"));
        labelUsername.textProperty().bind(DataPF.lsp.lsb("login.username"));
        labelPassword.textProperty().bind(DataPF.lsp.lsb("login.password"));
        registerButton.textProperty().bind(DataPF.lsp.lsb("login.register"));
        LabelRegister.textProperty().bind(DataPF.lsp.lsb("login.registerlabel"));
        errorLabel.textProperty().bind(DataPF.lsp.lsb("login.error"));

        usernameField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                if(!(passwordField.password.get() == null || passwordField.password.get().isEmpty())){
                    loginButton.fire();
                } else {
                    passwordField.requestFocusOnPassword();
                }
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                loginButton.fire();
            }
        });

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                FieldAnimationUtils.removeErrorStyles(usernameField);
                errorLabel.setOpacity(0);
            }
        });

        passwordField.password.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                FieldAnimationUtils.removeErrorStyles(passwordField);
                errorLabel.setOpacity(0);
            }
        });
    }

}
