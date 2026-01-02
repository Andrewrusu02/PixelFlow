package com.unical.pixelflow.view;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test per evitare regressioni su FXML: se un fx:id viene rinominato/mancante
 * o se un controller viene troncato, qui fallisce subito.
 */
public class FxmlSmokeTest {

    static {
        // Inizializza JavaFX toolkit in contesto test.
        new JFXPanel();
    }

    @Test
    void nodesViewFxmlLoads() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodesView.fxml"));
        Object root = loader.load();
        assertNotNull(root);
        assertNotNull(loader.getController());
    }
}

