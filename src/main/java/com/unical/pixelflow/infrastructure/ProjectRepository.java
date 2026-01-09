package com.unical.pixelflow.infrastructure;

import com.unical.pixelflow.model.DataPF;
import com.unical.pixelflow.model.ProjectMetadata;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public List<ProjectMetadata> getRecentProjects() {
        if (DataPF.dbUser == null) {
            return new ArrayList<>();
        }

        Task<List<ProjectMetadata>> task = DataPF.userDatabase.getUserProjects();

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
     * Placeholder: opening an existing project is not implemented yet.
     */
    public void openProject(String path) {
        System.out.println("Opening project: " + path);
    }
}
