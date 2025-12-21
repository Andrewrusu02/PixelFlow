package com.unical.pixelflow;

import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import com.unical.pixelflow.infrastructure.ImageIOHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FullSystemTest {
    public static void main(String[] args) {
        System.out.println("🚀 AVVIO TEST DI SISTEMA COMPLETO...");

        try {
            // --- 1. PREPARAZIONE INFRASTRUTTURA ---
            ImageIOHandler ioHandler = new ImageIOHandler();

            // Creiamo un'immagine di test: un quadrato scuro (Grigio 50)
            // Cosi se aumentiamo la luminosità, lo vedremo diventare chiaro.
            System.out.println("📸 1. Generazione immagine di test (500x500 px, Grigio Scuro)...");
            BufferedImage sourceImg = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = sourceImg.createGraphics();
            g.setColor(new Color(50, 50, 50)); // Grigio molto scuro
            g.fillRect(0, 0, 500, 500);

            // Aggiungiamo una striscia nera e una bianca per confronto
            g.setColor(Color.BLACK);
            g.fillRect(50, 50, 100, 400);
            g.setColor(Color.WHITE);
            g.fillRect(350, 50, 100, 400);
            g.dispose();

            // Salviamo l'originale per confronto
            ioHandler.saveImage(sourceImg, "test_full_original.png", "png");

            // --- 2. COSTRUZIONE DEL DOMINIO (NODI) ---
            System.out.println("🔨 2. Costruzione dei Nodi...");

            InputNode inputNode = new InputNode(sourceImg);

            BrightnessFilter filter = new BrightnessFilter();
            // Aumentiamo la luminosità di 100.
            // Il grigio (50) dovrebbe diventare grigio chiaro (150).
            // Il nero (0) dovrebbe diventare grigio scuro (100).
            // Il bianco (255) dovrebbe rimanere bianco (255 - clampato).
            filter.setParameterValue("brightness", 100);

            OutputNode outputNode = new OutputNode();

            // --- 3. ASSEMBLAGGIO PIPELINE ---
            System.out.println("🔗 3. Collegamento Pipeline...");
            Pipeline pipeline = new Pipeline();
            pipeline.addNode(inputNode);
            pipeline.addNode(filter);
            pipeline.addNode(outputNode);

            // Connessione logica: Input -> Filter -> Output
            pipeline.connect(inputNode.getId(), filter.getId());
            pipeline.connect(filter.getId(), outputNode.getId());

            // --- 4. ESECUZIONE (IL CUORE DEL SISTEMA) ---
            System.out.println("⚡ 4. Esecuzione PipelineExecutor...");
            PipelineExecutor executor = new PipelineExecutor();

            // QUI AVVIENE LA MAGIA: Non dobbiamo fare cicli manuali.
            // L'executor calcola l'ordine e processa tutto.
            BufferedImage result = executor.execute(pipeline);

            // --- 5. VERIFICA RISULTATO ---
            if (result != null) {
                System.out.println("💾 5. Salvataggio risultato in 'test_full_result.png'...");
                ioHandler.saveImage(result, "test_full_result.png", "png");

                // Analisi numerica di un pixel centrale (area grigia originale)
                int rgb = result.getRGB(250, 250);
                int r = (rgb >> 16) & 0xFF;

                System.out.println("\n✅ VERIFICA AUTOMATICA:");
                System.out.println("   Valore Rosso Originale: 50");
                System.out.println("   Valore Rosso Finale:    " + r);

                if (r == 150) {
                    System.out.println("   ESITO: SUCCESSO! (50 + 100 = 150)");
                } else {
                    System.err.println("   ESITO: ERRORE! Valore inatteso.");
                }

                System.out.println("\n📂 Controlla i file nella cartella del progetto!");
            } else {
                System.err.println("❌ ERRORE: L'executor ha restituito null!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}