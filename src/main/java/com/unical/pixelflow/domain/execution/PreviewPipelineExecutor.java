package com.unical.pixelflow.domain.execution;

import com.unical.pixelflow.domain.model.Connection;
import com.unical.pixelflow.domain.model.Node;
import com.unical.pixelflow.domain.model.Pipeline;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Executor dedicato alla preview: calcola l'immagine risultante fino ad un nodo target.
 *
 * <p>Nota: non crea/gestisce proxy. Il proxy (Virtual Proxy) viene gestito a valle nel layer Application,
 * ridimensionando la source dell'InputNode prima dell'esecuzione.
 */
public class PreviewPipelineExecutor {

    /**
     * Esegue una "slice" della pipeline (Input -> ... -> targetNodeId).
     *
     * @param pipeline pipeline valida
     * @param targetNodeId id del nodo di cui vogliamo l'output preview
     * @return immagine risultante al nodo target, oppure null se target non raggiungibile.
     */
    public BufferedImage executeUntil(Pipeline pipeline, String targetNodeId) {
        if (pipeline == null) throw new IllegalArgumentException("Pipeline cannot be null");
        if (targetNodeId == null || targetNodeId.isBlank()) {
            throw new IllegalArgumentException("targetNodeId cannot be null/blank");
        }

        Node target = pipeline.getNode(targetNodeId);
        if (target == null) return null;

        // calcola insieme dei nodi necessari: tutti i predecessori del target (incluso)
        Set<String> needed = collectAncestorsInclusive(pipeline, targetNodeId);
        if (needed.isEmpty()) return null;

        List<Node> order = topologicalOrderRestricted(pipeline, needed);
        if (order.isEmpty()) return null;

        BufferedImage current = null;
        for (Node n : order) {
            current = n.process(current);
            if (targetNodeId.equals(n.getId())) {
                return current;
            }
        }
        return null;
    }

    private static Set<String> collectAncestorsInclusive(Pipeline pipeline, String targetId) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(targetId);

        while (!stack.isEmpty()) {
            String cur = stack.pop();
            if (!visited.add(cur)) continue;

            // predecessori: connessioni che entrano in cur
            for (Connection c : pipeline.getConnections()) {
                String to = c.getDestination().getOwner().getId();
                if (!cur.equals(to)) continue;
                String from = c.getSource().getOwner().getId();
                stack.push(from);
            }
        }

        return visited;
    }

    private static List<Node> topologicalOrderRestricted(Pipeline pipeline, Set<String> includedIds) {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String id : includedIds) {
            inDegree.put(id, 0);
        }

        for (Connection c : pipeline.getConnections()) {
            String from = c.getSource().getOwner().getId();
            String to = c.getDestination().getOwner().getId();
            if (includedIds.contains(from) && includedIds.contains(to)) {
                inDegree.put(to, inDegree.get(to) + 1);
            }
        }

        ArrayDeque<String> q = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) q.add(e.getKey());
        }

        List<Node> result = new ArrayList<>();
        while (!q.isEmpty()) {
            String cur = q.removeFirst();
            Node n = pipeline.getNode(cur);
            if (n != null) result.add(n);

            for (Connection c : pipeline.getConnections()) {
                String from = c.getSource().getOwner().getId();
                String to = c.getDestination().getOwner().getId();
                if (!cur.equals(from)) continue;
                if (!includedIds.contains(to) || !includedIds.contains(from)) continue;

                int nd = inDegree.get(to) - 1;
                inDegree.put(to, nd);
                if (nd == 0) q.add(to);
            }
        }

        return result;
    }
}

