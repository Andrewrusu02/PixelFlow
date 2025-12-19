package com.unical.pixelflow.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.unical.pixelflow.domain.model.*;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.infrastructure.dto.ProjectDTO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProjectSerializer {

    private final ObjectMapper mapper;
    private final ImageIOHandler imageHandler;

    public ProjectSerializer() {
        this.mapper = new ObjectMapper();
        // Rende il JSON leggibile (indentato)
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.imageHandler = new ImageIOHandler();
    }

    /**
     * Salva la pipeline su file.
     */
    public void save(Pipeline pipeline, String sourceImagePath, String destinationPath) throws IOException {
        ProjectDTO dto = new ProjectDTO();
        dto.sourcePath = sourceImagePath;
        dto.nodes = new ArrayList<>();
        dto.connections = new ArrayList<>();

        // 1. Mappa Nodi -> DTO
        for (Node node : pipeline.getNodes().values()) {
            ProjectDTO.NodeDTO nodeDto = new ProjectDTO.NodeDTO();
            nodeDto.id = node.getId();
            nodeDto.type = node.getClass().getSimpleName();
            nodeDto.x = node.getX();
            nodeDto.y = node.getY();

            if (node instanceof Filter) {
                nodeDto.parameters = new HashMap<>();
                Filter filter = (Filter) node;
                // Estrae i valori dai parametri
                filter.getParameters().forEach((k, v) ->
                    nodeDto.parameters.put(k, v.getValue())
                );
            }
            dto.nodes.add(nodeDto);
        }

        // 2. Mappa Connessioni -> DTO
        for (Connection conn : pipeline.getConnections()) {
            ProjectDTO.ConnectionDTO connDto = new ProjectDTO.ConnectionDTO();
            connDto.sourceId = conn.getSource().getOwner().getId();
            connDto.destId = conn.getDestination().getOwner().getId();
            dto.connections.add(connDto);
        }

        // 3. Scrivi su disco
        mapper.writeValue(new File(destinationPath), dto);
    }

    /**
     * Carica una pipeline da file.
     * Nota: Tenta di caricare l'immagine originale specificata nel JSON.
     */
    public Pipeline load(String filePath) throws IOException {
        ProjectDTO dto = mapper.readValue(new File(filePath), ProjectDTO.class);
        Pipeline pipeline = new Pipeline();

        // Tentativo di caricare l'immagine originale (se esiste ancora)
        BufferedImage sourceImg = null;
        try {
            if (dto.sourcePath != null && !dto.sourcePath.isEmpty()) {
                sourceImg = imageHandler.loadImage(dto.sourcePath);
            }
        } catch (IOException e) {
            System.err.println("Attenzione: Impossibile caricare l'immagine originale (" + e.getMessage() + ")");
            // Continuiamo comunque a caricare la struttura dei nodi
        }

        // 1. Ricostruisci Nodi
        for (ProjectDTO.NodeDTO nodeDto : dto.nodes) {
            Node node = NodeFactory.createNode(nodeDto.type);
            node.setId(nodeDto.id);
            node.setX(nodeDto.x);
            node.setY(nodeDto.y);

            if (node instanceof InputNode && sourceImg != null) {
                ((InputNode) node).setSourceImage(sourceImg);
            } else if (node instanceof Filter && nodeDto.parameters != null) {
                Filter filter = (Filter) node;
                nodeDto.parameters.forEach(filter::setParameterValue);
            }
            pipeline.addNode(node);
        }

        // 2. Ricostruisci Connessioni
        if (dto.connections != null) {
            for (ProjectDTO.ConnectionDTO connDto : dto.connections) {
                try {
                    pipeline.connect(connDto.sourceId, connDto.destId);
                } catch (Exception e) {
                    System.err.println("Errore connessione: " + e.getMessage());
                }
            }
        }

        return pipeline;
    }
}

