package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

public class BlurFilter extends Filter {

    public BlurFilter() {
        super("Blur");
        // Parametro raggio: default 3, min 1, max 10
        // Nota: valori troppo alti rallentano molto senza ottimizzazioni
        this.parameters.put("radius", new Parameter<>("radius", 3, 1, 10));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int radius = (int) getParameterValue("radius");

        // Se raggio <= 1, nessun effetto visibile
        if (radius <= 1) return input;

        // Calcolo della dimensione della matrice di convoluzione (Kernel)
        // Deve essere dispari: es. raggio 1 -> 3x3, raggio 2 -> 5x5
        int size = radius * 2 + 1;
        int length = size * size;

        // Creiamo una matrice dove ogni cella ha peso uguale (Box Blur)
        // Per una sfocatura più bella si userebbe una Gaussiana, ma questa è ok per ora.
        float[] data = new float[length];
        float weight = 1.0f / length;
        Arrays.fill(data, weight);

        // Applica l'operazione di convoluzione
        Kernel kernel = new Kernel(size, size, data);

        // EDGE_NO_OP significa che i bordi dell'immagine non vengono processati
        // (per evitare crash sui pixel esterni).
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        // ConvolveOp richiede che l'immagine di destinazione sia compatibile
        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        return op.filter(input, output);
    }
}

