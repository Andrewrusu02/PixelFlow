package com.unical.pixelflow.model.types;


import com.unical.pixelflow.model.DataPF;

public enum Theme {
    SYSTEM,
    LIGHT,
    DARK;


    @Deprecated
    public static Theme fromString(String theme) {
        theme = theme.toLowerCase();
        if (DataPF.lsp.lsb("system").get().toLowerCase().equals(theme)) {
            return Theme.SYSTEM;
        } else if (DataPF.lsp.lsb("light").get().toLowerCase().equals(theme)) {
            return Theme.LIGHT;
        } else if (DataPF.lsp.lsb("dark").get().toLowerCase().equals(theme)) {
            return Theme.DARK;
        }
        return null;

    }

    public static String toString(Theme theme) {
        return DataPF.lsp.lsb(theme.toString().toLowerCase()).get();
    }


    public static Theme fromInt(int type) {
        if (type < 0 || type >= values().length) {
            return null;
        }
        return values()[type];
    }
}
