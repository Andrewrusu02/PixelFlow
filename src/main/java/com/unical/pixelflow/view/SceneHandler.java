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
    private static final String ICON_PATH = "/com/unical/pixelflow/images/IconPixelFlow.png";

    private static final double BASE_WIDTH = 1440;
    private static final double BASE_HEIGHT = 810;

    private double lastWidth = BASE_WIDTH;
    private double lastHeight = BASE_HEIGHT;

    private double lastX = -1;
    private double lastY = -1;

    private SceneHandler(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initStageListeners();
    }

    public static SceneHandler getInstance(Stage primaryStage) {
        if (instance == null) {
            instance = new SceneHandler(primaryStage);
        }
        return instance;
    }

    private void initStageListeners() {
        primaryStage.xProperty().addListener((obs, oldVal, newVal) -> {
            if (!primaryStage.isMaximized()) {
                lastX = newVal.doubleValue();
            }
        });

        primaryStage.yProperty().addListener((obs, oldVal, newVal) -> {
            if (!primaryStage.isMaximized()) {
                lastY = newVal.doubleValue();
            }
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!primaryStage.isMaximized()) {
                lastWidth = newVal.doubleValue();
            }
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!primaryStage.isMaximized()) {
                lastHeight = newVal.doubleValue();
            }
        });
    }

    private void setupScene(String fxmlPath, String title, boolean startMaximized) {
        try {
            setUserAgentStylesheet(getClass().getResource(STYLE_DARK_CSS_PATH).toExternalForm());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            try {
                if (getClass().getResourceAsStream(ICON_PATH) != null) {
                    Image icon = new Image(getClass().getResourceAsStream(ICON_PATH));
                    primaryStage.getIcons().add(icon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            primaryStage.setMinWidth(BASE_WIDTH);
            primaryStage.setMinHeight(BASE_HEIGHT);

            primaryStage.setWidth(Math.max(lastWidth, BASE_WIDTH));
            primaryStage.setHeight(Math.max(lastHeight, BASE_HEIGHT));

            if (startMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setMaximized(false);

                if (lastX == -1 || lastY == -1) {
                    primaryStage.centerOnScreen();
                } else {
                    primaryStage.setX(lastX);
                    primaryStage.setY(lastY);
                }
            }

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showHomePageScreen() {
        setupScene("/com/unical/pixelflow/fxml/homePage.fxml", "Pixel Flow", false);
    }

    public void showLoginScreen() {
        setupScene("/com/unical/pixelflow/fxml/LoginView.fxml", "Pixel Flow - Login", false);
    }

    public void showRegisterScreen() {
        setupScene("/com/unical/pixelflow/fxml/RegisterView.fxml", "Pixel Flow - Register", false);
    }

    public void startPixeFlow() {
        setupScene("/com/unical/pixelflow/fxml/DashBoard.fxml", "Pixel Flow - DashBoard", false);
    }
}