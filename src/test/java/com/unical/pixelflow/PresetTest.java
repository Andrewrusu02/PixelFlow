package com.unical.pixelflow;

import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.*;
import com.unical.pixelflow.infrastructure.ImageIOHandler;
import com.unical.pixelflow.infrastructure.ProjectSerializer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PresetTest {

    public static void main(String[] args) throws Exception {
        System.out.println("🧪 TEST: Applicazione Preset su Immagine Diversa");

        ImageIOHandler io = new ImageIOHandler();
        ProjectSerializer serializer = new ProjectSerializer();
        String presetFile = "my_preset.pfp";

        // --- FASE 1: CREAZIONE DEL PRESET (Su Immagine ROSSA) ---
        System.out.println("\n1. Creazione Preset su Immagine A (Rossa)...");
        BufferedImage imgA = createColorImage(Color.RED);
        io.saveImage(imgA, "image_A_source.png", "png");

        Pipeline pipelineA = new Pipeline();
        InputNode inputA = new InputNode(imgA);

        // Catena: Grayscale -> Brightness (+50)
        GrayscaleFilter gray = new GrayscaleFilter();
        BrightnessFilter bright = new BrightnessFilter();
        bright.setParameterValue("brightness", 50);
        OutputNode outputA = new OutputNode();

        pipelineA.addNode(inputA);
        pipelineA.addNode(gray);
        pipelineA.addNode(bright);
        pipelineA.addNode(outputA);

        pipelineA.connect(inputA.getId(), gray.getId());
        pipelineA.connect(gray.getId(), bright.getId());
        pipelineA.connect(bright.getId(), outputA.getId());

        // Salviamo il preset
        serializer.save(pipelineA, "image_A_source.png", presetFile);
        System.out.println("   Preset salvato in: " + presetFile);

        // Verifica del file JSON (Stampiamo a video per controllo)
        // Questo soddisfa la tua richiesta: "restituirmi il json"
        // (nella pratica basta aprirlo con un editor di testo, qui lo stampiamo solo)
        System.out.println("   JSON Generato (Preview):");
        java.nio.file.Files.lines(java.nio.file.Paths.get(presetFile))
                .limit(10).forEach(line -> System.out.println("   " + line));
        System.out.println("   ...");


        // --- FASE 2: CARICAMENTO SU NUOVO INPUT (Immagine BLU) ---
        System.out.println("\n2. Caricamento Preset su Immagine B (Blu)...");

        // Creiamo una nuova immagine totalmente diversa
        BufferedImage imgB = createColorImage(Color.BLUE);
        io.saveImage(imgB, "image_B_source.png", "png");

        // Carichiamo la pipeline dal file
        Pipeline loadedPipeline = serializer.load(presetFile);

        // QUI C'È IL TRUCCO: Sostituiamo l'immagine sorgente!
        // Cerchiamo il nodo di input nella pipeline caricata
        InputNode loadedInputNode = (InputNode) loadedPipeline.getNodes().values().stream()
                .filter(n -> n instanceof InputNode)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nodo input non trovato!"));

        // Iniettiamo la nuova immagine (Blu) al posto di quella salvata nel JSON (Rossa)
        System.out.println("   -> Iniezione Immagine B nel nodo Input caricato.");
        loadedInputNode.setSourceImage(imgB);

        // --- FASE 3: ESECUZIONE E VERIFICA ---
        System.out.println("\n3. Esecuzione...");
        PipelineExecutor executor = new PipelineExecutor();
        BufferedImage result = executor.execute(loadedPipeline);

        io.saveImage(result, "image_B_processed.png", "png");
        System.out.println("   Risultato salvato in: image_B_processed.png");

        // Verifica: Se era BLU (0,0,255), dopo Grayscale diventa Grigio (29) + Brightness (50) = ~79
        int pixel = result.getRGB(50, 50) & 0xFF; // Prendo componente Blu (che in scala di grigi è uguale a R e G)
        System.out.println("   Valore pixel finale: " + pixel);

        if (pixel > 70 && pixel < 90) {
            System.out.println("✅ SUCCESSO! L'immagine Blu è stata elaborata con la catena salvata.");
        } else {
            System.err.println("❌ ERRORE: Il risultato non sembra corretto.");
        }
    }

    private static BufferedImage createColorImage(Color c) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        return img;
    }
}

