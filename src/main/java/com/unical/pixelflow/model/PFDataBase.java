package com.unical.pixelflow.model;

import javafx.concurrent.Task;
import com.unical.pixelflow.utility.Encrypter;
import com.unical.pixelflow.model.DBObjects.dbUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PFDataBase extends Database {

    private static PFDataBase instance = null;

    public static PFDataBase getInstance() {
        if (instance == null) {
            instance = new PFDataBase();
        }
        return instance;
    }

    private PFDataBase() {
        super("data/pixel_flow.db");
        // Inizializza tabella Projects se non esiste
        createProjectsTable();
        // Assicura schema Users aggiornato (avatar)
        ensureUserProfileImageColumn();
    }

    public Task<Boolean> checkPassword(String username, String password) {
        return asyncCall(() -> {
            if (isConnected()) {
                String query = "SELECT password_hash FROM Users WHERE username=?;";
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        return Encrypter.check_string_bcrypt(password, storedHash);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    public Task<Boolean> createUser(String username, String password) {
        return asyncCall(() -> {
            if (isConnected()) {
                String query = "INSERT INTO Users (username, password_hash) VALUES (?, ?);";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, Encrypter.encrypt_string_bcrypt(password));
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1); // Ottieni l'ID generato
                    rs.close();
                    stmt.close();
                    createNewUserDB(userId);
                }
                rs.close();
                stmt.close();
                return true;
            }
            return false;
        });
    }

    public Task<Boolean> deleteUser(String username) {
        return asyncCall(() -> {
            if (isConnected()) {
                String query = "DELETE FROM Users WHERE username=?;";
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    public Task<Boolean> userExists(String username) {
        return asyncCall(() -> {
            if (isConnected()) {
                String query = "SELECT COUNT(*) AS count FROM Users WHERE username = ?;";
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("count") > 0;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    public Task<dbUser> getUser(String username) {
        return asyncCall(() -> {
            dbUser user = null;
            if (isConnected()) {
                String query = "SELECT * FROM Users WHERE username = ?;";
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        user = new dbUser(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return user;
        });
    }

    public Task<dbUser> getUser(int id) {
        return asyncCall(() -> {
            dbUser user = null;
            if (isConnected()) {
                String query = "SELECT * FROM Users WHERE id = ?;";
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        user = new dbUser(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return user;
        });
    }

    // ============================================================
    // GESTIONE PROGETTI - NUOVI METODI
    // ============================================================

    /**
     * Crea la tabella Projects se non esiste già
     */
    public void createProjectsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Projects (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "name TEXT NOT NULL, " +
                     "file_path TEXT NOT NULL UNIQUE, " +
                     "thumbnail_path TEXT, " +
                     "created_at DATETIME DEFAULT (datetime('now', 'localtime')), " +
                     "modified_at DATETIME DEFAULT (datetime('now', 'localtime')), " +
                     "FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE)";

        try (Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            // Indici per velocizzare le query
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_projects_user_id ON Projects(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_projects_modified ON Projects(modified_at DESC)");
            System.out.println("Tabella Projects inizializzata correttamente");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Errore creazione tabella Projects: " + e.getMessage());
        }
    }

    /**
     * Recupera tutti i progetti di un utente, ordinati per data modifica (più recenti prima)
     */
    public Task<List<ProjectMetadata>> getUserProjects(int userId) {
        return asyncCall(() -> {
            List<ProjectMetadata> projects = new ArrayList<>();
            String query = "SELECT * FROM Projects WHERE user_id = ? ORDER BY modified_at DESC";

            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // Gestione date sicura
                    String dateStr = rs.getString("modified_at");
                    LocalDateTime date = LocalDateTime.now(); // Fallback

                    try {
                        // SQLite salva come "yyyy-MM-dd HH:mm:ss"
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        date = LocalDateTime.parse(dateStr, formatter);
                    } catch (Exception e) {
                        // Se parsing fallisce, usa data corrente
                        System.err.println("Warning: impossibile parsare data '" + dateStr + "', uso now()");
                    }

                    projects.add(new ProjectMetadata(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
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
     * Salva un nuovo progetto nel database
     */
    public Task<Boolean> saveProject(int userId, String name, String filePath, String thumbnailPath) {
        return asyncCall(() -> {
            String query = "INSERT INTO Projects (user_id, name, file_path, thumbnail_path) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, name);
                stmt.setString(3, filePath);
                stmt.setString(4, thumbnailPath);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    /**
     * Elimina un progetto dal database
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
     * Aggiorna il nome di un progetto
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
     * Aggiorna la data di ultima modifica di un progetto
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

    /**
     * Aggiunge la colonna Users.profile_image se manca.
     * Questo rende l'app retro-compatibile con DB template vecchi.
     */
    private void ensureUserProfileImageColumn() {
        try (Statement stmt = con.createStatement()) {
            // SQLite non supporta IF NOT EXISTS su ADD COLUMN in tutte le versioni,
            // quindi facciamo best-effort e ignoriamo l'errore "duplicate column".
            stmt.execute("ALTER TABLE Users ADD COLUMN profile_image TEXT") ;
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("duplicate column") || msg.contains("already exists")) {
                return;
            }
            // Se Users non esiste o altro problema serio, stampiamo warning (non blocchiamo avvio)
            System.err.println("Warning: impossibile aggiungere colonna Users.profile_image: " + e.getMessage());
        }
    }

    /**
     * Aggiorna il path dell'immagine profilo per un utente.
     */
    public Task<Boolean> updateUserProfileImage(int userId, String imagePath) {
        return asyncCall(() -> {
            String query = "UPDATE Users SET profile_image = ? WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, imagePath);
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        });
    }
}

