package com.unical.pixelflow.domain;

import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.*;
import com.unical.pixelflow.infrastructure.ImageIOHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completo che verifica il funzionamento di una pipeline multi-filtro.
 * Testa sia immagini generate che immagini reali dal filesystem.
 */
class MultiFilterTest {

    private ImageIOHandler ioHandler;
    private PipelineExecutor executor;

    @BeforeEach
    void setUp() {
        ioHandler = new ImageIOHandler();
        executor = new PipelineExecutor();
    }

    /**
     * Helper per creare un'immagine colorata di test
     */
    private BufferedImage createColorfulTestImage() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Sfondo blu
        g.setColor(new Color(100, 150, 255));
        g.fillRect(0, 0, 200, 200);

        // Cerchio rosso
        g.setColor(new Color(255, 50, 50));
        g.fillOval(50, 50, 100, 100);

        // Rettangolo verde
        g.setColor(new Color(50, 255, 50));
        g.fillRect(10, 10, 50, 50);

        g.dispose();
        return img;
    }

    @Test
    @DisplayName("Pipeline Multi-Filtro: Input → Grayscale → Blur → Brightness → Output")
    void testMultiFilterPipeline() {
        System.out.println("🧪 Test Pipeline Multi-Filtro");

        // 1. Crea immagine di test
        BufferedImage testImage = createColorfulTestImage();

        // 2. Costruisci la pipeline
        Pipeline pipeline = new Pipeline();

        InputNode input = new InputNode(testImage);

        // Converti in bianco e nero
        GrayscaleFilter gray = new GrayscaleFilter();

        // Sfoca un po'
        BlurFilter blur = new BlurFilter();
        blur.setParameterValue("radius", 5);

        // Aumenta luminosità
        BrightnessFilter bright = new BrightnessFilter();
        bright.setParameterValue("brightness", 30);

        OutputNode output = new OutputNode();

        // 3. Aggiungi tutti i nodi
        pipeline.addNode(input);
        pipeline.addNode(gray);
        pipeline.addNode(blur);
        pipeline.addNode(bright);
        pipeline.addNode(output);

        // 4. Connetti a catena
        pipeline.connect(input.getId(), gray.getId());
        pipeline.connect(gray.getId(), blur.getId());
        pipeline.connect(blur.getId(), bright.getId());
        pipeline.connect(bright.getId(), output.getId());

        // 5. Esegui
        BufferedImage result = executor.execute(pipeline);

        // 6. Verifica risultato
        assertNotNull(result, "Il risultato non deve essere null");
        assertEquals(200, result.getWidth());
        assertEquals(200, result.getHeight());

        // Verifica che l'immagine sia in scala di grigi (R=G=B)
        int rgb = result.getRGB(100, 100);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        assertEquals(r, g, 2, "In scala di grigi, R ≈ G");
        assertEquals(g, b, 2, "In scala di grigi, G ≈ B");

        System.out.println("✅ Pipeline multi-filtro funziona correttamente");
        System.out.println("   Pixel centrale RGB: (" + r + ", " + g + ", " + b + ")");
    }

    @Test
    @DisplayName("Test con immagine reale: input.jpg dalla root del progetto")
    void testWithRealImage() throws Exception {
        System.out.println("🧪 Test con Immagine Reale (input.jpg)");

        // Percorso dell'immagine nella root del progetto
        File inputFile = new File("input.jpg");

        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato nella root del progetto.");
            System.out.println("   Test saltato. Per eseguirlo, inserisci un'immagine chiamata 'input.jpg' nella root.");
            return; // Skip test se l'immagine non esiste
        }

        System.out.println("📂 Caricamento immagine: " + inputFile.getAbsolutePath());

        // 1. Carica l'immagine
        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());
        assertNotNull(img, "L'immagine deve essere caricata correttamente");

        System.out.println("   Dimensioni: " + img.getWidth() + "x" + img.getHeight());

        // 2. Costruisci pipeline complessa
        Pipeline pipeline = new Pipeline();

        InputNode input = new InputNode(img);

        // Converti in bianco e nero
        GrayscaleFilter gray = new GrayscaleFilter();

        // Sfoca
        BlurFilter blur = new BlurFilter();
        blur.setParameterValue("radius", 5);

        // Aumenta luminosità
        BrightnessFilter bright = new BrightnessFilter();
        bright.setParameterValue("brightness", 30);

        OutputNode output = new OutputNode();

        pipeline.addNode(input);
        pipeline.addNode(gray);
        pipeline.addNode(blur);
        pipeline.addNode(bright);
        pipeline.addNode(output);

        // Connessioni
        pipeline.connect(input.getId(), gray.getId());
        pipeline.connect(gray.getId(), blur.getId());
        pipeline.connect(blur.getId(), bright.getId());
        pipeline.connect(bright.getId(), output.getId());

        // 3. Esegui
        System.out.println("⚙️  Esecuzione pipeline...");
        long startTime = System.currentTimeMillis();

        BufferedImage result = executor.execute(pipeline);

        long endTime = System.currentTimeMillis();
        System.out.println("   Tempo esecuzione: " + (endTime - startTime) + "ms");

        // 4. Verifica
        assertNotNull(result);
        assertEquals(img.getWidth(), result.getWidth());
        assertEquals(img.getHeight(), result.getHeight());

        // 5. Salva risultato
        String outputPath = "output_multifilter.jpg";
        ioHandler.saveImage(result, outputPath, "jpg");

        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Il file di output deve essere creato");

        System.out.println("✅ Pipeline eseguita con successo!");
        System.out.println("   Output salvato: " + outputFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Test singoli filtri su immagine reale")
    void testIndividualFiltersOnRealImage() throws Exception {
        System.out.println("🧪 Test Filtri Individuali su Immagine Reale");

        File inputFile = new File("input.jpg");

        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());
        assertNotNull(img);

        // Test 1: Solo Grayscale
        System.out.println("   Test 1: Grayscale");
        Pipeline p1 = createSimplePipeline(img, new GrayscaleFilter());
        BufferedImage gray = executor.execute(p1);
        ioHandler.saveImage(gray, "output_grayscale.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_grayscale.jpg");

        // Test 2: Solo Blur
        System.out.println("   Test 2: Blur");
        BlurFilter blurFilter = new BlurFilter();
        blurFilter.setParameterValue("radius", 7);
        Pipeline p2 = createSimplePipeline(img, blurFilter);
        BufferedImage blurred = executor.execute(p2);
        ioHandler.saveImage(blurred, "output_blur.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_blur.jpg");

        // Test 3: Solo Brightness
        System.out.println("   Test 3: Brightness");
        BrightnessFilter brightFilter = new BrightnessFilter();
        brightFilter.setParameterValue("brightness", 50);
        Pipeline p3 = createSimplePipeline(img, brightFilter);
        BufferedImage brightened = executor.execute(p3);
        ioHandler.saveImage(brightened, "output_brightness.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_brightness.jpg");

        System.out.println("✅ Tutti i filtri individuali testati con successo!");
    }

    /**
     * Helper per creare una pipeline semplice: Input → Filter → Output
     */
    private Pipeline createSimplePipeline(BufferedImage img, com.unical.pixelflow.domain.model.Filter filter) {
        Pipeline p = new Pipeline();
        InputNode input = new InputNode(img);
        OutputNode output = new OutputNode();

        p.addNode(input);
        p.addNode(filter);
        p.addNode(output);

        p.connect(input.getId(), filter.getId());
        p.connect(filter.getId(), output.getId());

        return p;
    }

    @Test
    @DisplayName("Test ordini diversi di applicazione filtri")
    void testDifferentFilterOrders() {
        System.out.println("🧪 Test Ordini Diversi di Filtri");

        BufferedImage testImage = createColorfulTestImage();

        // Ordine 1: Blur → Grayscale → Brightness
        System.out.println("   Ordine 1: Blur → Grayscale → Brightness");
        Pipeline p1 = new Pipeline();
        InputNode in1 = new InputNode(testImage);
        BlurFilter blur1 = new BlurFilter();
        blur1.setParameterValue("radius", 3);
        GrayscaleFilter gray1 = new GrayscaleFilter();
        BrightnessFilter bright1 = new BrightnessFilter();
        bright1.setParameterValue("brightness", 20);
        OutputNode out1 = new OutputNode();

        p1.addNode(in1);
        p1.addNode(blur1);
        p1.addNode(gray1);
        p1.addNode(bright1);
        p1.addNode(out1);

        p1.connect(in1.getId(), blur1.getId());
        p1.connect(blur1.getId(), gray1.getId());
        p1.connect(gray1.getId(), bright1.getId());
        p1.connect(bright1.getId(), out1.getId());

        BufferedImage result1 = executor.execute(p1);
        assertNotNull(result1);

        // Ordine 2: Brightness → Blur → Grayscale
        System.out.println("   Ordine 2: Brightness → Blur → Grayscale");
        Pipeline p2 = new Pipeline();
        InputNode in2 = new InputNode(testImage);
        BrightnessFilter bright2 = new BrightnessFilter();
        bright2.setParameterValue("brightness", 20);
        BlurFilter blur2 = new BlurFilter();
        blur2.setParameterValue("radius", 3);
        GrayscaleFilter gray2 = new GrayscaleFilter();
        OutputNode out2 = new OutputNode();

        p2.addNode(in2);
        p2.addNode(bright2);
        p2.addNode(blur2);
        p2.addNode(gray2);
        p2.addNode(out2);

        p2.connect(in2.getId(), bright2.getId());
        p2.connect(bright2.getId(), blur2.getId());
        p2.connect(blur2.getId(), gray2.getId());
        p2.connect(gray2.getId(), out2.getId());

        BufferedImage result2 = executor.execute(p2);
        assertNotNull(result2);

        // I risultati saranno diversi (ordine conta!)
        System.out.println("✅ Entrambi gli ordini funzionano correttamente");
        System.out.println("   Nota: I risultati sono diversi perché l'ordine dei filtri conta!");
    }
}

