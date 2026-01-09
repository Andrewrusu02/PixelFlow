package com.unical.pixelflow.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Applica un overlay scuro e un blur al contenuto sottostante quando si apre un dialog modale.
 *
 * Uso tipico:
 *   ModalEffects.ModalOverlay overlay = ModalEffects.apply(dashboardRoot);
 *   dialogStage.setOnHidden(e -> overlay.remove());
 */
public final class ModalEffects {

    private ModalEffects() {}

    private static final double BLUR_RADIUS = 12.0;
    private static final double GLASS_ALPHA = 0.25;
    private static final Duration ANIM_IN = Duration.millis(160);
    private static final Duration ANIM_OUT = Duration.millis(120);

    public static final class ModalOverlay {
        private final BorderPane parent;
        private final Rectangle glass;
        private final Effect oldParentEffect;
        private final java.util.Map<Node, Effect> previousChildEffects;
        private final java.util.Map<Node, GaussianBlur> blurByNode;

        private ModalOverlay(BorderPane parent,
                             Rectangle glass,
                             Effect oldParentEffect,
                             java.util.Map<Node, Effect> previousChildEffects,
                             java.util.Map<Node, GaussianBlur> blurByNode) {
            this.parent = parent;
            this.glass = glass;
            this.oldParentEffect = oldParentEffect;
            this.previousChildEffects = previousChildEffects;
            this.blurByNode = blurByNode;
        }

        /**
         * Rimuove l'overlay e ripristina gli effetti originali sul contenuto sottostante.
         */
        public void remove() {
            if (parent == null) return;

            Platform.runLater(() -> {
                try {
                    Timeline out = new Timeline();

                    out.getKeyFrames().add(new KeyFrame(
                            ANIM_OUT,
                            new KeyValue(glass.opacityProperty(), 0.0, Interpolator.EASE_BOTH)
                    ));

                    if (blurByNode != null) {
                        for (GaussianBlur gb : blurByNode.values()) {
                            out.getKeyFrames().add(new KeyFrame(
                                    ANIM_OUT,
                                    new KeyValue(gb.radiusProperty(), 0.0, Interpolator.EASE_BOTH)
                            ));
                        }
                    }

                    out.setOnFinished(_ -> {
                        try {
                            parent.getChildren().remove(glass);
                        } catch (Exception ignored) {
                        }

                        if (previousChildEffects != null) {
                            for (var entry : previousChildEffects.entrySet()) {
                                try {
                                    entry.getKey().setEffect(entry.getValue());
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        try {
                            parent.setEffect(oldParentEffect);
                        } catch (Exception ignored) {
                        }
                    });

                    out.play();
                } catch (Exception e) {
                    try {
                        parent.getChildren().remove(glass);
                    } catch (Exception ignored) {
                    }
                    if (previousChildEffects != null) {
                        for (var entry : previousChildEffects.entrySet()) {
                            try {
                                entry.getKey().setEffect(entry.getValue());
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    try {
                        parent.setEffect(oldParentEffect);
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    /**
     * Applica blur + glass pane al contenuto root.
     * Richiede che il root sia un StackPane.
     */
    public static ModalOverlay apply(BorderPane ownerRoot) {
        if (ownerRoot == null) {
            return new ModalOverlay(null, new Rectangle(), null, null, null);
        }

        javafx.collections.ObservableList<Node> children = ownerRoot.getChildren();

        Effect oldParentEffect = ownerRoot.getEffect();
        java.util.Map<Node, Effect> previous = new java.util.IdentityHashMap<>();
        java.util.Map<Node, GaussianBlur> blurByNode = new java.util.IdentityHashMap<>();

        for (Node n : children) {
            previous.put(n, n.getEffect());

            GaussianBlur gb = new GaussianBlur(0.0);
            n.setEffect(gb);
            blurByNode.put(n, gb);
        }

        ownerRoot.setEffect(null);

        Rectangle glass = new Rectangle();
        glass.setFill(Color.rgb(0, 0, 0, 1.0));
        glass.setOpacity(0.0);
        glass.widthProperty().bind(ownerRoot.widthProperty());
        glass.heightProperty().bind(ownerRoot.heightProperty());
        glass.setMouseTransparent(true);

        ownerRoot.getChildren().add(glass);

        Platform.runLater(() -> {
            try {
                Timeline in = new Timeline();

                in.getKeyFrames().add(new KeyFrame(
                        ANIM_IN,
                        new KeyValue(glass.opacityProperty(), GLASS_ALPHA, Interpolator.EASE_BOTH)
                ));

                for (GaussianBlur gb : blurByNode.values()) {
                    in.getKeyFrames().add(new KeyFrame(
                            ANIM_IN,
                            new KeyValue(gb.radiusProperty(), BLUR_RADIUS, Interpolator.EASE_BOTH)
                    ));
                }

                in.play();
            } catch (Exception ignored) {
            }
        });

        return new ModalOverlay(ownerRoot, glass, oldParentEffect, previous, blurByNode);
    }
}
