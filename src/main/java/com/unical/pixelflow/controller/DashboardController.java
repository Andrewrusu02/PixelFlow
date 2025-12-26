package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.view.ModalEffects;
import com.unical.pixelflow.view.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
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
    private StackPane dashboardRoot;

    @FXML
    public void initialize() {
        // 0. Bind traduzioni (sempre)
        btnNewProject.textProperty().bind(DataPF.lsp.lsb("dashboard.new_project"));
        btnProjects.textProperty().bind(DataPF.lsp.lsb("dashboard.recent_projects"));
        btnSettings.textProperty().bind(DataPF.lsp.lsb("dashboard.profile_settings"));
        btnLogout.textProperty().bind(DataPF.lsp.lsb("dashboard.logout"));

        // 1. Setup Utente Loggato
        if (DataPF.dbUser != null) {
            userNameLabel.setText(DataPF.dbUser.getUsername());

            // Assicura il clip circolare (in caso venga rimosso dal FXML)
            userAvatar.setClip(new Circle(20, 20, 20));

            // Caricamento iniziale avatar (custom se presente, altrimenti default)
            loadUserAvatar(DataPF.dbUser.getProfileImage());

            // Listener dinamico: se cambia il path nel model, ricarica l'immagine
            DataPF.dbUser.profileImageProperty().addListener((obs, oldVal, newVal) -> loadUserAvatar(newVal));

            // Carica logo header
            try {
                Image logo = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/unical/pixelflow/assets/logo/LogoPixelFlow.png")
                ));
                logoHeader.setImage(logo);
            } catch (Exception e) {
                System.err.println("Logo non trovato: " + e.getMessage());
            }
        }

        // 2. Carica la vista Progetti all'avvio
        showProjects(null);
    }

    private void loadUserAvatar(String avatarPath) {
        try {
            if (avatarPath != null && !avatarPath.isBlank()) {
                java.io.File f = new java.io.File(avatarPath);
                if (f.exists()) {
                    userAvatar.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }

            // Fallback: avatar di default
            Image img = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png")
            ));
            userAvatar.setImage(img);
        } catch (Exception e) {
            System.err.println("Avatar non trovato: " + e.getMessage());
        }
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
     * Apre il wizard per creare un nuovo progetto
     */
    @FXML
    private void handleNewProject(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/unical/pixelflow/fxml/NewProjectDialog.fxml")
            );
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(DataPF.lsp.lsb("dialog.new_project.window_title").get());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(btnNewProject.getScene().getWindow());
            dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            NewProjectController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            Stage ownerStage = (Stage) btnNewProject.getScene().getWindow();

            ModalEffects.ModalOverlay overlay = dashboardRoot != null ? ModalEffects.apply(dashboardRoot) : null;
            ModalEffects.ModalOverlay finalOverlay = overlay;
            dialogStage.setOnHidden(e -> {
                if (finalOverlay != null) {
                    finalOverlay.remove();
                }
            });

            dialogStage.setOnShown(e -> {
                double dialogWidth = dialogStage.getWidth();
                double dialogHeight = dialogStage.getHeight();

                double ownerX = ownerStage.getX();
                double ownerY = ownerStage.getY();
                double ownerWidth = ownerStage.getWidth();
                double ownerHeight = ownerStage.getHeight();

                dialogStage.setX(ownerX + (ownerWidth - dialogWidth) / 2);
                dialogStage.setY(ownerY + (ownerHeight - dialogHeight) / 2);
            });

            dialogStage.showAndWait();

            if (controller.isCreated()) {
                showProjects(null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
