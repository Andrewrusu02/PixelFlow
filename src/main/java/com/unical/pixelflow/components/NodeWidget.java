package com.unical.pixelflow.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class NodeWidget extends BorderPane {

    @FXML
    private SVGPath iconShape;

    @FXML
    private Circle inputSocket;

    @FXML
    private Label nodeTitle;

    @FXML
    private Circle outputSocket;

}
