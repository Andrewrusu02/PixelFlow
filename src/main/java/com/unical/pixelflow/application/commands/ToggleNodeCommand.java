package com.unical.pixelflow.application.commands;
import com.unical.pixelflow.domain.model.Node;

public class ToggleNodeCommand implements Command {
    private final Node node;
    private final boolean previousState;

    public ToggleNodeCommand(Node node) {
        this.node = node;
        this.previousState = node.isEnabled();
    }

    @Override
    public void execute() {
        // Inverte lo stato corrente
        node.setEnabled(!previousState);
    }

    @Override
    public void undo() {
        // Ripristina lo stato precedente
        node.setEnabled(previousState);
    }
}