package com.unical.pixelflow.view;

import javafx.application.Application;
import javafx.stage.Stage;
import com.unical.pixelflow.model.DataPF;

import java.util.Set;
import java.util.stream.Collectors;

public class Main extends Application {
    private Set<String> initialThreads;
    @Override
    public void start(Stage primaryStage) {
        initialThreads = Thread.getAllStackTraces().keySet().stream()
                .map(Thread::getName)
                .collect(Collectors.toSet());
        try {
            SceneHandler sceneHandler = SceneHandler.getInstance(primaryStage);
            sceneHandler.showHomePageScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Add a ShutdownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook is running!");
            Set<String> currentThreads = Thread.getAllStackTraces().keySet().stream()
                    .map(Thread::getName)
                    .collect(Collectors.toSet());
            currentThreads.removeAll(initialThreads);
            for (String threadName : currentThreads) {
                System.out.println("Thread still running: " + threadName);
            }
        }));
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        DataPF.esm.shutdown();
    }
}
