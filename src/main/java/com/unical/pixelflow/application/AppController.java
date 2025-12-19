package com.unical.pixelflow.application;

import com.unical.pixelflow.application.commands.*;
import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.*;
import com.unical.pixelflow.infrastructure.ImageIOHandler;
import com.unical.pixelflow.infrastructure.ProjectSerializer;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class AppController {

    private Pipeline pipeline;
    private final UndoManager undoManager;
    private final PipelineExecutor executor;
    private final ProjectSerializer serializer;
    private final ImageIOHandler imageHandler;

    public AppController() {
        this.pipeline = new Pipeline();
        this.undoManager = new UndoManager();
        this.executor = new PipelineExecutor();
        this.serializer = new ProjectSerializer();
        this.imageHandler = new ImageIOHandler();
    }

    // ================== GESTIONE PROGETTO ==================

    public void createNewProject() {
        this.pipeline = new Pipeline();
    }

    public void loadProject(String filePath) {
        try {
            this.pipeline = serializer.load(filePath);
            System.out.println("Progetto caricato: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore caricamento: " + e.getMessage());
        }
    }

    public void saveProject(String filePath, String sourceImagePath) {
        try {
            serializer.save(pipeline, sourceImagePath, filePath);
            System.out.println("Progetto salvato: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore salvataggio: " + e.getMessage());
        }
    }

    // ================== AZIONI UTENTE (COMMANDS) ==================

    public void addFilter(String filterType) {
        Node newNode = null;
        switch (filterType) {
            case "Brightness": newNode = new BrightnessFilter(); break;
            case "Grayscale": newNode = new GrayscaleFilter(); break;
            case "Blur": newNode = new BlurFilter(); break;
            case "Input": newNode = new InputNode(null); break;
            case "Output": newNode = new OutputNode(); break;
        }

        if (newNode != null) {
            newNode.setX(50);
            newNode.setY(50);

            Command cmd = new AddNodeCommand(pipeline, newNode);
            undoManager.executeCommand(cmd);
        }
    }

    public void removeNode(String nodeId) {
        Node node = pipeline.getNode(nodeId);
        if (node != null) {
            Command cmd = new RemoveNodeCommand(pipeline, node);
            undoManager.executeCommand(cmd);
        }
    }

    public void connect(String sourceId, String destId) {
        Command cmd = new ConnectNodesCommand(pipeline, sourceId, destId);
        undoManager.executeCommand(cmd);
    }

    public void disconnect(String sourceId, String destId) {
        Command cmd = new DisconnectNodesCommand(pipeline, sourceId, destId);
        undoManager.executeCommand(cmd);
    }

    public void updateParameter(String nodeId, String paramName, Object value) {
        Node node = pipeline.getNode(nodeId);
        if (node instanceof Filter) {
            Command cmd = new ChangeParameterCommand((Filter) node, paramName, value);
            undoManager.executeCommand(cmd);
        }
    }

    // ================== CONTROLLO UNDO/REDO ==================

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    // ================== ESECUZIONE & VIEW ==================

    public BufferedImage getPreview() {
        return executor.execute(pipeline);
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public ImageIOHandler getImageHandler() {
        return imageHandler;
    }
}

