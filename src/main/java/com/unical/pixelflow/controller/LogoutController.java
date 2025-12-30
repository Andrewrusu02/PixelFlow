package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class LogoutController {

    @FXML private Button btnCancel;

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleLogout() {
        DataPF.dbUser = null;
        DataPF.userDatabase = null;
        closeDialog();
        SceneHandler.getInstance(null).showLoginScreen();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}