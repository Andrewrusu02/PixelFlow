package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.NodeRole;
import com.unical.pixelflow.domain.model.Socket;

import java.awt.image.BufferedImage;

public class InputNode extends Node {
    private BufferedImage sourceImage;

    // Modifica: ora image può essere null
    public InputNode(BufferedImage image) {
        super("Input");
        setRole(NodeRole.INPUT);
        this.sourceImage = image;
        this.outputs.add(new Socket(this, Socket.Type.OUTPUT));
    }

    public void setSourceImage(BufferedImage image) {
        this.sourceImage = image;
    }

    public BufferedImage getSourceImage() {
        return sourceImage;
    }

    @Override
    protected boolean validateInput(BufferedImage input) {
        // Se sourceImage è null (progetto vuoto), il nodo non è valido per l'esecuzione
        return sourceImage != null;
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        return sourceImage;
    }
}