package com.unical.pixelflow.popUp;

import com.unical.pixelflow.model.DataPF;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDeleteController {

    @FXML
    private Label messageLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label warningLabel;

    @FXML
    private javafx.scene.control.Button btnCancel;

    @FXML
    private javafx.scene.control.Button btnConfirm;

    private Stage dialogStage;
    private boolean confirmed;

    private final StringProperty projectName = new SimpleStringProperty("");

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName == null ? "" : projectName);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.textProperty().bind(DataPF.lsp.lsb("dialog.delete_project.title"));
        }
        if (warningLabel != null) {
            warningLabel.textProperty().bind(DataPF.lsp.lsb("dialog.delete_project.warning"));
        }
        if (messageLabel != null) {
            messageLabel.textProperty().bind(DataPF.lsp.lsb("dialog.delete_project.message", projectName));
        }

        if (btnCancel != null) {
            btnCancel.textProperty().bind(DataPF.lsp.lsb("common.cancel"));
            btnCancel.setOnAction(e -> handleCancel());
        }
        if (btnConfirm != null) {
            btnConfirm.textProperty().bind(DataPF.lsp.lsb("common.delete"));
            btnConfirm.setOnAction(e -> handleConfirm());
        }
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
