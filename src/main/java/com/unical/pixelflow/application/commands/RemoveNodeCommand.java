package com.unical.pixelflow.application.commands;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;

public class RemoveNodeCommand implements Command {
    private final Pipeline pipeline;
    private final Node node;

    public RemoveNodeCommand(Pipeline pipeline, Node node) {
        this.pipeline = pipeline;
        this.node = node;
    }

    @Override
    public void execute() {
        if (pipeline.getNodes().containsKey(node.getId())) {
            pipeline.removeNode(node.getId());
        }
    }

    @Override
    public void undo() {
        pipeline.addNode(node);
    }
}

