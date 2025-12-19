package com.unical.pixelflow.domain.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe base astratta per tutti i nodi.
 * Implementa il pattern Template Method nel metodo process().
 */
public abstract class Node {
    protected String id;
    protected String name;
    protected double x; // Posizione UI (coordinate nel modello per persistenza)
    protected double y;

    protected List<Socket> inputs = new ArrayList<>();
    protected List<Socket> outputs = new ArrayList<>();

    public Node(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    /**
     * TEMPLATE METHOD: Definisce lo scheletro dell'algoritmo.
     * 1. Valida l'input
     * 2. Applica l'effetto (implementato dalle sottoclassi)
     */
    public final BufferedImage process(BufferedImage input) {
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Invalid input for node " + name);
        }
        return applyEffect(input);
    }

    // Hook method: le sottoclassi possono fare override se servono validazioni specifiche
    protected boolean validateInput(BufferedImage input) {
        return input != null;
    }

    // Metodo astratto che deve essere implementato dai nodi concreti
    protected abstract BufferedImage applyEffect(BufferedImage input);

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // Utile per caricamento
    public String getName() { return name; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public List<Socket> getInputs() { return inputs; }
    public List<Socket> getOutputs() { return outputs; }

    public Socket getInputSocket(int index) { return inputs.get(index); }
    public Socket getOutputSocket(int index) { return outputs.get(index); }
}

