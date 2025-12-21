package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class SharpenFilter extends Filter {

    public SharpenFilter() {
        super("Sharpen");
        this.parameters.put("amount", new Parameter<>("amount", 100, 0, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        double amount = (int) getParameterValue("amount") / 100.0;
        if (amount <= 0.01) return input;

        float[] matrix = {
            0.0f, -1.0f, 0.0f,
           -1.0f,  5.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage sharpened = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );
        op.filter(input, sharpened);

        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgbOrig = input.getRGB(x, y);
                int rgbSharp = sharpened.getRGB(x, y);

                int a = (rgbOrig >> 24) & 0xFF;

                int rO = (rgbOrig >> 16) & 0xFF;
                int gO = (rgbOrig >> 8) & 0xFF;
                int bO = rgbOrig & 0xFF;

                int rS = (rgbSharp >> 16) & 0xFF;
                int gS = (rgbSharp >> 8) & 0xFF;
                int bS = rgbSharp & 0xFF;

                int r = (int) (rO + (rS - rO) * amount);
                int g = (int) (gO + (gS - gO) * amount);
                int b = (int) (bO + (bS - bO) * amount);

                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }
}

