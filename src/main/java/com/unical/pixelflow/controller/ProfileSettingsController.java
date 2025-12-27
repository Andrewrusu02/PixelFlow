package com.unical.pixelflow.controller;

import com.unical.pixelflow.components.SwitchTheme;
import com.unical.pixelflow.components.ComboPasswordField;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.types.Theme;
import com.unical.pixelflow.utility.Encrypter;
import com.unical.pixelflow.utility.FieldAnimationUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileSettingsController {

    @FXML private Label EditProfileLabel;
    @FXML private Label LabeChangePassword;
    @FXML private Label accountTitleLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Label newPasswordLabel;
    @FXML private Label newPasswordLabel1;
    @FXML private Label preferenceLabel;
    @FXML private Label themeLabel;
    @FXML private Label languageLabel;
    @FXML private Label userIdLabel;
    @FXML private Label usernameLabel;

    @FXML private ComboPasswordField confirmPass;
    @FXML private ComboPasswordField newPass;
    @FXML private ComboPasswordField newPass1;

    @FXML private TextField usernameField;
    @FXML private TextField usernameField1;

    @FXML private ImageView image;
    @FXML private Pane profileImage1;

    @FXML private ComboBox<String> languageBox;
    @FXML private SwitchTheme themeSwitch;

    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @FXML private Label statusLabel;
    @FXML private Label themeStatusLabel;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png")));

    @FXML
    public void initialize() {
        DataPF.esm.register(executorService);

        statusLabel.setOpacity(0);
        themeStatusLabel.setOpacity(0);

        // Rimosso il clip statico qui, viene gestito dinamicamente da setCenteredCircleImage

        usernameField1.setEditable(false);
        if (DataPF.dbUser != null) {
            usernameField1.setText(String.valueOf(DataPF.dbUser.getId()));
        }

        bindTranslations();
        loadUserData();
        setupLanguageBox();
        setupThemeSwitch();

        profileImage1.setOnMouseClicked(e -> handleImageChange());
        cancelButton.setOnAction(this::handleCancel);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) FieldAnimationUtils.removeErrorStyles(usernameField);
        });

        confirmPass.password.addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) FieldAnimationUtils.removeErrorStyles(confirmPass);
        });
        newPass.password.addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) FieldAnimationUtils.removeErrorStyles(newPass);
        });
        newPass1.password.addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) FieldAnimationUtils.removeErrorStyles(newPass1);
        });
    }

    private void bindTranslations() {
        EditProfileLabel.textProperty().bind(DataPF.lsp.lsb("profile.edit_pic"));
        LabeChangePassword.textProperty().bind(DataPF.lsp.lsb("profile.change_password_title"));
        accountTitleLabel.textProperty().bind(DataPF.lsp.lsb("profile.account_title"));
        preferenceLabel.textProperty().bind(DataPF.lsp.lsb("profile.preferences"));
        userIdLabel.textProperty().bind(DataPF.lsp.lsb("profile.user_id"));
        usernameLabel.textProperty().bind(DataPF.lsp.lsb("profile.username"));
        confirmPasswordLabel.textProperty().bind(DataPF.lsp.lsb("profile.old_password"));
        newPasswordLabel.textProperty().bind(DataPF.lsp.lsb("profile.new_password"));
        newPasswordLabel1.textProperty().bind(DataPF.lsp.lsb("profile.repeat_password"));
        themeLabel.textProperty().bind(DataPF.lsp.lsb("settings.theme"));
        languageLabel.textProperty().bind(DataPF.lsp.lsb("settings.language"));
        saveButton.textProperty().bind(DataPF.lsp.lsb("profile.save"));
        cancelButton.textProperty().bind(DataPF.lsp.lsb("profile.discard"));
    }

    private void loadUserData() {
        if (DataPF.dbUser == null) return;
        usernameField.setText(DataPF.dbUser.getUsername());
        loadImage(false);
    }

    private void loadImage(boolean checkTmp) {
        Image imgToLoad = defaultAvatar;
        try {
            File directory = new File(DataPF.users_images_directory);
            String userId = String.valueOf(DataPF.dbUser.getId());

            if (checkTmp) {
                File[] tmpFiles = directory.listFiles((dir, name) -> name.startsWith(userId + "_tmp."));
                if (tmpFiles != null && tmpFiles.length > 0) {
                    imgToLoad = new Image(tmpFiles[0].toURI().toString());
                }
            } else {
                File[] files = directory.listFiles((dir, name) -> name.startsWith(userId + "."));
                if (files != null && files.length > 0) {
                    imgToLoad = new Image(files[0].toURI().toString());
                }
            }
        } catch (Exception e) {
            imgToLoad = defaultAvatar;
        }

        // Usa il metodo helper per centrare e tagliare
        setCenteredCircleImage(image, imgToLoad);
    }

    private void setupLanguageBox() {
        languageBox.getItems().addAll("Italiano", "English");
        String currentLang = DataPF.dbUser.getLanguage();
        languageBox.setValue("it".equalsIgnoreCase(currentLang) ? "Italiano" : "English");

        languageBox.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal != null) {
                DataPF.lsp.setSelectedLanguage(newVal.equals("Italiano") ? "it" : "en");
            }
        });
    }

    private void setupThemeSwitch() {
        boolean isDark = DataPF.dbUser.getTheme() == Theme.DARK;
        themeSwitch.setSwitchedOn(isDark);

        themeSwitch.switchedOnProperty().addListener((_, _, newVal) -> {
            try {
                DataPF.dbUser.setTheme(newVal ? Theme.DARK : Theme.LIGHT);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void handleImageChange() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(saveButton.getScene().getWindow());

        if (selectedFile != null) {
            String userId = String.valueOf(DataPF.dbUser.getId());
            String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
            File destFile = new File(DataPF.users_images_directory, userId + "_tmp" + ext);

            try {
                if (!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Carica e centra la nuova immagine temporanea
                Image newImg = new Image(destFile.toURI().toString());
                setCenteredCircleImage(image, newImg);

            } catch (IOException e) {
                showError("profile.error.generic");
            }
        }
    }

    // --- Metodi Helper per Ritaglio Immagine (Center Crop) ---

    private void setCenteredCircleImage(ImageView imageView, Image image) {
        imageView.setImage(image);
        imageView.setViewport(null); // Reset viewport

        if (image.getWidth() == 0) {
            image.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && image.getHeight() > 0) {
                    cropAndClip(imageView, image);
                }
            });
        } else {
            cropAndClip(imageView, image);
        }
    }

    private void cropAndClip(ImageView imageView, Image image) {
        double w = image.getWidth();
        double h = image.getHeight();
        double minDim = Math.min(w, h);
        double x = (w - minDim) / 2;
        double y = (h - minDim) / 2;

        imageView.setViewport(new Rectangle2D(x, y, minDim, minDim));
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        double fitW = imageView.getFitWidth();
        double fitH = imageView.getFitHeight();

        // Assicurati che nel FXML l'ImageView abbia dimensioni definite (es. 140x140)
        // Se fitWidth è 0, usa un fallback o attendi il layout,
        // ma di solito in initialize/loading è già settato se da FXML.
        if (fitW <= 0) fitW = 140;
        if (fitH <= 0) fitH = 140;

        Circle clip = new Circle(fitW / 2, fitH / 2, Math.min(fitW, fitH) / 2);
        imageView.setClip(clip);
    }

    // ---------------------------------------------------------

    @FXML
    void handleSave(ActionEvent event) {
        String newUsername = usernameField.getText();
        String oldPass = confirmPass.password.get() == null ? "" : confirmPass.password.get();
        String newPassVal = newPass.password.get() == null ? "" : newPass.password.get();
        String newPassRepeat = newPass1.password.get() == null ? "" : newPass1.password.get();

        if (newUsername.isEmpty()) {
            FieldAnimationUtils.animateFieldError(usernameField);
            showError("profile.error.username_empty");
            return;
        }

        boolean tryingToChangePass = !oldPass.isEmpty() || !newPassVal.isEmpty() || !newPassRepeat.isEmpty();

        if (tryingToChangePass) {
            if (oldPass.isEmpty()) {
                FieldAnimationUtils.animateFieldError(confirmPass);
                showError("profile.error.old_password_required");
                return;
            }
            if (newPassVal.isEmpty()) {
                FieldAnimationUtils.animateFieldError(newPass);
                showError("profile.error.new_password_empty");
                return;
            }
            if (!newPassVal.equals(newPassRepeat)) {
                FieldAnimationUtils.animateFieldError(newPass);
                FieldAnimationUtils.animateFieldError(newPass1);
                showError("profile.error.password_mismatch");
                return;
            }
        }

        final String finalOldPass = oldPass;
        final boolean isPasswordChangeRequested = tryingToChangePass;

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (isPasswordChangeRequested) {
                    Task<Boolean> checkTask = DataPF.pfDatabase.checkPassword(DataPF.dbUser.getUsername(), finalOldPass);
                    checkTask.run();
                    if (!checkTask.get()) {
                        throw new Exception("profile.error.old_password_wrong");
                    }
                    DataPF.dbUser.setPassword_hash(Encrypter.encrypt_string_bcrypt(newPassVal));
                }

                if (!newUsername.equals(DataPF.dbUser.getUsername())) {
                    DataPF.dbUser.setUsername(newUsername);
                }

                String selectedLang = languageBox.getValue().equals("Italiano") ? "it" : "en";
                if (!selectedLang.equals(DataPF.dbUser.getLanguage())) {
                    DataPF.dbUser.setLanguage(selectedLang);
                }

                commitImageChange();

                return true;
            }
        };

        saveTask.setOnSucceeded(e -> {
            showSuccess("profile.success.saved");
            confirmPass.clear();
            newPass.clear();
            newPass1.clear();
            FieldAnimationUtils.removeErrorStyles(confirmPass);
            FieldAnimationUtils.removeErrorStyles(newPass);
            FieldAnimationUtils.removeErrorStyles(newPass1);
            DataPF.userUpdated.set(true);
        });

        saveTask.setOnFailed(e -> {
            Throwable ex = saveTask.getException();
            String msg = ex.getMessage();

            if ("profile.error.old_password_wrong".equals(msg)) {
                FieldAnimationUtils.animateFieldError(confirmPass);
            }

            showError(msg != null && msg.startsWith("profile.error") ? msg : "profile.error.generic");
        });

        executorService.submit(saveTask);
    }

    private void commitImageChange() {
        File dir = new File(DataPF.users_images_directory);
        String userId = String.valueOf(DataPF.dbUser.getId());

        File[] tmpFiles = dir.listFiles((d, name) -> name.startsWith(userId + "_tmp."));
        if (tmpFiles != null && tmpFiles.length > 0) {
            File tmpFile = tmpFiles[0];
            String ext = tmpFile.getName().substring(tmpFile.getName().lastIndexOf('.'));
            File finalFile = new File(dir, userId + ext);

            File[] oldFiles = dir.listFiles((d, name) -> name.startsWith(userId + ".") && !name.contains("_tmp"));
            if (oldFiles != null) {
                for (File f : oldFiles) f.delete();
            }

            tmpFile.renameTo(finalFile);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        usernameField.setText(DataPF.dbUser.getUsername());
        confirmPass.clear();
        newPass.clear();
        newPass1.clear();

        FieldAnimationUtils.removeErrorStyles(usernameField);
        FieldAnimationUtils.removeErrorStyles(confirmPass);
        FieldAnimationUtils.removeErrorStyles(newPass);
        FieldAnimationUtils.removeErrorStyles(newPass1);

        setupLanguageBox();
        setupThemeSwitch();

        File dir = new File(DataPF.users_images_directory);
        String userId = String.valueOf(DataPF.dbUser.getId());
        File[] tmpFiles = dir.listFiles((d, name) -> name.startsWith(userId + "_tmp."));
        if (tmpFiles != null) {
            for (File f : tmpFiles) f.delete();
        }
        loadImage(false);
    }

    private void showError(String messageKey) {
        themeStatusLabel.textProperty().bind(DataPF.lsp.lsb(messageKey));
        themeStatusLabel.setOpacity(1);
        statusLabel.setOpacity(0);
        animateLabel(themeStatusLabel);
    }

    private void showSuccess(String messageKey) {
        statusLabel.textProperty().bind(DataPF.lsp.lsb(messageKey));
        statusLabel.setOpacity(1);
        themeStatusLabel.setOpacity(0);
        animateLabel(statusLabel);
    }

    private void animateLabel(Label label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> label.setVisible(true)),
                new KeyFrame(Duration.seconds(3), e -> label.setOpacity(0))
        );
        timeline.play();
    }
}