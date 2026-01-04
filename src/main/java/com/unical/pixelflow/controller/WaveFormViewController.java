package com.unical.pixelflow.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class WaveFormViewController {

    @FXML private VBox root;
    @FXML private StackPane canvasContainer;
    @FXML private Canvas waveformCanvas;

    // DEFINIZIONE EXECUTOR CON THREAD DAEMON (Risolve il problema della chiusura app)
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "waveform-thread");
        t.setDaemon(true); // Fondamentale: permette all'app di chiudersi completamente
        return t;
    });

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        // Binding per il ridimensionamento automatico
        if (canvasContainer != null && waveformCanvas != null) {
            waveformCanvas.widthProperty().bind(canvasContainer.widthProperty());
            waveformCanvas.heightProperty().bind(canvasContainer.heightProperty());

            canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> redrawGrid());
            canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> redrawGrid());
        }
        redrawGrid();
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

        if (reader == null) {
            isProcessing.set(false);
            return;
        }

        int[] srcPixels = new int[srcW * srcH];
        try {
            reader.getPixels(0, 0, srcW, srcH, javafx.scene.image.PixelFormat.getIntArgbInstance(), srcPixels, 0, srcW);
        } catch (Exception e) {
            isProcessing.set(false);
            return;
        }

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

    // Algoritmo Density RGB (stile DaVinci Resolve / Lightroom)
    private WritableImage generateWaveformData(int[] pixels, int srcW, int srcH, int destW, int destH) {
        float[][] densityBufferR = new float[destW][destH];
        float[][] densityBufferG = new float[destW][destH];
        float[][] densityBufferB = new float[destW][destH];

        int skip = 2; // Aumenta a 3 o 4 se noti lag con immagini grandi
        float intensity = 0.15f;

        for (int x = 0; x < srcW; x += skip) {
            int targetX = (int) ((x / (double) srcW) * destW);
            if (targetX >= destW) continue;

            for (int y = 0; y < srcH; y += skip) {
                int argb = pixels[y * srcW + x];

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Inversione asse Y per il canvas
                int yR = (int) (destH - 1 - (r / 255.0 * (destH - 1)));
                int yG = (int) (destH - 1 - (g / 255.0 * (destH - 1)));
                int yB = (int) (destH - 1 - (b / 255.0 * (destH - 1)));

                if (yR >= 0 && yR < destH) densityBufferR[targetX][yR] += intensity;
                if (yG >= 0 && yG < destH) densityBufferG[targetX][yG] += intensity;
                if (yB >= 0 && yB < destH) densityBufferB[targetX][yB] += intensity;
            }
        }

        WritableImage writableImage = new WritableImage(destW, destH);
        PixelWriter writer = writableImage.getPixelWriter();

        for (int x = 0; x < destW; x++) {
            for (int y = 0; y < destH; y++) {
                float valR = densityBufferR[x][y];
                float valG = densityBufferG[x][y];
                float valB = densityBufferB[x][y];

                if (valR <= 0 && valG <= 0 && valB <= 0) continue;

                int r = Math.min(255, (int)(valR * 255));
                int g = Math.min(255, (int)(valG * 255));
                int b = Math.min(255, (int)(valB * 255));

                int maxVal = Math.max(r, Math.max(g, b));
                int alpha = Math.min(255, maxVal);

                int finalColor = (alpha << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, finalColor);
            }
        }
        return writableImage;
    }

    private void drawResult(WritableImage image) {
        if (waveformCanvas == null) return;
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();

        // Pulisce lo sfondo per la trasparenza
        gc.clearRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());

        // Disegna linee guida
        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(1);
        double h = waveformCanvas.getHeight();
        double w = waveformCanvas.getWidth();
        gc.strokeLine(0, h * 0.25, w, h * 0.25);
        gc.strokeLine(0, h * 0.50, w, h * 0.50);
        gc.strokeLine(0, h * 0.75, w, h * 0.75);

        gc.drawImage(image, 0, 0);
    }

    private void redrawGrid() {
        if (!isProcessing.get() && waveformCanvas != null) {
            GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        }
    }

    public void dispose() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}