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
    private int currentProjectId = -1; // --- NUOVO: Serve per aggiornare il DB ---
    private String lastSelectedNodeId;

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
        javafx.application.Platform.runLater(this::refreshPreviewAndExportState);
    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/ExitProjectView.fxml"));
            Parent root = loader.load();

            ExitProjectController exitController = loader.getController();
            exitController.setOnSaveAction(this::handleSaveProject);

            Stage popupStage = new Stage();
            popupStage.initOwner(buttonHome.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            if (buttonHome.getScene() != null) {
                scene.getStylesheets().addAll(buttonHome.getScene().getStylesheets());
            }            popupStage.setScene(scene);

            showDialogWithBlur(popupStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagine Sorgente");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.bmp")
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

        BufferedImage img = null;
        try {
            img = backend.getPreview();
        } catch (Exception ex) {
            if (previewPanelController != null) {
                previewPanelController.showPlaceholder("Preview non disponibile: " + ex.getMessage());
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, "Preview non disponibile");
            }
            return;
        }

        Image fxImage = (img == null) ? null : SwingFXUtils.toFXImage(img, null);
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
        if (first == null) return "Collega l'Input all'Output per vedere l'anteprima";
        return switch (first) {
            case "Input is not connected" -> "Collega l'Input alla pipeline";
            case "Output is not connected" -> "Collega un nodo all'Output";
            case "No path from Input to Output" -> "Collega l'Input all'Output con una catena valida";
            case "Missing Input node" -> "Nodo Input mancante";
            case "Missing Output node" -> "Nodo Output mancante";
            default -> "Anteprima non disponibile: " + first;
        };
    }

    private String buildExportTooltip(GraphValidationResult r) {
        String msg = buildPreviewErrorMessage(r);
        return msg + " (Export disabilitato)";
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
            currentProjectId = project.getId(); // --- RECUPERIAMO L'ID ---
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
                previewPanelController.showPlaceholder("Seleziona un nodo per vedere l’anteprima");
            }
            if (nodeInfoController != null) {
                nodeInfoController.showNoSelection();
            }
            if (nodesViewController != null) {
                nodesViewController.setExportEnabled(false, "Seleziona un nodo per abilitare l’export");
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
                        previewPanelController.showPlaceholder("Preview non disponibile: " + ex.getMessage());
                    }
                    if (nodesViewController != null) {
                        nodesViewController.setExportEnabled(false, "Preview non disponibile");
                    }
                });
                return;
            }

            Image fxImage = (img == null) ? null : SwingFXUtils.toFXImage(img, null);
            javafx.application.Platform.runLater(() -> {
                if (reqId != previewRequestSeq.get()) return;
                if (previewPanelController == null) return;

                if (fxImage == null) {
                    previewPanelController.showPlaceholder("Preview non disponibile per il nodo selezionato");
                    if (nodesViewController != null) {
                        nodesViewController.setExportEnabled(false, "Preview non disponibile");
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

    // =========================================================
    //  SALVATAGGIO COMPLETO (PFP + THUMBNAIL + DB)
    // =========================================================
    private void handleSaveProject() {
        if (backend == null) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, "Backend non disponibile");
            return;
        }
        if (currentProjectFilePath == null || currentProjectFilePath.isBlank()) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, "Nessun file progetto associato");
            return;
        }

        try {
            // 1. Salva il file .pfp
            new ProjectSerializer().save(backend.getPipeline(), currentSourceImagePath, currentProjectFilePath);

            // 2. Genera o Aggiorna la Thumbnail
            int thumbW = 380;
            int thumbH = 280;

            BufferedImage currentPreview = null;
            try {
                // Tenta di ottenere la preview; se fallisce (es. grafo non valido), currentPreview resta null
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

            // Usa lo stesso nome base del file .pfp (UUID)
            File pfpFile = new File(currentProjectFilePath);
            String rawName = pfpFile.getName().replace(".pfp", "");
            File thumbFile = new File(thumbnailsDir, rawName + ".png");

            ImageIO.write(thumbnailToSave, "png", thumbFile);

            // 3. Aggiorna il Database (con ID valido)
            if (currentProjectId > 0) {
                DataPF.userDatabase.updateProjectThumbnail(currentProjectId, thumbFile.getAbsolutePath());
            }

            if (nodesViewController != null) {
                nodesViewController.showSaveToast(true, "Salvataggio completato");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (nodesViewController != null) {
                nodesViewController.showSaveToast(false, "Salvataggio non riuscito");
            }
        }
    }

    // --- METODI HELPER THUMBNAIL (Copiati da NewProjectController) ---

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

        String text = "Empty Project";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);
        g2d.dispose();
        return img;
    }
}