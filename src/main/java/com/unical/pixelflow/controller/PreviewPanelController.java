package com.unical.pixelflow.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class PreviewPanelController {

    @FXML
    private StackPane imageContainer;

    @FXML
    private ImageView previewImage;

    @FXML
    private Label titlePreview;

    @FXML
    private Label zoomLabel;

    @FXML
    void handleFitToScreen(ActionEvent event) {

    }

    @FXML
    void handleResetZoom(ActionEvent event) {

    }

}
