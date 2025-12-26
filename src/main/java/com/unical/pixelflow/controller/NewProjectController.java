package com.unical.pixelflow.controller;

import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.infrastructure.ProjectSerializer;
import com.unical.pixelflow.model.DataPF;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

public class NewProjectController {

    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label sourceLabel;
    @FXML private Button browseButton;
    @FXML private Button cancelButton;
    @FXML private Button createButton;

    @FXML
    private TextField nameField;

    @FXML
    private TextField pathField;

    private File selectedImageFile;
    private Stage dialogStage;
    private boolean isCreated = false; // Flag per dire alla Dashboard di aggiornare

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isCreated() {
        return isCreated;
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) titleLabel.textProperty().bind(DataPF.lsp.lsb("dialog.new_project.title"));
        if (nameLabel != null) nameLabel.textProperty().bind(DataPF.lsp.lsb("dialog.new_project.name"));
        if (sourceLabel != null) sourceLabel.textProperty().bind(DataPF.lsp.lsb("dialog.new_project.source"));
        if (browseButton != null) browseButton.textProperty().bind(DataPF.lsp.lsb("dialog.new_project.browse"));
        if (cancelButton != null) cancelButton.textProperty().bind(DataPF.lsp.lsb("common.cancel"));
        if (createButton != null) createButton.textProperty().bind(DataPF.lsp.lsb("dialog.new_project.create"));

        if (nameField != null) nameField.promptTextProperty().bind(DataPF.lsp.lsb("dialog.new_project.name.placeholder"));
        if (pathField != null) pathField.promptTextProperty().bind(DataPF.lsp.lsb("dialog.new_project.source.placeholder"));
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.titleProperty().bind(DataPF.lsp.lsb("dialog.new_project.file_chooser.title"));
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter(DataPF.lsp.lsb("imagefiles").get(), "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        selectedImageFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedImageFile != null) {
            pathField.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(DataPF.lsp.lsb("common.error").get(), DataPF.lsp.lsb("dialog.new_project.error.name").get());
            return;
        }
        if (selectedImageFile == null || !selectedImageFile.exists()) {
            showAlert(DataPF.lsp.lsb("common.error").get(), DataPF.lsp.lsb("dialog.new_project.error.source").get());
            return;
        }

        try {
            String uuid = UUID.randomUUID().toString();

            File projectsDir = new File("Data/projects");
            File thumbsDir = new File("Data/thumbnails");
            projectsDir.mkdirs();
            thumbsDir.mkdirs();

            String pfpPath = new File(projectsDir, uuid + ".pfp").getAbsolutePath();
            String thumbPath = new File(thumbsDir, uuid + ".png").getAbsolutePath();

            BufferedImage sourceImage = ImageIO.read(selectedImageFile);
            if (sourceImage == null) {
                showAlert(DataPF.lsp.lsb("common.error").get(), "Impossibile leggere l'immagine selezionata.");
                return;
            }

            Pipeline pipeline = new Pipeline();
            pipeline.addNode(new InputNode(sourceImage));

            new ProjectSerializer().save(pipeline, selectedImageFile.getAbsolutePath(), pfpPath);

            BufferedImage thumbnailImage = createThumbnail(sourceImage, 380, 280);
            ImageIO.write(thumbnailImage, "png", new File(thumbPath));

            Task<Boolean> dbTask = DataPF.pfDatabase.saveProject(
                    DataPF.dbUser.getId(),
                    name,
                    pfpPath,
                    thumbPath
            );

            Thread thread = new Thread(dbTask);
            thread.setDaemon(true);
            thread.start();

            if (dbTask.get()) {
                isCreated = true;
                dialogStage.close();
            } else {
                showAlert("Errore Database", "Impossibile salvare il progetto nel database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore Critico", "Errore durante la creazione: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Rende l'alert modale rispetto al dialog
        alert.initOwner(dialogStage);
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        // Rimuove bordi Windows e rende NON trascinabile
        alert.initStyle(javafx.stage.StageStyle.UTILITY);

        // Centra rispetto al dialog parent
        alert.showAndWait();
    }

    /**
     * Crea una thumbnail ridimensionata mantenendo l'aspect ratio.
     * Riempie lo spazio rimanente con nero.
     */
    private BufferedImage createThumbnail(BufferedImage source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Calcola il fattore di scala mantenendo le proporzioni
        double scaleX = (double) targetWidth / sourceWidth;
        double scaleY = (double) targetHeight / sourceHeight;
        double scale = Math.min(scaleX, scaleY); // Scala più piccola per contenere l'immagine

        int scaledWidth = (int) Math.round(sourceWidth * scale);
        int scaledHeight = (int) Math.round(sourceHeight * scale);

        // Crea l'immagine thumbnail con sfondo nero
        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = thumbnail.createGraphics();

        // Riempie con nero
        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        // Rendering di alta qualità
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        // Centra l'immagine ridimensionata
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        g2d.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return thumbnail;
    }
}
