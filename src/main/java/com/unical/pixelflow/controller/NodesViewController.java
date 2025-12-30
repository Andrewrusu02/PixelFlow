package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

public class NodesViewController  {

    @FXML
    private ToggleButton CopyButton;

    @FXML
    private ToggleButton DeleteButton;

    @FXML
    private ToggleButton DisableButton;

    @FXML
    private ToggleButton MoveButton;

    @FXML
    private ToggleButton RedoButton;

    @FXML
    private ToggleButton SaveProjectButton;

    @FXML
    private ToggleButton UndoButton;

    @FXML
    private Pane canvas;

    @FXML
    private SVGPath hideShowIcon;

    @FXML
    private StackPane root;

    @FXML
    private ScrollPane scrollPane;

}
