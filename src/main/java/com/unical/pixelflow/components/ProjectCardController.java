package com.unical.pixelflow.components;

import com.unical.pixelflow.model.ProjectMetadata;
import com.unical.pixelflow.popUp.ProjectMenuController;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class ProjectCardController {

    @FXML private VBox cardRoot;
    @FXML private StackPane imageContainer;
    @FXML private ImageView thumbnailView;
    @FXML private SVGPath fallbackIcon;
    @FXML private Label nameLabel;
    @FXML private Label dateLabel;

    private ProjectMetadata project;

    private Consumer<ProjectMetadata> onOpenAction;
    private Consumer<ProjectMetadata> onEditAction;
    private Consumer<ProjectMetadata> onDeleteAction;

    private final double TARGET_W = 190;
    private final double TARGET_H = 140;

    private Popup currentMenuPopup;

    public void setData(ProjectMetadata project,
                        Consumer<ProjectMetadata> onOpen,
                        Consumer<ProjectMetadata> onEdit,
                        Consumer<ProjectMetadata> onDelete) {

        this.project = project;
        this.onOpenAction = onOpen;
        this.onEditAction = onEdit;
        this.onDeleteAction = onDelete;

        nameLabel.setText(project.getName());
        dateLabel.setText(project.getFormattedDate());

        Rectangle clip = new Rectangle(TARGET_W, TARGET_H);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        if (imageContainer != null) {
            imageContainer.setClip(clip);
        }

        loadImage();
        setupInteractions();
    }

    private void loadImage() {
        if (project.getThumbnailPath() != null && !project.getThumbnailPath().isEmpty()) {
            File file = new File(project.getThumbnailPath());
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Image img = new Image(fis);
                    thumbnailView.setVisible(true);
                    fallbackIcon.setVisible(false);
                    setCenteredImage(thumbnailView, img);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        thumbnailView.setVisible(false);
        fallbackIcon.setVisible(true);
    }

    private void setCenteredImage(ImageView imageView, Image image) {
        imageView.setImage(image);
        imageView.setViewport(null);

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

    private void cropImage(ImageView imageView, Image image) {
        double imgW = image.getWidth();
        double imgH = image.getHeight();
        double targetRatio = TARGET_W / TARGET_H;
        double imgRatio = imgW / imgH;

        double cropW, cropH, x, y;

        if (imgRatio > targetRatio) {
            cropH = imgH;
            cropW = imgH * targetRatio;
            x = (imgW - cropW) / 2;
            y = 0;
        } else {
            cropW = imgW;
            cropH = imgW / targetRatio;
            x = 0;
            y = (imgH - cropH) / 2;
        }

        imageView.setViewport(new Rectangle2D(x, y, cropW, cropH));
        imageView.setFitWidth(TARGET_W);
        imageView.setFitHeight(TARGET_H);
        imageView.setSmooth(true);
    }

    private void setupInteractions() {
        cardRoot.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (onOpenAction != null) onOpenAction.accept(project);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                showCustomMenu(e.getScreenX(), e.getScreenY());
            }
        });

        cardRoot.setOnContextMenuRequested(e -> {
            showCustomMenu(e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private void showCustomMenu(double screenX, double screenY) {
        try {
            if (currentMenuPopup != null) {
                currentMenuPopup.hide();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/components/ProjectMenu.fxml"));
            Parent menuContent = loader.load();

            ProjectMenuController menuController = loader.getController();

            Popup popup = new Popup();
            popup.getContent().add(menuContent);
            popup.setAutoHide(true);

            menuController.setActions(
                    () -> {
                        popup.hide();
                        if (onOpenAction != null) onOpenAction.accept(project);
                    },
                    () -> {
                        popup.hide();
                        if (onEditAction != null) onEditAction.accept(project);
                    },
                    () -> {
                        popup.hide();
                        if (onDeleteAction != null) onDeleteAction.accept(project);
                    }
            );

            Window window = cardRoot.getScene().getWindow();
            popup.show(window, screenX, screenY);
            currentMenuPopup = popup;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}