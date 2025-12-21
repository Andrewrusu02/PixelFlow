package com.unical.pixelflow.domain.execution;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Responsabile dell'esecuzione sequenziale della Pipeline.
 * Incapsula la logica di attraversamento del grafo e propagazione dell'immagine.
 */
public class PipelineExecutor {

    /**
     * Esegue la pipeline passata in input.
     * Calcola l'ordine di esecuzione corretto e passa l'immagine da un nodo all'altro.
     *
     * @param pipeline La pipeline da eseguire
     * @return L'immagine finale elaborata, o null se la pipeline è vuota
     */
    public BufferedImage execute(Pipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline cannot be null");
        }

        // 1. Ottieni l'ordine lineare sicuro (Topological Sort)
        // Questo metodo (che hai già fatto) garantisce che i nodi vengano eseguiti
        // solo dopo che i loro predecessori hanno finito.
        List<Node> executionOrder = pipeline.getExecutionOrder();

        if (executionOrder.isEmpty()) {
            return null;
        }

        BufferedImage currentImage = null;

        // 2. Ciclo di elaborazione
        for (Node node : executionOrder) {
            // Log di debug opzionale (utile in sviluppo)
            // System.out.println("Executing node: " + node.getName());

            // Il nodo processa l'immagine corrente e ne restituisce una nuova trasformata.
            // Nota: Se il nodo è InputNode, ignora 'currentImage' (che è null all'inizio)
            // e inietta l'immagine sorgente nel flusso.
            currentImage = node.process(currentImage);
        }

        return currentImage;
    }
}

