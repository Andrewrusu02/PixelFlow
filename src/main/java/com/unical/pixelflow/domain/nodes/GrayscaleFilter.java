package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class GrayscaleFilter extends Filter {

    public GrayscaleFilter() {
        super("Grayscale");
        // Default 100%: i test assumono che un GrayscaleFilter istanziato applichi la conversione.
        this.parameters.put("intensity", new Parameter<>("intensity", 100, 0, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        double intensity = (int) getParameterValue("intensity") / 100.0;

        if (intensity <= 0.0) return input;

        BufferedImage result = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgb = input.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                int newR = (int) (r + (gray - r) * intensity);
                int newG = (int) (g + (gray - g) * intensity);
                int newB = (int) (b + (gray - b) * intensity);

                int newPixel = (a << 24) | (newR << 16) | (newG << 8) | newB;

                result.setRGB(x, y, newPixel);
            }
        }
        return result;
    }
}
