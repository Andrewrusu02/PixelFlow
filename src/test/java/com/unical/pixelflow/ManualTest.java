// src/test/java/com/unical/pixelflow/ManualTest.java
package com.unical.pixelflow;

import com.unical.pixelflow.infrastructure.ImageIOHandler;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ManualTest {
    public static void main(String[] args) throws Exception {
        ImageIOHandler handler = new ImageIOHandler();

        // creo un'immagine di prova
        System.out.println("Creazione immagine test...");
        BufferedImage testImg = new BufferedImage(2000, 1500,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImg.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 2000, 1500);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 100));
        g.drawString("RUSU GAY", 300, 750);
        g.dispose();

        // salvo l'immagine originale
        System.out.println("Salvataggio originale...");
        handler.saveImage(testImg, "test_original.png", "png");
        System.out.println("Salvato: test_original.png (2000x1500)");

        // creo il proxy
        System.out.println("Creazione proxy...");
        BufferedImage proxy = handler.createProxy(testImg);
        handler.saveImage(proxy, "test_proxy.png", "png");
        System.out.println("Salvato: test_proxy.png (" +
            proxy.getWidth() + "x" + proxy.getHeight() + ")");

        // ricarica e salva in jpg con varie qualità
        System.out.println("Export JPEG con qualità variabile...");
        handler.saveJPEG(testImg, "test_quality_100.jpg", 100);
        handler.saveJPEG(testImg, "test_quality_50.jpg", 50);
        handler.saveJPEG(testImg, "test_quality_10.jpg", 10);

        // confronto dimensione file
        long size100 = new File("test_quality_100.jpg").length() / 1024;
        long size50 = new File("test_quality_50.jpg").length() / 1024;
        long size10 = new File("test_quality_10.jpg").length() / 1024;

        System.out.println("\nRISULTATI:");
        System.out.println("   Quality 100: " + size100 + " KB");
        System.out.println("   Quality 50:  " + size50 + " KB");
        System.out.println("   Quality 10:  " + size10 + " KB");

        // verifica dimensione proxy
        int[] dims = handler.getImageDimensions("test_proxy.png");
        System.out.println("\nDimensioni proxy: " + dims[0] + "x" + dims[1]);
        System.out.println("   Riduzione: " +
            (100 - (dims[0] * 100 / 2000)) + "% larghezza");

        System.out.println("\nTUTTI I TEST MANUALI COMPLETATI!");
        System.out.println("   Controlla i file generati nella root del progetto.");
    }
}

