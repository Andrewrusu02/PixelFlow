package com.unical.pixelflow.controller;

import com.unical.pixelflow.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class ExitProjectController {

    @FXML private Button btnCancel;

    private Runnable onSaveAction;

    public void setOnSaveAction(Runnable onSaveAction) {
        this.onSaveAction = onSaveAction;
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
        if (onSaveAction != null) {
            onSaveAction.run();
        }
        closeDialog();
        goToHome();
    }

    private void goToHome() {
        SceneHandler.getInstance(null).startPixeFlow();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
