package com.unical.pixelflow.application;

import com.unical.pixelflow.application.commands.*;
import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.execution.PreviewPipelineExecutor;
import com.unical.pixelflow.domain.model.Filter;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.*;
import com.unical.pixelflow.infrastructure.ImageIOHandler;
import com.unical.pixelflow.infrastructure.ProjectSerializer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AppController {

    private Pipeline pipeline;
    private final UndoManager undoManager;
    private final PipelineExecutor executor;
    private final PreviewPipelineExecutor previewExecutor;
    private final ProjectSerializer serializer;
    private final ImageIOHandler imageHandler;

    public AppController() {
        this.pipeline = new Pipeline();
        this.undoManager = new UndoManager();
        this.executor = new PipelineExecutor();
        this.previewExecutor = new PreviewPipelineExecutor();
        this.serializer = new ProjectSerializer();
        this.imageHandler = new ImageIOHandler();
    }

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

    public void addFilter(String filterType) {
        if (pipeline == null) {
            pipeline = new Pipeline();
        }

        Node newNode = null;
        switch (filterType) {
            case "Brightness": newNode = new BrightnessFilter(); break;
            case "Grayscale": newNode = new GrayscaleFilter(); break;
            case "Blur": newNode = new BlurFilter(); break;
            case "Invert": newNode = new InvertFilter(); break;
            case "Sepia": newNode = new SepiaFilter(); break;
            case "Sharpen": newNode = new SharpenFilter(); break;
            case "Threshold": newNode = new ThresholdFilter(); break;
            case "Contrast": newNode = new ContrastFilter(); break;
            case "EdgeDetection": newNode = new EdgeDetectionFilter(); break;
            case "Pixelate": newNode = new PixelateFilter(); break;
            case "Vignette": newNode = new VignetteFilter(); break;
            case "Solarize": newNode = new SolarizeFilter(); break;
            case "Flip": newNode = new FlipFilter(); break;
            case "Scale": newNode = new ScaleFilter(); break;
            case "Rotate90": newNode = new Rotate90Filter(); break;
            case "Input": newNode = new InputNode(null); break;
            case "Output": newNode = new OutputNode(); break;
            default: break;
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

    public BufferedImage getPreviewForNode(String targetNodeId) {
        if (targetNodeId == null || targetNodeId.isBlank()) {
            throw new IllegalArgumentException("targetNodeId cannot be null/blank");
        }

        InputNode input = pipeline.getNodes().values().stream()
                .filter(n -> n instanceof InputNode)
                .map(n -> (InputNode) n)
                .findFirst()
                .orElse(null);

        if (input == null || input.getSourceImage() == null) {
            return null;
        }

        BufferedImage original = input.getSourceImage();
        BufferedImage proxy = imageHandler.createProxy(original);

        try {
            input.setSourceImage(proxy);
            return previewExecutor.executeUntil(pipeline, targetNodeId);
        } finally {
            input.setSourceImage(original);
        }
    }

    public BufferedImage getPreview() {
        InputNode input = pipeline.getNodes().values().stream()
                .filter(n -> n instanceof InputNode)
                .map(n -> (InputNode) n)
                .findFirst()
                .orElse(null);

        if (input == null || input.getSourceImage() == null) {
            return executor.execute(pipeline);
        }

        BufferedImage original = input.getSourceImage();
        BufferedImage proxy = imageHandler.createProxy(original);

        try {
            input.setSourceImage(proxy);
            return executor.execute(pipeline);
        } finally {
            input.setSourceImage(original);
        }
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public ImageIOHandler getImageHandler() {
        return imageHandler;
    }

    public void toggleNodeEnabled(String nodeId) {
        Node node = pipeline.getNode(nodeId);
        if (node != null) {
            Command cmd = new ToggleNodeCommand(node);
            undoManager.executeCommand(cmd);
        }
    }

    public void duplicateNode(String nodeId) {
        Node originalNode = pipeline.getNode(nodeId);

        if (originalNode == null || originalNode.isStructural()) {
            return;
        }

        try {
            Node newNode = createInstanceFromNode(originalNode);

            if (newNode == null) {
                return;
            }

            newNode.setX(originalNode.getX() + 40);
            newNode.setY(originalNode.getY() + 40);
            newNode.setEnabled(originalNode.isEnabled());

            if (originalNode instanceof Filter && newNode instanceof Filter) {
                Filter originalFilter = (Filter) originalNode;
                Filter newFilter = (Filter) newNode;
                newFilter.restoreMemento(originalFilter.createMemento());
            }

            Command cmd = new AddNodeCommand(pipeline, newNode);
            undoManager.executeCommand(cmd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createInstanceFromNode(Node original) {
        if (original instanceof BrightnessFilter) return new BrightnessFilter();
        if (original instanceof GrayscaleFilter) return new GrayscaleFilter();
        if (original instanceof BlurFilter) return new BlurFilter();
        if (original instanceof InvertFilter) return new InvertFilter();
        if (original instanceof SepiaFilter) return new SepiaFilter();
        if (original instanceof SharpenFilter) return new SharpenFilter();
        if (original instanceof ThresholdFilter) return new ThresholdFilter();
        if (original instanceof ContrastFilter) return new ContrastFilter();
        if (original instanceof EdgeDetectionFilter) return new EdgeDetectionFilter();
        if (original instanceof PixelateFilter) return new PixelateFilter();
        if (original instanceof VignetteFilter) return new VignetteFilter();
        if (original instanceof SolarizeFilter) return new SolarizeFilter();
        if (original instanceof FlipFilter) return new FlipFilter();
        if (original instanceof ScaleFilter) return new ScaleFilter();
        if (original instanceof Rotate90Filter) return new Rotate90Filter();
        return null;
    }

    public void exportImage(File file) throws IOException {
        if (file == null) return;

        BufferedImage resultImage = executor.execute(pipeline);

        if (resultImage == null) {
            throw new IOException("Pipeline result is empty");
        }

        String fileName = file.getName().toLowerCase();
        String format = "png";

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            format = "jpg";
        } else if (fileName.endsWith(".bmp")) {
            format = "bmp";
        } else if (fileName.endsWith(".gif")) {
            format = "gif";
        }

        if (format.equals("jpg") && resultImage.getColorModel().hasAlpha()) {
            resultImage = removeAlphaChannel(resultImage);
        }

        if (!ImageIO.write(resultImage, format, file)) {
            throw new IOException("No writer found for format: " + format);
        }
    }

    private BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }

        BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return target;
    }


}