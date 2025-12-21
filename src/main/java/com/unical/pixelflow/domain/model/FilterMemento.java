package com.unical.pixelflow.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Memento
 */

public class FilterMemento {
    // Salviamo solo i valori primitivi (int, double, etc.) per evitare riferimenti condivisi
    private final Map<String, Object> savedParameters;

    public FilterMemento(Map<String, Parameter<?>> activeParameters) {
        this.savedParameters = new HashMap<>();
        // Facciamo una copia profonda dei valori
        for (Map.Entry<String, Parameter<?>> entry : activeParameters.entrySet()) {
            this.savedParameters.put(entry.getKey(), entry.getValue().getValue());
        }
    }

    public Map<String, Object> getSavedState() {
        return new HashMap<>(savedParameters); // Restituisce una copia difensiva
    }
}

