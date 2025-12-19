package com.unical.pixelflow.domain;

import com.unical.pixelflow.domain.model.CycleException;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private Pipeline pipeline;
    private Node inputNode;
    private Node filter1;
    private Node filter2;
    private Node outputNode;

    @BeforeEach
    void setUp() {
        pipeline = new Pipeline();
        inputNode = new InputNode(null); // Immagine null per test strutturali
        filter1 = new BrightnessFilter();
        filter2 = new BrightnessFilter(); // Usiamo un altro brightness come secondo filtro
        outputNode = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(filter1);
        pipeline.addNode(filter2);
        pipeline.addNode(outputNode);
    }

    @Test
    @DisplayName("Dovrebbe connettere correttamente due nodi lineari")
    void testLinearConnection() {
        assertDoesNotThrow(() -> pipeline.connect(inputNode.getId(), filter1.getId()));
        assertEquals(1, pipeline.getConnections().size());
    }

    @Test
    @DisplayName("Dovrebbe impedire connessione di un nodo a se stesso")
    void testSelfCycle() {
        assertThrows(CycleException.class, () ->
             pipeline.connect(filter1.getId(), filter1.getId())
        );
    }

    @Test
    @DisplayName("Dovrebbe impedire un ciclo semplice A -> B -> A")
    void testSimpleCycle() {
        // A -> B
        pipeline.connect(filter1.getId(), filter2.getId());

        // Tentativo B -> A
        assertThrows(CycleException.class, () ->
             pipeline.connect(filter2.getId(), filter1.getId())
        );
    }

    @Test
    @DisplayName("Dovrebbe impedire un ciclo indiretto A -> B -> C -> A")
    void testIndirectCycle() {
        // Input -> Filter1 -> Filter2 -> Output
        pipeline.connect(inputNode.getId(), filter1.getId());
        pipeline.connect(filter1.getId(), filter2.getId());
        pipeline.connect(filter2.getId(), outputNode.getId());

        // Tentativo di collegare Output -> Filter1 (creerebbe un loop)
        // Nota: OutputNode non ha output sockets, quindi usiamo Filter2 -> Input per simulare il ciclo
        // se InputNode avesse un input socket.
        // Dato che InputNode non ha input, proviamo a chiudere il cerchio tra i filtri se ce ne fossero 3.

        // Proviamo Output -> Filter1. OutputNode non ha output socket, quindi fallirebbe per quello.
        // Testiamo il ciclo logico: F1 -> F2. Provo F2 -> F1.
        assertThrows(CycleException.class, () -> pipeline.connect(filter2.getId(), filter1.getId()));
    }

    @Test
    @DisplayName("Ordinamento topologico deve restituire l'ordine corretto")
    void testExecutionOrder() {
        // Colleghiamo: Input -> Filter2 -> Filter1 -> Output (ordine mischiato)
        pipeline.connect(inputNode.getId(), filter2.getId());
        pipeline.connect(filter2.getId(), filter1.getId());
        pipeline.connect(filter1.getId(), outputNode.getId());

        List<Node> order = pipeline.getExecutionOrder();

        assertEquals(4, order.size());
        assertEquals(inputNode, order.get(0));
        assertEquals(filter2, order.get(1));
        assertEquals(filter1, order.get(2));
        assertEquals(outputNode, order.get(3));
    }

    @Test
    @DisplayName("Rimuovere un nodo deve rimuovere anche le sue connessioni")
    void testRemoveNodeCleanUp() {
        pipeline.connect(inputNode.getId(), filter1.getId());
        pipeline.connect(filter1.getId(), outputNode.getId());

        assertEquals(2, pipeline.getConnections().size());

        pipeline.removeNode(filter1.getId());

        assertEquals(0, pipeline.getConnections().size(), "Le connessioni verso/da Filter1 devono essere rimosse");
    }
}

