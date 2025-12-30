package com.unical.pixelflow.controller;

import com.unical.pixelflow.components.ProjectCardController;
import com.unical.pixelflow.infrastructure.ProjectRepository;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import com.unical.pixelflow.popUp.ConfirmDeleteController;
import com.unical.pixelflow.view.ModalEffects;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HomeViewController {

    @FXML
    private FlowPane projectsGrid;
    @FXML
    private VBox emptyMessage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label emptyTitleLabel;
    @FXML
    private Label emptySubtitleLabel;

    private final ProjectRepository repo = new ProjectRepository();

    @FXML
    public void initialize() {
        if (titleLabel != null)
            titleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.title"));
        if (emptyTitleLabel != null)
            emptyTitleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.empty.title"));
        if (emptySubtitleLabel != null)
            emptySubtitleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.empty.subtitle"));

        loadProjects();
    }

    private void loadProjects() {
        projectsGrid.getChildren().clear();
        var projects = repo.getRecentProjects();

        if (projects.isEmpty()) {
            emptyMessage.setVisible(true);
            emptyMessage.setManaged(true);
        } else {
            emptyMessage.setVisible(false);
            emptyMessage.setManaged(false);

            for (ProjectMetadata project : projects) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/unical/pixelflow/components/ProjectCard.fxml"));
                    Parent cardNode = loader.load();

                    ProjectCardController controller = loader.getController();
                    controller.setData(project, this::openProject, this::openEditDialog, this::deleteProject);

                    projectsGrid.getChildren().add(cardNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openProject(ProjectMetadata project) {
        // repo.openProject(project.getPath()); // Vecchia logica
        loadEditor(project);
    }

    private void openEditDialog(ProjectMetadata project) {
        // Invece di aprire il dialog, apriamo l'editor
        loadEditor(project);
    }

    private void loadEditor(ProjectMetadata project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/MainView.fxml"));
            // Se l'errore precedente non è stato risolto, questa riga potrebbe ancora lanciare eccezione
            Parent root = loader.load();

            MainEditingController controller = loader.getController();
            controller.setProject(project);

            Scene currentScene = projectsGrid.getScene();

            // Cambio il contenuto della scena
            currentScene.setRoot(root);

            // --- MODIFICA PER FULL SCREEN ---
            // Recuperiamo lo Stage (la finestra) dalla scena
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Pixel Flow - " + project.getName());
            stage.setMaximized(true);
            // --------------------------------

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile caricare l'editor del progetto.");
        }
    }

    private void deleteProject(ProjectMetadata project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/unical/pixelflow/fxml/ConfirmDeleteDialog.fxml"));
            ConfirmDeleteController controller = new ConfirmDeleteController();
            loader.setController(controller);
            Parent page = loader.load();

            Stage dialogStage = createDialogStage(page, "dialog.delete_project.window_title");
            controller.setDialogStage(dialogStage);
            controller.setProjectName(project.getName());

            showDialogWithBlur(dialogStage);

            if (!controller.isConfirmed()) {
                return;
            }

            Task<Boolean> task = DataPF.userDatabase.deleteProject(project.getId());
            task.setOnSucceeded(ev -> {
                safeDeleteFile(project.getPath());
                safeDeleteFile(project.getThumbnailPath());
                loadProjects();
            });
            task.setOnFailed(ev -> {
                ev.getSource().getException().printStackTrace();
                showError("Impossibile eliminare il progetto dal database.");
            });

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Impossibile aprire la conferma di eliminazione.");
        }
    }

    private Stage createDialogStage(Parent page, String titleKey) {
        Stage stage = new Stage();
        stage.setTitle(DataPF.lsp.lsb(titleKey).get());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(projectsGrid.getScene().getWindow());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(page));
        stage.setResizable(false);
        return stage;
    }

    private void showDialogWithBlur(Stage dialogStage) {
        Stage ownerStage = (Stage) projectsGrid.getScene().getWindow();
        ModalEffects.ModalOverlay overlay = null;

        BorderPane dashboard = findDashboardRoot();
        if (dashboard != null) {
            overlay = ModalEffects.apply(dashboard);
        }

        ModalEffects.ModalOverlay finalOverlay = overlay;
        dialogStage.setOnHidden(e -> {
            if (finalOverlay != null)
                finalOverlay.remove();
        });

        dialogStage.setOnShown(ev -> {
            double centerX = ownerStage.getX() + (ownerStage.getWidth() - dialogStage.getWidth()) / 2;
            double centerY = ownerStage.getY() + (ownerStage.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);
        });

        dialogStage.showAndWait();
    }

    private BorderPane findDashboardRoot() {
        if (projectsGrid == null || projectsGrid.getScene() == null)
            return null;
        Parent root = projectsGrid.getScene().getRoot();

        if (root.lookup("#dashboardRoot") instanceof BorderPane bp) {
            return bp;
        }
        if (root instanceof BorderPane bp) {
            return bp;
        }
        return null;
    }

    private void safeDeleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isBlank()) {
                Files.deleteIfExists(Path.of(filePath));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showError(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PixelFlow Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}