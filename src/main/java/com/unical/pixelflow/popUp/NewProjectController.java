package com.unical.pixelflow.popUp;

import com.unical.pixelflow.controller.MainEditingController;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.infrastructure.ProjectSerializer;
import com.unical.pixelflow.model.DataPF;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewProjectController {

    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label sourceLabel;
    @FXML private Button browseButton;
    @FXML private Button cancelButton;
    @FXML private Button createButton;
    @FXML private TextField nameField;
    @FXML private TextField pathField;

    private File selectedImageFile;
    private Stage dialogStage;
    private boolean isCreated = false;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isCreated() {
        return isCreated;
    }

    @FXML
    public void initialize() {
        DataPF.esm.register(executorService);

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

        try {
            String uuid = UUID.randomUUID().toString();
            File projectsDir = new File("Data/projects");
            File thumbsDir = new File("Data/thumbnails");
            projectsDir.mkdirs();
            thumbsDir.mkdirs();

            String pfpPath = new File(projectsDir, uuid + ".pfp").getAbsolutePath();
            String thumbPath = new File(thumbsDir, uuid + ".png").getAbsolutePath();

            BufferedImage sourceImage = null;
            String sourcePathToSave = "";

            if (selectedImageFile != null && selectedImageFile.exists()) {
                sourceImage = ImageIO.read(selectedImageFile);
                if (sourceImage == null) {
                    showAlert(DataPF.lsp.lsb("common.error").get(), "Impossibile leggere l'immagine selezionata.");
                    return;
                }
                sourcePathToSave = selectedImageFile.getAbsolutePath();
            }

            Pipeline pipeline = new Pipeline();
            pipeline.addNode(new InputNode(sourceImage));

            new ProjectSerializer().save(pipeline, sourcePathToSave, pfpPath);

            BufferedImage thumbnailImage;
            if (sourceImage != null) {
                thumbnailImage = createThumbnail(sourceImage, 380, 280);
            } else {
                thumbnailImage = createDefaultThumbnail(380, 280);
            }
            ImageIO.write(thumbnailImage, "png", new File(thumbPath));

            Task<Boolean> dbTask = DataPF.userDatabase.saveProject(
                    name,
                    pfpPath,
                    thumbPath
            );

            dbTask.setOnSucceeded(e -> {
                if (dbTask.getValue()) {
                    isCreated = true;
                    dialogStage.close();
                    openEditor(pipeline);
                } else {
                    showAlert("Errore Database", "Impossibile salvare il progetto nel database.");
                }
            });

            dbTask.setOnFailed(e -> {
                Throwable ex = dbTask.getException();
                ex.printStackTrace();
                showAlert("Errore Database", "Errore durante il salvataggio: " + ex.getMessage());
            });

            executorService.submit(dbTask);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore Critico", "Errore durante la creazione: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void openEditor(Pipeline pipeline) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainEditingController editorCtrl = loader.getController();


            Stage primaryStage = (Stage) dialogStage.getOwner();

            if (primaryStage == null) {
                System.err.println("Primary Stage non trovato, impossibile cambiare scena.");
                return;
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.setMaximized(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Errore Caricamento", "Impossibile aprire l'editor: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        if (Platform.isFxApplicationThread()) {
            showAlertDialog(title, content);
        } else {
            Platform.runLater(() -> showAlertDialog(title, content));
        }
    }

    private void showAlertDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private BufferedImage createThumbnail(BufferedImage source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double scaleX = (double) targetWidth / sourceWidth;
        double scaleY = (double) targetHeight / sourceHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) Math.round(sourceWidth * scale);
        int scaledHeight = (int) Math.round(sourceHeight * scale);

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();

        g2d.setColor(Color.BLACK);
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
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.GRAY);
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