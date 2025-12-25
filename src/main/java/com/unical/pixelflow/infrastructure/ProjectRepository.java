package com.unical.pixelflow.infrastructure;

import com.unical.pixelflow.model.ProjectMetadata;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    /**
     * Recupera l'elenco dei progetti recenti dell'utente.
     * TODO: Implementare la query al database SQLite quando sarà pronto.
     * Per ora restituisce dati mock per testare l'interfaccia.
     */
    public List<ProjectMetadata> getRecentProjects() {
        // Mock data - sostituire con query DB reale
        List<ProjectMetadata> projects = new ArrayList<>();

        projects.add(new ProjectMetadata(
            "Portrait Studio",
            "D:/Desktop/Projects/portrait.pfp",
            LocalDateTime.now().minusDays(1),
            "/com/unical/pixelflow/images/project-placeholders/portrait-placeholder.png"
        ));

        projects.add(new ProjectMetadata(
            "Summer Vacation",
            "D:/Documents/summer.pfp",
            LocalDateTime.now().minusDays(3),
            "/com/unical/pixelflow/images/project-placeholders/landscape-placeholder.png"
        ));

        projects.add(new ProjectMetadata(
            "Photo Session",
            "C:/Users/Photos/session.pfp",
            LocalDateTime.now().minusWeeks(1),
            "/com/unical/pixelflow/images/project-placeholders/photo-placeholder.png"
        ));

        projects.add(new ProjectMetadata(
            "Design Project",
            "D:/Works/design.pfp",
            LocalDateTime.now().minusWeeks(2),
            "/com/unical/pixelflow/images/project-placeholders/design-placeholder.png"
        ));

        return projects;
    }

    /**
     * Apre un progetto esistente.
     * TODO: Implementare caricamento progetto da file.
     */
    public void openProject(String path) {
        System.out.println("Opening project: " + path);
        // Implementazione futura
    }

    /**
     * Crea un nuovo progetto.
     * TODO: Implementare creazione nuovo progetto.
     */
    public ProjectMetadata createNewProject(String name, String path) {
        System.out.println("Creating new project: " + name);
        // Implementazione futura
        return new ProjectMetadata(name, path);
    }
}

