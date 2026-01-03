package com.unical.pixelflow.controller;

import com.unical.pixelflow.utility.FilterResources;
import com.unical.pixelflow.components.SingleFilterViewController;
import com.unical.pixelflow.model.DataPF;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryPanelController {

    @FXML private VBox itemsContainer;
    @FXML private TextField searchField;
    @FXML private Label titleLibrary;

    // Struttura dati per le categorie
    private final List<FilterCategory> categories = new ArrayList<>();

    @FXML
    public void initialize() {
        if (DataPF.lsp != null) {
            // Binding lingue se necessario
        }

        loadFilterCategories();

        // Al'avvio mostriamo la vista categorizzata
        populateCategorized();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });
    }

    private void handleSearch(String query) {
        if (query == null || query.isBlank()) {
            // Se la ricerca è vuota, torniamo alla vista con le categorie
            populateCategorized();
            return;
        }

        // Se c'è una ricerca, appiattiamo tutto in una lista singola
        String lowerCaseQuery = query.toLowerCase();
        List<FilterDefinition> flatResults = new ArrayList<>();

        for (FilterCategory cat : categories) {
            for (FilterDefinition def : cat.filters) {
                if (def.name.toLowerCase().contains(lowerCaseQuery)) {
                    flatResults.add(def);
                }
            }
        }

        populateFlatList(flatResults);
    }

    // Metodo 1: Popola con categorie e intestazioni
    private void populateCategorized() {
        itemsContainer.getChildren().clear();

        for (FilterCategory cat : categories) {
            itemsContainer.getChildren().add(createCategoryHeader(cat.name));

            // 2. Aggiungi i filtri di quella categoria
            for (FilterDefinition filter : cat.filters) {
                addItemToView(filter);
            }

            // Spaziatura extra dopo ogni categoria (opzionale)
            Label spacer = new Label();
            spacer.setMinHeight(10);
            itemsContainer.getChildren().add(spacer);
        }
    }

    // Metodo 2: Popola come lista semplice (per la ricerca)
    private void populateFlatList(List<FilterDefinition> filters) {
        itemsContainer.getChildren().clear();

        if (filters.isEmpty()) {
            Label noRes = new Label("Nessun risultato");
            noRes.setStyle("-fx-text-fill: #888888; -fx-padding: 10;");
            itemsContainer.getChildren().add(noRes);
            return;
        }

        for (FilterDefinition filter : filters) {
            addItemToView(filter);
        }
    }

    // Helper per creare il nodo grafico del singolo filtro
    private void addItemToView(FilterDefinition filter) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/components/SingleFilterView.fxml"));
            Parent nodeItem = loader.load();

            SingleFilterViewController controller = loader.getController();
            controller.setData(filter.name, filter.svgIcon, filter.nodeType);

            itemsContainer.getChildren().add(nodeItem);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Crea l'etichetta per la categoria (es. "COLOR ADJUSTMENTS")
    private Label createCategoryHeader(String text) {
        Label label = new Label(text.toUpperCase());
        // Stile inline o classe CSS
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 10 0 5 5;");
        return label;
    }

    private void loadFilterCategories() {
        categories.clear();

        // --- Categoria 1: Colore ---
        FilterCategory colorCat = new FilterCategory("Color & Light");
        addDef(colorCat, "Brightness",       "BrightnessFilter");
        addDef(colorCat, "Contrast",         "ContrastFilter");
        addDef(colorCat, "Grayscale",        "GrayscaleFilter");
        addDef(colorCat, "Invert Colors",    "InvertFilter");
        addDef(colorCat, "Sepia",            "SepiaFilter");
        addDef(colorCat, "Solarize",         "SolarizeFilter");
        addDef(colorCat, "Threshold",        "ThresholdFilter");
        categories.add(colorCat);

        // --- Categoria 2: Effetti ---
        FilterCategory effectCat = new FilterCategory("Effects & Detail");
        addDef(effectCat, "Gaussian Blur",    "BlurFilter");
        addDef(effectCat, "Edge Detection",   "EdgeDetectionFilter");
        addDef(effectCat, "Pixelate",         "PixelateFilter");
        addDef(effectCat, "Sharpen",          "SharpenFilter");
        addDef(effectCat, "Vignette",         "VignetteFilter");
        categories.add(effectCat);

        // --- Categoria 3: Geometria ---
        FilterCategory geoCat = new FilterCategory("Geometry & Transform");
        addDef(geoCat, "Flip Image",       "FlipFilter");
        addDef(geoCat, "Rotate 90°",       "Rotate90Filter");
        addDef(geoCat, "Scale / Resize",   "ScaleFilter");
        categories.add(geoCat);
    }

    private void addDef(FilterCategory cat, String uiName, String nodeType) {
        String icon = FilterResources.getIconForType(nodeType);
        cat.filters.add(new FilterDefinition(uiName, nodeType, icon));
    }

    // --- CLASSI INTERNE ---

    private static class FilterCategory {
        String name;
        List<FilterDefinition> filters = new ArrayList<>();

        public FilterCategory(String name) {
            this.name = name;
        }
    }

    private static class FilterDefinition {
        String name;
        String nodeType;
        String svgIcon;

        public FilterDefinition(String name, String nodeType, String svgIcon) {
            this.name = name;
            this.nodeType = nodeType;
            this.svgIcon = svgIcon;
        }
    }
}