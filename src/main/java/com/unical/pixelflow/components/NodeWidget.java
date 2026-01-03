package com.unical.pixelflow.components;

import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.utility.FilterResources;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class NodeWidget {

    @FXML private BorderPane root;
    @FXML private VBox body;
    @FXML private SVGPath iconShape;
    @FXML private Circle inputSocket;
    @FXML private Label nodeTitle;
    @FXML private Circle outputSocket;
    @FXML private HBox contentBox;

    public void setTitle(String title) {
        if (nodeTitle != null) {
            nodeTitle.setText(title == null ? "" : title);
        }
    }

    public void setIcon(String svgPath) {
        if (iconShape != null && svgPath != null && !svgPath.isBlank()) {
            iconShape.setContent(svgPath);
        }
    }

    public void setNodeTypeCss(String cssClass) {
        if (root == null) return;
        root.getStyleClass().removeAll("node-filter", "node-input", "node-output");
        if (cssClass != null && !cssClass.isBlank()) {
            root.getStyleClass().add(cssClass);
        }
    }

    public void setSocketsEnabled(boolean inputEnabled, boolean outputEnabled) {
        if (inputSocket != null) {
            inputSocket.setVisible(inputEnabled);
            inputSocket.setManaged(inputEnabled);
        }
        if (outputSocket != null) {
            outputSocket.setVisible(outputEnabled);
            outputSocket.setManaged(outputEnabled);
        }
    }

    public HBox getContentBox() {
        return contentBox;
    }

    public Circle getInputSocket() {
        return inputSocket;
    }

    public Circle getOutputSocket() {
        return outputSocket;
    }

    public VBox getBody() {
        return body;
    }

    public void bindNode(Node node) {
        if (node == null) return;

        setTitle(node.getName() == null || node.getName().isBlank() ? node.getClass().getSimpleName() : node.getName());

        String typeName = node.getClass().getSimpleName();
        String svgContent = FilterResources.getIconForType(typeName);
        setIcon(svgContent);

        if (node.isInput()) {
            setNodeTypeCss("node-input");
            setSocketsEnabled(false, true);
        } else if (node.isOutput()) {
            setNodeTypeCss("node-output");
            setSocketsEnabled(true, false);
        } else {
            setNodeTypeCss("node-filter");
            setSocketsEnabled(true, true);
        }
    }
}
