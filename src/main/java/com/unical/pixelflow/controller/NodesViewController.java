package com.unical.pixelflow.controller;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;

public class NodesViewController {

    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 4.0;

    private static final double GRID_STEP = 40.0;
    private static final int GRID_MAJOR_EVERY = 5;

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

    private final Affine worldToScreen = new Affine();

    @FXML private Button CopyButton;
    @FXML private Button DeleteButton;
    @FXML private Button DisableButton;
    @FXML private ToggleButton MoveButton;
    @FXML private Button RedoButton;
    @FXML private Button SaveProjectButton;
    @FXML private Button UndoButton;

    @FXML private Pane canvas;
    @FXML private SVGPath hideShowIcon;
    @FXML private StackPane root;
    @FXML private ScrollPane scrollPane;

    @FXML private StackPane playground;
    @FXML private Canvas gridCanvas;
    @FXML private Pane inputOverlay;

    @FXML private javafx.scene.layout.HBox viewportControls;

    @FXML private Button ZoomIndicatorButton;
    @FXML private Button ResetViewButton;

    private Scene installedGlobalScene;

    @FXML
    private void initialize() {
        if (inputOverlay != null) {
            inputOverlay.setMouseTransparent(false);
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

        if (ZoomIndicatorButton != null) {
            ZoomIndicatorButton.setFocusTraversable(false);
            ZoomIndicatorButton.setMouseTransparent(true);
            updateZoomIndicator();
        }
        if (ResetViewButton != null) {
            ResetViewButton.setFocusTraversable(false);
            ResetViewButton.setOnAction(e -> resetView());
        }

        if (root != null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                uninstallGlobalSceneFilters(oldScene);
                installGlobalSceneFilters(newScene);
            });
            installGlobalSceneFilters(root.getScene());
        }

        updateInteractionModeUI();
    }

    private void bindToolMode() {
        if (MoveButton == null) return;

        toolMode = MoveButton.isSelected() ? ToolMode.MOVE_NODES : ToolMode.PAN;
        MoveButton.selectedProperty().addListener((obs, was, isSelected) -> {
            toolMode = isSelected ? ToolMode.MOVE_NODES : ToolMode.PAN;
            updateInteractionModeUI();
        });
    }

    private void updateInteractionModeUI() {
        if (inputOverlay == null) return;

        switch (toolMode) {
            case PAN -> inputOverlay.setCursor(Cursor.OPEN_HAND);
            case MOVE_NODES -> inputOverlay.setCursor(Cursor.DEFAULT);
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
        if (inputOverlay == null || root == null) return;
        if (!root.isHover()) return;

        if (toolMode == ToolMode.PAN) {
            inputOverlay.setCursor(Cursor.OPEN_HAND);
            return;
        }

        boolean wantsPanHint = e.isShortcutDown() || e.isShiftDown();
        inputOverlay.setCursor(wantsPanHint ? Cursor.OPEN_HAND : Cursor.DEFAULT);
    }

    private void handleGlobalCursorHints(MouseEvent e) {
        if (inputOverlay == null || root == null) return;
        if (!root.isHover()) return;

        if (toolMode == ToolMode.PAN) {
            inputOverlay.setCursor(Cursor.OPEN_HAND);
            return;
        }

        boolean wantsPanDragHint =
                e.isShortcutDown() ||
                        e.isShiftDown() ||
                        e.isMiddleButtonDown() ||
                        e.isSecondaryButtonDown();

        inputOverlay.setCursor(wantsPanDragHint ? Cursor.OPEN_HAND : Cursor.DEFAULT);
    }

    private void installInputHandlers() {
        inputOverlay.addEventFilter(ScrollEvent.SCROLL, this::handleScrollOrZoom);
        inputOverlay.addEventFilter(ZoomEvent.ZOOM, this::handlePinchZoom);

        inputOverlay.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handlePanStart);
        inputOverlay.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handlePanDrag);
        inputOverlay.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handlePanEnd);
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
            wantsPanDrag =
                    e.getButton() == MouseButton.PRIMARY ||
                            e.getButton() == MouseButton.MIDDLE ||
                            e.getButton() == MouseButton.SECONDARY;
        } else {
            wantsPanDrag =
                    e.getButton() == MouseButton.MIDDLE ||
                            e.getButton() == MouseButton.SECONDARY ||
                            (e.getButton() == MouseButton.PRIMARY && e.isShortcutDown()) ||
                            (e.getButton() == MouseButton.PRIMARY && e.isShiftDown());
        }

        if (!wantsPanDrag) return;

        isPanning = true;
        panStartScene = new Point2D(e.getSceneX(), e.getSceneY());
        panStartOffsetX = offsetX;
        panStartOffsetY = offsetY;

        if (inputOverlay != null) inputOverlay.setCursor(Cursor.CLOSED_HAND);
        e.consume();
    }

    private void handlePanDrag(MouseEvent e) {
        if (!isPanning || panStartScene == null) return;

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

        if (e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
            panStartScene = null;
            updateInteractionModeUI();
            e.consume();
        }
    }

    private void applyWorldTransform() {
        worldToScreen.setToIdentity();
        worldToScreen.appendTranslation(-offsetX, -offsetY);
        worldToScreen.appendScale(zoom, zoom);
        canvas.getTransforms().setAll(worldToScreen);
    }

    private Point2D sceneToWorld(Point2D scenePoint) {
        Point2D overlayLocal = inputOverlay.sceneToLocal(scenePoint);
        try {
            return worldToScreen.createInverse().transform(overlayLocal);
        } catch (Exception ex) {
            return new Point2D(0, 0);
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
        if (ZoomIndicatorButton == null) return;
        int percent = (int) Math.round(zoom * 100.0);
        ZoomIndicatorButton.setText(percent + "%");
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
