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

    /**
     * Canonical data folder for runtime persistence.
     *
     * Keep this aligned with the repository layout (`Data/`).
     */
    protected static final String DATA_DIR = "Data";

    private static final String USER_DB_FOLDER = DATA_DIR + "/user_dbs";
    private static final String USER_IMGS_FOLDER = DATA_DIR + "/user_imgs";
    private static final String DEFAULT_PROFILE_IMAGE_PATH = "/com/unical/pixelflow/images/userprofile.png";

    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    public Connection getConnection() {
        return con;
    }

    protected Database(String db) {
        File dbFile = new File(db);

        if (!dbFile.exists()) {
            try {
                ensureParentDir(dbFile);

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

    private static void ensureParentDir(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
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
        createDefaultProfileImage(id);
    }

    private static void createDefaultProfileImage(int id) throws IOException {
        File imgFolder = new File(USER_IMGS_FOLDER);
        if (!imgFolder.exists()) {
            imgFolder.mkdirs();
        }

        Path destImgPath = Paths.get(USER_IMGS_FOLDER, id + ".png");

        try (InputStream is = Database.class.getResourceAsStream(DEFAULT_PROFILE_IMAGE_PATH)) {
            if (is == null) {
                throw new IOException("Default profile image not found in resources: " + DEFAULT_PROFILE_IMAGE_PATH);
            }
            Files.copy(is, destImgPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Default profile image copied: " + destImgPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error copying default profile image", e);
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