package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ExitProjectController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Label warningLabel;

    @FXML private Button btnCancel;
    @FXML private Button btnDontSave;
    @FXML private Button btnSave;

    private Runnable onSaveAction;

    public void setOnSaveAction(Runnable onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    @FXML
    private void initialize() {
        if (titleLabel != null) titleLabel.textProperty().bind(DataPF.lsp.lsb("editor.exit.title"));
        if (messageLabel != null) messageLabel.textProperty().bind(DataPF.lsp.lsb("editor.exit.message"));
        if (warningLabel != null) warningLabel.textProperty().bind(DataPF.lsp.lsb("editor.exit.warning"));

        if (btnCancel != null) btnCancel.textProperty().bind(DataPF.lsp.lsb("common.cancel"));
        if (btnDontSave != null) btnDontSave.textProperty().bind(DataPF.lsp.lsb("editor.exit.dont_save"));
        if (btnSave != null) btnSave.textProperty().bind(DataPF.lsp.lsb("common.save"));
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleDontSave() {
        closeDialog();
        goToHome();
    }

    @FXML
    private void handleSaveAndExit() {
        // Esegue il metodo handleSaveProject() del MainEditingController
        // che ora gestisce correttamente file .pfp, thumbnail e database.
        if (onSaveAction != null) {
            onSaveAction.run();
        }
        closeDialog();
        goToHome();
    }

    private void goToHome() {
        try {
            SceneHandler.getInstance(null).startPixeFlow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}