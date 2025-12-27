package com.unical.pixelflow.infrastructure;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public List<ProjectMetadata> getRecentProjects() {
        // Se nessun utente è loggato, ritorna lista vuota
        if (DataPF.dbUser == null) {
            return new ArrayList<>();
        }

        // Recupera task dal DB
        Task<List<ProjectMetadata>> task = DataPF.userDatabase.getUserProjects();

        // Esegue il task in un thread separato
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        try {
            return task.get();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Apre un progetto esistente.
     * TODO: Implementare caricamento progetto da file + apertura EditorView
     */
    public void openProject(String path) {
        // TODO: deserializzare .pfp e aprire l'Editor
        System.out.println("Opening project: " + path);
    }
}
