package com.unical.pixelflow.domain.model;

import java.util.UUID;

public class Connection {
    private final String id;
    private final Socket source;      // Deve essere di tipo OUTPUT
    private final Socket destination; // Deve essere di tipo INPUT

    public Connection(Socket source, Socket destination) {
        if (source.getType() != Socket.Type.OUTPUT) {
            throw new IllegalArgumentException("Source socket must be of type OUTPUT");
        }
        if (destination.getType() != Socket.Type.INPUT) {
            throw new IllegalArgumentException("Destination socket must be of type INPUT");
        }

        this.id = UUID.randomUUID().toString();
        this.source = source;
        this.destination = destination;
    }

    public String getId() { return id; }
    public Socket getSource() { return source; }
    public Socket getDestination() { return destination; }
}

