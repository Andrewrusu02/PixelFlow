package com.unical.pixelflow.popUp;

import com.unical.pixelflow.model.DataPF;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ProjectMenuController {

    @FXML private Button btnOpen;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Runnable onOpen;
    private Runnable onEdit;
    private Runnable onDelete;

    @FXML
    public void initialize() {
        btnOpen.textProperty().bind(DataPF.lsp.lsb("project.menu.open"));
        btnEdit.textProperty().bind(DataPF.lsp.lsb("project.menu.edit"));
        btnDelete.textProperty().bind(DataPF.lsp.lsb("project.menu.delete"));

        btnOpen.setOnAction(_e -> { if (onOpen != null) onOpen.run(); });
        btnEdit.setOnAction(_e -> { if (onEdit != null) onEdit.run(); });
        btnDelete.setOnAction(_e -> { if (onDelete != null) onDelete.run(); });
    }

    public void setActions(Runnable onOpen, Runnable onEdit, Runnable onDelete) {
        this.onOpen = onOpen;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }
}