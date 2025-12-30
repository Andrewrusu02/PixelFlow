package com.unical.pixelflow.controller;

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
        // TODO: Inserisci qui la logica di logout (es. clear sessione DataPF)
        System.out.println("Logout effettuato");

        closeDialog();

        SceneHandler.getInstance(null).showLoginScreen();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}