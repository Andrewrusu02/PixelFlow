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

class GeometricFiltersTest {

    private ImageIOHandler ioHandler;
    private PipelineExecutor executor;

    @BeforeEach
    void setUp() {
        ioHandler = new ImageIOHandler();
        executor = new PipelineExecutor();
    }

    private BufferedImage createTestImage() {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.BLUE);
        g.fillRect(50, 0, 50, 50);
        g.dispose();
        return img;
    }

    @Test
    @DisplayName("FlipFilter orizzontale inverte l'asse X")
    void testFlipHorizontal() {
        BufferedImage input = createTestImage();

        FlipFilter filter = new FlipFilter();
        filter.setParameterValue("horizontal", 1);
        filter.setParameterValue("vertical", 0);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());

        int leftPixel = input.getRGB(10, 25) & 0xFFFFFF;
        int rightFlippedPixel = result.getRGB(89, 25) & 0xFFFFFF;

        assertEquals(leftPixel, rightFlippedPixel, "Flip orizzontale deve invertire X");
    }

    @Test
    @DisplayName("FlipFilter verticale inverte l'asse Y")
    void testFlipVertical() {
        BufferedImage input = createTestImage();

        FlipFilter filter = new FlipFilter();
        filter.setParameterValue("horizontal", 0);
        filter.setParameterValue("vertical", 1);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());

        int topPixel = input.getRGB(25, 10) & 0xFFFFFF;
        int bottomFlippedPixel = result.getRGB(25, 39) & 0xFFFFFF;

        assertEquals(topPixel, bottomFlippedPixel, "Flip verticale deve invertire Y");
    }

    @Test
    @DisplayName("FlipFilter con entrambi attivi inverte X e Y")
    void testFlipBoth() {
        BufferedImage input = createTestImage();

        FlipFilter filter = new FlipFilter();
        filter.setParameterValue("horizontal", 1);
        filter.setParameterValue("vertical", 1);

        BufferedImage result = filter.process(input);

        assertNotNull(result);

        int topLeftOriginal = input.getRGB(10, 10) & 0xFFFFFF;
        int bottomRightFlipped = result.getRGB(89, 39) & 0xFFFFFF;

        assertEquals(topLeftOriginal, bottomRightFlipped, "Flip completo deve invertire entrambi gli assi");
    }

    @Test
    @DisplayName("FlipFilter senza parametri restituisce originale")
    void testFlipNone() {
        BufferedImage input = createTestImage();

        FlipFilter filter = new FlipFilter();
        filter.setParameterValue("horizontal", 0);
        filter.setParameterValue("vertical", 0);

        BufferedImage result = filter.process(input);

        assertEquals(input, result, "Senza flip deve restituire l'originale");
    }

    @Test
    @DisplayName("ScaleFilter riduce dimensioni correttamente")
    void testScaleDown() {
        BufferedImage input = createTestImage();

        ScaleFilter filter = new ScaleFilter();
        filter.setParameterValue("percent", 50);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(50, result.getWidth(), "50% di 100 = 50");
        assertEquals(25, result.getHeight(), "50% di 50 = 25");
    }

    @Test
    @DisplayName("ScaleFilter ingrandisce dimensioni correttamente")
    void testScaleUp() {
        BufferedImage input = createTestImage();

        ScaleFilter filter = new ScaleFilter();
        filter.setParameterValue("percent", 150);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(150, result.getWidth(), "150% di 100 = 150");
        assertEquals(75, result.getHeight(), "150% di 50 = 75");
    }

    @Test
    @DisplayName("ScaleFilter con 100% restituisce originale")
    void testScaleNeutral() {
        BufferedImage input = createTestImage();

        ScaleFilter filter = new ScaleFilter();
        filter.setParameterValue("percent", 100);

        BufferedImage result = filter.process(input);

        assertEquals(input, result, "100% deve restituire l'originale");
    }

    @Test
    @DisplayName("ScaleFilter gestisce valori estremi")
    void testScaleExtremes() {
        BufferedImage input = createTestImage();

        ScaleFilter filter = new ScaleFilter();

        filter.setParameterValue("percent", 10);
        BufferedImage tiny = filter.process(input);
        assertTrue(tiny.getWidth() >= 1 && tiny.getHeight() >= 1,
            "Anche con 10% le dimensioni devono essere >= 1");

        filter.setParameterValue("percent", 200);
        BufferedImage huge = filter.process(input);
        assertEquals(200, huge.getWidth());
        assertEquals(100, huge.getHeight());
    }

    @Test
    @DisplayName("Rotate90Filter ruota di 90 gradi")
    void testRotate90() {
        BufferedImage input = createTestImage();

        Rotate90Filter filter = new Rotate90Filter();
        filter.setParameterValue("angle_steps", 1);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(50, result.getWidth(), "90°: larghezza diventa altezza");
        assertEquals(100, result.getHeight(), "90°: altezza diventa larghezza");
    }

    @Test
    @DisplayName("Rotate90Filter ruota di 180 gradi")
    void testRotate180() {
        BufferedImage input = createTestImage();

        Rotate90Filter filter = new Rotate90Filter();
        filter.setParameterValue("angle_steps", 2);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(100, result.getWidth(), "180°: larghezza rimane uguale");
        assertEquals(50, result.getHeight(), "180°: altezza rimane uguale");

        int topLeft = input.getRGB(0, 0) & 0xFFFFFF;
        int bottomRight = result.getRGB(99, 49) & 0xFFFFFF;
        assertEquals(topLeft, bottomRight, "180° inverte completamente");
    }

    @Test
    @DisplayName("Rotate90Filter ruota di 270 gradi")
    void testRotate270() {
        BufferedImage input = createTestImage();

        Rotate90Filter filter = new Rotate90Filter();
        filter.setParameterValue("angle_steps", 3);

        BufferedImage result = filter.process(input);

        assertNotNull(result);
        assertEquals(50, result.getWidth(), "270°: larghezza diventa altezza");
        assertEquals(100, result.getHeight(), "270°: altezza diventa larghezza");
    }

    @Test
    @DisplayName("Rotate90Filter senza rotazione restituisce originale")
    void testRotateNone() {
        BufferedImage input = createTestImage();

        Rotate90Filter filter = new Rotate90Filter();
        filter.setParameterValue("angle_steps", 0);

        BufferedImage result = filter.process(input);

        assertEquals(input, result, "0° deve restituire l'originale");
    }

    @Test
    @DisplayName("Pipeline geometrica: Flip + Scale + Rotate")
    void testGeometricPipeline() {
        System.out.println("🧪 Test Pipeline Geometrica");

        BufferedImage input = createTestImage();

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        FlipFilter flip = new FlipFilter();
        flip.setParameterValue("horizontal", 1);
        flip.setParameterValue("vertical", 0);

        ScaleFilter scale = new ScaleFilter();
        scale.setParameterValue("percent", 50);

        Rotate90Filter rotate = new Rotate90Filter();
        rotate.setParameterValue("angle_steps", 1);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(flip);
        pipeline.addNode(scale);
        pipeline.addNode(rotate);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), flip.getId());
        pipeline.connect(flip.getId(), scale.getId());
        pipeline.connect(scale.getId(), rotate.getId());
        pipeline.connect(rotate.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        assertEquals(25, result.getWidth(), "Dopo 50% scale e 90° rotate");
        assertEquals(50, result.getHeight(), "Dopo 50% scale e 90° rotate");

        System.out.println("✅ Pipeline Geometrica: Flip(H) → Scale(50%) → Rotate(90°) completata!");
    }

    @Test
    @DisplayName("Test filtri geometrici su immagine reale")
    void testGeometricFiltersOnRealImage() throws Exception {
        System.out.println("🧪 Test Filtri Geometrici su Immagine Reale");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());
        System.out.println("📂 Immagine caricata: " + img.getWidth() + "x" + img.getHeight());

        System.out.println("   Test 1: Flip Horizontal");
        FlipFilter flipH = new FlipFilter();
        flipH.setParameterValue("horizontal", 1);
        flipH.setParameterValue("vertical", 0);
        Pipeline p1 = createSimplePipeline(img, flipH);
        BufferedImage result1 = executor.execute(p1);
        ioHandler.saveImage(result1, "output_flip_h.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_flip_h.jpg");

        System.out.println("   Test 2: Scale 75%");
        ScaleFilter scale = new ScaleFilter();
        scale.setParameterValue("percent", 75);
        Pipeline p2 = createSimplePipeline(img, scale);
        BufferedImage result2 = executor.execute(p2);
        ioHandler.saveImage(result2, "output_scale_75.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_scale_75.jpg (" +
            result2.getWidth() + "x" + result2.getHeight() + ")");

        System.out.println("   Test 3: Rotate 90°");
        Rotate90Filter rotate = new Rotate90Filter();
        rotate.setParameterValue("angle_steps", 1);
        Pipeline p3 = createSimplePipeline(img, rotate);
        BufferedImage result3 = executor.execute(p3);
        ioHandler.saveImage(result3, "output_rotate_90.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_rotate_90.jpg (" +
            result3.getWidth() + "x" + result3.getHeight() + ")");

        System.out.println("✅ Tutti i filtri geometrici testati!");
    }

    @Test
    @DisplayName("Pipeline complessa con geometria e artistici")
    void testComplexGeometricArtisticPipeline() throws Exception {
        System.out.println("🧪 Test Pipeline Geometrica + Artistica");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());

        Pipeline pipeline = new Pipeline();

        InputNode input = new InputNode(img);

        ScaleFilter scale = new ScaleFilter();
        scale.setParameterValue("percent", 80);

        GrayscaleFilter gray = new GrayscaleFilter();
        gray.setParameterValue("intensity", 100);

        EdgeDetectionFilter edges = new EdgeDetectionFilter();

        InvertFilter invert = new InvertFilter();
        invert.setParameterValue("intensity", 100);

        Rotate90Filter rotate = new Rotate90Filter();
        rotate.setParameterValue("angle_steps", 1);

        OutputNode output = new OutputNode();

        pipeline.addNode(input);
        pipeline.addNode(scale);
        pipeline.addNode(gray);
        pipeline.addNode(edges);
        pipeline.addNode(invert);
        pipeline.addNode(rotate);
        pipeline.addNode(output);

        pipeline.connect(input.getId(), scale.getId());
        pipeline.connect(scale.getId(), gray.getId());
        pipeline.connect(gray.getId(), edges.getId());
        pipeline.connect(edges.getId(), invert.getId());
        pipeline.connect(invert.getId(), rotate.getId());
        pipeline.connect(rotate.getId(), output.getId());

        long start = System.currentTimeMillis();
        BufferedImage result = executor.execute(pipeline);
        long elapsed = System.currentTimeMillis() - start;

        ioHandler.saveImage(result, "output_geometric_artistic.jpg", "jpg");

        System.out.println("   Dimensione finale: " + result.getWidth() + "x" + result.getHeight());
        System.out.println("   Tempo esecuzione: " + elapsed + "ms");
        System.out.println("   ✓ Salvato: output_geometric_artistic.jpg");
        System.out.println("✅ Pipeline complessa: Scale → Grayscale → Edges → Invert → Rotate completata!");
    }

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
}

