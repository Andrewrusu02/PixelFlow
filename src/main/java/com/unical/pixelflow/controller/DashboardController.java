package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.UserDatabase;
import com.unical.pixelflow.model.types.Theme;
import com.unical.pixelflow.popUp.NewProjectController;
import com.unical.pixelflow.view.ModalEffects;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DashboardController {

    @FXML private Button btnImportProject;
    @FXML private Button btnLogout;
    @FXML private Button btnNewProject;
    @FXML private ToggleButton btnProjects;
    @FXML private ToggleButton btnSettings;
    @FXML private ProgressBar busy_indicator;
    @FXML private StackPane contentPane;
    @FXML private BorderPane dashboardRoot;
    @FXML private ImageView logoHeader;
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        DataPF.esm.register(executorService);
        if (DataPF.userDatabase == null) {
            DataPF.userDatabase = UserDatabase.getInstance();
        }
        if (btnImportProject != null) btnImportProject.textProperty().bind(DataPF.lsp.lsb("dashboard.import_project"));
        if (btnNewProject != null) btnNewProject.textProperty().bind(DataPF.lsp.lsb("dashboard.new_project"));
        if (btnProjects != null) btnProjects.textProperty().bind(DataPF.lsp.lsb("dashboard.recent_projects"));
        if (btnSettings != null) btnSettings.textProperty().bind(DataPF.lsp.lsb("dashboard.profile_settings"));
        if (btnLogout != null) btnLogout.textProperty().bind(DataPF.lsp.lsb("dashboard.logout"));

        DataPF.dbUser.themeProperty().addListener((_, _, newValue) -> {
            Platform.runLater(() -> {
                if (dashboardRoot.getScene() != null) {
                    applyTheme(dashboardRoot.getScene(), newValue);
                }
            });
        });

        dashboardRoot.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    applyTheme(newScene, DataPF.dbUser.getTheme());
                    DataPF.lsp.setSelectedLanguage(DataPF.dbUser.getLanguage());
                });
            }
        });

        DataPF.userUpdated.addListener((_, _, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    update_user();
                    DataPF.userUpdated.set(false);
                });
            }
        });

        DataPF.dbUser.usernameProperty().addListener((_, _, _) -> {
            Platform.runLater(() -> {
                update_user();
                DataPF.userUpdated.set(false);
            });
        });

        ToggleGroup navGroup = new ToggleGroup();
        if (btnProjects != null) btnProjects.setToggleGroup(navGroup);
        if (btnSettings != null) btnSettings.setToggleGroup(navGroup);

        navGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                if (oldVal != null) oldVal.setSelected(true);
            } else {
                if (newVal == btnProjects) {
                    showProjects();
                } else if (newVal == btnSettings) {
                    showSettings();
                }
            }
        });

        if (btnProjects != null) btnProjects.setSelected(true);
        update_user();
        showProjects();
    }

    @FXML
    void handleImportProject(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.titleProperty().bind(DataPF.lsp.lsb("dashboard.import_project.file_chooser.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                DataPF.lsp.lsb("dashboard.import_project.file_chooser.filter.pfp").get(), "*.pfp"));

        File selectedFile = fileChooser.showOpenDialog(dashboardRoot.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String newFileName = UUID.randomUUID() + ".pfp";
                File destDir = new File("Data/projects");
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, newFileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String originalName = selectedFile.getName().replace(".pfp", "");

                Task<Boolean> saveTask = DataPF.userDatabase.saveProject(
                        originalName,
                        destFile.getAbsolutePath(),
                        null
                );

                saveTask.setOnSucceeded(e -> Platform.runLater(this::showProjects));

                executorService.submit(saveTask);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        openModal("/com/unical/pixelflow/fxml/LogoutView.fxml", "Logout", null);
    }

    @FXML
    void handleNewProject(ActionEvent event) {
        openModal("/com/unical/pixelflow/fxml/NewProjectDialog.fxml",
                DataPF.lsp.lsb("dialog.new_project.window_title").get(),
                (loader, stage) -> {
                    NewProjectController controller = loader.getController();
                    controller.setDialogStage(stage);
                },
                (loader) -> {
                    NewProjectController controller = loader.getController();
                    if (controller.isCreated()) {
                        showProjects();
                    }
                }
        );
    }

    private void openModal(String fxmlPath, String title, Consumer<FXMLLoader> simpleConfig) {
        openModal(fxmlPath, title, simpleConfig, null, null);
    }

    private void openModal(String fxmlPath, String title,
                           BiConsumer<FXMLLoader, Stage> stageConfig,
                           Consumer<FXMLLoader> afterCloseAction) {
        openModal(fxmlPath, title, null, stageConfig, afterCloseAction);
    }

    private void openModal(String fxmlPath, String title,
                           Consumer<FXMLLoader> simpleConfig,
                           BiConsumer<FXMLLoader, Stage> stageConfig,
                           Consumer<FXMLLoader> afterCloseAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Scene scene = new Scene(page);
            scene.setFill(Color.TRANSPARENT);

            String cssPath = (DataPF.dbUser.getTheme() == Theme.DARK)
                    ? "/com/unical/pixelflow/css/style-dark.css"
                    : "/com/unical/pixelflow/css/style-light.css";
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setScene(scene);
            if (title != null) dialogStage.setTitle(title);

            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(dashboardRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setResizable(false);

            if (simpleConfig != null) {
                simpleConfig.accept(loader);
            }
            if (stageConfig != null) {
                stageConfig.accept(loader, dialogStage);
            }

            Stage ownerStage = (Stage) dashboardRoot.getScene().getWindow();
            ModalEffects.ModalOverlay overlay = (dashboardRoot != null) ? ModalEffects.apply(dashboardRoot) : null;

            dialogStage.setOnHidden(e -> {
                if (overlay != null) overlay.remove();
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

            if (afterCloseAction != null) {
                afterCloseAction.accept(loader);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyTheme(Scene scene, Theme theme) {
        if (scene == null) return;
        String lightCss = Objects.requireNonNull(getClass().getResource("/com/unical/pixelflow/css/style-light.css")).toExternalForm();
        String darkCss = Objects.requireNonNull(getClass().getResource("/com/unical/pixelflow/css/style-dark.css")).toExternalForm();

        scene.getStylesheets().remove(lightCss);
        scene.getStylesheets().remove(darkCss);

        if (theme == Theme.LIGHT) {
            scene.getStylesheets().add(lightCss);
        } else {
            scene.getStylesheets().add(darkCss);
        }
    }

    public void update_user() {
        try {
            File directory = new File(DataPF.users_images_directory);
            String username = String.valueOf(DataPF.dbUser.getId());

            if (!directory.exists()) directory.mkdirs();

            File[] files = directory.listFiles((_, name) -> name.startsWith(username + "."));

            Image image;
            if (files != null && files.length > 0) {
                String fileExtension = files[0].getName().substring(files[0].getName().lastIndexOf('.'));
                File newFile = new File(DataPF.users_images_directory, username + fileExtension);
                image = new Image(newFile.toURI().toString());
            } else {
                image = new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/unical/pixelflow/images/IconPixelFlow.png")
                ));
            }

            if (userAvatar != null) {
                setCenteredCircleImage(userAvatar, image);
            }
            if (userNameLabel != null) {
                userNameLabel.setText(DataPF.dbUser.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCenteredCircleImage(ImageView imageView, Image image) {
        imageView.setImage(image);
        imageView.setViewport(null);

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

        double fitW = imageView.getFitWidth() > 0 ? imageView.getFitWidth() : 40;
        double fitH = imageView.getFitHeight() > 0 ? imageView.getFitHeight() : 40;

        Circle clip = new Circle(fitW / 2, fitH / 2, Math.min(fitW, fitH) / 2);
        imageView.setClip(clip);
    }

    void showProjects() {
        if (btnProjects != null && !btnProjects.isSelected()) btnProjects.setSelected(true);

        if (btnNewProject != null) {
            btnNewProject.setVisible(true);
            btnNewProject.setManaged(true);
        }
        if (btnImportProject != null) {
            btnImportProject.setVisible(true);
            btnImportProject.setManaged(true);
        }

        loadView("/com/unical/pixelflow/fxml/HomeView.fxml");
    }

    void showSettings() {
        if (btnSettings != null && !btnSettings.isSelected()) btnSettings.setSelected(true);

        if (btnNewProject != null) {
            btnNewProject.setVisible(false);
            btnNewProject.setManaged(false);
        }
        if (btnImportProject != null) {
            btnImportProject.setVisible(false);
            btnImportProject.setManaged(false);
        }

        loadView("/com/unical/pixelflow/fxml/ProfileSettings.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}