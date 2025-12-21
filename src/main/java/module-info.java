module com.unical.pixelflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.web;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires spring.security.crypto;
    requires java.net.http;
    exports com.unical.pixelflow.components;
    opens com.unical.pixelflow to javafx.fxml;
    opens com.unical.pixelflow.controller to javafx.fxml;
    opens com.unical.pixelflow.view to javafx.fxml;
    opens com.unical.pixelflow.components to javafx.fxml;
    exports com.unical.pixelflow;
    exports com.unical.pixelflow.controller;
    exports com.unical.pixelflow.view;
    requires com.fasterxml.jackson.databind;
}