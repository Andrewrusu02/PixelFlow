package com.unical.pixelflow.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectMetadata {
    private String name;
    private String path;
    private LocalDateTime lastModified;
    private String thumbnailPath;

    public ProjectMetadata(String name, String path) {
        this.name = name;
        this.path = path;
        this.lastModified = LocalDateTime.now();
        this.thumbnailPath = null;
    }

    public ProjectMetadata(String name, String path, LocalDateTime lastModified) {
        this.name = name;
        this.path = path;
        this.lastModified = lastModified;
        this.thumbnailPath = null;
    }

    public ProjectMetadata(String name, String path, LocalDateTime lastModified, String thumbnailPath) {
        this.name = name;
        this.path = path;
        this.lastModified = lastModified;
        this.thumbnailPath = thumbnailPath;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public String getFormattedDate() {
        return lastModified.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

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

