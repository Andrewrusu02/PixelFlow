package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class EdgeDetectionFilter extends Filter {

    public EdgeDetectionFilter() {
        super("Edge Detection");
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        float[] matrix = {
            0.0f, -1.0f, 0.0f,
           -1.0f,  4.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };

        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        return op.filter(input, output);
    }
}

