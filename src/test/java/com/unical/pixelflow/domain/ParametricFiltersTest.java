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

class ParametricFiltersTest {

    private ImageIOHandler ioHandler;
    private PipelineExecutor executor;

    @BeforeEach
    void setUp() {
        ioHandler = new ImageIOHandler();
        executor = new PipelineExecutor();
    }

    private BufferedImage createTestImage(Color color) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        return img;
    }

    @Test
    @DisplayName("GrayscaleFilter con intensity=0 deve restituire originale")
    void testGrayscaleIntensityZero() {
        BufferedImage input = createTestImage(new Color(100, 150, 200));

        GrayscaleFilter filter = new GrayscaleFilter();
        filter.setParameterValue("intensity", 0);
        BufferedImage result = filter.process(input);

        int originalRgb = input.getRGB(50, 50);
        int resultRgb = result.getRGB(50, 50);

        assertEquals(originalRgb, resultRgb, "Intensity=0 deve preservare colori originali");
    }

    @Test
    @DisplayName("GrayscaleFilter con intensity=50 deve creare mix")
    void testGrayscaleIntensityPartial() {
        BufferedImage input = createTestImage(new Color(200, 100, 50));

        GrayscaleFilter filter = new GrayscaleFilter();
        filter.setParameterValue("intensity", 50);
        BufferedImage result = filter.process(input);

        int rgb = result.getRGB(50, 50);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        assertTrue(r != 200 && r != g, "Intensity=50 deve creare un mix tra colore e grigio");
        assertTrue(Math.abs(r - g) < 60, "I canali dovrebbero avvicinarsi");
    }

    @Test
    @DisplayName("InvertFilter con intensity variabile")
    void testInvertIntensityVariable() {
        BufferedImage input = createTestImage(new Color(100, 100, 100));

        InvertFilter filter = new InvertFilter();

        filter.setParameterValue("intensity", 0);
        BufferedImage result0 = filter.process(input);
        int rgb0 = result0.getRGB(50, 50) & 0xFFFFFF;

        filter.setParameterValue("intensity", 100);
        BufferedImage result100 = filter.process(input);
        int rgb100 = result100.getRGB(50, 50) & 0xFFFFFF;

        assertNotEquals(rgb0, rgb100, "Intensity diversa deve produrre risultati diversi");
        assertEquals(0x646464, rgb0, "Intensity=0 deve preservare originale");
        assertEquals(0x9B9B9B, rgb100, "Intensity=100 deve invertire completamente");
    }

    @Test
    @DisplayName("SepiaFilter con intensity=50 deve dare tono vintage leggero")
    void testSepiaIntensityPartial() {
        BufferedImage input = createTestImage(new Color(100, 100, 100));

        SepiaFilter filter = new SepiaFilter();
        filter.setParameterValue("intensity", 50);
        BufferedImage result = filter.process(input);

        int rgb = result.getRGB(50, 50);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        assertTrue(r > g && g > b, "Sepia deve mantenere: Rosso > Verde > Blu");
        assertTrue(r < 140, "Con intensity=50, il rosso non dovrebbe essere al massimo");
    }

    @Test
    @DisplayName("SharpenFilter con amount variabile")
    void testSharpenAmountVariable() {
        BufferedImage input = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = input.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 20, 20);
        g.setColor(Color.BLACK);
        g.fillRect(8, 8, 4, 4);
        g.dispose();

        SharpenFilter filter = new SharpenFilter();

        filter.setParameterValue("amount", 0);
        BufferedImage result0 = filter.process(input);

        filter.setParameterValue("amount", 100);
        BufferedImage result100 = filter.process(input);

        assertNotNull(result0);
        assertNotNull(result100);

        assertEquals(input.getRGB(0, 0), result0.getRGB(0, 0),
            "Amount=0 dovrebbe preservare originale");
    }

    @Test
    @DisplayName("ThresholdFilter con valori soglia diversi")
    void testThresholdDifferentValues() {
        BufferedImage input = createTestImage(new Color(128, 128, 128));

        ThresholdFilter filter = new ThresholdFilter();

        filter.setParameterValue("threshold", 100);
        BufferedImage result1 = filter.process(input);
        int rgb1 = result1.getRGB(50, 50) & 0xFFFFFF;

        filter.setParameterValue("threshold", 150);
        BufferedImage result2 = filter.process(input);
        int rgb2 = result2.getRGB(50, 50) & 0xFFFFFF;

        assertEquals(0xFFFFFF, rgb1, "Soglia 100: pixel dovrebbe essere bianco");
        assertEquals(0x000000, rgb2, "Soglia 150: pixel dovrebbe essere nero");
    }

    @Test
    @DisplayName("ContrastFilter con amount=100 è neutrale")
    void testContrastNeutral() {
        BufferedImage input = createTestImage(new Color(128, 128, 128));

        ContrastFilter filter = new ContrastFilter();
        filter.setParameterValue("amount", 100);
        BufferedImage result = filter.process(input);

        int originalRgb = input.getRGB(50, 50);
        int resultRgb = result.getRGB(50, 50);

        int originalR = (originalRgb >> 16) & 0xFF;
        int resultR = (resultRgb >> 16) & 0xFF;

        assertTrue(Math.abs(originalR - resultR) <= 2,
            "Amount=100 dovrebbe lasciare l'immagine quasi invariata");
    }

    @Test
    @DisplayName("Pipeline parametrica complessa: Vintage Look")
    void testVintagePipeline() {
        System.out.println("🧪 Test Pipeline Vintage Parametrica");

        BufferedImage input = createTestImage(new Color(150, 120, 100));

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        GrayscaleFilter desaturate = new GrayscaleFilter();
        desaturate.setParameterValue("intensity", 30);

        SepiaFilter sepia = new SepiaFilter();
        sepia.setParameterValue("intensity", 70);

        ContrastFilter contrast = new ContrastFilter();
        contrast.setParameterValue("amount", 110);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(desaturate);
        pipeline.addNode(sepia);
        pipeline.addNode(contrast);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), desaturate.getId());
        pipeline.connect(desaturate.getId(), sepia.getId());
        pipeline.connect(sepia.getId(), contrast.getId());
        pipeline.connect(contrast.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        System.out.println("✅ Pipeline Vintage: Desaturate(30%) → Sepia(70%) → Contrast(110%) completata!");
    }

    @Test
    @DisplayName("Pipeline parametrica: Sketch Effect")
    void testSketchPipeline() {
        System.out.println("🧪 Test Pipeline Sketch Parametrica");

        BufferedImage input = createTestImage(new Color(120, 130, 140));

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        GrayscaleFilter gray = new GrayscaleFilter();
        gray.setParameterValue("intensity", 100);

        EdgeDetectionFilter edges = new EdgeDetectionFilter();

        InvertFilter invert = new InvertFilter();
        invert.setParameterValue("intensity", 100);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(gray);
        pipeline.addNode(edges);
        pipeline.addNode(invert);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), gray.getId());
        pipeline.connect(gray.getId(), edges.getId());
        pipeline.connect(edges.getId(), invert.getId());
        pipeline.connect(invert.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        System.out.println("✅ Pipeline Sketch: Grayscale(100%) → Edges → Invert(100%) completata!");
    }

    @Test
    @DisplayName("Pipeline parametrica: Soft Enhancement")
    void testSoftEnhancementPipeline() {
        System.out.println("🧪 Test Pipeline Soft Enhancement");

        BufferedImage input = createTestImage(new Color(140, 140, 140));

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        BrightnessFilter brightness = new BrightnessFilter();
        brightness.setParameterValue("brightness", 15);

        ContrastFilter contrast = new ContrastFilter();
        contrast.setParameterValue("amount", 105);

        SharpenFilter sharpen = new SharpenFilter();
        sharpen.setParameterValue("amount", 40);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(brightness);
        pipeline.addNode(contrast);
        pipeline.addNode(sharpen);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), brightness.getId());
        pipeline.connect(brightness.getId(), contrast.getId());
        pipeline.connect(contrast.getId(), sharpen.getId());
        pipeline.connect(sharpen.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        System.out.println("✅ Pipeline Soft: Brightness(+15) → Contrast(105%) → Sharpen(40%) completata!");
    }

    @Test
    @DisplayName("Test filtri parametrici su immagine reale")
    void testParametricFiltersOnRealImage() throws Exception {
        System.out.println("🧪 Test Filtri Parametrici su Immagine Reale");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());
        System.out.println("📂 Immagine caricata: " + img.getWidth() + "x" + img.getHeight());

        System.out.println("   Test 1: Grayscale 50%");
        GrayscaleFilter gray50 = new GrayscaleFilter();
        gray50.setParameterValue("intensity", 50);
        Pipeline p1 = createSimplePipeline(img, gray50);
        BufferedImage result1 = executor.execute(p1);
        ioHandler.saveImage(result1, "output_grayscale_50.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_grayscale_50.jpg");

        System.out.println("   Test 2: Sepia 60%");
        SepiaFilter sepia60 = new SepiaFilter();
        sepia60.setParameterValue("intensity", 60);
        Pipeline p2 = createSimplePipeline(img, sepia60);
        BufferedImage result2 = executor.execute(p2);
        ioHandler.saveImage(result2, "output_sepia_60.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_sepia_60.jpg");

        System.out.println("   Test 3: Sharpen 50%");
        SharpenFilter sharpen50 = new SharpenFilter();
        sharpen50.setParameterValue("amount", 50);
        Pipeline p3 = createSimplePipeline(img, sharpen50);
        BufferedImage result3 = executor.execute(p3);
        ioHandler.saveImage(result3, "output_sharpen_50.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_sharpen_50.jpg");

        System.out.println("   Test 4: Invert 75%");
        InvertFilter invert75 = new InvertFilter();
        invert75.setParameterValue("intensity", 75);
        Pipeline p4 = createSimplePipeline(img, invert75);
        BufferedImage result4 = executor.execute(p4);
        ioHandler.saveImage(result4, "output_invert_75.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_invert_75.jpg");

        System.out.println("✅ Tutti i filtri parametrici testati!");
    }

    @Test
    @DisplayName("Test pipeline artistica vintage complessa su immagine reale")
    void testComplexVintagePipelineRealImage() throws Exception {
        System.out.println("🧪 Test Pipeline Vintage Complessa");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());

        Pipeline pipeline = new Pipeline();

        InputNode input = new InputNode(img);

        GrayscaleFilter desaturate = new GrayscaleFilter();
        desaturate.setParameterValue("intensity", 25);

        SepiaFilter sepia = new SepiaFilter();
        sepia.setParameterValue("intensity", 80);

        ContrastFilter contrast = new ContrastFilter();
        contrast.setParameterValue("amount", 115);

        SharpenFilter sharpen = new SharpenFilter();
        sharpen.setParameterValue("amount", 30);

        OutputNode output = new OutputNode();

        pipeline.addNode(input);
        pipeline.addNode(desaturate);
        pipeline.addNode(sepia);
        pipeline.addNode(contrast);
        pipeline.addNode(sharpen);
        pipeline.addNode(output);

        pipeline.connect(input.getId(), desaturate.getId());
        pipeline.connect(desaturate.getId(), sepia.getId());
        pipeline.connect(sepia.getId(), contrast.getId());
        pipeline.connect(contrast.getId(), sharpen.getId());
        pipeline.connect(sharpen.getId(), output.getId());

        long start = System.currentTimeMillis();
        BufferedImage result = executor.execute(pipeline);
        long elapsed = System.currentTimeMillis() - start;

        ioHandler.saveImage(result, "output_vintage_complex.jpg", "jpg");

        System.out.println("   Tempo esecuzione: " + elapsed + "ms");
        System.out.println("   ✓ Salvato: output_vintage_complex.jpg");
        System.out.println("✅ Pipeline Vintage Complessa completata!");
    }

    @Test
    @DisplayName("PixelateFilter con dimensioni diverse")
    void testPixelateFilter() {
        BufferedImage input = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = input.createGraphics();
        g.setColor(new Color(100, 150, 200));
        g.fillRect(0, 0, 50, 100);
        g.setColor(new Color(200, 100, 50));
        g.fillRect(50, 0, 50, 100);
        g.dispose();

        PixelateFilter filter = new PixelateFilter();

        filter.setParameterValue("pixelSize", 2);
        BufferedImage result1 = filter.process(input);
        assertNotNull(result1, "PixelSize=2 dovrebbe funzionare");

        filter.setParameterValue("pixelSize", 10);
        BufferedImage result2 = filter.process(input);
        assertNotNull(result2, "PixelSize=10 dovrebbe funzionare");

        assertEquals(100, result1.getWidth());
        assertEquals(100, result1.getHeight());
    }

    @Test
    @DisplayName("VignetteFilter con intensity=0 preserva originale")
    void testVignetteIntensityZero() {
        BufferedImage input = createTestImage(new Color(150, 150, 150));

        VignetteFilter filter = new VignetteFilter();
        filter.setParameterValue("intensity", 0);
        BufferedImage result = filter.process(input);

        assertEquals(input.getRGB(50, 50), result.getRGB(50, 50),
            "Intensity=0 non dovrebbe modificare l'immagine");
    }

    @Test
    @DisplayName("VignetteFilter scurisce i bordi")
    void testVignetteDarkensEdges() {
        BufferedImage input = createTestImage(new Color(200, 200, 200));

        VignetteFilter filter = new VignetteFilter();
        filter.setParameterValue("intensity", 80);
        BufferedImage result = filter.process(input);

        int centerRgb = result.getRGB(50, 50);
        int edgeRgb = result.getRGB(0, 0);

        int centerR = (centerRgb >> 16) & 0xFF;
        int edgeR = (edgeRgb >> 16) & 0xFF;

        assertTrue(centerR > edgeR,
            "Il centro dovrebbe essere più chiaro dei bordi");
    }

    @Test
    @DisplayName("SolarizeFilter con threshold variabile")
    void testSolarizeThreshold() {
        BufferedImage input = createTestImage(new Color(200, 100, 50));

        SolarizeFilter filter = new SolarizeFilter();

        filter.setParameterValue("threshold", 255);
        BufferedImage result1 = filter.process(input);
        assertEquals(input.getRGB(50, 50), result1.getRGB(50, 50),
            "Threshold=255 non dovrebbe modificare nulla");

        filter.setParameterValue("threshold", 150);
        BufferedImage result2 = filter.process(input);

        int rgb2 = result2.getRGB(50, 50);
        int r2 = (rgb2 >> 16) & 0xFF;

        assertEquals(55, r2, "R=200 > 150, quindi dovrebbe essere invertito a 55");
    }

    @Test
    @DisplayName("Pipeline artistica: Retro Poster Effect")
    void testRetroPosterPipeline() {
        System.out.println("🧪 Test Pipeline Retro Poster");

        BufferedImage input = createTestImage(new Color(180, 140, 100));

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        PixelateFilter pixelate = new PixelateFilter();
        pixelate.setParameterValue("pixelSize", 8);

        ThresholdFilter threshold = new ThresholdFilter();
        threshold.setParameterValue("threshold", 120);

        SolarizeFilter solarize = new SolarizeFilter();
        solarize.setParameterValue("threshold", 100);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(pixelate);
        pipeline.addNode(threshold);
        pipeline.addNode(solarize);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), pixelate.getId());
        pipeline.connect(pixelate.getId(), threshold.getId());
        pipeline.connect(threshold.getId(), solarize.getId());
        pipeline.connect(solarize.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        System.out.println("✅ Pipeline Retro: Pixelate(8) → Threshold(120) → Solarize(100) completata!");
    }

    @Test
    @DisplayName("Pipeline artistica: Dreamy Focus")
    void testDreamyFocusPipeline() {
        System.out.println("🧪 Test Pipeline Dreamy Focus");

        BufferedImage input = createTestImage(new Color(150, 170, 190));

        Pipeline pipeline = new Pipeline();

        InputNode inputNode = new InputNode(input);

        BlurFilter blur = new BlurFilter();
        blur.setParameterValue("radius", 5);

        BrightnessFilter brightness = new BrightnessFilter();
        brightness.setParameterValue("brightness", 20);

        VignetteFilter vignette = new VignetteFilter();
        vignette.setParameterValue("intensity", 60);

        OutputNode output = new OutputNode();

        pipeline.addNode(inputNode);
        pipeline.addNode(blur);
        pipeline.addNode(brightness);
        pipeline.addNode(vignette);
        pipeline.addNode(output);

        pipeline.connect(inputNode.getId(), blur.getId());
        pipeline.connect(blur.getId(), brightness.getId());
        pipeline.connect(brightness.getId(), vignette.getId());
        pipeline.connect(vignette.getId(), output.getId());

        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
        System.out.println("✅ Pipeline Dreamy: Blur(5) → Brightness(+20) → Vignette(60) completata!");
    }

    @Test
    @DisplayName("Test filtri artistici su immagine reale")
    void testArtisticFiltersOnRealImage() throws Exception {
        System.out.println("🧪 Test Filtri Artistici su Immagine Reale");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());
        System.out.println("📂 Immagine caricata: " + img.getWidth() + "x" + img.getHeight());

        System.out.println("   Test 1: Pixelate 15px");
        PixelateFilter pixelate = new PixelateFilter();
        pixelate.setParameterValue("pixelSize", 15);
        Pipeline p1 = createSimplePipeline(img, pixelate);
        BufferedImage result1 = executor.execute(p1);
        ioHandler.saveImage(result1, "output_pixelate_15.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_pixelate_15.jpg");

        System.out.println("   Test 2: Vignette 70%");
        VignetteFilter vignette = new VignetteFilter();
        vignette.setParameterValue("intensity", 70);
        Pipeline p2 = createSimplePipeline(img, vignette);
        BufferedImage result2 = executor.execute(p2);
        ioHandler.saveImage(result2, "output_vignette_70.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_vignette_70.jpg");

        System.out.println("   Test 3: Solarize 120");
        SolarizeFilter solarize = new SolarizeFilter();
        solarize.setParameterValue("threshold", 120);
        Pipeline p3 = createSimplePipeline(img, solarize);
        BufferedImage result3 = executor.execute(p3);
        ioHandler.saveImage(result3, "output_solarize_120.jpg", "jpg");
        System.out.println("   ✓ Salvato: output_solarize_120.jpg");

        System.out.println("✅ Tutti i filtri artistici testati!");
    }

    @Test
    @DisplayName("Pipeline artistica completa su immagine reale")
    void testCompleteArtisticPipeline() throws Exception {
        System.out.println("🧪 Test Pipeline Artistica Ultra-Complessa");

        File inputFile = new File("input.jpg");
        if (!inputFile.exists()) {
            System.out.println("⚠️  File input.jpg non trovato. Test saltato.");
            return;
        }

        BufferedImage img = ioHandler.loadImage(inputFile.getAbsolutePath());

        Pipeline pipeline = new Pipeline();

        InputNode input = new InputNode(img);

        PixelateFilter pixelate = new PixelateFilter();
        pixelate.setParameterValue("pixelSize", 12);

        SolarizeFilter solarize = new SolarizeFilter();
        solarize.setParameterValue("threshold", 100);

        ContrastFilter contrast = new ContrastFilter();
        contrast.setParameterValue("amount", 120);

        VignetteFilter vignette = new VignetteFilter();
        vignette.setParameterValue("intensity", 75);

        OutputNode output = new OutputNode();

        pipeline.addNode(input);
        pipeline.addNode(pixelate);
        pipeline.addNode(solarize);
        pipeline.addNode(contrast);
        pipeline.addNode(vignette);
        pipeline.addNode(output);

        pipeline.connect(input.getId(), pixelate.getId());
        pipeline.connect(pixelate.getId(), solarize.getId());
        pipeline.connect(solarize.getId(), contrast.getId());
        pipeline.connect(contrast.getId(), vignette.getId());
        pipeline.connect(vignette.getId(), output.getId());

        long start = System.currentTimeMillis();
        BufferedImage result = executor.execute(pipeline);
        long elapsed = System.currentTimeMillis() - start;

        ioHandler.saveImage(result, "output_artistic_ultra.jpg", "jpg");

        System.out.println("   Tempo esecuzione: " + elapsed + "ms");
        System.out.println("   ✓ Salvato: output_artistic_ultra.jpg");
        System.out.println("✅ Pipeline Ultra: Pixelate → Solarize → Contrast → Vignette completata!");
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

