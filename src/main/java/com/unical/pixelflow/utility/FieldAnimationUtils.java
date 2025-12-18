package com.unical.pixelflow.utility;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.util.Duration;
import com.unical.pixelflow.components.ComboPasswordField;

public class FieldAnimationUtils {
    private static final double SHAKE_DISTANCE = 2;
    private static final double SHAKE_DURATION = 0.25;

    public static void disableContextMenu(Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof ComboBox<?> comboBox) {
                comboBox.setContextMenu(null);
                comboBox.setOnContextMenuRequested(Event::consume);
            } else if (node instanceof TextArea textArea) {
                textArea.setContextMenu(null);
                textArea.setOnContextMenuRequested(Event::consume);
            }  else if (node instanceof TextField textField) {
                textField.setContextMenu(null);
                textField.setOnContextMenuRequested(Event::consume);
            } else if (node instanceof PasswordField passwordField) {

            }
        }
    }

    private static Timeline createShakeTimeline(javafx.scene.Node node) {
        return new Timeline(
                new KeyFrame(Duration.seconds(0), e -> node.setTranslateX(0)),
                new KeyFrame(Duration.seconds(0.05), e -> node.setTranslateX(-SHAKE_DISTANCE)),
                new KeyFrame(Duration.seconds(0.1), e -> node.setTranslateX(SHAKE_DISTANCE)),
                new KeyFrame(Duration.seconds(0.15), e -> node.setTranslateX(-SHAKE_DISTANCE)),
                new KeyFrame(Duration.seconds(0.20), e -> node.setTranslateX(SHAKE_DISTANCE)),
                new KeyFrame(Duration.seconds(SHAKE_DURATION), e -> node.setTranslateX(0))
        );
    }

    private static void animateError(javafx.scene.Node field, String errorStyleClass) {
        if (!field.getStyleClass().contains(errorStyleClass)) {
            field.getStyleClass().add(errorStyleClass);
        }
        Timeline shakeTimeline = createShakeTimeline(field);
        shakeTimeline.setCycleCount(1);
        shakeTimeline.play();
    }

    private static void removeErrorStyles(javafx.scene.Node field, String errorStyleClass) {
        field.getStyleClass().remove(errorStyleClass);
    }

    public static void animateFieldError(TextField field) {
        animateError(field, "errore");
    }

    public static void removeErrorStyles(TextField field) {
        removeErrorStyles(field, "errore");
    }

    public static void animateFieldError(ComboPasswordField field) {
        for (javafx.scene.Node child : field.getChildren()) {
            if (child instanceof TextField || child instanceof PasswordField) {
                animateError(child, "errore");
            }
        }
    }

    public static void removeErrorStyles(ComboPasswordField field) {
        for (javafx.scene.Node child : field.getChildren()) {
            removeErrorStyles(child, "errore");
        }
    }

    public static void animateFieldError(ComboBox<?> comboBox) {
        animateError(comboBox, "errore");
    }

    public static void removeErrorStyles(ComboBox<?> comboBox) {
        removeErrorStyles(comboBox, "errore");
    }

}

