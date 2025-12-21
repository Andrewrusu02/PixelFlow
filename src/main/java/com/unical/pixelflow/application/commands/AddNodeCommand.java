package com.unical.pixelflow.application.commands;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;

public class AddNodeCommand implements Command {
    private final Pipeline pipeline;
    private final Node node;

    public AddNodeCommand(Pipeline pipeline, Node node) {
        this.pipeline = pipeline;
        this.node = node;
    }

    @Override
    public void execute() {
        pipeline.addNode(node);
    }

    @Override
    public void undo() {
        pipeline.removeNode(node.getId());
    }
}

