package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.unical.pixelflow.view.SceneHandler;

import java.util.Objects;

public class HomeController {

    @FXML
    private WebView logoWebView;

    @FXML
    private Label welcomeText;

    @FXML
    private Button loginHomeButton;

    @FXML
    private Button RegisterHomeButton;

    @FXML
    public void initialize() {
        logoWebView.setContextMenuEnabled(false);

        try {
            logoWebView.setPageFill(Color.TRANSPARENT);
            String logoPath = Objects.requireNonNull(getClass().getResource("/com/unical/pixelflow/assets/logo/logocompletoPixelFlow.svg")).toExternalForm();
            logoWebView.getEngine().load(logoPath);
        } catch (Exception e) {
            System.err.println("Errore caricamento logo: " + e.getMessage());
        }

        logoWebView.setZoom(0.8);
        loginHomeButton.setOnAction(event -> handleLogin());
        RegisterHomeButton.setOnAction(event -> handleRegister());
    }

    private void handleLogin() {
        Stage stage = (Stage) loginHomeButton.getScene().getWindow();
        SceneHandler.getInstance(stage).showLoginScreen();
    }

    private void handleRegister() {
        Stage stage = (Stage) RegisterHomeButton.getScene().getWindow();
        SceneHandler.getInstance(stage).showRegisterScreen();
    }
}