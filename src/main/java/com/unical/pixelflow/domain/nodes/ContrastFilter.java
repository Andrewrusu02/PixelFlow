package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class ContrastFilter extends Filter {

    public ContrastFilter() {
        super("Contrast");
        this.parameters.put("amount", new Parameter<>("amount", 100, 0, 200));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        double amount = (int) getParameterValue("amount");
        double factor = (259 * (amount + 255)) / (255 * (259 - amount));

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

                r = clamp((int)(factor * (r - 128) + 128));
                g = clamp((int)(factor * (g - 128) + 128));
                b = clamp((int)(factor * (b - 128) + 128));

                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }

    private int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }
}

