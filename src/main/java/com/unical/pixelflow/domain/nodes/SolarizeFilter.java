package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class SolarizeFilter extends Filter {

    public SolarizeFilter() {
        super("Solarize");
        this.parameters.put("threshold", new Parameter<>("threshold", 128, 0, 255));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int threshold = (int) getParameterValue("threshold");

        if (threshold >= 255) return input;

        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgb = input.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r > threshold) r = 255 - r;
                if (g > threshold) g = 255 - g;
                if (b > threshold) b = 255 - b;

                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }
}

