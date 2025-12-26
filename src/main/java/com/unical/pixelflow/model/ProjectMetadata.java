package com.unical.pixelflow.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectMetadata {
    private int id;                    // ID del database
    private int userId;                // Foreign key utente
    private String name;
    private String path;
    private String thumbnailPath;
    private LocalDateTime lastModified;

    // Costruttore completo per il DB (quando si carica da database)
    public ProjectMetadata(int id, int userId, String name, String path, String thumbnailPath, LocalDateTime lastModified) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.path = path;
        this.thumbnailPath = thumbnailPath;
        this.lastModified = lastModified;
    }

    // Costruttore semplificato per nuovi progetti (prima del salvataggio DB)
    public ProjectMetadata(String name, String path, String thumbnailPath) {
        this(0, 0, name, path, thumbnailPath, LocalDateTime.now());
    }

    // Costruttori legacy per retrocompatibilità
    public ProjectMetadata(String name, String path) {
        this(0, 0, name, path, null, LocalDateTime.now());
    }

    public ProjectMetadata(String name, String path, LocalDateTime lastModified) {
        this(0, 0, name, path, null, lastModified);
    }

    public ProjectMetadata(String name, String path, LocalDateTime lastModified, String thumbnailPath) {
        this(0, 0, name, path, thumbnailPath, lastModified);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public String getFormattedDate() {
        return lastModified.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}

