package com.unical.pixelflow.controller;

import com.unical.pixelflow.infrastructure.ProjectSerializer;
import com.unical.pixelflow.infrastructure.dto.ProjectDTO;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditProjectController {

    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label sourceLabel;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        if (titleLabel != null) titleLabel.textProperty().bind(DataPF.lsp.lsb("dialog.edit_project.title"));
        if (nameLabel != null) nameLabel.textProperty().bind(DataPF.lsp.lsb("dialog.edit_project.name"));
        if (sourceLabel != null) sourceLabel.textProperty().bind(DataPF.lsp.lsb("dialog.edit_project.source"));
        if (cancelButton != null) cancelButton.textProperty().bind(DataPF.lsp.lsb("common.cancel"));
        if (saveButton != null) saveButton.textProperty().bind(DataPF.lsp.lsb("common.save"));
    }

    @FXML
    private TextField nameField;

    @FXML
    private TextField sourcePathField;

    private Stage dialogStage;
    private ProjectMetadata project;
    private boolean saved;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProject(ProjectMetadata project) {
        this.project = project;
        this.saved = false;

        nameField.setText(project.getName());

        try {
            ProjectSerializer serializer = new ProjectSerializer();
            ProjectDTO dto = serializer.loadDto(project.getPath());
            String src = dto != null ? dto.sourcePath : null;
            sourcePathField.setText(src == null || src.isBlank() ? DataPF.lsp.lsb("dialog.edit_project.source.unavailable").get() : src);
        } catch (Exception e) {
            sourcePathField.setText(DataPF.lsp.lsb("dialog.edit_project.source.unreadable").get());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (project == null) return;

        String newName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (newName.isEmpty()) {
            showError(DataPF.lsp.lsb("common.error").get(), DataPF.lsp.lsb("dialog.edit_project.error.name").get());
            return;
        }

        // No-op
        if (newName.equals(project.getName())) {
            saved = false;
            dialogStage.close();
            return;
        }

        Task<Boolean> task = DataPF.userDatabase.renameProject(project.getId(), newName);
        task.setOnSucceeded(e -> {
            Boolean ok = task.getValue();
            if (Boolean.TRUE.equals(ok)) {
                saved = true;
                dialogStage.close();
            } else {
                showError(DataPF.lsp.lsb("common.error").get(), DataPF.lsp.lsb("dialog.edit_project.error.generic").get());
            }
        });
        task.setOnFailed(e -> showError(DataPF.lsp.lsb("common.error").get(), String.valueOf(task.getException())));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("PixelFlow");
        a.setHeaderText(header);
        a.setContentText(content);
        if (dialogStage != null) {
            a.initOwner(dialogStage);
        }
        a.showAndWait();
    }
}
