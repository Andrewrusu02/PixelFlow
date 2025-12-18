module com.unical.pixelflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // AGGIUNTA FONDAMENTALE 1: Serve per vedere il logo SVG nella WebView
    requires javafx.web;

    // AGGIUNTA FONDAMENTALE 2: Serve per il Database SQLite
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires spring.security.crypto;
    requires java.net.http;
    requires org.json; // Se l'IDE non lo trova, prova a togliere questa riga, ma java.sql serve.

    opens com.unical.pixelflow to javafx.fxml;
    opens com.unical.pixelflow.controller to javafx.fxml;
    opens com.unical.pixelflow.view to javafx.fxml; // Hai aggiunto il package view, ottimo.

    exports com.unical.pixelflow;
    exports com.unical.pixelflow.controller;
    exports com.unical.pixelflow.view;
}