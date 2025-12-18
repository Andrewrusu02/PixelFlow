package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
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

        String logoPath = Objects.requireNonNull(getClass().getResource("/assets/logo/logocompletoPixelFlow.svg")).toExternalForm();

        logoWebView.getEngine().load(logoPath);

        // Rende lo zoom coerente
        logoWebView.setZoom(0.8);

        loginHomeButton.setOnAction(event -> handleLogin());
        RegisterHomeButton.setOnAction(event -> handleRegister());
    }

    private void handleLogin() {
        System.out.println("Login");
    }

    private void handleRegister() {
        System.out.println("Register");
    }
}