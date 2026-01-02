package com.unical.pixelflow.domain;

import com.unical.pixelflow.domain.model.GraphValidationResult;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class GraphValidationTest {

    @Test
    @DisplayName("Grafo valido: Input -> Filter -> Output")
    void validGraph() {
        Pipeline p = new Pipeline();
        InputNode in = new InputNode(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        BrightnessFilter f = new BrightnessFilter();
        OutputNode out = new OutputNode();

        p.addNode(in);
        p.addNode(f);
        p.addNode(out);
        p.connect(in.getId(), f.getId());
        p.connect(f.getId(), out.getId());

        GraphValidationResult r = p.validateGraph();
        assertTrue(r.isValid());
        assertTrue(r.hasPathInputToOutput());
        assertTrue(r.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Grafo invalido: manca connessione verso Output")
    void invalidGraphMissingOutputConnection() {
        Pipeline p = new Pipeline();
        InputNode in = new InputNode(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        OutputNode out = new OutputNode();

        p.addNode(in);
        p.addNode(out);

        GraphValidationResult r = p.validateGraph();
        assertFalse(r.isValid());
        assertFalse(r.hasPathInputToOutput());
        assertTrue(r.getErrors().contains("Input is not connected"));
        assertTrue(r.getErrors().contains("Output is not connected"));
    }

    @Test
    @DisplayName("Input/Output non devono essere rimossi")
    void structuralNodesNotRemovable() {
        Pipeline p = new Pipeline();
        InputNode in = new InputNode(null);
        OutputNode out = new OutputNode();
        BrightnessFilter f = new BrightnessFilter();

        p.addNode(in);
        p.addNode(out);
        p.addNode(f);

        int before = p.getNodes().size();
        p.removeNode(in.getId());
        p.removeNode(out.getId());
        assertEquals(before, p.getNodes().size());

        p.removeNode(f.getId());
        assertEquals(before - 1, p.getNodes().size());
    }
}

