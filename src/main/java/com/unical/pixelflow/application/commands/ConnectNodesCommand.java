package com.unical.pixelflow.application.commands;

import com.unical.pixelflow.domain.model.CycleException;
import com.unical.pixelflow.domain.model.Pipeline;

public class ConnectNodesCommand implements Command {
    private final Pipeline pipeline;
    private final String sourceId;
    private final String destId;
    private boolean executedSuccess = false;

    public ConnectNodesCommand(Pipeline pipeline, String sourceId, String destId) {
        this.pipeline = pipeline;
        this.sourceId = sourceId;
        this.destId = destId;
    }

    @Override
    public void execute() {
        try {
            pipeline.connect(sourceId, destId);
            executedSuccess = true;
        } catch (CycleException | IllegalArgumentException e) {
            System.err.println("Errore connessione: " + e.getMessage());
            executedSuccess = false;
        }
    }

    @Override
    public void undo() {
        if (executedSuccess) {
            pipeline.disconnect(sourceId, destId);
        }
    }
}

