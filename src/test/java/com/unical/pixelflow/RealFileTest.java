package com.unical.pixelflow;

import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import com.unical.pixelflow.infrastructure.ImageIOHandler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class RealFileTest {

    public static void main(String[] args) {
        System.out.println("PIXELFLOW - REAL IMAGE TEST");
        System.out.println("--------------------------------");

        // Configurazione
        String inputFilename = "input.jpg"; // <--- ASSICURATI CHE QUESTO FILE ESISTA!
        String outputFilename = "output_bright.jpg";
        int brightnessAmount = 50; // Quanta luminosità aggiungere (0-100)

        ImageIOHandler ioHandler = new ImageIOHandler();

        try {
            // 1. CONTROLLO FILE DI INPUT
            File inputFile = new File(inputFilename);
            if (!inputFile.exists()) {
                System.err.println("    ERRORE: Non trovo il file '" + inputFilename + "' nella cartella del progetto.");
                System.out.println("   Per favore, metti un'immagine chiamata 'input.jpg' accanto al pom.xml");
                System.out.println("   oppure modifica la variabile 'inputFilename' nel codice.");
                return;
            }

            // 2. CARICAMENTO
            System.out.println("1. Caricamento immagine da: " + inputFile.getAbsolutePath());
            BufferedImage sourceImg = ioHandler.loadImage(inputFilename);
            System.out.println("   Dimensioni: " + sourceImg.getWidth() + "x" + sourceImg.getHeight() + " pixel");

            // 3. CREAZIONE PIPELINE
            System.out.println("2. Configurazione Pipeline (Brightness: " + brightnessAmount + ")...");

            InputNode inputNode = new InputNode(sourceImg);

            BrightnessFilter brightFilter = new BrightnessFilter();
            brightFilter.setParameterValue("brightness", brightnessAmount);

            OutputNode outputNode = new OutputNode();

            Pipeline pipeline = new Pipeline();
            pipeline.addNode(inputNode);
            pipeline.addNode(brightFilter);
            pipeline.addNode(outputNode);

            pipeline.connect(inputNode.getId(), brightFilter.getId());
            pipeline.connect(brightFilter.getId(), outputNode.getId());

            // 4. ESECUZIONE
            System.out.println("3. Elaborazione in corso...");
            long startTime = System.currentTimeMillis();

            PipelineExecutor executor = new PipelineExecutor();
            BufferedImage result = executor.execute(pipeline);

            long endTime = System.currentTimeMillis();
            System.out.println("   Elaborazione completata in " + (endTime - startTime) + " ms");

            // 5. SALVATAGGIO
            if (result != null) {
                System.out.println("4. Salvataggio risultato in: " + outputFilename);
                // Salviamo in JPG con qualità alta (90)
                ioHandler.saveJPEG(result, outputFilename, 90);
                System.out.println("FATTO! Controlla il file '" + outputFilename + "'");
            } else {
                System.err.println("ERRORE: Risultato nullo dall'executor.");
            }

        } catch (Exception e) {
            System.err.println("ECCEZIONE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}