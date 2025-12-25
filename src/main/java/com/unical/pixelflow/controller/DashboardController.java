package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.view.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class DashboardController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNewProject;

    @FXML
    private Button btnProjects;

    @FXML
    private Button btnSettings;

    @FXML
    private ProgressBar busy_indicator;

    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView logoHeader;

    @FXML
    private BorderPane mainPane;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userNameLabel;

    @FXML
    public void initialize() {
        // 1. Setup Utente Loggato
        if (DataPF.dbUser != null) {
            userNameLabel.setText(DataPF.dbUser.getUsername());

            // Carica avatar di default
            try {
                Image img = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png")
                ));
                userAvatar.setImage(img);
                // Assicura il clip circolare
                userAvatar.setClip(new Circle(20, 20, 20));
            } catch (Exception e) {
                System.err.println("Avatar non trovato: " + e.getMessage());
            }

            // Carica logo header
            try {
                Image logo = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png")
                ));
                logoHeader.setImage(logo);
            } catch (Exception e) {
                System.err.println("Logo non trovato: " + e.getMessage());
            }
        }

        // 2. Carica la vista Progetti all'avvio
        showProjects(null);
    }

    @FXML
    void showProjects(ActionEvent event) {
        loadView("/com/unical/pixelflow/fxml/HomeView.fxml");
        updateNavStyle(btnProjects, btnSettings);
    }

    @FXML
    void showSettings(ActionEvent event) {
        loadView("/com/unical/pixelflow/fxml/ProfileSettings.fxml");
        updateNavStyle(btnSettings, btnProjects);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DataPF.dbUser = null;
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneHandler.getInstance(stage).showLoginScreen();
    }

    /**
     * Carica dinamicamente una vista nel contentPane centrale
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore caricamento vista: " + fxmlPath);
        }
    }

    /**
     * Gestisce lo stile "Attivo/Inattivo" dei bottoni di navigazione
     */
    private void updateNavStyle(Button active, Button inactive) {
        // Stile per bottone attivo (Giallo PixelFlow)
        active.setStyle(
            "-fx-text-fill: #E1C106; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: #E1C106; " +
            "-fx-border-width: 0 0 2 0; " +
            "-fx-font-weight: bold;"
        );

        // Stile per bottone inattivo (Grigio)
        inactive.setStyle(
            "-fx-text-fill: white; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent;"
        );
    }
}
