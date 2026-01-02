package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class InvertFilter extends Filter {

    public InvertFilter() {
        super("Invert");
        this.parameters.put("intensity", new Parameter<>("intensity", 0, 0, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        double intensity = (int) getParameterValue("intensity") / 100.0;
        if (intensity <= 0.01) return input;

        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int targetR = 255 - r;
                int targetG = 255 - g;
                int targetB = 255 - b;

                int newR = (int) (r + (targetR - r) * intensity);
                int newG = (int) (g + (targetG - g) * intensity);
                int newB = (int) (b + (targetB - b) * intensity);

                int newPixel = (a << 24) | (newR << 16) | (newG << 8) | newB;
                output.setRGB(x, y, newPixel);
            }
        }
        return output;
    }
}
