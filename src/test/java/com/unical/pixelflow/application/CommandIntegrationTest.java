package com.unical.pixelflow.application;

import com.unical.pixelflow.application.commands.AddNodeCommand;
import com.unical.pixelflow.application.commands.ChangeParameterCommand;
import com.unical.pixelflow.application.commands.UndoManager;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandIntegrationTest {

    private Pipeline pipeline;
    private UndoManager undoManager;

    @BeforeEach
    void setUp() {
        pipeline = new Pipeline();
        undoManager = new UndoManager();
    }

    @Test
    @DisplayName("Integrazione Memento+Command: Undo su cambio parametro")
    void testUndoParameterChange() {
        // 1. SETUP: Aggiungiamo un filtro Brightness
        BrightnessFilter filter = new BrightnessFilter();
        undoManager.executeCommand(new AddNodeCommand(pipeline, filter));

        // Verifica stato iniziale (Default 0)
        assertEquals(0, filter.getParameterValue("brightness"));

        // 2. EXECUTE: Cambiamo brightness a 100
        ChangeParameterCommand changeCmd = new ChangeParameterCommand(filter, "brightness", 100);
        undoManager.executeCommand(changeCmd);

        assertEquals(100, filter.getParameterValue("brightness"), "Il valore deve essere 100 dopo execute");

        // 3. UNDO: Torniamo indietro
        // Qui il comando userà il Memento salvato per ripristinare i valori
        undoManager.undo();

        assertEquals(0, filter.getParameterValue("brightness"), "Il valore deve tornare a 0 dopo undo");

        // 4. REDO: Riapplichiamo la modifica
        undoManager.redo();

        assertEquals(100, filter.getParameterValue("brightness"), "Il valore deve tornare a 100 dopo redo");
    }

    @Test
    @DisplayName("UndoManager: Verifica stato canUndo/canRedo")
    void testUndoRedoState() {
        BrightnessFilter filter = new BrightnessFilter();

        // Inizialmente non possiamo fare né undo né redo
        assertFalse(undoManager.canUndo());
        assertFalse(undoManager.canRedo());

        // Eseguiamo un comando
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 50));

        // Ora possiamo fare undo, ma non redo
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());

        // Facciamo undo
        undoManager.undo();

        // Ora possiamo fare redo, ma non undo (stack vuoto)
        assertFalse(undoManager.canUndo());
        assertTrue(undoManager.canRedo());
    }

    @Test
    @DisplayName("Undo/Redo multipli: Catena di modifiche")
    void testMultipleUndoRedo() {
        BrightnessFilter filter = new BrightnessFilter();
        undoManager.executeCommand(new AddNodeCommand(pipeline, filter));

        // Catena di modifiche: 0 -> 25 -> 50 -> 75
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 25));
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 50));
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 75));

        assertEquals(75, filter.getParameterValue("brightness"));

        // Undo x2: 75 -> 50 -> 25
        undoManager.undo();
        assertEquals(50, filter.getParameterValue("brightness"));

        undoManager.undo();
        assertEquals(25, filter.getParameterValue("brightness"));

        // Redo x1: 25 -> 50
        undoManager.redo();
        assertEquals(50, filter.getParameterValue("brightness"));
    }

    @Test
    @DisplayName("Nuova azione cancella il redo stack")
    void testNewActionClearsRedo() {
        BrightnessFilter filter = new BrightnessFilter();

        // Azione 1: 0 -> 50
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 50));

        // Undo: 50 -> 0
        undoManager.undo();
        assertTrue(undoManager.canRedo(), "Dovrebbe esserci redo disponibile");

        // Nuova azione (crea un branch): 0 -> 30
        undoManager.executeCommand(new ChangeParameterCommand(filter, "brightness", 30));

        // Il redo stack deve essere svuotato
        assertFalse(undoManager.canRedo(), "Il redo deve essere cancellato dopo nuova azione");
        assertEquals(30, filter.getParameterValue("brightness"));
    }

    @Test
    @DisplayName("AddNodeCommand: Undo deve rimuovere il nodo")
    void testAddNodeUndo() {
        BrightnessFilter filter = new BrightnessFilter();

        // Verifica che il nodo non esista
        assertNull(pipeline.getNode(filter.getId()));

        // Aggiungi il nodo
        undoManager.executeCommand(new AddNodeCommand(pipeline, filter));
        assertNotNull(pipeline.getNode(filter.getId()), "Il nodo deve esistere dopo execute");

        // Undo: rimuovi il nodo
        undoManager.undo();
        assertNull(pipeline.getNode(filter.getId()), "Il nodo deve essere rimosso dopo undo");

        // Redo: ri-aggiungi il nodo
        undoManager.redo();
        assertNotNull(pipeline.getNode(filter.getId()), "Il nodo deve esistere dopo redo");
    }
}

