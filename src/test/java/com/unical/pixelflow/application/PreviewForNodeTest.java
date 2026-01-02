package com.unical.pixelflow.application;

import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class PreviewForNodeTest {

    @Test
    void previewForNode_usesProxyAndDoesNotMutateInputSource() {
        AppController app = new AppController();

        // setup minimal pipeline with Input -> Output
        InputNode in = new InputNode(null);
        OutputNode out = new OutputNode();
        app.getPipeline().addNode(in);
        app.getPipeline().addNode(out);
        app.getPipeline().connect(in.getId(), out.getId());

        BufferedImage original = new BufferedImage(2000, 1200, BufferedImage.TYPE_INT_RGB);
        in.setSourceImage(original);

        BufferedImage result = app.getPreviewForNode(out.getId());
        assertNotNull(result);

        // InputNode must still hold the original full-res image after preview.
        assertSame(original, in.getSourceImage());

        // Preview should be based on proxy, so should not exceed proxy max (800).
        assertTrue(result.getWidth() <= 800 && result.getHeight() <= 800,
                "Preview should be proxy-sized (<=800px max side)");
    }
}

