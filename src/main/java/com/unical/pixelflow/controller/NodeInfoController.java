package com.unical.pixelflow.controller;

import com.unical.pixelflow.model.DataPF;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class NodeInfoController {

    @FXML
    private VBox OverViewNoNodeSelected;

    @FXML
    private VBox PanelControlsEffects;

    @FXML
    private Label alertInfoLabel;

    @FXML
    private Label connectionsCountText;

    @FXML
    private Label connectionsLabel;

    @FXML
    private HBox connectionsStatsBox;

    @FXML
    private HBox headerBox;

    @FXML
    private StackPane imageContainer;

    @FXML
    private HBox infoBox;

    @FXML
    private SVGPath infoIcon;

    @FXML
    private Label nodesCountText;

    @FXML
    private Label nodesLabel;

    @FXML
    private HBox nodesStatsBox;

    @FXML
    private Label projectOverviewTitle;

    @FXML
    private VBox root;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label titleProperties;

    private Runnable onParametersChanged;

    public void setOnParametersChanged(Runnable onParametersChanged) {
        this.onParametersChanged = onParametersChanged;
    }

    public void showNoSelection() {
        if (PanelControlsEffects != null) {
            PanelControlsEffects.getChildren().clear();
            PanelControlsEffects.setVisible(false);
        }
        if (OverViewNoNodeSelected != null) {
            OverViewNoNodeSelected.setVisible(true);
        }
        if (infoBox != null) {
            infoBox.setVisible(true);
        }
        // alertInfoLabel is bound in initialize()
    }

    private static final java.time.Duration PARAM_DEBOUNCE = java.time.Duration.ofMillis(120);
    private javafx.animation.PauseTransition debounce;

    @FXML
    private void initialize() {
        debounce = new javafx.animation.PauseTransition(javafx.util.Duration.millis(PARAM_DEBOUNCE.toMillis()));
        debounce.setOnFinished(e -> {
            if (onParametersChanged != null) {
                onParametersChanged.run();
            }
        });

        // Bind static UI strings
        if (titleProperties != null) titleProperties.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.properties.title"));
        if (projectOverviewTitle != null) projectOverviewTitle.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.project_overview.title"));
        if (nodesLabel != null) nodesLabel.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.project_overview.nodes"));
        if (connectionsLabel != null) connectionsLabel.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.project_overview.connections"));
        if (alertInfoLabel != null) alertInfoLabel.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.hint.select_node"));

        // Stato iniziale overview
        setGraphStats(0, 0);
    }

    private void requestParametersChangedDebounced() {
        if (debounce == null) {
            if (onParametersChanged != null) onParametersChanged.run();
            return;
        }
        debounce.stop();
        debounce.playFromStart();
    }

    public void showForNode(com.unical.pixelflow.domain.model.Node node) {
        if (node == null) {
            showNoSelection();
            return;
        }

        if (OverViewNoNodeSelected != null) {
            OverViewNoNodeSelected.setVisible(false);
        }
        if (infoBox != null) {
            infoBox.setVisible(false);
        }

        if (PanelControlsEffects != null) {
            PanelControlsEffects.getChildren().clear();
            PanelControlsEffects.setVisible(true);
        }

        // Solo filtri hanno parametri.
        if (!(node instanceof com.unical.pixelflow.domain.model.Filter filter)) {
            if (PanelControlsEffects != null) {
                javafx.scene.control.Label lbl = new javafx.scene.control.Label();
                lbl.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.no_editable_params.node"));
                lbl.getStyleClass().add("text-body");
                PanelControlsEffects.getChildren().add(lbl);
            }
            return;
        }

        // Geometric: rotate / flip (detected by well-known parameters)
        if (filter.getParameters() != null && filter.getParameters().containsKey("angle_steps")) {
            buildRotateControls(filter);
            return;
        }
        if (filter.getParameters() != null && filter.getParameters().containsKey("horizontal") && filter.getParameters().containsKey("vertical")) {
            buildFlipControls(filter);
            return;
        }

        // Parametric: slider(s)
        buildNumericParameterControls(filter);
    }

    private void buildRotateControls(com.unical.pixelflow.domain.model.Filter filter) {
        javafx.scene.control.Label title = new javafx.scene.control.Label();
        title.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.rotate.title"));
        title.getStyleClass().add("text-body");

        javafx.scene.control.Button left = new javafx.scene.control.Button("⟲");
        javafx.scene.control.Button right = new javafx.scene.control.Button("⟳");
        javafx.scene.control.Button reset = new javafx.scene.control.Button();
        reset.textProperty().bind(DataPF.lsp.lsb("common.reset"));

        left.getStyleClass().add("transparent-button");
        right.getStyleClass().add("transparent-button");
        reset.getStyleClass().add("transparent-button");

        java.util.function.IntSupplier getSteps = () -> (int) filter.getParameterValue("angle_steps");
        java.util.function.IntConsumer setSteps = v -> filter.setParameterValue("angle_steps", (v % 4 + 4) % 4);

        left.setOnAction(e -> {
            setSteps.accept(getSteps.getAsInt() - 1);
            requestParametersChangedDebounced();
        });
        right.setOnAction(e -> {
            setSteps.accept(getSteps.getAsInt() + 1);
            requestParametersChangedDebounced();
        });
        reset.setOnAction(e -> {
            setSteps.accept(0);
            requestParametersChangedDebounced();
        });

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10, left, right, reset);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        PanelControlsEffects.getChildren().addAll(title, row);
    }

    private void buildFlipControls(com.unical.pixelflow.domain.model.Filter filter) {
        javafx.scene.control.Label title = new javafx.scene.control.Label();
        title.textProperty().bind(DataPF.lsp.lsb("node.flip.name"));
        title.getStyleClass().add("text-body");

        javafx.scene.control.Button flipH = new javafx.scene.control.Button("H");
        javafx.scene.control.Button flipV = new javafx.scene.control.Button("V");
        javafx.scene.control.Button reset = new javafx.scene.control.Button();
        reset.textProperty().bind(DataPF.lsp.lsb("common.reset"));

        flipH.getStyleClass().add("transparent-button");
        flipV.getStyleClass().add("transparent-button");
        reset.getStyleClass().add("transparent-button");

        java.util.function.IntSupplier getH = () -> (int) filter.getParameterValue("horizontal");
        java.util.function.IntSupplier getV = () -> (int) filter.getParameterValue("vertical");

        flipH.setOnAction(e -> {
            filter.setParameterValue("horizontal", getH.getAsInt() == 1 ? 0 : 1);
            requestParametersChangedDebounced();
        });
        flipV.setOnAction(e -> {
            filter.setParameterValue("vertical", getV.getAsInt() == 1 ? 0 : 1);
            requestParametersChangedDebounced();
        });
        reset.setOnAction(e -> {
            filter.setParameterValue("horizontal", 0);
            filter.setParameterValue("vertical", 0);
            requestParametersChangedDebounced();
        });

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10, flipH, flipV, reset);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        PanelControlsEffects.getChildren().addAll(title, row);
    }

    private void buildNumericParameterControls(com.unical.pixelflow.domain.model.Filter filter) {
        boolean any = false;

        for (com.unical.pixelflow.domain.model.Parameter<?> p : filter.getParameters().values()) {
            Object min = p.getMinValue();
            Object max = p.getMaxValue();
            Object cur = p.getValue();

            if (!(cur instanceof Number) || !(min instanceof Number) || !(max instanceof Number)) {
                continue;
            }

            double minD = ((Number) min).doubleValue();
            double maxD = ((Number) max).doubleValue();
            double curD = ((Number) cur).doubleValue();
            if (maxD <= minD) continue;

            any = true;

            javafx.scene.control.Label name = new javafx.scene.control.Label(prettyParamName(p.getName()));
            name.getStyleClass().add("text-body");

            javafx.scene.control.Slider slider = new javafx.scene.control.Slider(minD, maxD, curD);
            slider.setMaxWidth(Double.MAX_VALUE);
            slider.setPickOnBounds(true);
            javafx.scene.layout.HBox.setHgrow(slider, javafx.scene.layout.Priority.ALWAYS);

            // Reset: torna al default (che abbiamo impostato come "nessun effetto" nei filtri)
            javafx.scene.control.Button reset = new javafx.scene.control.Button();
            reset.textProperty().bind(DataPF.lsp.lsb("common.reset"));
            reset.getStyleClass().add("transparent-button");

            javafx.scene.control.Label valueLabel = new javafx.scene.control.Label();
            valueLabel.getStyleClass().add("text-body");

            java.util.function.DoubleConsumer updateValueLabel = v -> {
                valueLabel.setText(formatValue(cur, v));
            };
            updateValueLabel.accept(curD);

            slider.valueProperty().addListener((obs, oldV, newV) -> {
                double v = newV.doubleValue();

                if (cur instanceof Integer) {
                    filter.setParameterValue(p.getName(), (int) Math.round(v));
                } else {
                    filter.setParameterValue(p.getName(), v);
                }

                updateValueLabel.accept(v);

                // Se sto trascinando lo slider, uso debounce; se è un click singolo, idem.
                requestParametersChangedDebounced();
            });

            Object def = p.getDefaultValue();
            reset.setOnAction(e -> {
                if (def instanceof Number n) {
                    double dv = n.doubleValue();
                    slider.setValue(dv);
                }
                requestParametersChangedDebounced();
            });

            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10, slider, valueLabel, reset);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.layout.VBox group = new javafx.scene.layout.VBox(6);
            group.getChildren().addAll(name, row);

            PanelControlsEffects.getChildren().add(group);
        }

        if (!any) {
            javafx.scene.control.Label lbl = new javafx.scene.control.Label();
            lbl.textProperty().bind(DataPF.lsp.lsb("editor.nodeinfo.no_editable_params.filter"));
            lbl.getStyleClass().add("text-body");
            PanelControlsEffects.getChildren().add(lbl);
        }
    }

    private static String prettyParamName(String raw) {
        if (raw == null) return DataPF.lsp.lsb("editor.nodeinfo.param.unknown").get();
        String key = switch (raw) {
            case "radius" -> "editor.nodeinfo.param.radius";
            case "brightness" -> "editor.nodeinfo.param.brightness";
            case "amount" -> "editor.nodeinfo.param.amount";
            case "intensity" -> "editor.nodeinfo.param.intensity";
            case "threshold" -> "editor.nodeinfo.param.threshold";
            case "pixelSize" -> "editor.nodeinfo.param.pixelsize";
            case "percent" -> "editor.nodeinfo.param.percent";
            case "angle_steps" -> "editor.nodeinfo.param.angle_steps";
            case "horizontal" -> "editor.nodeinfo.param.horizontal";
            case "vertical" -> "editor.nodeinfo.param.vertical";
            default -> null;
        };
        return key == null ? raw : DataPF.lsp.lsb(key).get();
    }

    private static String formatValue(Object cur, double v) {
        if (cur instanceof Integer) {
            return String.valueOf((int) Math.round(v));
        }
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    /**
     * Aggiorna la sezione "Project overview" con il numero totale di nodi e connessioni.
     * Viene chiamato dal controller della vista nodi quando il grafo cambia o quando si carica un progetto.
     */
    public void setGraphStats(int nodesCount, int connectionsCount) {
        if (nodesCountText != null) {
            nodesCountText.setText(Integer.toString(Math.max(0, nodesCount)));
        }
        if (connectionsCountText != null) {
            connectionsCountText.setText(Integer.toString(Math.max(0, connectionsCount)));
        }

        // In alcune UI i box possono essere nascosti quando non c' e8 selezione:
        // qui li lasciamo comunque aggiornati senza forzare la visibilit e0.
    }
}
