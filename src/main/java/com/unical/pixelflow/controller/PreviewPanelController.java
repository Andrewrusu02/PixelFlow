package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class PreviewPanelController {

    @FXML
    private StackPane imageContainer;

    @FXML
    private ImageView previewImage;

    @FXML
    private Label placeholderLabel;

    @FXML
    private Label titlePreview;

    @FXML
    private Label zoomLabel;

    @FXML
    private javafx.scene.control.ScrollPane scrollPane;

    private double zoom = 1.0;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 8.0;

    private double dragStartX;
    private double dragStartY;
    private double startH;
    private double startV;

    private boolean isFirstPreview = true;
    private boolean hasInitializedCamera = false;

    @FXML
    private void initialize() {
        if (titlePreview != null) {
            titlePreview.textProperty().bind(DataPF.lsp.lsb("editor.preview.title"));
        }

        if (scrollPane != null) {
            scrollPane.setPannable(false); // usiamo pan custom per una UX consistente
            scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);

            scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
                if (previewImage == null || previewImage.getImage() == null) return;

                // Ctrl/Cmd + scroll = zoom
                if (e.isControlDown() || e.isShortcutDown()) {
                    double factor = Math.pow(1.0015, e.getDeltaY());
                    setZoom(clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM));
                    e.consume();
                }
            });

            scrollPane.addEventFilter(javafx.scene.input.ZoomEvent.ZOOM, e -> {
                if (previewImage == null || previewImage.getImage() == null) return;
                setZoom(clamp(zoom * e.getZoomFactor(), MIN_ZOOM, MAX_ZOOM));
                e.consume();
            });

            // Pan con trascinamento (drag) del mouse nella viewport
            scrollPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                if (previewImage == null || previewImage.getImage() == null) return;
                dragStartX = e.getSceneX();
                dragStartY = e.getSceneY();
                startH = scrollPane.getHvalue();
                startV = scrollPane.getVvalue();
            });

            scrollPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
                if (previewImage == null || previewImage.getImage() == null) return;

                double dx = e.getSceneX() - dragStartX;
                double dy = e.getSceneY() - dragStartY;

                // Normalizziamo rispetto alle dimensioni contenuto (immagine scalata)
                double contentW = previewImage.getFitWidth() > 0 ? previewImage.getFitWidth() : previewImage.getImage().getWidth() * zoom;
                double contentH = previewImage.getFitHeight() > 0 ? previewImage.getFitHeight() : previewImage.getImage().getHeight() * zoom;
                double viewportW = scrollPane.getViewportBounds().getWidth();
                double viewportH = scrollPane.getViewportBounds().getHeight();

                double maxX = Math.max(1, contentW - viewportW);
                double maxY = Math.max(1, contentH - viewportH);

                double deltaH = dx / maxX;
                double deltaV = dy / maxY;

                scrollPane.setHvalue(clamp01(startH - deltaH));
                scrollPane.setVvalue(clamp01(startV - deltaV));
            });
        }
    }

    public void setPreviewImage(Image image) {
        previewImage.setImage(image);
        previewImage.setVisible(image != null);
        placeholderLabel.setVisible(image == null || !placeholderLabel.getText().isBlank());

        if (image != null) {
            if (isFirstPreview || !hasInitializedCamera) {
                isFirstPreview = false;
                hasInitializedCamera = true;
                javafx.application.Platform.runLater(() -> {
                    fitToScreen();
                });
            } else {
                setZoom(zoom);
            }
        }
    }

    /**
     * Reset della camera state - utile quando si carica un nuovo progetto o si cambia source image
     */
    public void resetCameraState() {
        isFirstPreview = true;
        hasInitializedCamera = false;
    }

    public void showPlaceholder(String message) {
        previewImage.setImage(null);
        previewImage.setVisible(false);
        placeholderLabel.setText(message == null ? "" : message);
        placeholderLabel.setVisible(true);
    }

    public void clearPlaceholder() {
        placeholderLabel.setText("");
        placeholderLabel.setVisible(false);
    }

    @FXML
    void handleFitToScreen(ActionEvent event) {
        fitToScreen();
    }

    @FXML
    void handleResetZoom(ActionEvent event) {
        setZoom(1.0);
        center();
    }

    private void fitToScreen() {
        if (previewImage == null || previewImage.getImage() == null || scrollPane == null) return;

        double imgW = previewImage.getImage().getWidth();
        double imgH = previewImage.getImage().getHeight();
        double vpW = scrollPane.getViewportBounds().getWidth();
        double vpH = scrollPane.getViewportBounds().getHeight();
        if (imgW <= 0 || imgH <= 0 || vpW <= 0 || vpH <= 0) return;

        double factor = Math.min(vpW / imgW, vpH / imgH);
        setZoom(clamp(factor, MIN_ZOOM, MAX_ZOOM));
        center();
    }

    private void center() {
        if (scrollPane == null) return;
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    private void setZoom(double newZoom) {
        zoom = newZoom;
        if (previewImage != null && previewImage.getImage() != null) {
            double imgW = previewImage.getImage().getWidth();
            double imgH = previewImage.getImage().getHeight();
            previewImage.setFitWidth(imgW * zoom);
            previewImage.setFitHeight(imgH * zoom);

            if (imageContainer != null && scrollPane != null) {
                double vpW = scrollPane.getViewportBounds().getWidth();
                double vpH = scrollPane.getViewportBounds().getHeight();

                imageContainer.setMinWidth(Math.max(vpW, imgW * zoom));
                imageContainer.setMinHeight(Math.max(vpH, imgH * zoom));
                imageContainer.setPrefWidth(Math.max(vpW, imgW * zoom));
                imageContainer.setPrefHeight(Math.max(vpH, imgH * zoom));
            }
        }
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        if (zoomLabel == null) return;
        int percent = (int) Math.round(zoom * 100.0);
        zoomLabel.setText(percent + "%");
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
