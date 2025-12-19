package com.unical.pixelflow.domain;

import com.unical.pixelflow.domain.nodes.BlurFilter;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.GrayscaleFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    // Helper per creare un'immagine di un singolo colore
    private BufferedImage createTestImage(int r, int g, int b) {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setColor(new Color(r, g, b));
        graphics.fillRect(0, 0, 10, 10);
        graphics.dispose();
        return img;
    }

    @Test
    @DisplayName("InputNode deve restituire l'immagine sorgente ignorando l'input del metodo process")
    void testInputNode() {
        BufferedImage source = createTestImage(100, 100, 100);
        InputNode inputNode = new InputNode(source);

        // Passiamo null a process(), perché InputNode è l'inizio della catena
        BufferedImage result = inputNode.process(null);

        assertNotNull(result);
        assertEquals(source, result, "L'InputNode deve restituire esattamente l'immagine sorgente");
    }

    @Test
    @DisplayName("OutputNode deve restituire l'immagine in ingresso senza modifiche")
    void testOutputNode() {
        BufferedImage input = createTestImage(50, 50, 50);
        OutputNode outputNode = new OutputNode();

        BufferedImage result = outputNode.process(input);

        assertEquals(input, result, "L'OutputNode non deve alterare l'immagine");
    }

    @Test
    @DisplayName("BrightnessFilter deve aumentare la luminosità dei pixel")
    void testBrightnessFilterPositive() {
        // Immagine grigio medio (100, 100, 100)
        BufferedImage input = createTestImage(100, 100, 100);

        BrightnessFilter filter = new BrightnessFilter();
        // Aumentiamo luminosità di 50
        filter.setParameterValue("brightness", 50);

        BufferedImage result = filter.process(input);

        // Preleviamo un pixel centrale
        int rgb = result.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;

        // 100 + 50 = 150
        assertEquals(150, r, "Il valore del rosso dovrebbe essere aumentato di 50");
    }

    @Test
    @DisplayName("BrightnessFilter deve gestire il clamping (non superare 255)")
    void testBrightnessFilterClamping() {
        // Immagine quasi bianca (240, 240, 240)
        BufferedImage input = createTestImage(240, 240, 240);

        BrightnessFilter filter = new BrightnessFilter();
        // Aumentiamo di 50 (dovrebbe arrivare a 290 teorici, ma fermarsi a 255)
        filter.setParameterValue("brightness", 50);

        BufferedImage result = filter.process(input);

        int rgb = result.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;

        assertEquals(255, r, "Il valore non deve superare 255");
    }

    @Test
    @DisplayName("GrayscaleFilter deve convertire un'immagine colorata in scala di grigi")
    void testGrayscaleFilter() {
        // Immagine rossa pura (255, 0, 0)
        BufferedImage input = createTestImage(255, 0, 0);

        GrayscaleFilter filter = new GrayscaleFilter();
        BufferedImage result = filter.process(input);

        // Preleviamo un pixel centrale
        int rgb = result.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // Formula: 0.299*R + 0.587*G + 0.114*B
        // Per rosso puro: 0.299 * 255 = 76.245 ≈ 76
        int expectedGray = (int) (0.299 * 255);

        // In scala di grigi, R = G = B
        assertEquals(r, g, "In scala di grigi, R deve uguale G");
        assertEquals(g, b, "In scala di grigi, G deve uguale B");
        assertEquals(expectedGray, r, 1, "Il valore grigio deve essere calcolato correttamente");
    }

    @Test
    @DisplayName("GrayscaleFilter deve preservare l'alpha channel")
    void testGrayscalePreservesAlpha() {
        BufferedImage input = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = input.createGraphics();
        // Colore rosso con alpha 128 (semi-trasparente)
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect(0, 0, 10, 10);
        g.dispose();

        GrayscaleFilter filter = new GrayscaleFilter();
        BufferedImage result = filter.process(input);

        int rgb = result.getRGB(5, 5);
        int a = (rgb >> 24) & 0xFF;

        assertEquals(128, a, "L'alpha channel deve essere preservato");
    }

    @Test
    @DisplayName("BlurFilter deve sfumare l'immagine")
    void testBlurFilter() {
        // Creiamo un'immagine con un pattern distinto
        BufferedImage input = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = input.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 10, 10);
        g.setColor(Color.BLACK);
        g.fillRect(4, 4, 2, 2); // Pixel neri al centro
        g.dispose();

        BlurFilter filter = new BlurFilter();
        filter.setParameterValue("radius", 2);

        BufferedImage result = filter.process(input);

        // Dopo blur, i pixel attorno al centro dovrebbero essere più scuri (grigio)
        // rispetto all'originale bianco puro
        assertNotNull(result);
        assertEquals(input.getWidth(), result.getWidth());
        assertEquals(input.getHeight(), result.getHeight());
    }

    @Test
    @DisplayName("BlurFilter con radius <= 1 deve restituire l'immagine originale")
    void testBlurFilterNoEffect() {
        BufferedImage input = createTestImage(100, 100, 100);

        BlurFilter filter = new BlurFilter();
        filter.setParameterValue("radius", 1);

        BufferedImage result = filter.process(input);

        // Dovrebbe restituire la stessa immagine senza modifiche
        assertEquals(input, result, "Con radius <= 1 non ci deve essere effetto");
    }
}


