package com.unical.pixelflow.domain.model;

/**
 * Eccezione lanciata quando un tentativo di connessione creerebbe un ciclo (loop infinito) nel grafo.
 */
public class CycleException extends RuntimeException {
    public CycleException(String message) {
        super(message);
    }
}

