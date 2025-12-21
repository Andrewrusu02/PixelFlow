package com.unical.pixelflow.application.commands;

import com.unical.pixelflow.domain.model.Pipeline;

public class DisconnectNodesCommand implements Command {
    private final Pipeline pipeline;
    private final String sourceId;
    private final String destId;
    private boolean wasConnected = false;

    public DisconnectNodesCommand(Pipeline pipeline, String sourceId, String destId) {
        this.pipeline = pipeline;
        this.sourceId = sourceId;
        this.destId = destId;
    }

    @Override
    public void execute() {
        wasConnected = pipeline.isConnected(sourceId, destId);
        if (wasConnected) {
            pipeline.disconnect(sourceId, destId);
        }
    }

    @Override
    public void undo() {
        if (wasConnected) {
            pipeline.connect(sourceId, destId);
        }
    }
}

