package com.unical.pixelflow.components;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SwitchTheme extends StackPane {

    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(0.25));
    private final FillTransition fillAnimation = new FillTransition(Duration.seconds(0.25));
    private final ParallelTransition animation = new ParallelTransition(translateAnimation, fillAnimation);

    public BooleanProperty switchedOnProperty() {
        return switchedOn;
    }

    public boolean isSwitchedOn() {
        return switchedOn.get();
    }

    public void setSwitchedOn(boolean on) {
        switchedOn.set(on);
        updateState(on);
    }

    public SwitchTheme() {
        double width = 50;
        double height = 24;
        double radius = height / 2.0;

        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(height);
        background.setArcHeight(height);
        background.setFill(Color.valueOf("#3e3e3e"));
        background.setStroke(Color.TRANSPARENT);

        Circle trigger = new Circle(radius - 2);
        trigger.setFill(Color.valueOf("#cccccc"));
        trigger.setEffect(new DropShadow(2, Color.BLACK));
        trigger.setTranslateX(-(width / 2) + radius);

        getChildren().addAll(background, trigger);

        translateAnimation.setNode(trigger);
        fillAnimation.setShape(background);

        setOnMouseClicked(event -> {
            switchedOn.set(!switchedOn.get());
        });

        switchedOn.addListener((obs, oldState, newState) -> {
            boolean isOn = newState;
            double endX = isOn ? (width / 2) - radius : -(width / 2) + radius;
            translateAnimation.setToX(endX);

            fillAnimation.setFromValue((Color) background.getFill());
            fillAnimation.setToValue(isOn ? Color.valueOf("#E1C302") : Color.valueOf("#3e3e3e"));

            if (isOn) {
                trigger.setFill(Color.WHITE);
            } else {
                trigger.setFill(Color.valueOf("#cccccc"));
            }

            animation.play();
        });
    }

    private void updateState(boolean isOn) {
        double width = 50;
        double height = 24;
        double radius = height / 2.0;

        Rectangle background = (Rectangle) getChildren().get(0);
        Circle trigger = (Circle) getChildren().get(1);

        trigger.setTranslateX(isOn ? (width / 2) - radius : -(width / 2) + radius);
        background.setFill(isOn ? Color.valueOf("#E1C302") : Color.valueOf("#3e3e3e"));
        trigger.setFill(isOn ? Color.WHITE : Color.valueOf("#cccccc"));
    }
}