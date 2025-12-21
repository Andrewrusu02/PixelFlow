package com.unical.pixelflow.domain.nodes;

import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Parameter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ScaleFilter extends Filter {

    public ScaleFilter() {
        super("Scale");
        this.parameters.put("percent", new Parameter<>("percent", 100, 10, 200));
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage input) {
        int percent = (int) getParameterValue("percent");

        if (percent == 100) return input;

        double scale = percent / 100.0;
        int newW = (int) (input.getWidth() * scale);
        int newH = (int) (input.getHeight() * scale);

        newW = Math.max(1, newW);
        newH = Math.max(1, newH);

        BufferedImage output = new BufferedImage(newW, newH, input.getType());
        Graphics2D g2d = output.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(input, 0, 0, newW, newH, null);
        g2d.dispose();

        return output;
    }
}

