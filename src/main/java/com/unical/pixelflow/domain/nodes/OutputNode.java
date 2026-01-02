package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.NodeRole;
import com.unical.pixelflow.domain.model.Socket;

import java.awt.image.BufferedImage;

public class OutputNode extends Node {

    public OutputNode() {
        super("Output");
        setRole(NodeRole.OUTPUT);
        // L'OutputNode ha solo 1 Input, nessun Output
        this.inputs.add(new Socket(this, Socket.Type.INPUT));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        // Pass-through: restituisce l'immagine così com'è
        return input;
    }
}
