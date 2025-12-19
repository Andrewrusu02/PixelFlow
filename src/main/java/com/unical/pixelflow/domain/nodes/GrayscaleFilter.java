package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import java.awt.image.BufferedImage;

public class GrayscaleFilter extends Filter {

    public GrayscaleFilter() {
        super("Grayscale");
        // Nessun parametro necessario per una conversione standard
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        BufferedImage result = new BufferedImage(
            input.getWidth(), input.getHeight(), input.getType()
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int rgb = input.getRGB(x, y);

                // Estrazione componenti
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Formula standard per la luminanza (percepita dall'occhio umano)
                // oppure media semplice (r+g+b)/3
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Ricostruzione pixel grigio
                int newPixel = (a << 24) | (gray << 16) | (gray << 8) | gray;

                result.setRGB(x, y, newPixel);
            }
        }
        return result;
    }
}

