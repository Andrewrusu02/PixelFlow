package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.DBObjects.dbUser;
import com.unical.pixelflow.model.types.Theme;
import com.unical.pixelflow.utility.Encrypter;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

        // Popola campo username (read-only)
        usernameField.setText(currentUser.getUsername());
        userIdLabel.setText("ID: " + currentUser.getId());

        // Popola lingua - SOLO ITALIANO E INGLESE
        languageBox.getItems().addAll("Italiano", "English");

        // Imposta lingua corrente convertendo da codice ISO a nome
        String currentLangCode = currentUser.getLanguage();
        if (currentLangCode != null && !currentLangCode.isEmpty()) {
            String displayName = LANG_CODE_TO_NAME.getOrDefault(currentLangCode.toLowerCase(), "Italiano");
            languageBox.setValue(displayName);
        } else {
            languageBox.setValue("Italiano");
        }

        // LISTENER: Salva lingua IMMEDIATAMENTE al cambio
        languageBox.setOnAction(event -> handleLanguageChange());

        // Gestione Tema
        Theme currentTheme = currentUser.getTheme();
        boolean isDark = (currentTheme == Theme.DARK);
        themeToggle.setSelected(isDark);
        updateThemeText(isDark);

        // LISTENER: Salva tema IMMEDIATAMENTE al cambio
        themeToggle.setOnAction(event -> handleThemeChange());
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
            if (languageCode != null && !languageCode.equals(currentUser.getLanguage())) {
                try {
                    currentUser.setLanguage(languageCode);
                    showStatus("Lingua aggiornata: " + selectedLanguageName, "success");
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


