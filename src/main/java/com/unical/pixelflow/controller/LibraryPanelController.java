package com.unical.pixelflow.controller;

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
import java.util.stream.Collectors;

public class LibraryPanelController {

    @FXML private VBox itemsContainer;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private Label titleLibrary;

    private final List<FilterDefinition> allFilters = new ArrayList<>();

    @FXML
    public void initialize() {
        if (DataPF.lsp != null) {
            // Binding lingue se necessario
        }

        loadFilterDefinitions();
        populateList(allFilters);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterList(newValue);
        });
    }

    private void filterList(String query) {
        if (query == null || query.isBlank()) {
            populateList(allFilters);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        List<FilterDefinition> filtered = allFilters.stream()
                .filter(f -> f.name.toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());

        populateList(filtered);
    }

    private void populateList(List<FilterDefinition> filters) {
        itemsContainer.getChildren().clear();

        for (FilterDefinition filter : filters) {
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
    }

    private void loadFilterDefinitions() {
        // --- ICONE SVG ---
        String iconInput = "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z";
        String iconOutput = "M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2v9.67z";
        String iconBlur = "M6 13c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm0 4c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm0-8c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm-3 .5c-.28 0-.5.22-.5.5s.22.5.5.5.5-.22.5-.5-.22-.5-.5-.5zM6 5c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm15 5.5c.28 0 .5-.22.5-.5s-.22-.5-.5-.5-.5.22-.5.5.22.5.5.5zM14 7c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm0-3.5c.28 0 .5-.22.5-.5s-.22-.5-.5-.5-.5.22-.5.5.22.5.5.5zm-11 10c.28 0 .5-.22.5-.5s-.22-.5-.5-.5-.5.22-.5.5.22.5.5.5zm7 7c-.28 0-.5.22-.5.5s.22.5.5.5.5-.22.5-.5-.22-.5-.5-.5zm0-17c.28 0 .5-.22.5-.5s-.22-.5-.5-.5-.5.22-.5.5.22.5.5.5zM10 7c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm0 5.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm8 .5c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm0 4c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zM14 13c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm0 4c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1z";
        String iconBrightness = "M20 8.69V4h-4.69L12 .69 8.69 4H4v4.69L.69 12 4 15.31V20h4.69L12 23.31 15.31 20H20v-4.69L23.31 12 20 8.69zM12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6zm0-10c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4z";
        String iconContrast = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm0 10V6c2.21 0 4 1.79 4 4s-1.79 4-4 4z";
        String iconEdge = "M3 3v18h18V3H3zm16 16H5V5h14v14zM11 7h2v2h-2zM7 7h2v2H7zm8 0h2v2h-2zm-8 4h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2zm-8 4h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2z";
        String iconFlip = "M15 21h2v-2h-2v2zm4-12h2V7h-2v2zM3 5v14c0 1.1.9 2 2 2h4v-2H5V5h4V3H5c-1.1 0-2 .9-2 2zm16-2v2h2c0-1.1-.9-2-2-2zm-8 20h2V1h-2v22zm8-6h2v-2h-2v2zM15 5h2V3h-2v2zm4 8h2v-2h-2v2zm0 8c1.1 0 2-.9 2-2h-2v2z";
        String iconGrayscale = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
        String iconInvert = "M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z";
        String iconPixelate = "M3 3v18h18V3H3zm16 16H5V5h14v14zM7 7h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2zm-8 4h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2zm-8 4h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2z"; // Simile a edge ma per concetto va bene
        String iconRotate = "M15.95 10.78c.03-.25.05-.51.05-.78s-.02-.53-.06-.78l1.69-1.32c.15-.12.19-.34.1-.51l-1.6-2.77c-.1-.18-.31-.24-.49-.18l-1.99.8c-.42-.32-.86-.58-1.35-.78L12 2.34c-.03-.2-.2-.34-.4-.34H8.4c-.2 0-.36.14-.39.34l-.3 1.9c-.49.2-.94.47-1.35.78l-1.99-.8c-.18-.07-.39 0-.49.18l-1.6 2.77c-.1.18-.06.39.1.51l1.69 1.32c-.04.25-.07.52-.07.78s.02.53.06.78L2.33 12.1c-.15.12-.19.34-.1.51l1.6 2.77c.1.18.31.24.49.18l1.99-.8c.42.32.86.58 1.35.78l.3 1.9c.03.2.2.34.4.34h3.2c.2 0 .36-.14.39-.34l.3-1.9c.49-.2.94-.47 1.35-.78l1.99.8c.18.07.39 0 .49-.18l1.6-2.77c.1-.18.06-.39-.1-.51l-1.67-1.32zM10 13c-1.65 0-3-1.35-3-3s1.35-3 3-3 3 1.35 3 3-1.35 3-3 3z"; // In realtà gear, metto rotate
        String iconRotateCorrect = "M7.11 8.53L5.7 7.11C4.8 8.27 4.24 9.61 4.07 11h2.02c.14-.87.49-1.72 1.02-2.47zM6.09 13H4.07c.17 1.39.72 2.73 1.62 3.89l1.41-1.42c-.52-.75-.87-1.59-1.01-2.47zm1.01 5.32c1.16.9 2.51 1.44 3.9 1.61V17.9c-.87-.15-1.71-.49-2.46-1.03L7.1 18.32zM13 4.07V1L8.45 5.55 13 10V6.09c2.84.48 5 2.94 5 5.91s-2.16 5.43-5 5.91v2.02c3.95-.49 7-3.85 7-7.93s-3.05-7.44-7-7.93z";
        String iconScale = "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 9h-2V7h-2v5H6v2h2v5h2v-5h2v-2z"; // Simbolico resize
        String iconSepia = "M17.5,12c0,0.8 -0.15,1.57 -0.42,2.28c0.58,-0.74 0.92,-1.67 0.92,-2.68c0,-2.37 -1.82,-4.32 -4.14,-4.54c1.84,-0.44 3.65,-0.6 3.65,-0.6c0,0 2.25,3.38 0,5.54zM22,8l-1.64,-0.92c-0.29,-1.2 -0.83,-2.29 -1.56,-3.23l1.19,-1.35l-1.42,-1.42l-1.35,1.2c-0.94,-0.73 -2.03,-1.27 -3.23,-1.56l-0.92,-1.65l-2.14,0l-0.92,1.65c-1.2,0.29 -2.29,0.83 -3.23,1.56l-1.35,-1.2l-1.42,1.42l1.2,1.35c-0.73,0.94 -1.27,2.03 -1.56,3.23l-1.65,0.92l0,2.14l1.65,0.92c0.29,1.2 0.83,2.29 1.56,3.23l-1.2,1.35l1.42,1.42l1.35,-1.2c0.94,0.73 2.03,1.27 3.23,1.56l0.92,1.65l2.14,0l0.92,-1.65c1.2,-0.29 2.29,-0.83 3.23,-1.56l1.35,1.2l1.42,-1.42l-1.2,-1.35c0.73,-0.94 1.27,-2.03 1.56,-3.23l1.64,-0.92l0,-2.14z";
        String iconSharpen = "M12 2L1 21h22L12 2zm0 3.99L19.53 19H4.47L12 5.99z"; // Triangolo/Sharp
        String iconSolarize = "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1z";
        String iconThreshold = "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14H7v-2h5v2zm0-4H7v-2h5v2zm0-4H7V7h5v2zm5 8h-3V7h3v10z";
        String iconVignette = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z";

        // --- POPOLAMENTO LISTA ---
        allFilters.clear();

        // Input & Output
        allFilters.add(new FilterDefinition("Input Source", "InputNode", iconInput));
        allFilters.add(new FilterDefinition("Output", "OutputNode", iconOutput));

        // Filtri Base
        allFilters.add(new FilterDefinition("Gaussian Blur", "BlurFilter", iconBlur));
        allFilters.add(new FilterDefinition("Brightness", "BrightnessFilter", iconBrightness));
        allFilters.add(new FilterDefinition("Contrast", "ContrastFilter", iconContrast));
        allFilters.add(new FilterDefinition("Edge Detection", "EdgeDetectionFilter", iconEdge));
        allFilters.add(new FilterDefinition("Flip Image", "FlipFilter", iconFlip));
        allFilters.add(new FilterDefinition("Grayscale", "GrayscaleFilter", iconGrayscale));
        allFilters.add(new FilterDefinition("Invert Colors", "InvertFilter", iconInvert));
        allFilters.add(new FilterDefinition("Pixelate", "PixelateFilter", iconPixelate));
        allFilters.add(new FilterDefinition("Rotate 90°", "Rotate90Filter", iconRotateCorrect));
        allFilters.add(new FilterDefinition("Scale / Resize", "ScaleFilter", iconScale));
        allFilters.add(new FilterDefinition("Sepia", "SepiaFilter", iconSepia));
        allFilters.add(new FilterDefinition("Sharpen", "SharpenFilter", iconSharpen));
        allFilters.add(new FilterDefinition("Solarize", "SolarizeFilter", iconSolarize));
        allFilters.add(new FilterDefinition("Threshold", "ThresholdFilter", iconThreshold));
        allFilters.add(new FilterDefinition("Vignette", "VignetteFilter", iconVignette));
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