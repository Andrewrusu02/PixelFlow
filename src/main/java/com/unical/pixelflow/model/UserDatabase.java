package com.unical.pixelflow.model;

import javafx.concurrent.Task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends Database {
    private static UserDatabase instance = null;

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public UserDatabase() {
        // Ogni utente ha il suo DB, quindi user_id non serve più nella tabella Projects locale
        super("data/user_dbs/" + DataPF.dbUser.getId() + ".db");
        createProjectTable();
    }

    private void createProjectTable() {
        String createProjectsTable = "CREATE TABLE IF NOT EXISTS Projects (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "file_path TEXT NOT NULL UNIQUE, " +
                "thumbnail_path TEXT, " +
                "created_at DATETIME DEFAULT (datetime('now', 'localtime')), " +
                "modified_at DATETIME DEFAULT (datetime('now', 'localtime'))" +
                ");";

        try (Statement stmt = con.createStatement()) {
            stmt.execute(createProjectsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // GESTIONE PROGETTI (DB LOCALE UTENTE)
    // ============================================================

    /**
     * Recupera tutti i progetti dal DB locale dell'utente corrente.
     * Non serve più passare userId.
     */
    public Task<List<ProjectMetadata>> getUserProjects() {
        return asyncCall(() -> {
            List<ProjectMetadata> projects = new ArrayList<>();
            // Rimosso "WHERE user_id = ?" perché il DB è già specifico dell'utente
            String query = "SELECT * FROM Projects ORDER BY modified_at DESC";

            try (PreparedStatement stmt = con.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String dateStr = rs.getString("modified_at");
                    LocalDateTime date = LocalDateTime.now();

                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        date = LocalDateTime.parse(dateStr, formatter);
                    } catch (Exception e) {
                        System.err.println("Warning: impossibile parsare data '" + dateStr + "', uso now()");
                    }

                    projects.add(new ProjectMetadata(
                            rs.getInt("id"),
                            DataPF.dbUser.getId(), // Passiamo l'ID dell'utente corrente preso da DataPF
                            rs.getString("name"),
                            rs.getString("file_path"),
                            rs.getString("thumbnail_path"),
                            date
                    ));
                }
            }
            return projects;
        });
    }

    /**
     * Salva un nuovo progetto.
     * Rimosso parametro userId.
     */
    public Task<Boolean> saveProject(String name, String filePath, String thumbnailPath) {
        return asyncCall(() -> {
            // Rimosso user_id dalla INSERT
            String query = "INSERT INTO Projects (name, file_path, thumbnail_path) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, filePath);
                stmt.setString(3, thumbnailPath);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    /**
     * Elimina un progetto.
     */
    public Task<Boolean> deleteProject(int projectId) {
        return asyncCall(() -> {
            String query = "DELETE FROM Projects WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, projectId);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    /**
     * Rinomina un progetto.
     */
    public Task<Boolean> renameProject(int projectId, String newName) {
        return asyncCall(() -> {
            String query = "UPDATE Projects SET name = ?, modified_at = datetime('now', 'localtime') WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, newName);
                stmt.setInt(2, projectId);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    /**
     * Aggiorna data ultima modifica.
     */
    public Task<Boolean> updateProjectModifiedDate(int projectId) {
        return asyncCall(() -> {
            String query = "UPDATE Projects SET modified_at = datetime('now', 'localtime') WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, projectId);
                return stmt.executeUpdate() > 0;
            }
        });
    }
}