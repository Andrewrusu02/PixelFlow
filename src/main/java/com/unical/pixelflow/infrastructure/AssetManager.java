package com.unical.pixelflow.infrastructure;
import com.unical.pixelflow.utility.SVGLoader;

public class AssetManager {

    // Salviamo le stringhe dei path (d="...")
    public static String ICON_EYE_SHOW;
    public static String ICON_EYE_HIDE;

    static {
        // Carichiamo usando la tua classe SVGLoader
        // Nota: non serve mettere l'estensione o il path completo grazie alla logica del tuo Loader
        ICON_EYE_SHOW = new SVGLoader("ic_fluent_eye_show_24_filled").getPath();
        ICON_EYE_HIDE = new SVGLoader("ic_fluent_eye_hide_24_filled").getPath();
    }
}