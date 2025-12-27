package com.unical.pixelflow.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

import static javafx.application.Application.setUserAgentStylesheet;

public class SceneHandler {
    private static SceneHandler instance;
    private static Stage primaryStage;

    private static final String STYLE_DARK_CSS_PATH = "/com/unical/pixelflow/css/style-dark.css";
    private static final String STYLE_LIGHT_CSS_PATH = "/com/unical/pixelflow/css/style-light.css";
    private static final String ICON_PATH = "/com/unical/pixelflow/images/IconPixelFlow.png";
    private static final double MIN_WIDTH = 960;
    private static final double MIN_HEIGHT = 600;
    private static final double BASE_WIDTH = 1440;
    private static final double BASE_HEIGHT = 810;

    private double lastWidth = MIN_WIDTH;
    private double lastHeight = MIN_HEIGHT;

    private SceneHandler(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static SceneHandler getInstance(Stage primaryStage) {
        if (instance == null) {
            instance = new SceneHandler(primaryStage);
        }
        return instance;
    }


    private void setupScene(String fxmlPath, String title, boolean isMaximized) {
        try {
            setUserAgentStylesheet(getClass().getResource(STYLE_DARK_CSS_PATH).toExternalForm());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            primaryStage.setMinHeight(BASE_HEIGHT);
            primaryStage.setMinWidth(BASE_WIDTH);

            primaryStage.setWidth(lastWidth);
            primaryStage.setHeight(lastHeight);

            primaryStage.setTitle(title);

            Image icon = new Image(getClass().getResourceAsStream(ICON_PATH));
            primaryStage.getIcons().add(icon);

            primaryStage.setScene(scene);
            primaryStage.setMaximized(isMaximized);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginScreen() {
        lastWidth = primaryStage.getWidth();
        lastHeight = primaryStage.getHeight();
        setupScene("/com/unical/pixelflow/fxml/LoginView.fxml", "Pixel Flow  - Login", false);
    }

    public void showRegisterScreen() {
        lastWidth = primaryStage.getWidth();
        lastHeight = primaryStage.getHeight();
        setupScene("/com/unical/pixelflow/fxml/RegisterView.fxml", "Pixel Flow - Register", false);
    }

    public void startPixeFlow() {
        lastWidth = primaryStage.getWidth();
        lastHeight = primaryStage.getHeight();
        primaryStage.setMinHeight(BASE_HEIGHT);
        primaryStage.setMinWidth(BASE_WIDTH);
        setupScene("/com/unical/pixelflow/fxml/DashBoard.fxml", "Pixel Flow - DashBoard", true);
    }

    public void showHomePageScreen() {
        primaryStage.setMinHeight(BASE_HEIGHT);
        primaryStage.setMinWidth(BASE_WIDTH);
        setupScene("/com/unical/pixelflow/fxml/homePage.fxml", "Pixel Flow", false);
    }
}
