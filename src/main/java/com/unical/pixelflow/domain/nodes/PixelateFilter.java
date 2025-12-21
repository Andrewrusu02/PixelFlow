package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class PixelateFilter extends Filter {

    public PixelateFilter() {
        super("Pixelate");
        this.parameters.put("pixelSize", new Parameter<>("pixelSize", 10, 2, 100));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int pixelSize = (int) getParameterValue("pixelSize");
        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, input.getType());

        for (int y = 0; y < height; y += pixelSize) {
            for (int x = 0; x < width; x += pixelSize) {

                int w = Math.min(pixelSize, width - x);
                int h = Math.min(pixelSize, height - y);

                long sumR = 0, sumG = 0, sumB = 0;

                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int rgb = input.getRGB(x + dx, y + dy);
                        sumR += (rgb >> 16) & 0xFF;
                        sumG += (rgb >> 8) & 0xFF;
                        sumB += rgb & 0xFF;
                    }
                }

                int numPixels = w * h;
                int avgR = (int) (sumR / numPixels);
                int avgG = (int) (sumG / numPixels);
                int avgB = (int) (sumB / numPixels);

                int avgColor = (255 << 24) | (avgR << 16) | (avgG << 8) | avgB;

                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        output.setRGB(x + dx, y + dy, avgColor);
                    }
                }
            }
        }
        return output;
    }
}

