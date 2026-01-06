package com.unical.pixelflow.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe base per nodi che applicano trasformazioni (Filtri).
 * Gestisce una mappa di parametri configurabili.
 */
public abstract class Filter extends Node {

    // Mappa nomeParametro -> Oggetto Parametro
    protected Map<String, Parameter<?>> parameters = new HashMap<>();

    public Filter(String name) {
        super(name);
        // I filtri hanno standard 1 input e 1 output
        this.inputs.add(new Socket(this, Socket.Type.INPUT));
        this.outputs.add(new Socket(this, Socket.Type.OUTPUT));
    }

    public Map<String, Parameter<?>> getParameters() {
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public <T> void setParameterValue(String name, T value) {
        if (parameters.containsKey(name)) {
            ((Parameter<T>) parameters.get(name)).setValue(value);
        } else {
            throw new IllegalArgumentException("Parameter not found: " + name);
        }
    }

    public Object getParameterValue(String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name).getValue();
        }
        return null;
    }

    /**
     * Crea un'istantanea (Memento) dello stato attuale.
     */
    public FilterMemento createMemento() {
        return new FilterMemento(this.parameters);
    }

    /**
     * Ripristina lo stato usando un Memento precedente.
     */
    public void restoreMemento(FilterMemento memento) {
        Map<String, Object> state = memento.getSavedState();

        for (Map.Entry<String, Object> entry : state.entrySet()) {
            String paramName = entry.getKey();
            Object value = entry.getValue();

            // Ripristina il valore solo se il parametro esiste ancora nel filtro
            if (this.parameters.containsKey(paramName)) {
                setParameterValue(paramName, value);
            }
        }
    }



}

