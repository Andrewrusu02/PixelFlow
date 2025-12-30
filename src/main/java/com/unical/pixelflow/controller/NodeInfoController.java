package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class NodeInfoController {

    @FXML
    private VBox OverViewNoNodeSelected;

    @FXML
    private VBox PanelControlsEffects;

    @FXML
    private Label alertInfoLabel;

    @FXML
    private Label connectionsCountText;

    @FXML
    private Label connectionsLabel;

    @FXML
    private HBox connectionsStatsBox;

    @FXML
    private HBox headerBox;

    @FXML
    private StackPane imageContainer;

    @FXML
    private HBox infoBox;

    @FXML
    private SVGPath infoIcon;

    @FXML
    private Label nodesCountText;

    @FXML
    private Label nodesLabel;

    @FXML
    private HBox nodesStatsBox;

    @FXML
    private Label projectOverviewTitle;

    @FXML
    private BorderPane root;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label titleProperties;

}
