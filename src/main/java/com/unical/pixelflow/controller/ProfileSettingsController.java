package com.unical.pixelflow.controller;

import com.unical.pixelflow.components.SwitchTheme;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class ProfileSettingsController {

    @FXML
    private Label EditProfileLabel;

    @FXML
    private Label LabeChangePassword;

    @FXML
    private Label accountTitleLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private PasswordField confirmPass;

    @FXML
    private Label confirmPasswordLabel;

    @FXML
    private ImageView image;

    @FXML
    private ComboBox<?> languageBox;

    @FXML
    private Label languageLabel;

    @FXML
    private PasswordField newPass;

    @FXML
    private PasswordField newPass1;

    @FXML
    private Label newPasswordLabel;

    @FXML
    private Label newPasswordLabel1;

    @FXML
    private Label preferenceLabel;

    @FXML
    private Pane profileImage1;

    @FXML
    private Button saveButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusLabel1;

    @FXML
    private Label themeLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField usernameField1;

    @FXML
    private Label usernameLabel;

    @FXML
    private SwitchTheme themeSwitch;

    @FXML
    void handleSave(ActionEvent event) {

    }



}
