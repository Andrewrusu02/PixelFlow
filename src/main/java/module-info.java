module com.unical.pixelflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.web;
    requires javafx.swing;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires spring.security.crypto;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Exports per JavaFX
    exports com.unical.pixelflow;
    exports com.unical.pixelflow.controller;
    exports com.unical.pixelflow.view;
    exports com.unical.pixelflow.components;
    exports com.unical.pixelflow.model;
    exports com.unical.pixelflow.model.DBObjects;

    // Exports per Jackson (serializzazione .pfp)
    exports com.unical.pixelflow.infrastructure.dto;
    exports com.unical.pixelflow.domain.model;
    exports com.unical.pixelflow.domain.nodes;

    // Opens per JavaFX FXML
    opens com.unical.pixelflow to javafx.fxml;
    opens com.unical.pixelflow.controller to javafx.fxml;
    opens com.unical.pixelflow.view to javafx.fxml;
    opens com.unical.pixelflow.components to javafx.fxml;

    // Opens per Jackson reflection (necessario per serializzazione)
    opens com.unical.pixelflow.infrastructure.dto to com.fasterxml.jackson.databind;
    opens com.unical.pixelflow.domain.model to com.fasterxml.jackson.databind;
    opens com.unical.pixelflow.domain.nodes to com.fasterxml.jackson.databind;
    exports com.unical.pixelflow.popUp;
    opens com.unical.pixelflow.popUp to javafx.fxml;
}