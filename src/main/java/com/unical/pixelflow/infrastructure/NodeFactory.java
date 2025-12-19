package com.unical.pixelflow.infrastructure;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.nodes.*;

public class NodeFactory {
    public static Node createNode(String type) {
        switch (type) {
            case "InputNode": return new InputNode(null);
            case "OutputNode": return new OutputNode();
            case "BrightnessFilter": return new BrightnessFilter();
            case "GrayscaleFilter": return new GrayscaleFilter();
            case "BlurFilter": return new BlurFilter();
            default: throw new IllegalArgumentException("Unknown node type: " + type);
        }
    }
}

