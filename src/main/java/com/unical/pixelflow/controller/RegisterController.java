package com.unical.pixelflow.controller;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.unical.pixelflow.components.ComboPasswordField;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.PFDataBase;
import com.unical.pixelflow.view.SceneHandler;
import com.unical.pixelflow.utility.FieldAnimationUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterController {

    @FXML
    private Label labelLogin;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginChangeButton;

    @FXML
    private Label labelPassword;

    @FXML
    private Label labelUsername;

    @FXML
    private Label labelConfirmPassword;

    @FXML
    private ComboPasswordField passwordFieldRegister;

    @FXML
    private ComboPasswordField passwordConfirmPassword;

    @FXML
    private Button registerButton;

    @FXML
    private TextField usernameFieldRegister;

    private final PFDataBase db = PFDataBase.getInstance();

    @FXML
    void ChangeToLogin(ActionEvent event) {
        SceneHandler.getInstance((Stage) loginChangeButton.getScene().getWindow()).showLoginScreen();
    }

    @FXML
    void RegisterUser(ActionEvent event) {
        String username = usernameFieldRegister.getText();
        String password = passwordFieldRegister.password.get();
        String confirmPassword = passwordConfirmPassword.password.get();

        Map<Node, String> validationErrorsRegister = new LinkedHashMap<>();

        if (username.isEmpty() && (password == null || password.isEmpty()) && (confirmPassword == null || confirmPassword.isEmpty())) {
            validationErrorsRegister.put(usernameFieldRegister, "register.error.missing");
            validationErrorsRegister.put(passwordFieldRegister, "register.error.missing");
            validationErrorsRegister.put(passwordConfirmPassword, "register.error.missing");
        } else {
            if (username.isEmpty()) {
                validationErrorsRegister.put(usernameFieldRegister, "register.error.username");
            }
            if (password == null || password.isEmpty()) {
                validationErrorsRegister.put(passwordFieldRegister, "register.error.password");
            }
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                validationErrorsRegister.put(passwordConfirmPassword, "register.error.confirm");
            }
            if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
                validationErrorsRegister.put(passwordFieldRegister, "register.error.mismatch");
                validationErrorsRegister.put(passwordConfirmPassword, "register.error.mismatch");
            }
        }

        handleValidationErrors(validationErrorsRegister);
        if (!validationErrorsRegister.isEmpty()) return;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Task<Boolean> existTask = DataPF.pfDatabase.userExists(username);

        existTask.setOnSucceeded(e -> {
            Boolean exists = existTask.getValue();
            if (exists != null && !exists) {
                registerNewUser(username, password, executorService);
            } else {
                showError("register.error.exists");
                executorService.shutdown();
            }
        });

        existTask.setOnFailed(e -> {
            showError("register.error.generic");
            executorService.shutdown();
        });

        executorService.submit(existTask);
    }


    private void registerNewUser(String username, String password, ExecutorService executorService) {
        Task<Boolean> registrationTask = DataPF.pfDatabase.createUser(username, password);

        registrationTask.setOnSucceeded(e -> {
            Boolean isRegistered = registrationTask.getValue();
            if (isRegistered != null && isRegistered) {
                SceneHandler.getInstance((Stage) registerButton.getScene().getWindow()).showLoginScreen();
            } else {
                showError("register.error.generic");
            }
            executorService.shutdown();
        });

        registrationTask.setOnFailed(e -> {
            showError("register.error.generic");
            executorService.shutdown();
        });

        executorService.submit(registrationTask);
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
        labelLogin.textProperty().bind(DataPF.lsp.lsb("register.loginlabel"));
        labelUsername.textProperty().bind(DataPF.lsp.lsb("register.username"));
        labelPassword.textProperty().bind(DataPF.lsp.lsb("register.password"));
        labelConfirmPassword.textProperty().bind(DataPF.lsp.lsb("register.confirmpassword"));
        registerButton.textProperty().bind(DataPF.lsp.lsb("register.registertext"));
        loginChangeButton.textProperty().bind(DataPF.lsp.lsb("register.changetologin"));


        usernameFieldRegister.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                if (!(passwordFieldRegister.password.get() == null || passwordFieldRegister.password.get().isEmpty()) &&
                        !(passwordConfirmPassword.password.get() == null || passwordConfirmPassword.password.get().isEmpty())) {
                    registerButton.fire();
                } else {
                    passwordFieldRegister.requestFocusOnPassword();
                }
            }
        });

        passwordFieldRegister.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                if (!(usernameFieldRegister.getText() == null || usernameFieldRegister.getText().isEmpty()) &&
                        !(passwordConfirmPassword.password.get() == null || passwordConfirmPassword.password.get().isEmpty())) {
                    registerButton.fire();
                } else {
                    passwordConfirmPassword.requestFocusOnPassword();
                }
            }
        });

        passwordConfirmPassword.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                registerButton.fire();
            }
        });

        usernameFieldRegister.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                FieldAnimationUtils.removeErrorStyles(usernameFieldRegister);
                errorLabel.setOpacity(0);
            }
        });

        passwordFieldRegister.password.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                FieldAnimationUtils.removeErrorStyles(passwordFieldRegister);
                errorLabel.setOpacity(0);
            }
        });

        passwordConfirmPassword.password.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                FieldAnimationUtils.removeErrorStyles(passwordConfirmPassword);
                errorLabel.setOpacity(0);
            }
        });
    }

}
