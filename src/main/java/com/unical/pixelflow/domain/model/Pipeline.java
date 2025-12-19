package com.unical.pixelflow.domain.model;

import java.util.*;

/**
 * Gestisce il grafo dei nodi e delle connessioni.
 * Responsabile della validazione topologica (prevenzione cicli).
 */
public class Pipeline {
    // Mappa ID -> Nodo
    private final Map<String, Node> nodes = new HashMap<>();
    // Lista delle connessioni attive
    private final List<Connection> connections = new ArrayList<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public void removeNode(String nodeId) {
        if (!nodes.containsKey(nodeId)) return;

        // Rimuovi il nodo
        nodes.remove(nodeId);

        // Rimuovi tutte le connessioni collegate a questo nodo (in entrata o uscita)
        connections.removeIf(conn ->
                conn.getSource().getOwner().getId().equals(nodeId) ||
                conn.getDestination().getOwner().getId().equals(nodeId)
        );
    }

    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public List<Connection> getConnections() {
        return new ArrayList<>(connections); // Copia difensiva
    }

    public Map<String, Node> getNodes() {
        return new HashMap<>(nodes); // Copia difensiva
    }

    /**
     * Tenta di creare una connessione tra due nodi.
     * @param sourceNodeId ID del nodo sorgente
     * @param destNodeId ID del nodo destinazione
     * @throws CycleException se la connessione crea un ciclo
     */
    public void connect(String sourceNodeId, String destNodeId) {
        Node sourceNode = nodes.get(sourceNodeId);
        Node destNode = nodes.get(destNodeId);

        if (sourceNode == null || destNode == null) {
            throw new IllegalArgumentException("Nodes not found");
        }

        // Per semplicità, in questa fase prendiamo il primo socket disponibile.
        // In futuro si potrebbe specificare l'indice del socket.
        if (sourceNode.getOutputs().isEmpty() || destNode.getInputs().isEmpty()) {
            throw new IllegalStateException("Nodes do not have valid sockets");
        }

        Socket sourceSocket = sourceNode.getOutputs().get(0);
        Socket destSocket = destNode.getInputs().get(0);

        // 1. VALIDAZIONE CICLI
        // Se esiste già un percorso da Destinazione a Sorgente,
        // aggiungere Sorgente->Destinazione chiuderebbe il cerchio.
        if (wouldCreateCycle(sourceNodeId, destNodeId)) {
            throw new CycleException("Cannot connect: this would create an infinite loop.");
        }

        // 2. PULIZIA: Un socket di input può avere solo UNA connessione in entrata.
        // Se c'è già qualcosa collegato all'input di destinazione, lo rimuoviamo.
        connections.removeIf(c -> c.getDestination().equals(destSocket));

        // 3. CREAZIONE
        Connection newConnection = new Connection(sourceSocket, destSocket);
        connections.add(newConnection);
    }

    /**
     * Verifica se collegare 'from' a 'to' creerebbe un ciclo.
     * Usa una DFS (Depth First Search) per vedere se 'to' può già raggiungere 'from'.
     */
    private boolean wouldCreateCycle(String from, String to) {
        // Se tentiamo di collegare un nodo a se stesso
        if (from.equals(to)) return true;

        Set<String> visited = new HashSet<>();
        // Controlla se c'è un percorso da 'to' verso 'from'
        return hasPath(to, from, visited);
    }

    // DFS ricorsiva
    private boolean hasPath(String current, String target, Set<String> visited) {
        if (current.equals(target)) return true;
        if (visited.contains(current)) return false;

        visited.add(current);

        // Trova tutti i nodi collegati in uscita dal nodo corrente
        for (Connection conn : connections) {
            if (conn.getSource().getOwner().getId().equals(current)) {
                String nextNodeId = conn.getDestination().getOwner().getId();
                if (hasPath(nextNodeId, target, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Restituisce i nodi nell'ordine corretto di esecuzione (Input -> Filtri -> Output).
     * Usa l'algoritmo di Kahn per l'ordinamento topologico.
     */
    public List<Node> getExecutionOrder() {
        Map<String, Integer> inDegree = new HashMap<>();
        // Inizializza gradi in entrata a 0
        for (String nodeId : nodes.keySet()) {
            inDegree.put(nodeId, 0);
        }

        // Calcola gradi in entrata reali
        for (Connection conn : connections) {
            String destId = conn.getDestination().getOwner().getId();
            inDegree.put(destId, inDegree.getOrDefault(destId, 0) + 1);
        }

        // Coda per i nodi con grado 0 (nessuna dipendenza)
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Node> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            result.add(nodes.get(currentId));

            // "Rimuovi" virtualmente il nodo e riduci il grado dei vicini
            for (Connection conn : connections) {
                if (conn.getSource().getOwner().getId().equals(currentId)) {
                    String neighborId = conn.getDestination().getOwner().getId();
                    inDegree.put(neighborId, inDegree.get(neighborId) - 1);

                    if (inDegree.get(neighborId) == 0) {
                        queue.add(neighborId);
                    }
                }
            }
        }

        return result;
    }
}

