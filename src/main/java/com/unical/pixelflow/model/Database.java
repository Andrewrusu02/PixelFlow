package com.unical.pixelflow.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.concurrent.Task;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database implements AutoCloseable {

    protected Connection con = null;

    private static final String TEMPLATE_PATH = "/com/unical/pixelflow/databases/usertemplate.db";
    private static final String USER_DB_FOLDER = "Data/user_dbs";

    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    public Connection getConnection() {
        return con;
    }

    protected Database(String db) {
        File dbFile = new File(db);

        if (!dbFile.exists()) {
            try {
                if (dbFile.getParentFile() != null) {
                    dbFile.getParentFile().mkdirs();
                }

                String templatePath = "/com/unical/pixelflow/databases/pixelFlowDataBaseTemplate.db";

                try (InputStream is = getClass().getResourceAsStream(templatePath)) {
                    if (is != null) {
                        Files.copy(is, dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String url = "jdbc:sqlite:" + db;
            con = DriverManager.getConnection(url);
            if (isConnected())
                System.out.println("Database " + db + " connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if(con != null) {
                con.close();
            }
        } catch (SQLException ignored) {
        }
    }

    public static void createNewUserDB(int id) throws IOException {
        File folder = new File(USER_DB_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Path userDbPath = Paths.get(USER_DB_FOLDER, id + ".db");

        try (InputStream is = Database.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) {
                throw new IOException("Template not found: " + TEMPLATE_PATH);
            }
            Files.copy(is, userDbPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File db copy success: " + userDbPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error during copy db", e);
        }
    }

    protected <T> Task<T> asyncCall(Callable<T> callable) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };
        new Thread(task).start();
        return task;
    }
}