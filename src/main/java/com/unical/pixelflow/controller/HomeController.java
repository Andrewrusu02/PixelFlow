package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.unical.pixelflow.view.SceneHandler;

import java.io.InputStream;
import java.util.Objects;

public class HomeController {

    @FXML
    private ImageView ImageHome;

    @FXML
    private Button RegisterHomeButton;

    @FXML
    private Button loginHomeButton;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Text textEffectsDesc;

    @FXML
    private Text textEffectsTitle;

    @FXML
    private Text textResultsDesc;

    @FXML
    private Text textResultsTitle;

    @FXML
    private Text textWelcomeBody;

    @FXML
    private Text textWelcomeTitle;

    @FXML
    private Text textWorkflowDesc;

    @FXML
    private Text textWorkflowTitle;

    @FXML
    public void initialize() {
        try {
            String path = "/com/unical/pixelflow/assets/logo/LogoPixelFlow.png";
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Errore caricamento logo register: " + e.getMessage());
        }
        loginHomeButton.setOnAction(event -> handleLogin());
        RegisterHomeButton.setOnAction(event -> handleRegister());
        bindTranslations();
    }

    private void handleLogin() {
        Stage stage = (Stage) loginHomeButton.getScene().getWindow();
        SceneHandler.getInstance(stage).showLoginScreen();
    }

    private void handleRegister() {
        Stage stage = (Stage) RegisterHomeButton.getScene().getWindow();
        SceneHandler.getInstance(stage).showRegisterScreen();
    }

    private void bindTranslations() {
        // Pulsanti
        loginHomeButton.textProperty().bind(DataPF.lsp.lsb("home.button.login"));
        RegisterHomeButton.textProperty().bind(DataPF.lsp.lsb("home.button.register"));

        // Testo Principale (Hero)
        textWelcomeTitle.textProperty().bind(DataPF.lsp.lsb("home.welcome.title"));
        textWelcomeBody.textProperty().bind(DataPF.lsp.lsb("home.welcome.body"));

        // Feature 1: Workflow
        textWorkflowTitle.textProperty().bind(DataPF.lsp.lsb("home.feature.workflow.title"));
        textWorkflowDesc.textProperty().bind(DataPF.lsp.lsb("home.feature.workflow.desc"));

        // Feature 2: Effects
        textEffectsTitle.textProperty().bind(DataPF.lsp.lsb("home.feature.effects.title"));
        textEffectsDesc.textProperty().bind(DataPF.lsp.lsb("home.feature.effects.desc"));

        // Feature 3: Results
        textResultsTitle.textProperty().bind(DataPF.lsp.lsb("home.feature.results.title"));
        textResultsDesc.textProperty().bind(DataPF.lsp.lsb("home.feature.results.desc"));
    }
}