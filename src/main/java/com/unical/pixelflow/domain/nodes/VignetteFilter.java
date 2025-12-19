package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class VignetteFilter extends Filter {

    public VignetteFilter() {
        super("Vignette");
        this.parameters.put("intensity", new Parameter<>("intensity", 50, 0, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int intensity = (int) getParameterValue("intensity");

        if (intensity == 0) return input;

        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        double maxDist = Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = input.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                double dx = x - centerX;
                double dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);

                double factor = 1.0 - (dist / maxDist) * (intensity / 100.0);

                factor = Math.max(0.0, Math.min(1.0, factor));

                r = (int) (r * factor);
                g = (int) (g * factor);
                b = (int) (b * factor);

                output.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }
}

