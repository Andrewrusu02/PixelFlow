package com.unical.pixelflow.components;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class SingleFilterViewController {

    @FXML private HBox root;
    @FXML private SVGPath iconFilter;
    @FXML private Label labelNameFilter;

    private String nodeType;

    public void setData(String name, String svgContent, String nodeType) {
        this.labelNameFilter.setText(name);
        this.nodeType = nodeType;

        if (svgContent != null && !svgContent.isEmpty()) {
            this.iconFilter.setContent(svgContent);
        }

        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        root.setOnDragDetected(event -> {
            Dragboard db = root.startDragAndDrop(TransferMode.COPY);

            ClipboardContent content = new ClipboardContent();
            content.putString(nodeType);
            db.setContent(content);

            HBox dummyNode = new HBox(15);
            dummyNode.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            dummyNode.setPadding(new javafx.geometry.Insets(0, 20, 0, 20));
            dummyNode.setPrefSize(270, 50);
            // Copiamo lo stile di sfondo (assicurati che il colore corrisponda al tuo tema)
            dummyNode.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5; -fx-border-color: #444; -fx-border-radius: 5;");

            // Ricreiamo l'icona per la foto
            SVGPath dummyIcon = new SVGPath();
            dummyIcon.setContent(iconFilter.getContent());
            dummyIcon.setFill(javafx.scene.paint.Color.WHITE);
            dummyIcon.setScaleX(1.2);
            dummyIcon.setScaleY(1.2);

            // Ricreiamo il testo per la foto
            Label dummyLabel = new Label(labelNameFilter.getText());
            dummyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

            dummyNode.getChildren().addAll(dummyIcon, dummyLabel);

            // 3. Facciamo lo snapshot del nodo fantasma (che non è nella scena!)
            // Questo garantisce che la tua finestra principale NON POSSA ridimensionarsi.
            Scene offScreenScene = new Scene(dummyNode); // Serve una scena temporanea per il rendering corretto
            offScreenScene.setFill(Color.TRANSPARENT);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            WritableImage snapshot = dummyNode.snapshot(params, null);

            // 4. Imposta l'immagine
            db.setDragView(snapshot);
            db.setDragViewOffsetX(snapshot.getWidth() / 2);
            db.setDragViewOffsetY(snapshot.getHeight() / 2);

            event.consume();
        });
    }
}