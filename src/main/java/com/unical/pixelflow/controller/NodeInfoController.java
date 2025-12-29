package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class NodeInfoController {

    @FXML
    private VBox OverViewNoNodeSelected;

    @FXML
    private VBox PanelControlsEffects;

    @FXML
    private Text alertInfoLabel;

    @FXML
    private Text connectionsCountText;

    @FXML
    private Text connectionsLabel;

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
    private Text nodesCountText;

    @FXML
    private Text nodesLabel;

    @FXML
    private HBox nodesStatsBox;

    @FXML
    private Text projectOverviewTitle;

    @FXML
    private BorderPane root;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label titleProperties;

}
