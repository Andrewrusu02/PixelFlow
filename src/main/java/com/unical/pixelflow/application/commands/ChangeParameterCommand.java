package com.unical.pixelflow.application.commands;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.FilterMemento;

public class ChangeParameterCommand implements Command {
    private final Filter filter;
    private final String paramName;
    private final Object newValue;

    // Qui conserviamo il Memento creato PRIMA dell'esecuzione
    private FilterMemento previousState;

    public ChangeParameterCommand(Filter filter, String paramName, Object newValue) {
        this.filter = filter;
        this.paramName = paramName;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        // 1. Salva lo stato attuale prima di modificarlo
        this.previousState = filter.createMemento();

        // 2. Applica la modifica
        filter.setParameterValue(paramName, newValue);
    }

    @Override
    public void undo() {
        // 3. Ripristina lo stato precedente
        if (previousState != null) {
            filter.restoreMemento(previousState);
        }
    }
}

