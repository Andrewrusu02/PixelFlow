package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Socket;
import java.awt.image.BufferedImage;

public class InputNode extends Node {
    private BufferedImage sourceImage;

    public InputNode(BufferedImage image) {
        super("Input");
        this.sourceImage = image;
        // L'InputNode ha solo 1 Output, nessun Input
        this.outputs.add(new Socket(this, Socket.Type.OUTPUT));
    }

    public void setSourceImage(BufferedImage image) {
        this.sourceImage = image;
    }

    @Override
    protected boolean validateInput(BufferedImage input) {
        // Override critico: L'InputNode non ha bisogno di un input valido dal nodo precedente
        // (che sarà null), ma deve avere una sourceImage interna valida.
        return sourceImage != null;
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        // Ignora l'input (che è null) e restituisce la sorgente
        return sourceImage;
    }
}

