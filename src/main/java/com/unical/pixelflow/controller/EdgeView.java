package com.unical.pixelflow.controller;

import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;

/**
 * View minimale per un edge (Bezier) tra due punti.
 *
 * Nota: non gestisce hit-test/splicing ancora; serve solo per rendering e rubber-band.
 */
final class EdgeView {

    private static final double DEFAULT_STROKE_WIDTH = 2.0;

    private final CubicCurve curve;

    EdgeView() {
        this.curve = new CubicCurve();
        curve.setFill(null);
        curve.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        curve.getStyleClass().add("edge-curve");
        curve.setMouseTransparent(true);
    }

    CubicCurve getCurve() {
        return curve;
    }

    void setEndpoints(Point2D from, Point2D to) {
        curve.setStartX(from.getX());
        curve.setStartY(from.getY());
        curve.setEndX(to.getX());
        curve.setEndY(to.getY());

        double dx = Math.max(60, Math.abs(to.getX() - from.getX()) * 0.5);

        curve.setControlX1(from.getX() + dx);
        curve.setControlY1(from.getY());
        curve.setControlX2(to.getX() - dx);
        curve.setControlY2(to.getY());
    }
}

