package com.unical.pixelflow.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class WaveFormViewController {

    @FXML private VBox root;
    @FXML private Canvas waveformCanvas;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        waveformCanvas.widthProperty().bind(root.widthProperty());
        waveformCanvas.heightProperty().bind(root.heightProperty());

        root.widthProperty().addListener((obs, oldVal, newVal) -> redrawGrid());
        root.heightProperty().addListener((obs, oldVal, newVal) -> redrawGrid());
    }

    public void updateWaveform(Image sourceImage) {
        if (sourceImage == null || isProcessing.get()) return;

        double canvasW = waveformCanvas.getWidth();
        double canvasH = waveformCanvas.getHeight();

        if (canvasW <= 0 || canvasH <= 0) return;

        isProcessing.set(true);

        int srcW = (int) sourceImage.getWidth();
        int srcH = (int) sourceImage.getHeight();
        PixelReader reader = sourceImage.getPixelReader();

        int[] srcPixels = new int[srcW * srcH];
        reader.getPixels(0, 0, srcW, srcH, javafx.scene.image.PixelFormat.getIntArgbInstance(), srcPixels, 0, srcW);

        executor.submit(() -> {
            try {
                WritableImage result = generateWaveformData(srcPixels, srcW, srcH, (int) canvasW, (int) canvasH);
                Platform.runLater(() -> {
                    drawResult(result);
                    isProcessing.set(false);
                });
            } catch (Exception e) {
                isProcessing.set(false);
            }
        });
    }

    private WritableImage generateWaveformData(int[] pixels, int srcW, int srcH, int destW, int destH) {
        int[] accumBuffer = new int[destW * destH];

        int skipX = Math.max(1, srcW / destW);
        int skipY = Math.max(2, srcH / 300);

        for (int x = 0; x < srcW; x += skipX) {
            int targetX = (int) ((x / (double) srcW) * destW);
            if (targetX >= destW) continue;

            for (int y = 0; y < srcH; y += skipY) {
                int argb = pixels[y * srcW + x];

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int yR = (int) (destH - 1 - (r / 255.0 * (destH - 1)));
                int yG = (int) (destH - 1 - (g / 255.0 * (destH - 1)));
                int yB = (int) (destH - 1 - (b / 255.0 * (destH - 1)));

                addToBuffer(accumBuffer, targetX, yR, destW, 1, 0, 0);
                addToBuffer(accumBuffer, targetX, yG, destW, 0, 1, 0);
                addToBuffer(accumBuffer, targetX, yB, destW, 0, 0, 1);
            }
        }

        WritableImage writableImage = new WritableImage(destW, destH);
        PixelWriter writer = writableImage.getPixelWriter();

        for (int i = 0; i < accumBuffer.length; i++) {
            if (accumBuffer[i] != 0) {
                int x = i % destW;
                int y = i / destW;

                int r = (accumBuffer[i] >> 16) & 0xFF;
                int g = (accumBuffer[i] >> 8) & 0xFF;
                int b = accumBuffer[i] & 0xFF;

                r = Math.min(255, r);
                g = Math.min(255, g);
                b = Math.min(255, b);

                int finalColor = (255 << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, finalColor);
            }
        }
        return writableImage;
    }

    private void addToBuffer(int[] buffer, int x, int y, int width, int rInc, int gInc, int bInc) {
        int index = y * width + x;
        if (index >= 0 && index < buffer.length) {
            int current = buffer[index];
            int r = (current >> 16) & 0xFF;
            int g = (current >> 8) & 0xFF;
            int b = current & 0xFF;

            r += rInc * 40;
            g += gInc * 40;
            b += bInc * 40;

            buffer[index] = (r << 16) | (g << 8) | b;
        }
    }

    private void drawResult(WritableImage image) {
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());

        gc.setStroke(Color.rgb(60, 60, 60));
        gc.setLineWidth(1);
        double h = waveformCanvas.getHeight();
        double w = waveformCanvas.getWidth();
        gc.strokeLine(0, h * 0.25, w, h * 0.25);
        gc.strokeLine(0, h * 0.50, w, h * 0.50);
        gc.strokeLine(0, h * 0.75, w, h * 0.75);

        gc.drawImage(image, 0, 0);
    }

    private void redrawGrid() {
        if (!isProcessing.get()) {
            GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        }
    }

    public void dispose() {
        executor.shutdownNow();
    }
}