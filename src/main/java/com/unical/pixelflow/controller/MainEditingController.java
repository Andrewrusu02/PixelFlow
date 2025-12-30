package com.unical.pixelflow.controller;

import com.unical.pixelflow.application.AppController;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.model.ProjectMetadata;
import com.unical.pixelflow.view.ModalEffects;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainEditingController {

    @FXML private StackPane CenterNodePane;
    @FXML private BorderPane GraphPane;
    @FXML private BorderPane LeftPane;
    @FXML private VBox LibraryPanelPane;
    @FXML private BorderPane PreviewPane;
    @FXML private BorderPane ProprietiesPane;
    @FXML private BorderPane RighPane;
    @FXML private BorderPane homeViewRoot;

    @FXML private Button browseButton;
    @FXML private Button buttonHome;
    @FXML private TextField pathField;

    private AppController backend;

    private NodesViewController nodesViewController;

    @FXML
    public void initialize() {
        if (this.backend == null) {
            this.backend = new AppController();
        }

        try {
            loadSubViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/ExitProjectView.fxml"));
            Parent root = loader.load();

            // Setup del popup
            Stage popupStage = new Stage();
            popupStage.initOwner(buttonHome.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(null);
            popupStage.setScene(scene);

            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossibile caricare ExitProjectView.fxml");
        }
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagine Sorgente");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        showDialogWithBlur(stage);

        if (selectedFile != null) {
            pathField.setText(selectedFile.getName());
            addSourceImageNode(selectedFile);
        }
    }

    private void addSourceImageNode(File file) {
        try {
            BufferedImage sourceImage = ImageIO.read(file);

            if (sourceImage != null) {
                InputNode inputNode = new InputNode(sourceImage);

                backend.getPipeline().addNode(inputNode);

                System.out.println("Immagine caricata e nodo creato: " + file.getName());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento dell'immagine: " + e.getMessage());
        }
    }

    public void setProject(ProjectMetadata project) {
        this.backend = new AppController();
        if (project != null) {
            backend.loadProject(project.getPath());
        }

    }

    private void loadSubViews() throws IOException {
        // Library Panel
        FXMLLoader libLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/LibraryPanel.fxml"));
        LibraryPanelPane.getChildren().add(libLoader.load());

        // WaveForm View
        FXMLLoader waveLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/WaveFormView.fxml"));
        GraphPane.getChildren().add(waveLoader.load());

        // --- NODES VIEW (Cruciale: salviamo il controller) ---
        FXMLLoader nodesLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodesView.fxml"));
        Parent nodesRoot = nodesLoader.load();
        this.nodesViewController = nodesLoader.getController();
        CenterNodePane.getChildren().add(nodesRoot);

        // Preview Panel
        FXMLLoader prevLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/PreviewPanel.fxml"));
        PreviewPane.setCenter(prevLoader.load());

        // Properties Panel
        FXMLLoader propLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodeInfoView.fxml"));
        ProprietiesPane.setCenter(propLoader.load());
    }


    private void showDialogWithBlur(Stage dialogStage) {
        Stage ownerStage = (Stage) buttonHome.getScene().getWindow();
        ModalEffects.ModalOverlay overlay = null;

        if (homeViewRoot!= null) {
            overlay = ModalEffects.apply(homeViewRoot);
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
}