package com.unical.pixelflow.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    @FXML
    private ImageView logoImageView;

    @FXML
    private Label footerLabel;

    private final PFDataBase db = PFDataBase.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
                showSuccessPopup();
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

    private void showSuccessPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/RegisterSuccessPopup.fxml"));
            Parent root = loader.load();


            Label lblTitle = (Label) root.lookup("#lblTitle");
            Label lblBody = (Label) root.lookup("#lblBody");
            Button btnProceed = (Button) root.lookup("#btnProceed");

            if (lblTitle != null) lblTitle.textProperty().bind(DataPF.lsp.lsb("popup.register.success.title"));
            if (lblBody != null) lblBody.textProperty().bind(DataPF.lsp.lsb("popup.register.success.body"));
            if (btnProceed != null) btnProceed.textProperty().bind(DataPF.lsp.lsb("popup.register.success.button"));

            Stage popupStage = new Stage();

            if (btnProceed != null) {
                btnProceed.setOnAction(event -> {
                    popupStage.close();
                    SceneHandler.getInstance((Stage) registerButton.getScene().getWindow()).showLoginScreen();
                });
            } else {
                popupStage.setOnHidden(ev -> SceneHandler.getInstance((Stage) registerButton.getScene().getWindow()).showLoginScreen());
            }

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            popupStage.setScene(scene);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.initModality(Modality.APPLICATION_MODAL);

            Stage mainStage = (Stage) registerButton.getScene().getWindow();
            popupStage.initOwner(mainStage);

            popupStage.setOnShown(ev -> {
                popupStage.setX(mainStage.getX() + (mainStage.getWidth() / 2) - (popupStage.getWidth() / 2));
                popupStage.setY(mainStage.getY() + (mainStage.getHeight() / 2) - (popupStage.getHeight() / 2));
            });

            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            SceneHandler.getInstance((Stage) registerButton.getScene().getWindow()).showLoginScreen();
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
    public void initialize() {
        // Bind UI texts to i18n.
        if (labelUsername != null) labelUsername.textProperty().bind(DataPF.lsp.lsb("register.username"));
        if (labelPassword != null) labelPassword.textProperty().bind(DataPF.lsp.lsb("register.password"));
        if (labelConfirmPassword != null) labelConfirmPassword.textProperty().bind(DataPF.lsp.lsb("register.confirmpassword"));

        if (registerButton != null) registerButton.textProperty().bind(DataPF.lsp.lsb("register.registertext"));

        if (labelLogin != null) labelLogin.textProperty().bind(DataPF.lsp.lsb("register.loginlabel"));
        if (loginChangeButton != null) loginChangeButton.textProperty().bind(DataPF.lsp.lsb("register.changetologin"));

        if (footerLabel != null) {
            footerLabel.textProperty().bind(DataPF.lsp.lsb("register.footer"));
        }

        // Clear error label initially; it will be bound when errors happen.
        if (errorLabel != null) {
            errorLabel.textProperty().unbind();
            errorLabel.setText("");
        }

        if (logoImageView != null) {
            try {
                logoImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/unical/pixelflow/images/LogoPixelFlow.png"))));
            } catch (Exception ignored) {
            }
        }
    }

}
