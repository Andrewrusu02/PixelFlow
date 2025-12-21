package com.unical.pixelflow.domain.model;

/**
 * Rappresenta un parametro configurabile di un filtro.
 * Supporta valori generici (Integer, Double, Boolean, ecc.)
 */
public class Parameter<T> {
    private String name;
    private T value;
    private T minValue; // Opzionale, per slider
    private T maxValue; // Opzionale, per slider

    public Parameter(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public Parameter(String name, T value, T minValue, T maxValue) {
        this.name = name;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getName() { return name; }

    public T getValue() { return value; }

    public void setValue(T value) {
        // Qui si potrebbe aggiungere logica di validazione range
        this.value = value;
    }

    public T getMinValue() { return minValue; }
    public T getMaxValue() { return maxValue; }
}

