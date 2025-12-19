package com.unical.pixelflow.domain.model;

import java.util.UUID;

public class Socket {
    public enum Type {
        INPUT, OUTPUT
    }

    private final String id;
    private final Type type;
    private final Node owner;
    // Indichiamo il tipo di dato (per ora solo IMAGE, ma estensibile)
    private final String dataType = "IMAGE";

    public Socket(Node owner, Type type) {
        this.id = UUID.randomUUID().toString();
        this.owner = owner;
        this.type = type;
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public Node getOwner() { return owner; }
    public String getDataType() { return dataType; }
}

