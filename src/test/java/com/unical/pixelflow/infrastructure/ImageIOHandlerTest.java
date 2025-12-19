// src/test/java/com/pixelflow/infrastructure/ImageIOHandlerTest.java
package com.unical.pixelflow.infrastructure;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per ImageIOHandler.
 * Verifica tutte le funzionalità di caricamento, salvataggio e proxy.
 */
class ImageIOHandlerTest {

    private ImageIOHandler handler;

    /**
     * Directory temporanea per i test (gestita automaticamente da JUnit).
     */
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new ImageIOHandler();
    }

    /**
     * Utility: Crea un'immagine di test con dimensioni e colore specificati.
     */
    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }

    /**
     * Utility: Salva un'immagine temporanea per test di caricamento.
     */
    private File createTempImageFile(String fileName, BufferedImage image)
            throws IOException {
        File file = tempDir.resolve(fileName).toFile();
        javax.imageio.ImageIO.write(image, "png", file);
        return file;
    }

    // ==================== TEST CARICAMENTO ====================

    @Test
    @DisplayName("Dovrebbe caricare correttamente un'immagine PNG")
    void testLoadPNG() throws IOException {
        // Arrange
        BufferedImage original = createTestImage(100, 100, Color.RED);
        File tempFile = createTempImageFile("test.png", original);

        // Act
        BufferedImage loaded = handler.loadImage(tempFile.getAbsolutePath());

        // Assert
        assertNotNull(loaded, "L'immagine caricata non dovrebbe essere null");
        assertEquals(100, loaded.getWidth(), "Larghezza non corretta");
        assertEquals(100, loaded.getHeight(), "Altezza non corretta");
    }

    @Test
    @DisplayName("Dovrebbe caricare correttamente un'immagine JPG")
    void testLoadJPG() throws IOException {
        // Arrange
        BufferedImage original = createTestImage(200, 150, Color.BLUE);
        File tempFile = tempDir.resolve("test.jpg").toFile();
        javax.imageio.ImageIO.write(original, "jpg", tempFile);

        // Act
        BufferedImage loaded = handler.loadImage(tempFile.getAbsolutePath());

        // Assert
        assertNotNull(loaded);
        assertEquals(200, loaded.getWidth());
        assertEquals(150, loaded.getHeight());
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se il file non esiste")
    void testLoadNonExistentFile() {
        // Arrange
        String nonExistentPath = tempDir.resolve("non_existent.png")
                .toAbsolutePath().toString();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            handler.loadImage(nonExistentPath);
        });

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se il path è null")
    void testLoadNullPath() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.loadImage(null);
        });
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se il path è vuoto")
    void testLoadEmptyPath() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.loadImage("   ");
        });
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione per formato non supportato")
    void testLoadUnsupportedFormat() throws IOException {
        // Arrange
        File txtFile = tempDir.resolve("test.txt").toFile();
        txtFile.createNewFile();

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            handler.loadImage(txtFile.getAbsolutePath());
        });

        assertTrue(exception.getMessage().contains("Unsupported format"));
    }

    // ==================== TEST PROXY ====================

    @Test
    @DisplayName("Dovrebbe creare un proxy ridimensionato per immagini grandi")
    void testCreateProxyForLargeImage() {
        // Arrange
        BufferedImage largeImage = createTestImage(2000, 1500, Color.GREEN);

        // Act
        BufferedImage proxy = handler.createProxy(largeImage);

        // Assert
        assertNotNull(proxy);
        assertTrue(proxy.getWidth() <= 800,
                "Larghezza proxy dovrebbe essere <= 800");
        assertTrue(proxy.getHeight() <= 800,
                "Altezza proxy dovrebbe essere <= 800");

        // Verifica mantenimento proporzioni
        double originalRatio = (double) largeImage.getWidth() /
                largeImage.getHeight();
        double proxyRatio = (double) proxy.getWidth() / proxy.getHeight();
        assertEquals(originalRatio, proxyRatio, 0.01,
                "Le proporzioni dovrebbero essere mantenute");
    }

    @Test
    @DisplayName("Dovrebbe restituire l'immagine originale se già piccola")
    void testCreateProxyForSmallImage() {
        // Arrange
        BufferedImage smallImage = createTestImage(500, 400, Color.YELLOW);

        // Act
        BufferedImage proxy = handler.createProxy(smallImage);

        // Assert
        assertSame(smallImage, proxy,
                "Per immagini piccole dovrebbe restituire l'originale");
    }

    @Test
    @DisplayName("Dovrebbe gestire immagini landscape (orizzontali)")
    void testCreateProxyLandscape() {
        // Arrange
        BufferedImage landscape = createTestImage(1600, 900, Color.CYAN);

        // Act
        BufferedImage proxy = handler.createProxy(landscape);

        // Assert
        assertEquals(800, proxy.getWidth(),
                "Larghezza dovrebbe essere scalata a 800");
        assertEquals(450, proxy.getHeight(),
                "Altezza dovrebbe mantenere proporzione");
    }

    @Test
    @DisplayName("Dovrebbe gestire immagini portrait (verticali)")
    void testCreateProxyPortrait() {
        // Arrange
        BufferedImage portrait = createTestImage(900, 1600, Color.MAGENTA);

        // Act
        BufferedImage proxy = handler.createProxy(portrait);

        // Assert
        assertEquals(450, proxy.getWidth(),
                "Larghezza dovrebbe mantenere proporzione");
        assertEquals(800, proxy.getHeight(),
                "Altezza dovrebbe essere scalata a 800");
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se l'immagine è null")
    void testCreateProxyNullImage() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.createProxy(null);
        });
    }

    // ==================== TEST SALVATAGGIO ====================

    @Test
    @DisplayName("Dovrebbe salvare correttamente un'immagine PNG")
    void testSavePNG() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(300, 200, Color.ORANGE);
        String outputPath = tempDir.resolve("output.png")
                .toAbsolutePath().toString();

        // Act
        handler.saveImage(image, outputPath, "png");

        // Assert
        File savedFile = new File(outputPath);
        assertTrue(savedFile.exists(), "Il file dovrebbe esistere");

        BufferedImage loaded = handler.loadImage(outputPath);
        assertEquals(300, loaded.getWidth());
        assertEquals(200, loaded.getHeight());
    }

    @Test
    @DisplayName("Dovrebbe salvare correttamente un'immagine JPG")
    void testSaveJPG() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(400, 300, Color.PINK);
        String outputPath = tempDir.resolve("output.jpg")
                .toAbsolutePath().toString();

        // Act
        handler.saveImage(image, outputPath, "jpg");

        // Assert
        File savedFile = new File(outputPath);
        assertTrue(savedFile.exists());

        BufferedImage loaded = handler.loadImage(outputPath);
        assertEquals(400, loaded.getWidth());
        assertEquals(300, loaded.getHeight());
    }

    @Test
    @DisplayName("Dovrebbe accettare 'jpeg' come formato valido")
    void testSaveJPEGFormat() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(100, 100, Color.GRAY);
        String outputPath = tempDir.resolve("output.jpeg")
                .toAbsolutePath().toString();

        // Act
        handler.saveImage(image, outputPath, "jpeg");

        // Assert
        assertTrue(new File(outputPath).exists());
    }

    @Test
    @DisplayName("Dovrebbe creare directory mancanti durante il salvataggio")
    void testSaveCreatesDirectories() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(100, 100, Color.BLACK);
        String outputPath = tempDir.resolve("subdir/nested/output.png")
                .toAbsolutePath().toString();

        // Act
        handler.saveImage(image, outputPath, "png");

        // Assert
        File savedFile = new File(outputPath);
        assertTrue(savedFile.exists(), "Il file dovrebbe esistere");
        assertTrue(savedFile.getParentFile().exists(),
                "Le directory dovrebbero essere create");
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione per formato non supportato")
    void testSaveUnsupportedFormat() {
        // Arrange
        BufferedImage image = createTestImage(100, 100, Color.WHITE);
        String outputPath = tempDir.resolve("output.bmp")
                .toAbsolutePath().toString();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.saveImage(image, outputPath, "bmp");
        });
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se l'immagine è null")
    void testSaveNullImage() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.saveImage(null, "test.png", "png");
        });
    }

    // ==================== TEST SALVATAGGIO JPEG CON QUALITÀ ====================

    @Test
    @DisplayName("Dovrebbe salvare JPEG con qualità 100")
    void testSaveJPEGHighQuality() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(500, 500, Color.RED);
        String outputPath = tempDir.resolve("high_quality.jpg")
                .toAbsolutePath().toString();

        // Act
        handler.saveJPEG(image, outputPath, 100);

        // Assert
        File savedFile = new File(outputPath);
        assertTrue(savedFile.exists());
        assertTrue(savedFile.length() > 0, "File dovrebbe avere contenuto");
    }

    @Test
    @DisplayName("Dovrebbe salvare JPEG con qualità 50")
    void testSaveJPEGMediumQuality() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(500, 500, Color.BLUE);
        String outputPath = tempDir.resolve("medium_quality.jpg")
                .toAbsolutePath().toString();

        // Act
        handler.saveJPEG(image, outputPath, 50);

        // Assert
        File savedFile = new File(outputPath);
        assertTrue(savedFile.exists());
    }

    @Test
    @DisplayName("File con qualità 100 dovrebbe essere più grande di qualità 10")
    void testJPEGQualityAffectsFileSize() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(800, 600, Color.GREEN);
        String highQualityPath = tempDir.resolve("high.jpg")
                .toAbsolutePath().toString();
        String lowQualityPath = tempDir.resolve("low.jpg")
                .toAbsolutePath().toString();

        // Act
        handler.saveJPEG(image, highQualityPath, 100);
        handler.saveJPEG(image, lowQualityPath, 10);

        // Assert
        long highSize = new File(highQualityPath).length();
        long lowSize = new File(lowQualityPath).length();
        assertTrue(highSize > lowSize,
                "File ad alta qualità dovrebbe essere più grande");
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione per qualità fuori range")
    void testSaveJPEGInvalidQuality() {
        // Arrange
        BufferedImage image = createTestImage(100, 100, Color.BLACK);
        String path = tempDir.resolve("test.jpg").toAbsolutePath().toString();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            handler.saveJPEG(image, path, -1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            handler.saveJPEG(image, path, 101);
        });
    }

    // ==================== TEST UTILITY ====================

    @Test
    @DisplayName("Dovrebbe riconoscere formati supportati")
    void testIsSupportedFormat() {
        assertTrue(handler.isSupportedFormat("jpg"));
        assertTrue(handler.isSupportedFormat("jpeg"));
        assertTrue(handler.isSupportedFormat("png"));
        assertTrue(handler.isSupportedFormat("JPG")); // Case insensitive
        assertTrue(handler.isSupportedFormat("PNG"));

        assertFalse(handler.isSupportedFormat("bmp"));
        assertFalse(handler.isSupportedFormat("gif"));
        assertFalse(handler.isSupportedFormat("txt"));
        assertFalse(handler.isSupportedFormat(null));
        assertFalse(handler.isSupportedFormat(""));
    }

    @Test
    @DisplayName("Dovrebbe ottenere dimensioni senza caricare l'immagine")
    void testGetImageDimensions() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(640, 480, Color.YELLOW);
        File tempFile = createTempImageFile("dimensions.png", image);

        // Act
        int[] dimensions = handler.getImageDimensions(
                tempFile.getAbsolutePath());

        // Assert
        assertEquals(2, dimensions.length);
        assertEquals(640, dimensions[0], "Larghezza non corretta");
        assertEquals(480, dimensions[1], "Altezza non corretta");
    }

    @Test
    @DisplayName("Dovrebbe validare file immagine esistente")
    void testValidateImageFile() throws IOException {
        // Arrange
        BufferedImage image = createTestImage(100, 100, Color.CYAN);
        File validFile = createTempImageFile("valid.png", image);

        // Act & Assert
        assertTrue(handler.validateImageFile(validFile.getAbsolutePath()));
        assertFalse(handler.validateImageFile("non_existent.png"));
        assertFalse(handler.validateImageFile(null));
        assertFalse(handler.validateImageFile(""));
    }

    // ==================== TEST INTEGRAZIONE ====================

    @Test
    @DisplayName("Test integrazione completo: Load → Proxy → Save")
    void testCompleteWorkflow() throws IOException {
        // Arrange
        BufferedImage original = createTestImage(1920, 1080, Color.BLUE);
        File sourceFile = createTempImageFile("source.png", original);

        // Act 1: Carica
        BufferedImage loaded = handler.loadImage(sourceFile.getAbsolutePath());

        // Act 2: Crea proxy
        BufferedImage proxy = handler.createProxy(loaded);

        // Act 3: Salva proxy
        String proxyPath = tempDir.resolve("proxy_output.png")
                .toAbsolutePath().toString();
        handler.saveImage(proxy, proxyPath, "png");

        // Assert
        assertNotNull(loaded);
        assertEquals(1920, loaded.getWidth());
        assertEquals(1080, loaded.getHeight());

        assertTrue(proxy.getWidth() <= 800);
        assertTrue(proxy.getHeight() <= 800);

        File proxyFile = new File(proxyPath);
        assertTrue(proxyFile.exists());
        assertTrue(proxyFile.length() > 0);
    }

    @Test
    @DisplayName("Dovrebbe gestire ciclo completo con diversi formati")
    void testMultiFormatWorkflow() throws IOException {
        // Arrange
        BufferedImage original = createTestImage(1024, 768, Color.RED);

        // PNG → JPG
        String pngPath = tempDir.resolve("test.png")
                .toAbsolutePath().toString();
        handler.saveImage(original, pngPath, "png");

        BufferedImage loadedPNG = handler.loadImage(pngPath);

        String jpgPath = tempDir.resolve("test.jpg")
                .toAbsolutePath().toString();
        handler.saveJPEG(loadedPNG, jpgPath, 90);

        // Assert
        assertTrue(new File(pngPath).exists());
        assertTrue(new File(jpgPath).exists());

        BufferedImage loadedJPG = handler.loadImage(jpgPath);
        assertEquals(1024, loadedJPG.getWidth());
        assertEquals(768, loadedJPG.getHeight());
    }
}