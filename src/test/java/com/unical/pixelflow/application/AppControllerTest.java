package com.unical.pixelflow.application;

import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppControllerTest {

    private AppController controller;

    @BeforeEach
    void setUp() {
        controller = new AppController();
    }

    @Test
    @DisplayName("Controller workflow completo: add, undo, redo")
    void testControllerWorkflow() {
        controller.addFilter("Input");
        controller.addFilter("Brightness");
        controller.addFilter("Output");

        assertEquals(3, controller.getPipeline().getNodes().size(),
            "Dovrebbero esserci 3 nodi");

        controller.undo();
        assertEquals(2, controller.getPipeline().getNodes().size(),
            "Undo dovrebbe rimuovere l'ultimo nodo aggiunto");

        controller.redo();
        assertEquals(3, controller.getPipeline().getNodes().size(),
            "Redo dovrebbe ri-aggiungere il nodo");
    }

    @Test
    @DisplayName("Test connessione e disconnessione nodi")
    void testConnectionWorkflow() {
        controller.addFilter("Input");
        controller.addFilter("Brightness");

        String inputId = controller.getPipeline().getNodes().values().stream()
                .filter(n -> n instanceof InputNode)
                .findFirst().get().getId();

        String brightId = controller.getPipeline().getNodes().values().stream()
                .filter(n -> n instanceof BrightnessFilter)
                .findFirst().get().getId();

        controller.connect(inputId, brightId);
        assertTrue(controller.getPipeline().isConnected(inputId, brightId),
            "I nodi dovrebbero essere connessi");

        controller.undo();
        assertFalse(controller.getPipeline().isConnected(inputId, brightId),
            "Undo dovrebbe scollegare i nodi");

        controller.redo();
        assertTrue(controller.getPipeline().isConnected(inputId, brightId),
            "Redo dovrebbe ricollegare i nodi");
    }

    @Test
    @DisplayName("Test rimozione nodo con undo")
    void testRemoveNodeWithUndo() {
        controller.addFilter("Brightness");

        String nodeId = controller.getPipeline().getNodes().values().stream()
                .findFirst().get().getId();

        assertEquals(1, controller.getPipeline().getNodes().size());

        controller.removeNode(nodeId);
        assertEquals(0, controller.getPipeline().getNodes().size(),
            "Il nodo dovrebbe essere rimosso");

        controller.undo();
        assertEquals(1, controller.getPipeline().getNodes().size(),
            "Undo dovrebbe ripristinare il nodo");
    }

    @Test
    @DisplayName("Test modifica parametro con undo")
    void testParameterChangeWithUndo() {
        controller.addFilter("Brightness");

        String nodeId = controller.getPipeline().getNodes().values().stream()
                .findFirst().get().getId();

        controller.updateParameter(nodeId, "brightness", 50);

        BrightnessFilter filter = (BrightnessFilter) controller.getPipeline().getNode(nodeId);
        assertEquals(50, filter.getParameterValue("brightness"));

        controller.undo();
        assertEquals(0, filter.getParameterValue("brightness"),
            "Undo dovrebbe ripristinare il valore originale");
    }

    @Test
    @DisplayName("Test stato canUndo/canRedo")
    void testUndoRedoState() {
        assertFalse(controller.canUndo());
        assertFalse(controller.canRedo());

        controller.addFilter("Input");
        assertTrue(controller.canUndo());
        assertFalse(controller.canRedo());

        controller.undo();
        assertFalse(controller.canUndo());
        assertTrue(controller.canRedo());
    }

    @Test
    @DisplayName("Test disconnessione esplicita")
    void testExplicitDisconnect() {
        controller.addFilter("Input");
        controller.addFilter("Output");

        String inputId = controller.getPipeline().getNodes().values().stream()
                .filter(n -> n instanceof InputNode)
                .findFirst().get().getId();

        String outputId = controller.getPipeline().getNodes().values().stream()
                .filter(n -> n instanceof OutputNode)
                .findFirst().get().getId();

        controller.connect(inputId, outputId);
        assertTrue(controller.getPipeline().isConnected(inputId, outputId));

        controller.disconnect(inputId, outputId);
        assertFalse(controller.getPipeline().isConnected(inputId, outputId),
            "I nodi dovrebbero essere scollegati");

        controller.undo();
        assertTrue(controller.getPipeline().isConnected(inputId, outputId),
            "Undo dovrebbe ricollegare i nodi");
    }

    @Test
    @DisplayName("Test creazione nuovo progetto")
    void testNewProject() {
        controller.addFilter("Input");
        controller.addFilter("Brightness");
        assertEquals(2, controller.getPipeline().getNodes().size());

        controller.createNewProject();
        assertEquals(0, controller.getPipeline().getNodes().size(),
            "Nuovo progetto dovrebbe avere pipeline vuota");
    }
}

