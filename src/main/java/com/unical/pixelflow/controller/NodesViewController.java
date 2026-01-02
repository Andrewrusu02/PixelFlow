package com.unical.pixelflow.controller;

import com.unical.pixelflow.application.AppController;
import com.unical.pixelflow.components.NodeWidget;
import com.unical.pixelflow.domain.model.Node;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;

public class NodesViewController {

    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 4.0;

    // Debug: abilita log dettagliati sugli edge (disattivato di default)
    private static final boolean DEBUG_EDGES = false;

    private static final double GRID_STEP = 40.0;
    private static final int GRID_MAJOR_EVERY = 5;

    private static final double NODE_WIDGET_WIDTH = 180;
    private static final double NODE_WIDGET_HEIGHT = 110;

    private enum ToolMode {
        PAN,
        MOVE_NODES
    }

    private ToolMode toolMode = ToolMode.PAN;

    private double zoom = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private boolean gridDirty = true;
    private boolean isPanning = false;
    private Point2D panStartScene;
    private double panStartOffsetX;
    private double panStartOffsetY;

    private boolean isDraggingNode = false;
    private String draggingNodeId = null;
    private Point2D dragStartWorld;
    private Point2D dragStartNodePos;

    private final Affine worldToScreen = new Affine();

    @FXML private Button copyButton;
    @FXML private Button deleteButton;
    @FXML private Button disableButton;
    @FXML private ToggleButton moveButton;
    @FXML private Button redoButton;
    @FXML private Button saveProjectButton;
    @FXML private Button undoButton;
    @FXML private Button exportButton;

    @FXML private Pane canvas;
    @FXML private SVGPath hideShowIcon;
    @FXML private StackPane root;
    @FXML private ScrollPane scrollPane;

    @FXML private StackPane playground;
    @FXML private Canvas gridCanvas;
    @FXML private Pane inputOverlay;

    @FXML private javafx.scene.layout.HBox viewportControls;

    @FXML private Button zoomIndicatorButton;
    @FXML private Button resetViewButton;

    @FXML private Pane edgesLayer;

    private final java.util.Map<String, javafx.scene.Node> nodeViewsById = new java.util.HashMap<>();
    private final java.util.Map<String, NodeWidget> nodeWidgetById = new java.util.HashMap<>();

    // Connessione in corso (rubber-band)
    private boolean isConnecting = false;
    private String connectingSourceNodeId = null;
    private final EdgeView rubberBand = new EdgeView();

    // Target socket attualmente sotto al cursore durante una connessione.
    private String hoveredTargetNodeId = null;

    private Scene installedGlobalScene;

    private AppController backend;

    public void setBackend(AppController backend) {
        this.backend = backend;
        redrawNodesFromPipeline();
        redrawEdgesFromPipeline();
    }

    @FXML
    private void initialize() {
        if (inputOverlay != null) {
            // L'overlay deve lasciare passare gli eventi ai nodi nel canvas.
            inputOverlay.setMouseTransparent(true);
            inputOverlay.setPickOnBounds(true);
        }

        scrollPane.viewportBoundsProperty().addListener((obs, oldB, newB) -> {
            if (newB == null) return;
            gridCanvas.setWidth(Math.max(1, newB.getWidth()));
            gridCanvas.setHeight(Math.max(1, newB.getHeight()));
            gridDirty = true;
        });

        installInputHandlers();
        bindToolMode();
        applyWorldTransform();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gridDirty) {
                    redrawGrid();
                    gridDirty = false;
                }
            }
        }.start();

        if (zoomIndicatorButton != null) {
            zoomIndicatorButton.setFocusTraversable(false);
            zoomIndicatorButton.setMouseTransparent(true);
            updateZoomIndicator();
        }
        if (resetViewButton != null) {
            resetViewButton.setFocusTraversable(false);
            resetViewButton.setOnAction(e -> resetView());
        }

        if (root != null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                uninstallGlobalSceneFilters(oldScene);
                installGlobalSceneFilters(newScene);
            });
            installGlobalSceneFilters(root.getScene());
        }

        if (edgesLayer != null) {
            edgesLayer.setMouseTransparent(true);
            edgesLayer.setPickOnBounds(false);

            // Debug-safety: se per qualche motivo il CSS non viene applicato, assicuriamo uno stroke visibile.
            rubberBand.getCurve().setStyle("-fx-stroke: rgba(255,255,255,0.85);");
        }

        updateInteractionModeUI();
        installCanvasDnD();
        installNodeMoveHandlers();

        // Rubber-band edge sempre in layer (ma visibile solo quando serve)
        if (edgesLayer != null) {
            rubberBand.getCurve().setVisible(false);
            edgesLayer.getChildren().add(rubberBand.getCurve());
            rubberBand.getCurve().toFront();
        }
    }

    private void bindToolMode() {
        if (moveButton == null) return;

        toolMode = moveButton.isSelected() ? ToolMode.MOVE_NODES : ToolMode.PAN;
        moveButton.selectedProperty().addListener((obs, was, isSelected) -> {
            toolMode = isSelected ? ToolMode.MOVE_NODES : ToolMode.PAN;
            updateInteractionModeUI();
        });
    }

    private void updateInteractionModeUI() {
        switch (toolMode) {
            case PAN -> setCanvasCursor(Cursor.OPEN_HAND);
            case MOVE_NODES -> setCanvasCursor(Cursor.DEFAULT);
        }

        if (toolMode == ToolMode.MOVE_NODES && isPanning) {
            isPanning = false;
            panStartScene = null;
        }
    }

    private void installGlobalSceneFilters(Scene scene) {
        if (scene == null || scene == installedGlobalScene) return;
        installedGlobalScene = scene;

        scene.addEventFilter(ScrollEvent.SCROLL, this::handleScrollOrZoom);
        scene.addEventFilter(ZoomEvent.ZOOM, this::handlePinchZoom);

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleGlobalCursorHints);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleGlobalCursorHints);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalModifierKey);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleGlobalModifierKey);
    }

    private void uninstallGlobalSceneFilters(Scene scene) {
        if (scene == null) return;
        scene.removeEventFilter(ScrollEvent.SCROLL, this::handleScrollOrZoom);
        scene.removeEventFilter(ZoomEvent.ZOOM, this::handlePinchZoom);
        scene.removeEventFilter(MouseEvent.MOUSE_MOVED, this::handleGlobalCursorHints);
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleGlobalCursorHints);
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalModifierKey);
        scene.removeEventFilter(KeyEvent.KEY_RELEASED, this::handleGlobalModifierKey);
        if (scene == installedGlobalScene) installedGlobalScene = null;
    }

    private void handleGlobalModifierKey(KeyEvent e) {
        if (root == null) return;
        if (!root.isHover()) return;

        if (toolMode == ToolMode.PAN) {
            setCanvasCursor(Cursor.OPEN_HAND);
            return;
        }

        boolean wantsPanHint = e.isShortcutDown() || e.isShiftDown();
        setCanvasCursor(wantsPanHint ? Cursor.OPEN_HAND : Cursor.DEFAULT);
    }

    private void handleGlobalCursorHints(MouseEvent e) {
        if (root == null) return;
        if (!root.isHover()) return;

        if (toolMode == ToolMode.PAN) {
            setCanvasCursor(Cursor.OPEN_HAND);
            return;
        }

        boolean wantsPanDragHint =
                e.isShortcutDown() ||
                        e.isShiftDown() ||
                        e.isMiddleButtonDown() ||
                        e.isSecondaryButtonDown();

        setCanvasCursor(wantsPanDragHint ? Cursor.OPEN_HAND : Cursor.DEFAULT);
    }

    private void installInputHandlers() {
        // Ora che inputOverlay è mouseTransparent, gli eventi arrivano al canvas.
        javafx.scene.Node eventSurface = getEventSurface();
        if (eventSurface == null) return;

        eventSurface.addEventFilter(ScrollEvent.SCROLL, this::handleScrollOrZoom);
        eventSurface.addEventFilter(ZoomEvent.ZOOM, this::handlePinchZoom);

        eventSurface.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handlePanStart);
        eventSurface.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handlePanDrag);
        eventSurface.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handlePanEnd);

        // Rubber-band move
        eventSurface.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleRubberBandMove);
        eventSurface.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleRubberBandMove);
        eventSurface.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleRubberBandRelease);
    }

    private void handleRubberBandMove(MouseEvent e) {
        if (!isConnecting || connectingSourceNodeId == null) return;
        if (edgesLayer == null) return;

        Point2D from = getOutputSocketWorldCenter(connectingSourceNodeId);
        if (from == null) return;

        // Se siamo sopra un input socket, facciamo "snap" al centro del socket.
        String targetUnderMouse = findInputSocketOwnerAtScene(new Point2D(e.getSceneX(), e.getSceneY()));
        hoveredTargetNodeId = targetUnderMouse;

        Point2D to;
        if (targetUnderMouse != null) {
            to = getInputSocketWorldCenter(targetUnderMouse);
        } else {
            to = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
        }

        if (to == null) {
            to = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
        }

        rubberBand.setEndpoints(from, to);
        rubberBand.getCurve().setVisible(true);
    }

    /**
     * Converte coordinate Scene nelle coordinate WORLD del graph.
     */
    private Point2D sceneToWorld(Point2D scenePoint) {
        if (playground == null) return new Point2D(0, 0);
        Point2D playgroundLocal = playground.sceneToLocal(scenePoint);
        try {
            return worldToScreen.createInverse().transform(playgroundLocal);
        } catch (Exception ex) {
            return new Point2D(0, 0);
        }
    }

    private Point2D socketCenterInWorld(javafx.scene.shape.Circle socket) {
        if (socket == null || playground == null) return null;
        javafx.geometry.Bounds local = socket.getBoundsInLocal();
        Point2D socketScene = socket.localToScene(local.getCenterX(), local.getCenterY());
        return sceneToWorld(socketScene);
    }

    private Point2D getOutputSocketWorldCenter(String nodeId) {
        NodeWidget w = nodeWidgetById.get(nodeId);
        if (w == null) return null;
        return socketCenterInWorld(w.getOutputSocket());
    }

    private Point2D getInputSocketWorldCenter(String nodeId) {
        NodeWidget w = nodeWidgetById.get(nodeId);
        if (w == null) return null;
        return socketCenterInWorld(w.getInputSocket());
    }

    private String findInputSocketOwnerAtScene(Point2D scenePoint) {
        for (java.util.Map.Entry<String, NodeWidget> e : nodeWidgetById.entrySet()) {
            NodeWidget w = e.getValue();
            if (w == null || w.getInputSocket() == null) continue;
            if (!w.getInputSocket().isVisible()) continue;

            javafx.geometry.Bounds b = w.getInputSocket().localToScene(w.getInputSocket().getBoundsInLocal());
            if (b.contains(scenePoint)) return e.getKey();
        }
        return null;
    }

    private void startConnectingFrom(String sourceNodeId) {
        if (sourceNodeId == null) return;
        if (isPanning || isDraggingNode) return;

        // Se il nodo ha già una connessione in uscita, stiamo facendo 'rewire':
        // la linea fissa deve sparire immediatamente al click sul socket out.
        if (backend != null && backend.getPipeline() != null) {
            for (com.unical.pixelflow.domain.model.Connection c : backend.getPipeline().getOutgoingConnections(sourceNodeId)) {
                String prevDest = c.getDestination().getOwner().getId();
                backend.disconnect(sourceNodeId, prevDest);
            }
            // Aggiorna subito il rendering: rimuove la linea fissa nell'istante del click.
            redrawEdgesFromPipeline();
        }

        isConnecting = true;
        connectingSourceNodeId = sourceNodeId;
        rubberBand.getCurve().setVisible(true);
    }

    // Debounce per evitare loop infinito di redraw via runLater
    private boolean pendingEdgesRedraw = false;

    private void requestEdgesRedrawOnce() {
        if (pendingEdgesRedraw) return;
        pendingEdgesRedraw = true;
        javafx.application.Platform.runLater(() -> {
            pendingEdgesRedraw = false;
            redrawEdgesFromPipeline();
        });
    }

    private void handleRubberBandRelease(MouseEvent e) {
        if (!isConnecting) return;

        String sourceId = connectingSourceNodeId;
        String targetId = hoveredTargetNodeId;
        if (targetId == null) {
            targetId = findInputSocketOwnerAtScene(new Point2D(e.getSceneX(), e.getSceneY()));
        }

        System.out.println("[EDGE] release source=" + sourceId + " target=" + targetId);

        boolean connected = false;

        if (backend != null && backend.getPipeline() != null && sourceId != null) {
            if (targetId == null) {
                // Rilascio nel vuoto: spezza la connessione uscente di source (se esiste)
                for (com.unical.pixelflow.domain.model.Connection c : backend.getPipeline().getOutgoingConnections(sourceId)) {
                    String prevDest = c.getDestination().getOwner().getId();
                    backend.disconnect(sourceId, prevDest);
                }
                connected = false;
                requestEdgesRedrawOnce();
            } else {
                backend.connect(sourceId, targetId);
                connected = backend.getPipeline().isConnected(sourceId, targetId);
                System.out.println("[EDGE] connected=" + connected + " connections=" + backend.getPipeline().getConnections().size());
                requestEdgesRedrawOnce();
            }
        }

        isConnecting = false;
        connectingSourceNodeId = null;
        hoveredTargetNodeId = null;
        rubberBand.getCurve().setVisible(false);

        if (connected) e.consume();
    }

    private void redrawEdgesFromPipeline() {
        if (edgesLayer == null) return;

        edgesLayer.toFront();
        if (canvas != null) canvas.toFront();

        if (playground != null) {
            playground.applyCss();
            playground.layout();
        }

        edgesLayer.getChildren().clear();
        edgesLayer.getChildren().add(rubberBand.getCurve());

        if (backend == null || backend.getPipeline() == null) return;

        int rendered = 0;
        for (com.unical.pixelflow.domain.model.Connection c : backend.getPipeline().getConnections()) {
            String fromId = c.getSource().getOwner().getId();
            String toId = c.getDestination().getOwner().getId();

            Point2D from = getOutputSocketWorldCenter(fromId);
            Point2D to = getInputSocketWorldCenter(toId);

            if (from == null) {
                javafx.scene.Node n = nodeViewsById.get(fromId);
                if (n != null) {
                    javafx.geometry.Bounds b = n.localToScene(n.getBoundsInLocal());
                    from = sceneToWorld(new Point2D(b.getCenterX(), b.getCenterY()));
                }
            }
            if (to == null) {
                javafx.scene.Node n = nodeViewsById.get(toId);
                if (n != null) {
                    javafx.geometry.Bounds b = n.localToScene(n.getBoundsInLocal());
                    to = sceneToWorld(new Point2D(b.getCenterX(), b.getCenterY()));
                }
            }

            if (from == null || to == null) continue;

            EdgeView ev = new EdgeView();
            ev.setEndpoints(from, to);
            ev.getCurve().setStyle("-fx-stroke: rgba(255,255,255,0.75); -fx-stroke-width: 2.2; -fx-stroke-line-cap: round;");
            edgesLayer.getChildren().add(ev.getCurve());
            rendered++;
        }

        rubberBand.getCurve().toFront();

        if (DEBUG_EDGES) {
            System.out.println("[EDGE] renderedCurves=" + rendered + " layerChildren=" + edgesLayer.getChildren().size());
        }
    }

    private void redrawNodesFromPipeline() {
        // Evita ricostruzioni del canvas durante drag/connecting, altrimenti i nodi "flashano" a posizioni vecchie.
        if (isDraggingNode || isConnecting) {
            redrawEdgesFromPipeline();
            return;
        }

        if (canvas == null) return;
        canvas.getChildren().clear();
        nodeViewsById.clear();
        nodeWidgetById.clear();

        if (backend == null || backend.getPipeline() == null) return;

        for (Node n : backend.getPipeline().getNodes().values()) {
            NodeCanvasController.NodeView nv = NodeCanvasController.createNodeViewWithController(n);
            canvas.getChildren().add(nv.root);
            nodeViewsById.put(n.getId(), nv.root);
            if (nv.controller != null) {
                nodeWidgetById.put(n.getId(), nv.controller);

                // Drag edge da OUTPUT
                if (nv.controller.getOutputSocket() != null) {
                    nv.controller.getOutputSocket().addEventFilter(MouseEvent.DRAG_DETECTED, me -> {
                        if (me.getButton() != MouseButton.PRIMARY) return;
                        startConnectingFrom(n.getId());
                        me.consume();
                    });
                    nv.controller.getOutputSocket().addEventFilter(MouseEvent.MOUSE_DRAGGED, me -> {
                        if (me.getButton() != MouseButton.PRIMARY) return;
                        if (!isConnecting) startConnectingFrom(n.getId());
                        me.consume();
                    });
                }

                // Rewire trascinando dal socket IN del nodo destinazione
                if (nv.controller.getInputSocket() != null) {
                    nv.controller.getInputSocket().addEventFilter(MouseEvent.DRAG_DETECTED, me -> {
                        if (me.getButton() != MouseButton.PRIMARY) return;
                        startRewireFromInput(n.getId());
                        me.consume();
                    });
                    nv.controller.getInputSocket().addEventFilter(MouseEvent.MOUSE_DRAGGED, me -> {
                        if (me.getButton() != MouseButton.PRIMARY) return;
                        if (!isConnecting) startRewireFromInput(n.getId());
                        me.consume();
                    });
                }
            }
        }

        redrawEdgesFromPipeline();
    }

    private void applyWorldTransform() {
        worldToScreen.setToIdentity();
        worldToScreen.appendTranslation(-offsetX, -offsetY);
        worldToScreen.appendScale(zoom, zoom);
        canvas.getTransforms().setAll(worldToScreen);
        if (edgesLayer != null) {
            edgesLayer.getTransforms().setAll(worldToScreen);
        }
    }

    private void handleScrollOrZoom(ScrollEvent e) {
        if (root != null && !root.isHover() && e.getSource() instanceof Scene) {
            return;
        }

        if (e.isControlDown()) {
            handleZoomFromScroll(e);
        } else {
            handlePanFromScroll(e);
        }
        e.consume();
    }

    private void handlePanFromScroll(ScrollEvent e) {
        offsetX += (-e.getDeltaX() / zoom);
        offsetY += (-e.getDeltaY() / zoom);

        applyWorldTransform();
        updateZoomIndicator();
        gridDirty = true;
    }

    private void handleZoomFromScroll(ScrollEvent e) {
        double delta = e.getDeltaY();
        if (delta == 0) return;

        Point2D mouseScene = new Point2D(e.getSceneX(), e.getSceneY());
        Point2D mouseWorldBefore = sceneToWorld(mouseScene);

        double zoomFactor = Math.pow(1.0015, delta);
        double newZoom = clamp(zoom * zoomFactor, MIN_ZOOM, MAX_ZOOM);
        if (newZoom == zoom) return;
        zoom = newZoom;

        Point2D mouseWorldAfter = sceneToWorld(mouseScene);
        Point2D diff = mouseWorldAfter.subtract(mouseWorldBefore);
        offsetX += diff.getX();
        offsetY += diff.getY();

        applyWorldTransform();
        updateZoomIndicator();
        gridDirty = true;
    }

    private void handlePinchZoom(ZoomEvent e) {
        if (root != null && !root.isHover() && e.getSource() instanceof Scene) {
            return;
        }

        double factor = e.getZoomFactor();
        if (factor == 0 || Double.isNaN(factor) || Double.isInfinite(factor)) return;

        Point2D mouseScene = new Point2D(e.getSceneX(), e.getSceneY());
        Point2D mouseWorldBefore = sceneToWorld(mouseScene);

        double newZoom = clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
        if (newZoom == zoom) return;
        zoom = newZoom;

        Point2D mouseWorldAfter = sceneToWorld(mouseScene);
        Point2D diff = mouseWorldAfter.subtract(mouseWorldBefore);
        offsetX += diff.getX();
        offsetY += diff.getY();

        applyWorldTransform();
        updateZoomIndicator();
        gridDirty = true;
        e.consume();
    }

    private void handlePanStart(MouseEvent e) {
        boolean wantsPanDrag;

        if (toolMode == ToolMode.PAN) {
            // Modalità PAN: pan sempre con tasti mouse standard.
            wantsPanDrag =
                    e.getButton() == MouseButton.PRIMARY ||
                            e.getButton() == MouseButton.MIDDLE ||
                            e.getButton() == MouseButton.SECONDARY;
        } else {
            // Modalità MOVE_NODES: pan solo come override esplicito
            // (cmd/ctrl, shift, tasto centrale o destro).
            wantsPanDrag =
                    e.getButton() == MouseButton.MIDDLE ||
                            e.getButton() == MouseButton.SECONDARY ||
                            (e.getButton() == MouseButton.PRIMARY && (e.isShortcutDown() || e.isShiftDown()));
        }

        if (!wantsPanDrag) return;

        isPanning = true;
        panStartScene = new Point2D(e.getSceneX(), e.getSceneY());
        panStartOffsetX = offsetX;
        panStartOffsetY = offsetY;

        setCanvasCursor(Cursor.CLOSED_HAND);
        e.consume();
    }

    private void handlePanDrag(MouseEvent e) {
        // In MOVE_NODES, pan solo se è stato attivato come override (isPanning true).
        if (!isPanning || panStartScene == null) return;

        // Se l'utente sta trascinando un nodo, non deve partire anche il pan.
        if (toolMode == ToolMode.MOVE_NODES && isDraggingNode) return;

        Point2D now = new Point2D(e.getSceneX(), e.getSceneY());
        Point2D deltaScene = now.subtract(panStartScene);

        offsetX = panStartOffsetX + (-deltaScene.getX() / zoom);
        offsetY = panStartOffsetY + (-deltaScene.getY() / zoom);

        applyWorldTransform();
        updateZoomIndicator();
        gridDirty = true;
        e.consume();
    }

    private void handlePanEnd(MouseEvent e) {
        if (!isPanning) return;

        // In MOVE_NODES, non chiudiamo il pan su primary se era drag nodo.
        if (toolMode == ToolMode.MOVE_NODES && isDraggingNode) {
            return;
        }

        if (e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
            panStartScene = null;
            updateInteractionModeUI();
            e.consume();
        }
    }

    private void redrawGrid() {
        if (gridCanvas.getWidth() <= 1 || gridCanvas.getHeight() <= 1) return;

        GraphicsContext g = gridCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        Color minor = Color.rgb(255, 255, 255, 0.06);
        Color major = Color.rgb(255, 255, 255, 0.12);

        Point2D topLeftWorld;
        Point2D bottomRightWorld;
        try {
            topLeftWorld = worldToScreen.createInverse().transform(0, 0);
            bottomRightWorld = worldToScreen.createInverse().transform(gridCanvas.getWidth(), gridCanvas.getHeight());
        } catch (Exception ex) {
            return;
        }

        double minX = Math.min(topLeftWorld.getX(), bottomRightWorld.getX());
        double maxX = Math.max(topLeftWorld.getX(), bottomRightWorld.getX());
        double minY = Math.min(topLeftWorld.getY(), bottomRightWorld.getY());
        double maxY = Math.max(topLeftWorld.getY(), bottomRightWorld.getY());

        double startX = Math.floor(minX / GRID_STEP) * GRID_STEP;
        double startY = Math.floor(minY / GRID_STEP) * GRID_STEP;

        g.setLineWidth(1.0);

        for (double x = startX; x <= maxX; x += GRID_STEP) {
            boolean isMajor = (Math.round(x / GRID_STEP) % GRID_MAJOR_EVERY) == 0;
            g.setStroke(isMajor ? major : minor);

            double screenX = worldToScreen.transform(x, 0).getX();
            screenX = Math.floor(screenX) + 0.5;
            g.strokeLine(screenX, 0, screenX, gridCanvas.getHeight());
        }

        for (double y = startY; y <= maxY; y += GRID_STEP) {
            boolean isMajor = (Math.round(y / GRID_STEP) % GRID_MAJOR_EVERY) == 0;
            g.setStroke(isMajor ? major : minor);

            double screenY = worldToScreen.transform(0, y).getY();
            screenY = Math.floor(screenY) + 0.5;
            g.strokeLine(0, screenY, gridCanvas.getWidth(), screenY);
        }
    }

    private void resetView() {
        zoom = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        applyWorldTransform();
        updateZoomIndicator();
        gridDirty = true;
    }

    private void updateZoomIndicator() {
        if (zoomIndicatorButton == null) return;
        int percent = (int) Math.round(zoom * 100.0);
        zoomIndicatorButton.setText(percent + "%");
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Abilita/disabilita Export e imposta un tooltip esplicativo quando disabilitato.
     */
    public void setExportEnabled(boolean enabled, String tooltipText) {
        if (exportButton == null) return;
        exportButton.setDisable(!enabled);

        if (!enabled && tooltipText != null && !tooltipText.isBlank()) {
            exportButton.setTooltip(new Tooltip(tooltipText));
        } else {
            exportButton.setTooltip(null);
        }
    }

    private void installCanvasDnD() {
        // L'overlay è mouseTransparent: usiamo la superficie canvas.
        if (canvas == null) return;

        canvas.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        canvas.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString() && backend != null) {
                String nodeType = db.getString();

                // Non permettiamo di creare Input/Output qui: sono nodi strutturali unici.
                if (!"InputNode".equals(nodeType) && !"OutputNode".equals(nodeType)) {
                    java.util.Set<String> beforeIds = new java.util.HashSet<>(backend.getPipeline().getNodes().keySet());

                    Point2D world = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));

                    String addFilterKey = mapNodeTypeToAddFilterKey(nodeType);
                    backend.addFilter(addFilterKey);

                    String createdId = null;
                    for (String id : backend.getPipeline().getNodes().keySet()) {
                        if (!beforeIds.contains(id)) {
                            createdId = id;
                            break;
                        }
                    }

                    if (createdId != null) {
                        Node created = backend.getPipeline().getNode(createdId);
                        if (created != null) {
                          created.setX(world.getX() - (NODE_WIDGET_WIDTH / 2.0));
                          created.setY(world.getY() - (NODE_WIDGET_HEIGHT / 2.0));
                        }
                    }

                    redrawNodesFromPipeline();
                    redrawEdgesFromPipeline();
                    success = true;
                }
            }

            e.setDropCompleted(success);
            e.consume();
        });
    }

    private static String mapNodeTypeToAddFilterKey(String nodeType) {
        return switch (nodeType) {
            case "BlurFilter" -> "Blur";
            case "BrightnessFilter" -> "Brightness";
            case "ContrastFilter" -> "Contrast";
            case "EdgeDetectionFilter" -> "EdgeDetection";
            case "FlipFilter" -> "Flip";
            case "GrayscaleFilter" -> "Grayscale";
            case "InvertFilter" -> "Invert";
            case "PixelateFilter" -> "Pixelate";
            case "Rotate90Filter" -> "Rotate90";
            case "ScaleFilter" -> "Scale";
            case "SepiaFilter" -> "Sepia";
            case "SharpenFilter" -> "Sharpen";
            case "SolarizeFilter" -> "Solarize";
            case "ThresholdFilter" -> "Threshold";
            case "VignetteFilter" -> "Vignette";
            default -> nodeType;
        };
    }

    /**
     * Superficie che riceve effettivamente gli input utente.
     * L'overlay è mouseTransparent, quindi di default è il canvas.
     */
    private javafx.scene.Node getEventSurface() {
        return (canvas != null) ? canvas : inputOverlay;
    }

    private void setCanvasCursor(Cursor cursor) {
        javafx.scene.Node surface = getEventSurface();
        if (surface != null) surface.setCursor(cursor);
    }

    private void installNodeMoveHandlers() {
        if (canvas == null) return;

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (toolMode != ToolMode.MOVE_NODES) return;
            if (backend == null) return;

            // Se siamo in connecting mode e l'utente clicca altrove, annulliamo.
            if (isConnecting) {
                isConnecting = false;
                connectingSourceNodeId = null;
                hoveredTargetNodeId = null;
                rubberBand.getCurve().setVisible(false);
            }

            // Se il click arriva da un socket, NON iniziare il drag del nodo.
            if (e.getTarget() instanceof javafx.scene.shape.Circle) {
                return;
            }

            // In MOVE_NODES, se l'utente sta attivando il pan override (cmd/ctrl o shift), non iniziamo drag nodo.
            if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY ||
                    (e.getButton() == MouseButton.PRIMARY && (e.isShortcutDown() || e.isShiftDown()))) {
                return;
            }

            javafx.scene.Node n = (javafx.scene.Node) e.getTarget();
            while (n != null && n.getUserData() == null) {
                n = n.getParent();
            }
            if (n == null || n.getUserData() == null) return;

            String nodeId = String.valueOf(n.getUserData());
            Node model = backend.getPipeline().getNode(nodeId);
            if (model == null) return;

            isDraggingNode = true;
            draggingNodeId = nodeId;
            dragStartWorld = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
            dragStartNodePos = new Point2D(model.getX(), model.getY());

            e.consume();
        });

        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (toolMode != ToolMode.MOVE_NODES) return;
            if (!isDraggingNode || draggingNodeId == null) return;
            if (backend == null) return;

            Node model = backend.getPipeline().getNode(draggingNodeId);
            if (model == null) return;

            Point2D nowWorld = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
            Point2D delta = nowWorld.subtract(dragStartWorld);

            double newX = dragStartNodePos.getX() + delta.getX();
            double newY = dragStartNodePos.getY() + delta.getY();

            model.setX(newX);
            model.setY(newY);

            for (javafx.scene.Node child : canvas.getChildren()) {
                if (child.getUserData() != null && draggingNodeId.equals(String.valueOf(child.getUserData()))) {
                    child.setLayoutX(newX);
                    child.setLayoutY(newY);
                    break;
                }
            }

            redrawEdgesFromPipeline();
            e.consume();
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (toolMode != ToolMode.MOVE_NODES) return;
            if (!isDraggingNode) return;

            isDraggingNode = false;
            draggingNodeId = null;
            dragStartWorld = null;
            dragStartNodePos = null;
            e.consume();
        });
    }

    // Quando trascini dal socket IN, stai spostando un collegamento esistente: source = nodo in ingresso precedente
    private void startRewireFromInput(String targetNodeId) {
        if (targetNodeId == null) return;
        if (isPanning || isDraggingNode) return;
        if (backend == null || backend.getPipeline() == null) return;

        java.util.List<com.unical.pixelflow.domain.model.Connection> incoming = backend.getPipeline().getIncomingConnections(targetNodeId);
        if (incoming.isEmpty()) return;

        String sourceId = incoming.get(0).getSource().getOwner().getId();

        // Spezza subito la connessione A->B: il filo fisso deve sparire all'istante.
        backend.disconnect(sourceId, targetNodeId);
        redrawEdgesFromPipeline();

        // Inizia la connessione come se partisse da A
        isConnecting = true;
        connectingSourceNodeId = sourceId;
        rubberBand.getCurve().setVisible(true);
    }
}



