package com.unical.pixelflow.controller;

import com.unical.pixelflow.infrastructure.ProjectRepository;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import com.unical.pixelflow.view.ModalEffects;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
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
        // Bind traduzioni (istananee quando cambia lingua)
        if (titleLabel != null) {
            titleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.title"));
        }
        if (emptyTitleLabel != null) {
            emptyTitleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.empty.title"));
        }
        if (emptySubtitleLabel != null) {
            emptySubtitleLabel.textProperty().bind(DataPF.lsp.lsb("homeview.empty.subtitle"));
        }

        loadProjects();
    }

    private void loadProjects() {
        projectsGrid.getChildren().clear();

        var projects = repo.getRecentProjects();

        // Gestione visibilità messaggio vuoto
        if (projects.isEmpty()) {
            emptyMessage.setVisible(true);
            emptyMessage.setManaged(true);
        } else {
            emptyMessage.setVisible(false);
            emptyMessage.setManaged(false);

            // Popola griglia
            for (ProjectMetadata project : projects) {
                projectsGrid.getChildren().add(createProjectCard(project));
            }
        }
    }

    private VBox createProjectCard(ProjectMetadata project) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: #2D2D2D; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        card.setPrefSize(220, 240);
        card.setMaxSize(220, 240);
        card.setMinSize(220, 240);

        // Hover effect
        card.setOnMouseEntered(_ -> card.setStyle(
            "-fx-background-color: #353535; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(225,193,6,0.4), 15, 0, 0, 3);"
        ));

        card.setOnMouseExited(_ -> card.setStyle(
            "-fx-background-color: #2D2D2D; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        ));

        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("Apri");
        MenuItem editItem = new MenuItem("Modifica");
        MenuItem deleteItem = new MenuItem("Elimina");

        contextMenu.getItems().setAll(openItem, editItem, deleteItem);

        openItem.setOnAction(_ -> openProject(project));
        editItem.setOnAction(_ -> openEditDialog(project));
        deleteItem.setOnAction(_ -> deleteProject(project));

        card.setOnContextMenuRequested(e -> {
            contextMenu.hide();
            contextMenu.show(card, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                openProject(project);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.hide();
                contextMenu.show(card, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        // Thumbnail container
        BorderPane thumbnail = new BorderPane();
        thumbnail.setPrefSize(190, 140);
        thumbnail.setMaxSize(190, 140);
        thumbnail.setMinSize(190, 140);
        thumbnail.setStyle(
            "-fx-background-color: #1E1E1E; " +
            "-fx-background-radius: 5;"
        );

        // Carica thumbnail
        if (project.getThumbnailPath() != null && !project.getThumbnailPath().isEmpty()) {
            try {
                // Le thumbnail sono su file system, non nel classpath
                // Usa file:// URL per caricare da disco
                File thumbnailFile = new File(project.getThumbnailPath());

                if (thumbnailFile.exists()) {
                    Image thumbnailImage = new Image(thumbnailFile.toURI().toString());
                    ImageView thumbnailView = new ImageView(thumbnailImage);
                    thumbnailView.setFitWidth(190);
                    thumbnailView.setFitHeight(140);
                    thumbnailView.setPreserveRatio(false);
                    thumbnailView.setSmooth(true);
                    thumbnail.getChildren().add(thumbnailView);
                } else {
                    // File non esiste, usa fallback
                    System.err.println("Thumbnail non trovata: " + project.getThumbnailPath());
                    Label iconLabel = new Label("📷");
                    iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666;");
                    thumbnail.getChildren().add(iconLabel);
                }
            } catch (Exception ex) {
                // Fallback a emoji se caricamento fallisce
                System.err.println("Errore caricamento thumbnail: " + ex.getMessage());
                Label iconLabel = new Label("📷");
                iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666;");
                thumbnail.getChildren().add(iconLabel);
            }
        } else {
            // Fallback a emoji se nessun thumbnail
            Label iconLabel = new Label("📷");
            iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666;");
            thumbnail.getChildren().add(iconLabel);
        }

        // Project name
        Label nameLabel = new Label(project.getName());
        nameLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px;"
        );
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);

        // Last modified date
        Label dateLabel = new Label(project.getFormattedDate());
        dateLabel.setStyle(
            "-fx-text-fill: #AAAAAA; " +
            "-fx-font-size: 11px;"
        );

        // Traduzioni delle voci menu (live)
        openItem.textProperty().bind(DataPF.lsp.lsb("project.menu.open"));
        editItem.textProperty().bind(DataPF.lsp.lsb("project.menu.edit"));
        deleteItem.textProperty().bind(DataPF.lsp.lsb("project.menu.delete"));

        card.getChildren().addAll(thumbnail, nameLabel, dateLabel);

        return card;
    }

    private BorderPane findDashboardRootForBlur() {
        try {
            if (projectsGrid == null || projectsGrid.getScene() == null) return null;
            var root = projectsGrid.getScene().getRoot();
            if (root instanceof BorderPane sp && sp.lookup("#dashboardRoot") != null) {
                // root è dashboardRoot
                return sp;
            }
            // se siamo dentro dashboardRoot ma root diverso, prova lookup
            var n = root.lookup("#dashboardRoot");
            if (n instanceof BorderPane sp2) return sp2;
        } catch (Exception ignored) {
        }
        return null;
    }

    private void openEditDialog(ProjectMetadata project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/EditProjectDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(DataPF.lsp.lsb("dialog.edit_project.window_title").get());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(projectsGrid.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(page));
            dialogStage.setResizable(false);

            EditProjectController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProject(project);

            Stage ownerStage = (Stage) projectsGrid.getScene().getWindow();

            ModalEffects.ModalOverlay overlay = null;
            BorderPane dashboard = findDashboardRootForBlur();
            if (dashboard != null) {
                overlay = ModalEffects.apply(dashboard);
            } else if (projectsGrid != null && projectsGrid.getParent() instanceof BorderPane sp) {
                overlay = ModalEffects.apply(sp);
            }
            ModalEffects.ModalOverlay finalOverlay = overlay;
            dialogStage.setOnHidden(e -> {
                if (finalOverlay != null) {
                    finalOverlay.remove();
                }
            });

            dialogStage.setOnShown(ev -> {
                double dialogWidth = dialogStage.getWidth();
                double dialogHeight = dialogStage.getHeight();

                double ownerX = ownerStage.getX();
                double ownerY = ownerStage.getY();
                double ownerWidth = ownerStage.getWidth();
                double ownerHeight = ownerStage.getHeight();

                double centerX = ownerX + (ownerWidth - dialogWidth) / 2;
                double centerY = ownerY + (ownerHeight - dialogHeight) / 2;

                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadProjects();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("PixelFlow");
            a.setHeaderText("Errore");
            a.setContentText("Impossibile aprire la finestra di modifica progetto.");
            a.showAndWait();
        }
    }

    private void deleteProject(ProjectMetadata project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/ConfirmDeleteDialog.fxml"));
            ConfirmDeleteController controller = new ConfirmDeleteController();
            loader.setController(controller);
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(DataPF.lsp.lsb("dialog.delete_project.window_title").get());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(projectsGrid.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(page));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            controller.setProjectName(project.getName());

            Stage ownerStage = (Stage) projectsGrid.getScene().getWindow();

            ModalEffects.ModalOverlay overlay = null;
            BorderPane dashboard = findDashboardRootForBlur();
            if (dashboard != null) {
                overlay = ModalEffects.apply(dashboard);
            } else if (projectsGrid != null && projectsGrid.getParent() instanceof BorderPane sp) {
                overlay = ModalEffects.apply(sp);
            }
            ModalEffects.ModalOverlay finalOverlay = overlay;
            dialogStage.setOnHidden(e -> {
                if (finalOverlay != null) finalOverlay.remove();
            });

            dialogStage.setOnShown(ev -> {
                double dialogWidth = dialogStage.getWidth();
                double dialogHeight = dialogStage.getHeight();

                double ownerX = ownerStage.getX();
                double ownerY = ownerStage.getY();
                double ownerWidth = ownerStage.getWidth();
                double ownerHeight = ownerStage.getHeight();

                double centerX = ownerX + (ownerWidth - dialogWidth) / 2;
                double centerY = ownerY + (ownerHeight - dialogHeight) / 2;

                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });

            dialogStage.showAndWait();

            if (!controller.isConfirmed()) {
                return;
            }

            // 1) DB
            Task<Boolean> task = DataPF.userDatabase.deleteProject(project.getId());
            task.setOnSucceeded(ev -> {
                // 2) filesystem (best-effort)
                safeDeleteFile(project.getPath());
                safeDeleteFile(project.getThumbnailPath());
                loadProjects();
            });
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                if (ex != null) {
                    ex.printStackTrace();
                }
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("PixelFlow");
                err.setHeaderText("Errore eliminazione");
                err.setContentText("Impossibile eliminare il progetto dal database.");
                err.showAndWait();
            });

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("PixelFlow");
            err.setHeaderText("Errore");
            err.setContentText("Impossibile aprire la conferma di eliminazione.");
            err.showAndWait();
        }
    }

    private void safeDeleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isBlank()) return;
            Path p = Path.of(filePath);
            Files.deleteIfExists(p);
        } catch (Exception ex) {
            System.err.println("Warning: impossibile eliminare file '" + filePath + "': " + ex.getMessage());
        }
    }

    private void openProject(ProjectMetadata project) {
        System.out.println("Opening project: " + project.getName() + " at " + project.getPath());
        // TODO: Implementare apertura progetto
        repo.openProject(project.getPath());
    }
}

