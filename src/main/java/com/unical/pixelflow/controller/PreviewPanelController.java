package com.unical.pixelflow.controller;

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

    public void setPreviewImage(Image image) {
        previewImage.setImage(image);
        previewImage.setVisible(image != null);
        placeholderLabel.setVisible(image == null || !placeholderLabel.getText().isBlank());
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
        // TBD: zoom/pan support (non necessario per MVP preview)
    }

    @FXML
    void handleResetZoom(ActionEvent event) {
        // TBD: zoom/pan support (non necessario per MVP preview)
    }
}
