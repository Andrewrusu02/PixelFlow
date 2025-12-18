package com.unical.pixelflow.model.DBObjects;

import javafx.beans.property.*;
import com.unical.pixelflow.model.PFDataBase;
import com.unical.pixelflow.model.types.Theme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class dbUser {

    private static final PFDataBase db = PFDataBase.getInstance();

    private final int id;
    private final StringProperty username;
    private final StringProperty password;
    private final ObjectProperty<Theme> theme;
    private final StringProperty language;

    public dbUser(int id, String username, String password, int theme, String language) {
        this.id = id;
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.theme = new SimpleObjectProperty<>(Theme.fromInt(theme));
        this.language = new SimpleStringProperty(language);
    }

    public dbUser(ResultSet rs) throws SQLException {
        this(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getInt("theme"),
                rs.getString("language")
        );
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username.get();
    }

    public ReadOnlyStringProperty usernameProperty() {
        return username;
    }

    public ReadOnlyStringProperty passwordProperty() {
        return password;
    }

    public Theme getTheme() {
        return theme.get();
    }

    public ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public String getLanguage() {
        return language.get();
    }

    public ReadOnlyStringProperty languageProperty() {
        return language;
    }

    public void setUsername(String newUsername) throws SQLException {
        updateField("username", newUsername);
        this.username.set(newUsername);
    }

    public void setPassword(String newPassword) throws SQLException {
        updateField("password_hash", newPassword);
        this.password.set(newPassword);
    }

    public void setTheme(Theme newTheme) throws SQLException {
        updateField("theme", newTheme.ordinal());
        this.theme.set(newTheme);
    }

    public void setLanguage(String newLanguage) throws SQLException {
        updateField("language", newLanguage);
        this.language.set(newLanguage);
    }

    private void updateField(String columnName, Object value) throws SQLException {
        String query = "UPDATE Users SET " + columnName + " = ? WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(query)) {
            stmt.setObject(1, value);
            stmt.setInt(2, this.id);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (dbUser) obj;
        return this.id == that.id &&
                Objects.equals(this.username.get(), that.username.get()) &&
                Objects.equals(this.password.get(), that.password.get()) &&
                Objects.equals(this.theme.get(), that.theme.get()) &&
                Objects.equals(this.language.get(), that.language.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username.get(), password.get(), theme.get(), language.get());
    }

    @Override
    public String toString() {
        return "dbUser[" +
                "id=" + id + ", " +
                "username=" + username.get() + ", " +
                "theme=" + theme.get() + ", " +
                "language=" + language.get() + ']';
    }
}