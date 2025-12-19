package com.unical.pixelflow.domain;

import com.unical.pixelflow.domain.execution.PipelineExecutor;
import com.unical.pixelflow.domain.model.Pipeline;
import com.unical.pixelflow.domain.nodes.BrightnessFilter;
import com.unical.pixelflow.domain.nodes.InputNode;
import com.unical.pixelflow.domain.nodes.OutputNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class PipelineExecutorTest {

    @Test
    @DisplayName("L'Executor deve processare l'intera catena e produrre un risultato")
    void testExecuteCompleteFlow() {
        // 1. Setup Immagine (Grigio scuro: 50, 50, 50)
        BufferedImage source = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = source.createGraphics();
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        // 2. Setup Pipeline
        Pipeline pipeline = new Pipeline();
        InputNode input = new InputNode(source);

        BrightnessFilter bright = new BrightnessFilter();
        bright.setParameterValue("brightness", 50); // Aggiunge 50 -> Totale atteso 100

        OutputNode output = new OutputNode();

        pipeline.addNode(input);
        pipeline.addNode(bright);
        pipeline.addNode(output);

        // Connessioni (l'ordine di inserimento non conta, conta la logica del grafo)
        pipeline.connect(input.getId(), bright.getId());
        pipeline.connect(bright.getId(), output.getId());

        // 3. Esecuzione
        PipelineExecutor executor = new PipelineExecutor();
        BufferedImage result = executor.execute(pipeline);

        // 4. Verifica
        assertNotNull(result, "Il risultato non deve essere null");

        // Verifica pixel (50 iniziale + 50 filtro = 100)
        int rgb = result.getRGB(50, 50);
        int r = (rgb >> 16) & 0xFF;
        assertEquals(100, r, "Il filtro Brightness dovrebbe essere stato applicato");
    }

    @Test
    @DisplayName("Se la pipeline è disconnessa, l'executor esegue comunque i nodi nell'ordine possibile")
    void testDisconnectedPipeline() {
        // Test interessante: cosa succede se ho un InputNode e un Filtro NON collegati?
        // L'ordinamento topologico li metterà comunque in lista.
        // L'InputNode produrrà l'immagine.
        // Il Filtro riceverà l'immagine dall'InputNode? NO.
        // Nella nostra implementazione attuale del ciclo for, 'currentImage' viene passato sequenzialmente.
        // Se non c'è connessione logica nella Pipeline, il topological sort mette comunque i nodi in sequenza
        // se non ci sono dipendenze.
        // PERO': In un sistema a nodi reale, i dati fluiscono solo se c'è connessione.
        // La nostra implementazione SEMPLIFICATA attuale passa l'output di N all'input di N+1 nella lista ordinata.
        // Questo è un comportamento accettabile per un MVP lineare, ma nota bene:
        // Se volessimo essere rigorosi, l'executor dovrebbe guardare le connessioni per passare i dati.
        // Per ora, va bene così come logica sequenziale semplice.

        BufferedImage source = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Pipeline pipeline = new Pipeline();
        pipeline.addNode(new InputNode(source));

        PipelineExecutor executor = new PipelineExecutor();
        BufferedImage result = executor.execute(pipeline);

        assertNotNull(result);
    }
}

