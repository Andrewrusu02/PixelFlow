package com.unical.pixelflow.model;

import javafx.beans.property.*;
import com.unical.pixelflow.model.DBObjects.dbUser;

public class DataPF {
    public static final PFDataBase pfDatabase = PFDataBase.getInstance();

    public static UserDatabase userDatabase;
    public static final BooleanProperty userUpdated = new SimpleBooleanProperty(false);

    public static dbUser dbUser;

    public static final LocalizationService localizationService = new LocalizationService("en");
    public static final LocalizationService lsp = localizationService;

    public static final ExecutorsServiceManager esm = new ExecutorsServiceManager();

    private static final IntegerProperty busy = new SimpleIntegerProperty(0);

    public static ReadOnlyIntegerProperty busyProperty() {
        return busy;
    }

    public static void subscribe_busy() {
        busy.set(busy.get() + 1);
    }

    public static void unsubscribe_busy() {
        busy.set(busy.get() - 1);
        if (busy.get() < 0) {
            throw new IllegalStateException("Busy counter is negative");
        }
    }

    public static final String users_images_directory = "Data/user_imgs";
}
