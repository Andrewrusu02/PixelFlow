package com.unical.pixelflow.controller;

import com.unical.pixelflow.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class ExitProjectController {

    @FXML private Button btnCancel;

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
        // TODO: Chiama il metodo di salvataggio del progetto
        closeDialog();
        goToHome();
    }

    private void goToHome() {
        System.out.println("Torno alla Home...");
        SceneHandler.getInstance(null).startPixeFlow();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}