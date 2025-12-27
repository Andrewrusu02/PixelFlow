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
}

