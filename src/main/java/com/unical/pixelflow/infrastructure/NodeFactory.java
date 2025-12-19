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
            case "InvertFilter": return new InvertFilter();
            case "SepiaFilter": return new SepiaFilter();
            case "SharpenFilter": return new SharpenFilter();
            case "ThresholdFilter": return new ThresholdFilter();
            case "ContrastFilter": return new ContrastFilter();
            case "EdgeDetectionFilter": return new EdgeDetectionFilter();
            case "PixelateFilter": return new PixelateFilter();
            case "VignetteFilter": return new VignetteFilter();
            case "SolarizeFilter": return new SolarizeFilter();
            default: throw new IllegalArgumentException("Unknown node type: " + type);
        }
    }
}

