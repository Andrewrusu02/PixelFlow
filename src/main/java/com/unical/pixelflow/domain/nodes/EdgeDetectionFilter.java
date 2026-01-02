package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class EdgeDetectionFilter extends Filter {

    public EdgeDetectionFilter() {
        super("Edge Detection");
        this.parameters.put("intensity", new com.unical.pixelflow.domain.model.Parameter<>("intensity", 0, 0, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        double intensity = (int) getParameterValue("intensity") / 100.0;
        if (intensity <= 0.01) return input;

        float[] matrix = {
            0.0f, -1.0f, 0.0f,
            -1.0f,  4.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };

        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage edges = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        op.filter(input, edges);

        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int orig = input.getRGB(x, y);
                int ed = edges.getRGB(x, y);

                int a = (orig >> 24) & 0xFF;
                int rO = (orig >> 16) & 0xFF;
                int gO = (orig >> 8) & 0xFF;
                int bO = orig & 0xFF;

                int rE = (ed >> 16) & 0xFF;
                int gE = (ed >> 8) & 0xFF;
                int bE = ed & 0xFF;

                int r = (int) (rO + (rE - rO) * intensity);
                int g = (int) (gO + (gE - gO) * intensity);
                int b = (int) (bO + (bE - bO) * intensity);

                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return output;
    }
}
