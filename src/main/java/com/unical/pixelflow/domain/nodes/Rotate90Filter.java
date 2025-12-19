package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class Rotate90Filter extends Filter {

    public Rotate90Filter() {
        super("Rotate 90");
        this.parameters.put("angle_steps", new Parameter<>("angle_steps", 0, 0, 3));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int steps = (int) getParameterValue("angle_steps");
        if (steps == 0) return input;

        int w = input.getWidth();
        int h = input.getHeight();

        int newW = (steps % 2 == 1) ? h : w;
        int newH = (steps % 2 == 1) ? w : h;

        BufferedImage output = new BufferedImage(newW, newH, input.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = input.getRGB(x, y);

                int destX = x;
                int destY = y;

                switch (steps) {
                    case 1:
                        destX = h - 1 - y;
                        destY = x;
                        break;
                    case 2:
                        destX = w - 1 - x;
                        destY = h - 1 - y;
                        break;
                    case 3:
                        destX = y;
                        destY = w - 1 - x;
                        break;
                }
                output.setRGB(destX, destY, rgb);
            }
        }
        return output;
    }
}

