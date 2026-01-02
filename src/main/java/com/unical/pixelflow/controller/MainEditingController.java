package com.unical.pixelflow.controller;

import com.unical.pixelflow.application.AppController;
import com.unical.pixelflow.domain.model.GraphValidationResult;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import com.unical.pixelflow.infrastructure.ProjectSerializer;
import com.unical.pixelflow.infrastructure.dto.ProjectDTO;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainEditingController {

    @FXML private StackPane CenterNodePane;
    @FXML private BorderPane GraphPane;
    @FXML private BorderPane LeftPane;
    @FXML private VBox LibraryPanelPane;
    @FXML private BorderPane PreviewPane;
    @FXML private BorderPane ProprietiesPane;
    @FXML private BorderPane RighPane;
    @FXML private BorderPane homeViewRoot;
    @FXML private Button browseButton;
    @FXML private Button buttonHome;
    @FXML private TextField pathField;

    private AppController backend;
    private NodesViewController nodesViewController;
    private PreviewPanelController previewPanelController;
    private NodeInfoController nodeInfoController;

    private String currentProjectFilePath;
    private String currentSourceImagePath;
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

        // Progetto "vuoto" appena aperto: vogliamo comunque Input/Output pronti.
        ensureStructuralNodes();
        refreshPreviewAndExportState();
    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/ExitProjectView.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initOwner(buttonHome.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            popupStage.setScene(scene);

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
                // Imposta la source sull'InputNode strutturale (o crealo se mancava)
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

                // Se il progetto è già su disco (.pfp), aggiorniamo subito la sourcePath.
                // Manteniamo l'implementazione minimale: riscriviamo il file .pfp con sourcePath aggiornato.
                if (currentProjectFilePath != null && !currentProjectFilePath.isBlank()) {
                    try {
                        backend.saveProject(currentProjectFilePath, currentSourceImagePath);
                    } catch (Exception ignored) {
                        // backend.saveProject gestisce già internamente le eccezioni
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
            // Se l'executor fallisce (es. input null), gestiamo come placeholder.
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

        // IMPORTANT: se la view è già stata creata, NodesViewController deve puntare al nuovo backend.
        if (nodesViewController != null) {
            nodesViewController.setBackend(backend);
        }

        if (project != null) {
            currentProjectFilePath = project.getPath();
            backend.loadProject(project.getPath());

            // Assicura sempre i nodi pilastro
            ensureStructuralNodes();

            // Dopo load+ensure, riallineiamo ancora la view e forziamo un redraw.
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

                    // Proviamo anche a caricare l'immagine nel nodo Input (se esiste ancora sul FS)
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
        LibraryPanelPane.getChildren().add(libLoader.load());

        FXMLLoader waveLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/WaveFormView.fxml"));
        GraphPane.getChildren().add(waveLoader.load());

        FXMLLoader nodesLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/NodesView.fxml"));
        Parent nodesRoot = nodesLoader.load();
        this.nodesViewController = nodesLoader.getController();
        CenterNodePane.getChildren().add(nodesRoot);

        FXMLLoader prevLoader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/PreviewPanel.fxml"));
        Parent prevRoot = prevLoader.load();
        this.previewPanelController = prevLoader.getController();
        PreviewPane.setCenter(prevRoot);

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
        ProprietiesPane.setCenter(propRoot);

        // Wiring minimo: permettere a NodesViewController di abilitare/disabilitare Export.
        if (nodesViewController != null) {
            nodesViewController.setBackend(backend);
            nodesViewController.setOnNodeSelected(this::refreshPreviewForSelectedNode);
            nodesViewController.setOnSaveRequested(this::handleSaveProject);
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

        // Deselezione: placeholder e reset controlli
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
                }
            });
        });
    }

    private void handleSaveProject() {
        if (backend == null) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, "Salvataggio non riuscito");
            return;
        }
        if (currentProjectFilePath == null || currentProjectFilePath.isBlank()) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, "Salvataggio non riuscito");
            return;
        }

        try {
            new ProjectSerializer().save(backend.getPipeline(), currentSourceImagePath, currentProjectFilePath);
            if (nodesViewController != null) nodesViewController.showSaveToast(true, "Salvataggio completato");
        } catch (Exception ex) {
            if (nodesViewController != null) nodesViewController.showSaveToast(false, "Salvataggio non riuscito");
        }
    }
}
