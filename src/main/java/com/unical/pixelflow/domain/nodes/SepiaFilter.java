package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class SepiaFilter extends Filter {

    public SepiaFilter() {
        super("Sepia");
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

                int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                tr = Math.min(255, tr);
                tg = Math.min(255, tg);
                tb = Math.min(255, tb);

                int newR = (int) (r + (tr - r) * intensity);
                int newG = (int) (g + (tg - g) * intensity);
                int newB = (int) (b + (tb - b) * intensity);

                int newPixel = (a << 24) | (newR << 16) | (newG << 8) | newB;
                output.setRGB(x, y, newPixel);
            }
        }
        return output;
    }
}
