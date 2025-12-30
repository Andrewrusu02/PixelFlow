package com.unical.pixelflow.controller;

import com.unical.pixelflow.application.AppController;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainEditingController {

    @FXML
    private StackPane CenterNodePane;
    @FXML
    private BorderPane GraphPane;
    @FXML
    private BorderPane LeftPane;
    @FXML
    private VBox LibraryPanelPane;
    @FXML
    private BorderPane PreviewPane;
    @FXML
    private BorderPane ProprietiesPane;
    @FXML
    private BorderPane RighPane;
    @FXML
    private BorderPane homeViewRoot;

    private LibraryPanelController libraryController;
    private WaveFormViewController waveFormController;
    private NodesViewController nodesViewController;
    private PreviewPanelController previewController;
    private NodeInfoController nodeInfoController;

    private AppController backend;

    @FXML
    public void initialize() {
        try {
            loadSubViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProject(ProjectMetadata project) {
        this.backend = new AppController();
        if (project != null) {
            backend.loadProject(project.getPath());
        }
    }

    private void loadSubViews() throws IOException {
        FXMLLoader libLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/LibraryPanel.fxml"));
        Parent libRoot = libLoader.load();
        System.out.println("LibraryPanel.fxml loaded successfully.");
        libraryController = libLoader.getController();
        LibraryPanelPane.getChildren().add(libRoot);

        FXMLLoader waveLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/WaveFormView.fxml"));
        Parent waveRoot = waveLoader.load();
        System.out.println("WaveFormView.fxml loaded successfully.");
        waveFormController = waveLoader.getController();
        GraphPane.getChildren().add(waveRoot);

        FXMLLoader nodesLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodesView.fxml"));
        Parent nodesRoot = nodesLoader.load();
        System.out.println("NodesView.fxml loaded successfully.");
        nodesViewController = nodesLoader.getController();
        CenterNodePane.getChildren().add(nodesRoot);

        FXMLLoader prevLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/PreviewPanel.fxml"));
        Parent prevRoot = prevLoader.load();
        System.out.println("PreviewPanel.fxml loaded successfully.");
        previewController = prevLoader.getController();
        PreviewPane.setCenter(prevRoot);

        FXMLLoader propLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodeInfoView.fxml"));
        Parent propRoot = propLoader.load();
        System.out.println("NodeInfoView.fxml loaded successfully.");
        nodeInfoController = propLoader.getController();
        ProprietiesPane.setCenter(propRoot);
    }

}