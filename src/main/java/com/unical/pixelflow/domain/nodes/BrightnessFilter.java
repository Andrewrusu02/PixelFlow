package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class BrightnessFilter extends Filter {

    public BrightnessFilter() {
        super("Brightness");
        // Definiamo il parametro 'brightness' con valore default 0, min -100, max 100
        this.parameters.put("brightness", new Parameter<>("brightness", 0, -100, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        // Recupera il valore del parametro
        int brightness = (int) getParameterValue("brightness");

        // Se la luminosità è 0, ottimizzazione: restituisci l'originale
        if (brightness == 0) {
            return input;
        }

        int width = input.getWidth();
        int height = input.getHeight();

        // Crea una nuova immagine per il risultato (non modificare l'originale!)
        BufferedImage output = new BufferedImage(width, height, input.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = input.getRGB(x, y);

                // Estrai canali (supporta anche Alpha se presente)
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Applica luminosità e clamp (limita tra 0 e 255)
                r = clamp(r + brightness);
                g = clamp(g + brightness);
                b = clamp(b + brightness);

                // Ricomponi il pixel
                int newRgb = (a << 24) | (r << 16) | (g << 8) | b;
                output.setRGB(x, y, newRgb);
            }
        }
        return output;
    }

    // Helper per mantenere i valori RGB validi
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

