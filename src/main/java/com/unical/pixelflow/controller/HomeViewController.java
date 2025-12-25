package com.unical.pixelflow.controller;

import com.unical.pixelflow.infrastructure.ProjectRepository;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HomeViewController {

    @FXML
    private FlowPane projectsGrid;

    private final ProjectRepository repo = new ProjectRepository();

    @FXML
    public void initialize() {
        loadProjects();
    }

    private void loadProjects() {
        projectsGrid.getChildren().clear();

        for (ProjectMetadata project : repo.getRecentProjects()) {
            projectsGrid.getChildren().add(createProjectCard(project));
        }

        // Se non ci sono progetti, mostra messaggio
        if (repo.getRecentProjects().isEmpty()) {
            Label emptyLabel = new Label("Nessun progetto recente.\nClicca su '+ New Project' per iniziare.");
            emptyLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 16px; -fx-text-alignment: center;");
            projectsGrid.getChildren().add(emptyLabel);
        }
    }

    private VBox createProjectCard(ProjectMetadata project) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: #2D2D2D; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        card.setPrefSize(220, 240);
        card.setMaxSize(220, 240);
        card.setMinSize(220, 240);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: #353535; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(225,193,6,0.4), 15, 0, 0, 3);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: #2D2D2D; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
            );
        });

        // Click handler
        card.setOnMouseClicked(e -> openProject(project));

        // Thumbnail container
        StackPane thumbnail = new StackPane();
        thumbnail.setAlignment(Pos.CENTER);
        thumbnail.setPrefSize(190, 140);
        thumbnail.setMaxSize(190, 140);
        thumbnail.setMinSize(190, 140);
        thumbnail.setStyle(
            "-fx-background-color: #1E1E1E; " +
            "-fx-background-radius: 5;"
        );

        // Carica thumbnail (SVG o placeholder)
        if (project.getThumbnailPath() != null && !project.getThumbnailPath().isEmpty()) {
            try {
                Image thumbnailImage = new Image(
                    getClass().getResourceAsStream(project.getThumbnailPath())
                );
                ImageView thumbnailView = new ImageView(thumbnailImage);
                thumbnailView.setFitWidth(190);
                thumbnailView.setFitHeight(140);
                thumbnailView.setPreserveRatio(false);
                thumbnailView.setSmooth(true);
                thumbnail.getChildren().add(thumbnailView);
            } catch (Exception ex) {
                // Fallback a emoji se caricamento fallisce
                System.err.println("Errore caricamento thumbnail: " + ex.getMessage());
                Label iconLabel = new Label("📷");
                iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666;");
                thumbnail.getChildren().add(iconLabel);
            }
        } else {
            // Fallback a emoji se nessun thumbnail
            Label iconLabel = new Label("📷");
            iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666;");
            thumbnail.getChildren().add(iconLabel);
        }

        // Project name
        Label nameLabel = new Label(project.getName());
        nameLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px;"
        );
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);

        // Last modified date
        Label dateLabel = new Label(project.getFormattedDate());
        dateLabel.setStyle(
            "-fx-text-fill: #AAAAAA; " +
            "-fx-font-size: 11px;"
        );

        card.getChildren().addAll(thumbnail, nameLabel, dateLabel);

        return card;
    }

    private void openProject(ProjectMetadata project) {
        System.out.println("Opening project: " + project.getName() + " at " + project.getPath());
        // TODO: Implementare apertura progetto
        repo.openProject(project.getPath());
    }
}

