package com.unical.pixelflow.controller;

import com.unical.pixelflow.application.AppController;
import com.unical.pixelflow.domain.model.GraphValidationResult;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import com.unical.pixelflow.infrastructure.ProjectSerializer;
import com.unical.pixelflow.infrastructure.dto.ProjectDTO;
import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import com.unical.pixelflow.view.ModalEffects;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainEditingController {

    @FXML private StackPane CenterNodePane;
    @FXML private VBox LeftPane;
    @FXML private VBox RighPane;
    @FXML private Button browseButton;
    @FXML private Button buttonHome;
    @FXML private BorderPane homeViewRoot;
    @FXML private TextField pathField;

    private AppController backend;
    private NodesViewController nodesViewController;
    private PreviewPanelController previewPanelController;
    private NodeInfoController nodeInfoController;
    private WaveFormViewController waveFormViewController;

    private String currentProjectFilePath;
    private String currentSourceImagePath;
    private int currentProjectId = -1;
    private String lastSelectedNodeId;

    private String lastSavedSnapshot;

    private final java.util.concurrent.ExecutorService previewExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "preview-executor");
        t.setDaemon(true);
        return t;
    });

    private final java.util.concurrent.atomic.AtomicLong previewRequestSeq = new java.util.concurrent.atomic.AtomicLong();

    @FXML
    public void initialize() {
        if (this.backend == null) {
            this.backend = new AppController();
        }

        try {
            loadSubViews();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ensureStructuralNodes();
        markProjectAsSaved();
        javafx.application.Platform.runLater(this::refreshPreviewAndExportState);
    }

    @FXML
    private void handleHome() {
        if (!isProjectModified()) {
            goToDashboard();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/ExitProjectView.fxml"));
            Parent root = loader.load();

            ExitProjectController exitController = loader.getController();
            exitController.setOnSaveAction(() -> {
                handleSaveProject();
                markProjectAsSaved();
            });

            Stage popupStage = new Stage();
            popupStage.initOwner(buttonHome.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            if (buttonHome.getScene() != null) {
                scene.getStylesheets().addAll(buttonHome.getScene().getStylesheets());
            }
            popupStage.setScene(scene);

            showDialogWithBlur(popupStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToDashboard() {
        try {
            com.unical.pixelflow.view.SceneHandler.getInstance(null).startPixeFlow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isProjectModified() {
        if (backend == null || backend.getPipeline() == null) return false;
        if (lastSavedSnapshot == null) return true;
        String now = buildProjectSnapshot();
        return !lastSavedSnapshot.equals(now);
    }

    private void markProjectAsSaved() {
        if (backend == null || backend.getPipeline() == null) {
            lastSavedSnapshot = null;
            return;
        }
        lastSavedSnapshot = buildProjectSnapshot();
    }

    private String buildProjectSnapshot() {
        var pipeline = backend.getPipeline();
        if (pipeline == null) return "";

        java.util.List<String> parts = new java.util.ArrayList<>();

        java.util.List<com.unical.pixelflow.domain.model.Node> nodes = new java.util.ArrayList<>(pipeline.getNodes().values());
        nodes.sort(java.util.Comparator.comparing(com.unical.pixelflow.domain.model.Node::getId));

        parts.add("nodes=" + nodes.size());

        for (com.unical.pixelflow.domain.model.Node n : nodes) {
            StringBuilder sb = new StringBuilder();
            sb.append(n.getId()).append('|').append(n.getClass().getSimpleName()).append('|');
            sb.append(java.lang.Double.toString(n.getX())).append('|').append(java.lang.Double.toString(n.getY()));

            if (n instanceof com.unical.pixelflow.domain.model.Filter f) {
                java.util.List<String> keys = new java.util.ArrayList<>(f.getParameters().keySet());
                keys.sort(String::compareTo);
                for (String k : keys) {
                    Object v = f.getParameterValue(k);
                    sb.append('|').append(k).append('=').append(v == null ? "null" : v.toString());
                }
            }

            parts.add(sb.toString());
        }

        return String.join("\n", parts);
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.titleProperty().bind(com.unical.pixelflow.model.DataPF.lsp.lsb("editor.file_chooser.source.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(com.unical.pixelflow.model.DataPF.lsp.lsb("imagefiles").get(), "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            pathField.setText(selectedFile.getName());
            addSourceImageNode(selectedFile);
        }
    }

    private void ensureStructuralNodes() {
        boolean hasInput = backend.getPipeline().getNodes().values().stream().anyMatch(Node::isInput);
        boolean hasOutput = backend.getPipeline().getNodes().values().stream().anyMatch(Node::isOutput);

        if (!hasInput) {
            InputNode in = new InputNode(null);
            in.setX(80);
            in.setY(200);
            backend.getPipeline().addNode(in);
        }

        if (!hasOutput) {
            OutputNode out = new OutputNode();
            out.setX(820);
            out.setY(200);
            backend.getPipeline().addNode(out);
        }
    }

    private void addSourceImageNode(File file) {
        try {
            BufferedImage sourceImage = ImageIO.read(file);

            if (sourceImage != null) {
                InputNode inputNode = backend.getPipeline().getNodes().values().stream()
                        .filter(n -> n instanceof InputNode)
                        .map(n -> (InputNode) n)
                        .findFirst()
                        .orElseGet(() -> {
                            InputNode created = new InputNode(null);
                            backend.getPipeline().addNode(created);
                            return created;
                        });

                inputNode.setSourceImage(sourceImage);
                currentSourceImagePath = file.getAbsolutePath();

                if (previewPanelController != null) {
                    previewPanelController.resetCameraState();
                }

                if (currentProjectFilePath != null && !currentProjectFilePath.isBlank()) {
                    try {
                        backend.saveProject(currentProjectFilePath, currentSourceImagePath);
                    } catch (Exception ignored) {
                    }
                }

                refreshPreviewAndExportState();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshPreviewAndExportState() {
        if (backend == null) return;


        if (lastSelectedNodeId != null && !lastSelectedNodeId.isBlank()) {
            refreshPreviewForSelectedNode(lastSelectedNodeId);
            return;
        }

        GraphValidationResult result = backend.getPipeline().validateGraph();

        if (!result.isValid()) {
            if (previewPanelController != null) {
                previewPanelController.showPlaceholder(buildPreviewErrorMessage(result));
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, buildExportTooltip(result));
            }
            return;
        }

        long reqId = previewRequestSeq.incrementAndGet();

        BufferedImage img;
        try {
            img = backend.getPreview();
        } catch (Exception ex) {
            if (previewPanelController != null) {
                previewPanelController.showPlaceholder(DataPF.lsp.lsb("editor.preview.unavailable").get() + ": " + ex.getMessage());
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, DataPF.lsp.lsb("editor.preview.unavailable.short").get());
            }
            return;
        }

        Image fxImage = (img == null) ? null : SwingFXUtils.toFXImage(img, null);

        if (reqId != previewRequestSeq.get()) return;

        if (previewPanelController != null) {
            previewPanelController.clearPlaceholder();
            previewPanelController.setPreviewImage(fxImage);
            if (waveFormViewController != null) {
                waveFormViewController.updateWaveform(fxImage);
            }
        }
        if (nodesViewController != null) {
            nodesViewController.setExportEnabled(true, null);
        }
    }

    private String buildPreviewErrorMessage(GraphValidationResult r) {
        String first = r.getFirstErrorOrNull();
        if (first == null) return com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.connect_input_output").get();
        return switch (first) {
            case "Input is not connected" -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.input_not_connected").get();
            case "Output is not connected" -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.output_not_connected").get();
            case "No path from Input to Output" -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.no_path").get();
            case "Missing Input node" -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.missing_input").get();
            case "Missing Output node" -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.missing_output").get();
            default -> com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.error.generic").get() + ": " + first;
        };
    }

    private String buildExportTooltip(GraphValidationResult r) {
        String msg = buildPreviewErrorMessage(r);
        return msg + " (" + DataPF.lsp.lsb("editor.export.disabled.suffix").get() + ")";
    }

    public void setProject(ProjectMetadata project) {
        this.backend = new AppController();
        this.currentProjectFilePath = null;
        this.currentSourceImagePath = null;
        this.currentProjectId = -1;

        if (previewPanelController != null) {
            previewPanelController.resetCameraState();
        }

        if (nodesViewController != null) {
            nodesViewController.setBackend(backend);
        }

        if (project != null) {
            currentProjectFilePath = project.getPath();
            currentProjectId = project.getId();
            backend.loadProject(project.getPath());

            ensureStructuralNodes();

            if (nodesViewController != null) {
                nodesViewController.setBackend(backend);
            }

            try {
                ProjectSerializer serializer = new ProjectSerializer();
                ProjectDTO dto = serializer.loadDto(project.getPath());

                if (dto != null && dto.sourcePath != null && !dto.sourcePath.isBlank()) {
                    File sourceFile = new File(dto.sourcePath);
                    currentSourceImagePath = dto.sourcePath;
                    pathField.setText(sourceFile.getName());

                    if (sourceFile.exists()) {
                        try {
                            BufferedImage src = ImageIO.read(sourceFile);
                            if (src != null) {
                                backend.getPipeline().getNodes().values().stream()
                                        .filter(n -> n instanceof InputNode)
                                        .map(n -> (InputNode) n)
                                        .findFirst()
                                        .ifPresent(in -> in.setSourceImage(src));
                            }
                        } catch (IOException ignored) {
                        }
                    }

                } else {
                    pathField.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            markProjectAsSaved();
            refreshPreviewAndExportState();
        }
    }

    private void loadSubViews() throws IOException {
        FXMLLoader libLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/LibraryPanel.fxml"));
        Parent libRoot = libLoader.load();

        FXMLLoader waveLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/WaveFormView.fxml"));
        Parent waveRoot = waveLoader.load();
        this.waveFormViewController = waveLoader.getController();
        LeftPane.getChildren().addAll(libRoot, waveRoot);

        javafx.scene.layout.VBox.setVgrow(libRoot, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(waveRoot, javafx.scene.layout.Priority.NEVER);

        FXMLLoader nodesLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodesView.fxml"));
        Parent nodesRoot = nodesLoader.load();
        this.nodesViewController = nodesLoader.getController();
        CenterNodePane.getChildren().add(nodesRoot);

        FXMLLoader prevLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/PreviewPanel.fxml"));
        Parent prevRoot = prevLoader.load();
        this.previewPanelController = prevLoader.getController();
        RighPane.getChildren().add(prevRoot);

        FXMLLoader propLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodeInfoView.fxml"));
        Parent propRoot = propLoader.load();
        this.nodeInfoController = propLoader.getController();

        if (this.nodeInfoController != null) {
            this.nodeInfoController.setOnParametersChanged(() -> {
                if (lastSelectedNodeId != null && !lastSelectedNodeId.isBlank()) {
                    refreshPreviewForSelectedNode(lastSelectedNodeId);
                }
            });
            this.nodeInfoController.showNoSelection();
        }
        RighPane.getChildren().add(propRoot);
        javafx.scene.layout.VBox.setVgrow(propRoot, javafx.scene.layout.Priority.ALWAYS);

        if (nodesViewController != null) {
            nodesViewController.setBackend(backend);
            nodesViewController.setOnNodeSelected(this::refreshPreviewForSelectedNode);
            nodesViewController.setOnSaveRequested(this::handleSaveProject);
            nodesViewController.setOnGraphModified(this::refreshPreviewAndExportState);

            // Aggiorna Project Overview (nodi/connessioni totali)
            nodesViewController.setOnGraphStatsChanged((nodes, connections) -> {
                if (nodeInfoController != null) {
                    nodeInfoController.setGraphStats(nodes, connections);
                }
            });
        }

    }

    private void showDialogWithBlur(Stage dialogStage) {
        Stage ownerStage = (Stage) buttonHome.getScene().getWindow();
        ModalEffects.ModalOverlay overlay = null;

        if (homeViewRoot != null) {
            overlay = ModalEffects.apply(homeViewRoot);
        }

        ModalEffects.ModalOverlay finalOverlay = overlay;
        dialogStage.setOnHidden(e -> {
            if (finalOverlay != null)
                finalOverlay.remove();
        });

        dialogStage.setOnShown(ev -> {
            double centerX = ownerStage.getX() + (ownerStage.getWidth() - dialogStage.getWidth()) / 2;
            double centerY = ownerStage.getY() + (ownerStage.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);
        });

        dialogStage.showAndWait();
    }

    private void refreshPreviewForSelectedNode(String nodeId) {
        if (backend == null) return;

        lastSelectedNodeId = (nodeId == null || nodeId.isBlank()) ? null : nodeId;

        if (nodeId == null || nodeId.isBlank()) {
            if (previewPanelController != null) {
                previewPanelController.showPlaceholder(com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.placeholder.select_node").get());
            }
            if (nodeInfoController != null) {
                nodeInfoController.showNoSelection();
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.export.disabled.select_node").get());
            }
            return;
        }

        if (nodeInfoController != null) {
            nodeInfoController.showForNode(backend.getPipeline().getNode(nodeId));
        }

        GraphValidationResult result = backend.getPipeline().validateGraph();
        if (!result.isValid()) {
            if (previewPanelController != null) {
                previewPanelController.showPlaceholder(buildPreviewErrorMessage(result));
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, buildExportTooltip(result));
            }
            return;
        }

        long reqId = previewRequestSeq.incrementAndGet();

        previewExecutor.submit(() -> {
            BufferedImage img;
            try {
                img = backend.getPreviewForNode(nodeId);
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    if (reqId != previewRequestSeq.get()) return;
                    if (previewPanelController != null) {
                        previewPanelController.showPlaceholder(com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.unavailable").get() + ": " + ex.getMessage());
                    }
                    if (nodesViewController != null) {
                        nodesViewController.setExportEnabled(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.unavailable.short").get());
                    }
                });
                return;
            }

            Image fxImage = (img == null) ? null : SwingFXUtils.toFXImage(img, null);
            javafx.application.Platform.runLater(() -> {
                if (reqId != previewRequestSeq.get()) return;
                if (previewPanelController == null) return;

                if (fxImage == null) {
                    previewPanelController.showPlaceholder(com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.unavailable.for_node").get());
                    if (nodesViewController != null) {
                        nodesViewController.setExportEnabled(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.preview.unavailable.short").get());
                    }
                } else {
                    previewPanelController.clearPlaceholder();
                    previewPanelController.setPreviewImage(fxImage);
                    if (nodesViewController != null) {
                        nodesViewController.setExportEnabled(true, null);
                    }
                    if (waveFormViewController != null) {
                        waveFormViewController.updateWaveform(fxImage);
                    }
                }
            });
        });

    }

    private void handleSaveProject() {
        if (backend == null) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.save.backend_unavailable").get());
            return;
        }
        if (currentProjectFilePath == null || currentProjectFilePath.isBlank()) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.save.no_project_file").get());
            return;
        }

        try {
            // 1. Salva il file .pfp
            new ProjectSerializer().save(backend.getPipeline(), currentSourceImagePath, currentProjectFilePath);

            int thumbW = 380;
            int thumbH = 280;

            BufferedImage currentPreview = null;
            try {
                currentPreview = backend.getPreview();
            } catch (Exception ignored) {}

            BufferedImage thumbnailToSave;
            if (currentPreview != null) {
                thumbnailToSave = createThumbnail(currentPreview, thumbW, thumbH);
            } else {
                thumbnailToSave = createDefaultThumbnail(thumbW, thumbH);
            }

            File thumbnailsDir = new File("Data/thumbnails");
            if (!thumbnailsDir.exists()) thumbnailsDir.mkdirs();

            File pfpFile = new File(currentProjectFilePath);
            String rawName = pfpFile.getName().replace(".pfp", "");
            File thumbFile = new File(thumbnailsDir, rawName + ".png");

            ImageIO.write(thumbnailToSave, "png", thumbFile);

            if (currentProjectId > 0) {
                DataPF.userDatabase.updateProjectThumbnail(currentProjectId, thumbFile.getAbsolutePath());
            }

            if (nodesViewController != null) {
                nodesViewController.showSaveToast(true, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.save.success").get());
            }
            markProjectAsSaved();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (nodesViewController != null) {
                nodesViewController.showSaveToast(false, com.unical.pixelflow.model.DataPF.lsp.lsb("editor.save.failed").get());
            }
        }
    }

    private BufferedImage createThumbnail(BufferedImage source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double scaleX = (double) targetWidth / sourceWidth;
        double scaleY = (double) targetHeight / sourceHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) Math.round(sourceWidth * scale);
        int scaledHeight = (int) Math.round(sourceHeight * scale);

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = thumbnail.createGraphics();

        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        g2d.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return thumbnail;
    }

    private BufferedImage createDefaultThumbnail(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = img.createGraphics();

        g2d.setColor(new java.awt.Color(30, 30, 30));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(java.awt.Color.GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        String text = DataPF.lsp.lsb("editor.thumbnail.empty").get();
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);
        g2d.dispose();
        return img;
    }
}

