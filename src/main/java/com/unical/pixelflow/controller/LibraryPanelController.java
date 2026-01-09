package com.unical.pixelflow.controller;

import com.unical.pixelflow.utility.FilterResources;
import com.unical.pixelflow.components.SingleFilterViewController;
import com.unical.pixelflow.model.DataPF;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    private final List<FilterCategory> categories = new ArrayList<>();

    @FXML
    public void initialize() {
        // Titolo + placeholder localizzati
        if (titleLibrary != null) titleLibrary.textProperty().bind(DataPF.lsp.lsb("library.title"));
        if (searchField != null) searchField.promptTextProperty().bind(DataPF.lsp.lsb("library.search.placeholder"));

        loadFilterCategories();
        populateCategorized();

        // Quando cambia lingua, rigenera la UI così aggiorna categorie + eventuale messaggio "no results"
        DataPF.lsp.selectedLanguageProperty().addListener((_, _, __) -> {
            if (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) {
                handleSearch(searchField.getText());
            } else {
                populateCategorized();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });
    }

    private String getTranslationKeyForNode(String nodeType) {
        if (nodeType == null) return "node.unknown";
        String normalized = nodeType.replace("Filter", "")
                .replace("Node", "")
                .toLowerCase();

        return switch (normalized) {
            case "edgedetection" -> "node.edge_detection.name";
            case "rotate90" -> "node.rotate90.name";
            default -> "node." + normalized + ".name";
        };
    }

    private void handleSearch(String query) {
        if (query == null || query.isBlank()) {
            populateCategorized();
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        List<FilterDefinition> flatResults = new ArrayList<>();

        for (FilterCategory cat : categories) {
            for (FilterDefinition def : cat.filters) {
                String currentTranslatedName = DataPF.lsp.lsb(def.translationKey).get();
                if (currentTranslatedName.toLowerCase().contains(lowerCaseQuery)) {
                    flatResults.add(def);
                }
            }
        }

        populateFlatList(flatResults);
    }

    private void populateCategorized() {
        itemsContainer.getChildren().clear();

        for (FilterCategory cat : categories) {
            itemsContainer.getChildren().add(createCategoryHeader(cat.name));

            for (FilterDefinition filter : cat.filters) {
                addItemToView(filter);
            }

            Label spacer = new Label();
            spacer.setMinHeight(10);
            itemsContainer.getChildren().add(spacer);
        }
    }

    private void populateFlatList(List<FilterDefinition> filters) {
        itemsContainer.getChildren().clear();

        if (filters.isEmpty()) {
            Label noRes = new Label();
            noRes.textProperty().bind(DataPF.lsp.lsb("library.search.no_results"));
            noRes.setStyle("-fx-text-fill: #888888; -fx-padding: 10;");
            itemsContainer.getChildren().add(noRes);
            return;
        }

        for (FilterDefinition filter : filters) {
            addItemToView(filter);
        }
    }

    private void addItemToView(FilterDefinition filter) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unical/pixelflow/components/SingleFilterView.fxml"));
            Parent nodeItem = loader.load();

            SingleFilterViewController controller = loader.getController();

            String translatedName = DataPF.lsp.lsb(filter.translationKey).get();

            controller.setData(translatedName, filter.svgIcon, filter.nodeType);

            itemsContainer.getChildren().add(nodeItem);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Label createCategoryHeader(String keyOrText) {
        Label label = new Label();
        // Le categorie sono localizzate via chiavi, ma teniamo fallback se serve.
        label.textProperty().bind(DataPF.lsp.lsb(keyOrText));
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 10 0 5 5;");
        return label;
    }

    private void loadFilterCategories() {
        categories.clear();

        FilterCategory colorCat = new FilterCategory("library.category.color_light");
        addDef(colorCat, "BrightnessFilter");
        addDef(colorCat, "ContrastFilter");
        addDef(colorCat, "GrayscaleFilter");
        addDef(colorCat, "InvertFilter");
        addDef(colorCat, "SepiaFilter");
        addDef(colorCat, "SolarizeFilter");
        addDef(colorCat, "ThresholdFilter");
        categories.add(colorCat);

        FilterCategory effectCat = new FilterCategory("library.category.effects_detail");
        addDef(effectCat, "BlurFilter");
        addDef(effectCat, "EdgeDetectionFilter");
        addDef(effectCat, "PixelateFilter");
        addDef(effectCat, "SharpenFilter");
        addDef(effectCat, "VignetteFilter");
        categories.add(effectCat);

        FilterCategory geoCat = new FilterCategory("library.category.geometry_transform");
        addDef(geoCat, "FlipFilter");
        addDef(geoCat, "Rotate90Filter");
        addDef(geoCat, "ScaleFilter");
        categories.add(geoCat);
    }

    private void addDef(FilterCategory cat, String nodeType) {
        String icon = FilterResources.getIconForType(nodeType);
        String key = getTranslationKeyForNode(nodeType);
        cat.filters.add(new FilterDefinition(key, nodeType, icon));
    }

    private static class FilterCategory {
        String name;
        List<FilterDefinition> filters = new ArrayList<>();

        public FilterCategory(String name) {
            this.name = name;
        }
    }

    private static class FilterDefinition {
        String translationKey;
        String nodeType;
        String svgIcon;

        public FilterDefinition(String translationKey, String nodeType, String svgIcon) {
            this.translationKey = translationKey;
            this.nodeType = nodeType;
            this.svgIcon = svgIcon;
        }
    }
}

