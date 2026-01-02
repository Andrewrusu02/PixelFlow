package com.unical.pixelflow.controller;

import com.unical.pixelflow.components.NodeWidget;
import com.unical.pixelflow.domain.model.Node;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Utility per creare la UI di un Node nel canvas.
 */
final class NodeCanvasController {

    private NodeCanvasController() {}

    static final class NodeView {
        final Parent root;
        final NodeWidget controller;

        NodeView(Parent root, NodeWidget controller) {
            this.root = root;
            this.controller = controller;
        }
    }

    static NodeView createNodeViewWithController(Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(NodeCanvasController.class.getResource("/com/unical/pixelflow/components/NodeWidget.fxml"));
            Parent root = loader.load();
            NodeWidget controller = loader.getController();

            if (controller != null) {
                controller.bindNode(node);
            }

            root.setLayoutX(node.getX());
            root.setLayoutY(node.getY());
            root.setUserData(node.getId());
            return new NodeView(root, controller);
        } catch (Exception ignored) {
            Parent root = createNodeView(node);
            return new NodeView(root, null);
        }
    }

    static Parent createNodeView(Node node) {
        // Preferiamo il widget FXML "vero".
        try {
            FXMLLoader loader = new FXMLLoader(NodeCanvasController.class.getResource("/com/unical/pixelflow/components/NodeWidget.fxml"));
            Parent root = loader.load();

            NodeWidget controller = loader.getController();
            if (controller != null) {
                controller.bindNode(node);
            }

            root.setLayoutX(node.getX());
            root.setLayoutY(node.getY());
            root.setUserData(node.getId());
            return root;
        } catch (Exception ignored) {
            // Fallback: render minimale sempre visibile.
            Label title = new Label(node.getName() == null ? node.getClass().getSimpleName() : node.getName());
            title.getStyleClass().add("text-body");

            StackPane box = new StackPane(title);
            box.getStyleClass().add("background-pop-up");
            box.setPadding(new Insets(10, 14, 10, 14));
            box.setMinWidth(160);
            box.setMinHeight(60);

            if (node.isInput()) {
                box.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2;");
            } else if (node.isOutput()) {
                box.setStyle("-fx-border-color: #e67e22; -fx-border-width: 2;");
            }

            box.setLayoutX(node.getX());
            box.setLayoutY(node.getY());
            box.setUserData(node.getId());
            return box;
        }
    }
}
