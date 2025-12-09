module com.unical.pixelflow {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.unical.pixelflow to javafx.fxml;
    exports com.unical.pixelflow;
}