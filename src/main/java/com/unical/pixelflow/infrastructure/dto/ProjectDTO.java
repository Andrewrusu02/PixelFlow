package com.unical.pixelflow.infrastructure.dto;

import java.util.List;
import java.util.Map;

/**
 * Questa classe definisce la struttura esatta del file .pfp (JSON).
 */
public class ProjectDTO {
    public String version = "1.0";
    // Il percorso dell'immagine usata quando è stato salvato il progetto
    public String sourcePath;
    public List<NodeDTO> nodes;
    public List<ConnectionDTO> connections;

    public static class NodeDTO {
        public String id;
        public String type; // Es: "BrightnessFilter"
        public double x;
        public double y;
        public Map<String, Object> parameters;
    }

    public static class ConnectionDTO {
        public String sourceId;
        public String destId;
    }
}

