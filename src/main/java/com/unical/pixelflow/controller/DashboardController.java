package com.unical.pixelflow.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNewProject;

    @FXML
    private Button btnProjects;

    @FXML
    private Button btnSettings;

    @FXML
    private ProgressBar busy_indicator;

    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView logoHeader;

    @FXML
    private BorderPane mainPane;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userNameLabel;

    @FXML
    void handleLogout(ActionEvent event) {

    }

    @FXML
    void showProjects(ActionEvent event) {

    }

    @FXML
    void showSettings(ActionEvent event) {

    }

}
