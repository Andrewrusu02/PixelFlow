package com.unical.pixelflow.controller;

import javafx.application.Platform;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.UserDatabase;
import com.unical.pixelflow.model.types.Theme;
import com.unical.pixelflow.view.ModalEffects;
import com.unical.pixelflow.view.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNewProject;

    @FXML
    private ToggleButton btnProjects;

    @FXML
    private ToggleButton btnSettings;

    @FXML
    private ProgressBar busy_indicator;

    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView logoHeader;


    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userNameLabel;

    @FXML
    private BorderPane dashboardRoot;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        DataPF.esm.register(executorService);
        DataPF.userDatabase = new UserDatabase();

        btnNewProject.textProperty().bind(DataPF.lsp.lsb("dashboard.new_project"));
        btnProjects.textProperty().bind(DataPF.lsp.lsb("dashboard.recent_projects"));
        btnSettings.textProperty().bind(DataPF.lsp.lsb("dashboard.profile_settings"));
        btnLogout.textProperty().bind(DataPF.lsp.lsb("dashboard.logout"));


        DataPF.dbUser.themeProperty().addListener((_, _, newValue) -> {
            System.out.println("Theme changed to " + newValue);
            Scene scene = dashboardRoot.getScene();
            switch (newValue) {
                case DARK, SYSTEM:
                    scene.getStylesheets().remove(getClass().getResource("/com/unical/pixelflow/css/style-light.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/com/unical/pixelflow/css/style-dark.css").toExternalForm());
                    break;
                case LIGHT:
                    scene.getStylesheets().remove(getClass().getResource("/com/unical/pixelflow/css/style-dark.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/com/unical/pixelflow/css/style-light.css").toExternalForm());
                    break;
            }
        });

        btnProjects.sceneProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (DataPF.dbUser.themeProperty().get() == Theme.LIGHT) {
                    newValue.getStylesheets().remove(getClass().getResource("/com/unical/pixelflow/css/style-dark.css").toExternalForm());
                    newValue.getStylesheets().add(getClass().getResource("/com/unical/pixelflow/css/style-light.css").toExternalForm());
                } else {
                    newValue.getStylesheets().remove(getClass().getResource("/com/unical/pixelflow/css/style-light.css").toExternalForm());
                    newValue.getStylesheets().add(getClass().getResource("/com/unical/pixelflow/css/style-dark.css").toExternalForm());
                }
                DataPF.lsp.setSelectedLanguage(DataPF.dbUser.languageProperty().get());
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
        btnProjects.setToggleGroup(navGroup);
        btnSettings.setToggleGroup(navGroup);

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
        btnProjects.setSelected(true);
        update_user();
        showProjects();
    }

    public void update_user() {
        File directory = new File(DataPF.users_images_directory);
        String username = String.valueOf(DataPF.dbUser.getId());
        File[] files = directory.listFiles((_, name) -> name.startsWith(username + "."));

        Image image;
        if (files != null && files.length > 0) {
            String fileExtension = files[0].getName().substring(files[0].getName().lastIndexOf('.'));
            File newFile = new File(DataPF.users_images_directory, username + fileExtension);
            image = new Image(newFile.toURI().toString());
        } else {
            image = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/unical/pixelflow/assets/logo/LogoPixelFlow.png")
            ));
        }

        // Imposta l'immagine con la logica di ritaglio centrale
        setCenteredCircleImage(userAvatar, image);

        userNameLabel.setText(DataPF.dbUser.getUsername());
    }

    private void setCenteredCircleImage(ImageView imageView, Image image) {
        imageView.setImage(image);

        // Resetta la viewport nel caso l'immagine precedente avesse dimensioni diverse
        imageView.setViewport(null);

        // Poiché il caricamento dell'immagine potrebbe essere asincrono,
        // usiamo un listener per assicurarci di avere le dimensioni reali
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
        double fitW = imageView.getFitWidth();
        double fitH = imageView.getFitHeight();

        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        Circle clip = new Circle(fitW / 2, fitH / 2, Math.min(fitW, fitH) / 2);
        imageView.setClip(clip);
    }

    void showProjects() {
        if (!btnProjects.isSelected()) {
            btnProjects.setSelected(true);
        }
        loadView("/com/unical/pixelflow/fxml/HomeView.fxml");
    }


    void showSettings() {
        if (!btnSettings.isSelected()) {
            btnSettings.setSelected(true);
        }
        loadView("/com/unical/pixelflow/fxml/ProfileSettings.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DataPF.dbUser = null;
        Stage stage = (Stage) btnLogout.getScene().getWindow();
        SceneHandler.getInstance(stage).showLoginScreen();
    }

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
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(page);

            dialogStage.setScene(scene);
            scene.setFill(Color.TRANSPARENT);
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
                showProjects();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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

}
