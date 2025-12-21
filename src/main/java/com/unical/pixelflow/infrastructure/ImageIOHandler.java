// src/main/java/com/pixelflow/infrastructure/ImageIOHandler.java
package com.unical.pixelflow.infrastructure;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Handler per operazioni di I/O su immagini.
 * Gestisce caricamento, salvataggio e creazione di proxy a bassa risoluzione.
 *
 * Pattern utilizzato: Facade (semplifica le API complesse di ImageIO)
 */
public class ImageIOHandler {

    /**
     * Dimensione massima (larghezza o altezza) per le immagini proxy.
     * Utilizzato per ottimizzare le prestazioni durante l'editing.
     */
    private static final int PROXY_MAX_SIZE = 800;

    /**
     * Formati immagine supportati dal sistema.
     */
    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png"};

    /**
     * Carica un'immagine dal file system.
     *
     * @param path Percorso assoluto del file immagine
     * @return BufferedImage contenente l'immagine caricata
     * @throws IOException Se il file non esiste, non è leggibile o il formato non è supportato
     * @throws IllegalArgumentException Se il path è null o vuoto
     */
    public BufferedImage loadImage(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }

        if (!file.canRead()) {
            throw new IOException("File is not readable: " + path);
        }

        // verifica formato supportato
        String extension = getFileExtension(path).toLowerCase();
        if (!isSupportedFormat(extension)) {
            throw new IOException("Unsupported format: " + extension +
                    ". Supported formats: jpg, jpeg, png");
        }

        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new IOException("Failed to decode image: " + path);
        }

        return image;
    }

    /**
     * Crea una versione ridotta (proxy) dell'immagine originale.
     * La dimensione massima è PROXY_MAX_SIZE pixel per il lato più lungo.
     * Mantiene le proporzioni originali.
     *
     * @param original Immagine originale da ridimensionare
     * @return BufferedImage ridimensionata (o l'originale se già sotto PROXY_MAX_SIZE)
     * @throws IllegalArgumentException Se l'immagine è null
     */
    public BufferedImage createProxy(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        // Se l'immagine è già piccola, restituiscila così com'è
        if (width <= PROXY_MAX_SIZE && height <= PROXY_MAX_SIZE) {
            return original;
        }

        // Calcola il fattore di scala mantenendo le proporzioni
        double scale = (double) PROXY_MAX_SIZE / Math.max(width, height);
        int newWidth = (int) Math.round(width * scale);
        int newHeight = (int) Math.round(height * scale);

        // Crea l'immagine ridimensionata con rendering di alta qualità
        BufferedImage proxy = new BufferedImage(newWidth, newHeight,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = proxy.createGraphics();

        // Abilita rendering di alta qualità
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Disegna l'immagine ridimensionata
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return proxy;
    }

    /**
     * Salva un'immagine su disco nel formato specificato.
     *
     * @param image Immagine da salvare
     * @param path Percorso di destinazione (include nome file ed estensione)
     * @param format Formato di salvataggio ("jpg", "jpeg" o "png")
     * @throws IOException Se il salvataggio fallisce
     * @throws IllegalArgumentException Se i parametri sono invalidi
     */
    public void saveImage(BufferedImage image, String path, String format)
            throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be null or empty");
        }

        format = format.toLowerCase();
        if (!isSupportedFormat(format)) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        File outputFile = new File(path);

        // Crea le directory necessarie
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }
        }

        // Normalizza il formato (jpeg -> jpg)
        if (format.equals("jpeg")) {
            format = "jpg";
        }

        boolean success = ImageIO.write(image, format, outputFile);

        if (!success) {
            throw new IOException("Failed to write image in format: " + format);
        }
    }

    /**
     * Salva un'immagine JPEG con qualità specificata.
     *
     * @param image Immagine da salvare
     * @param path Percorso di destinazione
     * @param quality Qualità JPEG (0-100, dove 100 è massima qualità)
     * @throws IOException Se il salvataggio fallisce
     * @throws IllegalArgumentException Se quality non è nel range 0-100
     */
    public void saveJPEG(BufferedImage image, String path, int quality)
            throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException(
                    "Quality must be between 0 and 100, got: " + quality);
        }

        File outputFile = new File(path);

        // Crea directory se necessario
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }
        }

        // Ottieni il writer JPEG
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer found");
        }

        ImageWriter writer = writers.next();

        // Configura parametri di compressione
        JPEGImageWriteParam params = new JPEGImageWriteParam(null);
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(quality / 100f);

        try (ImageOutputStream output =
                     ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Verifica se un formato è supportato dal sistema.
     *
     * @param format Estensione del file (es. "jpg", "png")
     * @return true se il formato è supportato, false altrimenti
     */
    public boolean isSupportedFormat(String format) {
        if (format == null) {
            return false;
        }

        String normalizedFormat = format.toLowerCase().trim();
        for (String supported : SUPPORTED_FORMATS) {
            if (supported.equals(normalizedFormat)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Estrae l'estensione da un percorso file.
     *
     * @param path Percorso del file
     * @return Estensione del file (senza punto) o stringa vuota se non presente
     */
    private String getFileExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
            return "";
        }

        return path.substring(lastDotIndex + 1);
    }

    /**
     * Ottiene le dimensioni di un'immagine senza caricarla completamente in memoria.
     * Utile per verificare dimensioni prima del caricamento.
     *
     * @param path Percorso del file immagine
     * @return Array di 2 interi [larghezza, altezza]
     * @throws IOException Se il file non può essere letto
     */
    public int[] getImageDimensions(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }

        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IOException("Failed to read image dimensions");
        }

        int[] dimensions = new int[]{img.getWidth(), img.getHeight()};
        img.flush(); // Libera risorse

        return dimensions;
    }

    /**
     * Verifica se un file esiste ed è un'immagine valida.
     *
     * @param path Percorso del file
     * @return true se il file esiste ed è un'immagine valida, false altrimenti
     */
    public boolean validateImageFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            return false;
        }

        String extension = getFileExtension(path);
        if (!isSupportedFormat(extension)) {
            return false;
        }

        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return false;
            }
            img.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}