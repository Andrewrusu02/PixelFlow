package com.unical.pixelflow.controller;

import com.unical.pixelflow.controller.ProjectMenuController;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ProjectCardController {

    @FXML private VBox cardRoot;
    @FXML private StackPane imageContainer; // FONDAMENTALE: deve avere fx:id nel FXML
    @FXML private ImageView thumbnailView;
    @FXML private SVGPath fallbackIcon;
    @FXML private Label nameLabel;
    @FXML private Label dateLabel;

    private ProjectMetadata project;

    // Dimensioni fisse della card (Devono corrispondere al prefWidth/Height dello StackPane nel FXML)
    private final double TARGET_W = 190;
    private final double TARGET_H = 140;

    // Riferimento al popup corrente per poterlo gestire (chiudere se ne apro un altro)
    private Popup currentMenuPopup;

    /**
     * Metodo principale per inizializzare la card con i dati e le azioni.
     */
    public void setData(ProjectMetadata project,
                        Consumer<ProjectMetadata> onOpen,
                        Consumer<ProjectMetadata> onEdit,
                        Consumer<ProjectMetadata> onDelete) {
        this.project = project;

        // 1. Imposta i testi
        nameLabel.setText(project.getName());
        dateLabel.setText(project.getFormattedDate());

        // 2. Applica la maschera arrotondata al contenitore (StackPane)
        // Questo taglia tutto ciò che esce dai bordi, creando l'effetto border-radius sull'immagine
        Rectangle clip = new Rectangle(TARGET_W, TARGET_H);
        clip.setArcWidth(10); // Raggio curvatura
        clip.setArcHeight(10);
        if (imageContainer != null) {
            imageContainer.setClip(clip);
        } else {
            System.err.println("ERRORE: imageContainer è NULL. Controlla fx:id in ProjectCard.fxml");
        }

        // 3. Carica l'immagine
        loadImage();

        // 4. Configura i click (Sinistro = Apri, Destro = Menu Custom)
        setupInteractions(onOpen, onEdit, onDelete);
    }

    private void loadImage() {
        if (project.getThumbnailPath() != null && !project.getThumbnailPath().isEmpty()) {
            File file = new File(project.getThumbnailPath());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());

                thumbnailView.setVisible(true);
                fallbackIcon.setVisible(false);

                // Avvia la logica di ritaglio e centratura
                setCenteredImage(thumbnailView, img);
                return;
            }
        }

        // Fallback se non c'è immagine
        thumbnailView.setVisible(false);
        fallbackIcon.setVisible(true);
    }

    private void setCenteredImage(ImageView imageView, Image image) {
        imageView.setImage(image);
        imageView.setViewport(null); // Reset viewport precedente

        // Gestione caricamento asincrono: aspetta che width > 0
        if (image.getWidth() <= 0) {
            image.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && image.getHeight() > 0) {
                    Platform.runLater(() -> cropImage(imageView, image));
                }
            });
        } else {
            cropImage(imageView, image);
        }
    }

    /**
     * Calcola il ritaglio (Viewport) per ottenere l'effetto "Object-Fit: Cover"
     */
    private void cropImage(ImageView imageView, Image image) {
        double imgW = image.getWidth();
        double imgH = image.getHeight();

        double targetRatio = TARGET_W / TARGET_H;
        double imgRatio = imgW / imgH;

        double cropW, cropH;
        double x, y;

        if (imgRatio > targetRatio) {
            // Immagine più larga del target -> Taglia i lati
            cropH = imgH;
            cropW = imgH * targetRatio;
            x = (imgW - cropW) / 2;
            y = 0;
        } else {
            // Immagine più alta del target -> Taglia sopra e sotto
            cropW = imgW;
            cropH = imgW / targetRatio;
            x = 0;
            y = (imgH - cropH) / 2;
        }

        // Imposta il rettangolo di visualizzazione
        imageView.setViewport(new Rectangle2D(x, y, cropW, cropH));

        // Forza le dimensioni nel layout
        imageView.setFitWidth(TARGET_W);
        imageView.setFitHeight(TARGET_H);
        imageView.setSmooth(true);
    }

    private void setupInteractions(Consumer<ProjectMetadata> onOpen,
                                   Consumer<ProjectMetadata> onEdit,
                                   Consumer<ProjectMetadata> onDelete) {

        // Click sul nodo principale
        cardRoot.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // Tasto Sinistro -> APRI
                onOpen.accept(project);
            }
            else if (e.getButton() == MouseButton.SECONDARY) {
                // Tasto Destro -> MENU CUSTOM
                showCustomMenu(e.getScreenX(), e.getScreenY(), onOpen, onEdit, onDelete);
            }
        });

        // Evento specifico menu contestuale (per tastiere o doppio tap)
        cardRoot.setOnContextMenuRequested(e -> {
            showCustomMenu(e.getScreenX(), e.getScreenY(), onOpen, onEdit, onDelete);
            e.consume(); // Blocca il menu nativo di sistema
        });
    }

    private void showCustomMenu(double screenX, double screenY,
                                Consumer<ProjectMetadata> onOpen,
                                Consumer<ProjectMetadata> onEdit,
                                Consumer<ProjectMetadata> onDelete) {
        try {
            // Se c'è già un menu aperto, chiudilo
            if (currentMenuPopup != null) {
                currentMenuPopup.hide();
            }

            // Carica il file FXML del menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/components/ProjectMenu.fxml"));
            Parent menuContent = loader.load();

            // Recupera il controller del menu per impostare le azioni
            ProjectMenuController menuController = loader.getController();

            // Crea il Popup
            Popup popup = new Popup();
            popup.getContent().add(menuContent);
            popup.setAutoHide(true); // Si chiude se clicchi fuori

            // Imposta le azioni sui tasti del menu
            menuController.setActions(
                    () -> { popup.hide(); onOpen.accept(project); },
                    () -> { popup.hide(); onEdit.accept(project); },
                    () -> { popup.hide(); onDelete.accept(project); }
            );

            // Mostra il popup
            Window window = cardRoot.getScene().getWindow();
            popup.show(window, screenX, screenY);

            currentMenuPopup = popup;

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Impossibile caricare il menu custom: " + ex.getMessage());
        }
    }
}