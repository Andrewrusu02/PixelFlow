package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.DBObjects.dbUser;
import com.unical.pixelflow.model.types.Theme;
import com.unical.pixelflow.utility.Encrypter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ProfileSettingsController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label userIdLabel;

    @FXML
    private ComboBox<String> languageBox;

    @FXML
    private PasswordField newPass;

    @FXML
    private PasswordField confirmPass;

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView profileImage;

    @FXML private Label accountTitleLabel;
    @FXML private Label profileImageLabel;
    @FXML private Button changeAvatarButton;
    @FXML private Label usernameLabel;
    @FXML private Label languageLabel;
    @FXML private Label themeLabel;
    @FXML private Label themeHintLabel;
    @FXML private Label securityTitleLabel;
    @FXML private Label changePasswordLabel;
    @FXML private Label newPasswordLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Button saveButton;

    private dbUser currentUser;

    // Mappatura codici ISO <-> Nomi lingue visualizzati
    private static final Map<String, String> LANG_CODE_TO_NAME = new HashMap<>();
    private static final Map<String, String> LANG_NAME_TO_CODE = new HashMap<>();

    static {
        LANG_CODE_TO_NAME.put("it", "Italiano");
        LANG_CODE_TO_NAME.put("en", "English");

        LANG_NAME_TO_CODE.put("Italiano", "it");
        LANG_NAME_TO_CODE.put("English", "en");
    }

    @FXML
    public void initialize() {
        currentUser = DataPF.dbUser;

        if (currentUser == null) {
            showStatus("Errore: Utente non loggato", "error");
            return;
        }

        // Bind testi localizzati (live)
        if (accountTitleLabel != null) accountTitleLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.title"));
        if (profileImageLabel != null) profileImageLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.avatar"));
        if (changeAvatarButton != null) changeAvatarButton.textProperty().bind(DataPF.lsp.lsb("profile.settings.avatar.change"));
        if (usernameLabel != null) usernameLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.username"));
        if (languageLabel != null) languageLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.language"));
        if (themeLabel != null) themeLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.theme"));
        if (themeHintLabel != null) themeHintLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.theme.hint"));
        if (securityTitleLabel != null) securityTitleLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.security"));
        if (changePasswordLabel != null) changePasswordLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.change_password"));
        if (newPasswordLabel != null) newPasswordLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.new_password"));
        if (confirmPasswordLabel != null) confirmPasswordLabel.textProperty().bind(DataPF.lsp.lsb("profile.settings.confirm_password"));
        if (saveButton != null) saveButton.textProperty().bind(DataPF.lsp.lsb("profile.settings.save"));

        // Setup avatar
        loadAvatarImage(currentUser.getProfileImage());
        // Click sull'immagine = cambia avatar
        if (profileImage != null) {
            profileImage.setOnMouseClicked(_ -> handleChangeAvatar());
        }
        // Se cambia la property (update da DB o altrove), aggiorniamo preview
        currentUser.profileImageProperty().addListener((obs, oldVal, newVal) -> loadAvatarImage(newVal));

        // Popola campo username (read-only)
        usernameField.setText(currentUser.getUsername());
        userIdLabel.setText("ID: " + currentUser.getId());

        // Popola lingua - SOLO ITALIANO E INGLESE
        languageBox.getItems().addAll("Italiano", "English");

        // Imposta lingua corrente convertendo da codice ISO a nome
        String currentLangCode = currentUser.getLanguage();
        if (currentLangCode != null && !currentLangCode.isEmpty()) {
            String displayName = LANG_CODE_TO_NAME.getOrDefault(currentLangCode.toLowerCase(), "English");
            languageBox.setValue(displayName);
            // allinea anche il servizio di localizzazione al profilo utente
            DataPF.lsp.setSelectedLanguage(currentLangCode.toLowerCase());
        } else {
            // Default: inglese
            languageBox.setValue("English");
            DataPF.lsp.setSelectedLanguage("en");
        }

        // LISTENER: Salva lingua IMMEDIATAMENTE al cambio
        languageBox.setOnAction(_ -> handleLanguageChange());

        // Gestione Tema
        Theme currentTheme = currentUser.getTheme();
        boolean isDark = (currentTheme == Theme.DARK);
        themeToggle.setSelected(isDark);
        updateThemeText(isDark);

        // LISTENER: Salva tema IMMEDIATAMENTE al cambio
        themeToggle.setOnAction(_ -> handleThemeChange());
    }

    private void loadAvatarImage(String path) {
        try {
            if (profileImage == null) return;

            if (path != null && !path.isBlank()) {
                File f = new File(path);
                if (f.exists()) {
                    profileImage.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }

            // Fallback: icona di default
            profileImage.setImage(new Image(
                    java.util.Objects.requireNonNull(getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png"))
            ));
        } catch (Exception e) {
            System.err.println("Errore caricamento avatar: " + e.getMessage());
        }
    }

    /**
     * Aggiorna il testo del toggle tema.
     * CSS applicato tramite styleClass invece di setStyle inline.
     */
    private void updateThemeText(boolean isDark) {
        if (isDark) {
            themeToggle.setText("Modalità Scura: ON");
            themeToggle.getStyleClass().removeAll("theme-toggle-light");
            themeToggle.getStyleClass().add("theme-toggle-dark");
        } else {
            themeToggle.setText("Modalità Chiara: ON");
            themeToggle.getStyleClass().removeAll("theme-toggle-dark");
            themeToggle.getStyleClass().add("theme-toggle-light");
        }
    }

    /**
     * Gestisce il cambio lingua e salva IMMEDIATAMENTE nel DB
     */
    private void handleLanguageChange() {
        String selectedLanguageName = languageBox.getValue();
        if (selectedLanguageName != null && currentUser != null) {
            String languageCode = LANG_NAME_TO_CODE.get(selectedLanguageName);
            if (languageCode == null) return;

            DataPF.lsp.setSelectedLanguage(languageCode);

            if (!languageCode.equals(currentUser.getLanguage())) {
                try {
                    currentUser.setLanguage(languageCode);
                    showStatus(DataPF.lsp.lsb("profile.settings.language.updated").get() + ": " + selectedLanguageName, "success");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showStatus("Errore salvataggio lingua: " + e.getMessage(), "error");
                }
            }
        }
    }

    /**
     * Gestisce il cambio tema e salva IMMEDIATAMENTE nel DB
     */
    private void handleThemeChange() {
        if (currentUser == null) return;

        boolean isDark = themeToggle.isSelected();
        Theme selectedTheme = isDark ? Theme.DARK : Theme.LIGHT;

        if (currentUser.getTheme() != selectedTheme) {
            try {
                currentUser.setTheme(selectedTheme);
                updateThemeText(isDark);
                showStatus("Tema aggiornato. Riavvia l'app per applicare.", "info");
            } catch (SQLException e) {
                e.printStackTrace();
                showStatus("Errore salvataggio tema: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * handleSave ora gestisce SOLO il cambio password
     */
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            showStatus("Errore: Nessun utente loggato", "error");
            return;
        }

        String p1 = newPass.getText();
        String p2 = confirmPass.getText();

        // Verifica che almeno un campo password sia compilato
        if (p1.isEmpty() && p2.isEmpty()) {
            showStatus("Inserisci una nuova password per cambiarla", "info");
            return;
        }

        if (p1.isEmpty() || p2.isEmpty()) {
            showStatus("Compila entrambi i campi password", "error");
            return;
        }

        if (!p1.equals(p2)) {
            showStatus("Le password non coincidono", "error");
            return;
        }

        if (p1.length() < 4) {
            showStatus("Password troppo corta (min 4 caratteri)", "error");
            return;
        }

        try {
            // Hash della password con BCrypt prima di salvare
            String hashedPassword = Encrypter.encrypt_string_bcrypt(p1);
            currentUser.setPassword(hashedPassword);
            newPass.clear();
            confirmPass.clear();
            showStatus("Password aggiornata con successo!", "success");
        } catch (SQLException e) {
            e.printStackTrace();
            showStatus("Errore Database: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleChangeAvatar() {
        if (currentUser == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona Nuova Immagine Profilo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fc.showOpenDialog(usernameField.getScene().getWindow());
        if (selectedFile == null) return;

        try {
            File userImgDir = new File(DataPF.users_images_directory);
            if (!userImgDir.exists()) {
                boolean ok = userImgDir.mkdirs();
                if (!ok) {
                    showStatus("Impossibile creare la cartella avatar: " + userImgDir.getAbsolutePath(), "error");
                    return;
                }
            }

            // 2) Copia e rinomina file in modo deterministico (evita conflitti)
            String fileName = selectedFile.getName();
            String ext = ".png";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0 && dot < fileName.length() - 1) {
                ext = fileName.substring(dot).toLowerCase();
            }

            Path dest = Path.of(userImgDir.getPath(), "user_" + currentUser.getId() + ext);
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // 3) Aggiorna DB + model runtime (dbUser.setProfileImage fa entrambe le cose)
            String finalPath = dest.toFile().getAbsolutePath();
            currentUser.setProfileImage(finalPath);

            // 4) Feedback e refresh locale
            showStatus("Immagine profilo aggiornata", "success");
            loadAvatarImage(finalPath);

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Errore aggiornamento immagine profilo: " + e.getMessage(), "error");
        }
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success":
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px; -fx-font-weight: bold;");
                break;
            case "error":
                statusLabel.setStyle("-fx-text-fill: #FF5555; -fx-font-size: 12px; -fx-font-weight: bold;");
                break;
            case "info":
                statusLabel.setStyle("-fx-text-fill: #E1C106; -fx-font-size: 12px; -fx-font-weight: bold;");
                break;
        }

        // Auto-clear dopo 5 secondi
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
