package com.unical.pixelflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExecutorsServiceManager {
    private final List<ExecutorService> registered_services = new ArrayList<>();

    public void register(ExecutorService es) {
        registered_services.add(es);
    }

    public void shutdown() {
        for (ExecutorService es : registered_services) {
            if (es != null && !es.isShutdown())
                es.shutdown();
        }
    }
}
