package com.unical.pixelflow.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class LibraryPanelController {

    @FXML private VBox root;

    private Consumer<String> onNodeRequested;

    @FXML
    public void initialize() {
        // Qui andrà la logica per popolare la lista dei filtri
    }

}