package com.unical.pixelflow.application;

import com.unical.pixelflow.domain.nodes.BlurFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class PreviewForBlurNodeIT {

    @Test
    void previewForBlurNode_appliesEffect() {
        AppController app = new AppController();

        InputNode in = new InputNode(null);
        BlurFilter blur = new BlurFilter();
        OutputNode out = new OutputNode();

        app.getPipeline().addNode(in);
        app.getPipeline().addNode(blur);
        app.getPipeline().addNode(out);

        app.getPipeline().connect(in.getId(), blur.getId());
        app.getPipeline().connect(blur.getId(), out.getId());

        BufferedImage original = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        // crea immagine con un pixel bianco "isolato" su sfondo nero per rendere blur osservabile
        original.setRGB(600, 400, 0xFFFFFF);
        in.setSourceImage(original);

        BufferedImage blurPreview = app.getPreviewForNode(blur.getId());
        assertNotNull(blurPreview);

        // con blur radius default 3 l'immagine in output deve differire dall'input (almeno vicino al pixel)
        int center = blurPreview.getRGB(600 * blurPreview.getWidth() / original.getWidth(),
                400 * blurPreview.getHeight() / original.getHeight());

        // Il pixel centrale non dovrebbe restare bianco puro e l'immagine non dovrebbe essere identica.
        assertNotEquals(0xFFFFFFFF, center);

        // E l'input deve restare originale full-res
        assertSame(original, in.getSourceImage());
    }
}

