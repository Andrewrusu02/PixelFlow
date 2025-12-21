package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.image.BufferedImage;

public class FlipFilter extends Filter {

    public FlipFilter() {
        super("Flip");
        this.parameters.put("horizontal", new Parameter<>("horizontal", 0, 0, 1));
        this.parameters.put("vertical", new Parameter<>("vertical", 0, 0, 1));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        boolean flipH = ((int) getParameterValue("horizontal")) == 1;
        boolean flipV = ((int) getParameterValue("vertical")) == 1;

        if (!flipH && !flipV) return input;

        int w = input.getWidth();
        int h = input.getHeight();
        BufferedImage output = new BufferedImage(w, h, input.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int srcX = flipH ? (w - 1 - x) : x;
                int srcY = flipV ? (h - 1 - y) : y;

                output.setRGB(x, y, input.getRGB(srcX, srcY));
            }
        }
        return output;
    }
}

