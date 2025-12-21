package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class ThresholdFilter extends Filter {

    public ThresholdFilter() {
        super("Threshold");
        this.parameters.put("threshold", new Parameter<>("threshold", 128, 0, 255));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int threshold = (int) getParameterValue("threshold");
        BufferedImage output = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgb = input.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;

                int newColor = (brightness > threshold) ? 0xFFFFFF : 0x000000;

                int a = (rgb >> 24) & 0xFF;
                output.setRGB(x, y, (a << 24) | newColor);
            }
        }
        return output;
    }
}

