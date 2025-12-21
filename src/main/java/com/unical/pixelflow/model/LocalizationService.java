package com.unical.pixelflow.model;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import com.unical.pixelflow.model.interfaces.Translation;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizationService {
    private final Map<String, Translation> localizationData = new HashMap<>();
    private final StringProperty selectedLanguage;

    private final static String base_path = "/com/unical/pixelflow/lang/";

    public LocalizationService(String locale) {
        selectedLanguage = new SimpleStringProperty(locale);
        loadAllLocales();
    }

    public ReadOnlyStringProperty selectedLanguageProperty() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String language) {
        selectedLanguage.set(language);
    }

    private static List<String> extractTextWithinBrackets(String input) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }

    public StringBinding lsb(String key) {
        return localizedStringBinding(key);
    }

    public StringBinding localizedStringBinding(String key) {
        return Bindings.createStringBinding(() -> {
            String localizedString = getLocalizedString(key, selectedLanguage.get());
            if (localizedString == null) return "KEY_NOT_FOUND: " + key; // Protezione extra

            List<String> to_replace = extractTextWithinBrackets(localizedString);
            for (String s : to_replace) {
                localizedString = localizedString.replace("[" + s + "]", getLocalizedString(s, selectedLanguage.get()));
            }
            return localizedString;
        }, selectedLanguage);
    }

    @SafeVarargs
    public final StringBinding lsb(String key, ObservableValue<String>... arg) {
        return localizedStringBinding(key, arg);
    }

    @SafeVarargs
    public final StringBinding localizedStringBinding(String key, ObservableValue<String>... args) {
        Observable[] observables = new Observable[args.length + 1];
        observables[0] = selectedLanguage;
        System.arraycopy(args, 0, observables, 1, args.length);

        return Bindings.createStringBinding(() -> {
            String localizedString = getLocalizedString(key, selectedLanguage.get());
            if (localizedString == null) return "KEY_NOT_FOUND: " + key;

            for (int i = 0; i < args.length; i++) {
                if (args[i].getValue() == null) {
                    continue;
                }
                localizedString = localizedString.replace("{" + i + "}", args[i].getValue());
            }
            List<String> to_replace = extractTextWithinBrackets(localizedString);
            for (String s : to_replace) {
                localizedString = localizedString.replace("[" + s + "]", getLocalizedString(s, selectedLanguage.get()));
            }
            return localizedString;
        }, observables);
    }

    public List<String> getAvailableLocales() {
        return new ArrayList<>(localizationData.keySet());
    }

    public String getLocalizedString(String key, String locale) {
        if (!localizationData.containsKey(locale)) {
            return "LOCALE_MISSING_" + key;
        }
        return localizationData.get(locale).translate(key);
    }


    public void loadAllLocales() {
        var url = getClass().getResource(base_path);

        if (url == null) {
            System.err.println("ERRORE CRITICO: Impossibile trovare la cartella risorse: " + base_path);
            System.err.println("Verifica che in src/main/resources esista la cartella: com/unical/pixelflow/lang");
            return;
        }

        File dir = new File(url.getFile());
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                System.out.println("Caricamento lingua: " + file.getName());
                LocalizationMap properties = new LocalizationMap(file.getName());
                String localization_name = file.getName().replace(".properties", "");
                localizationData.put(localization_name, properties);
            }
        }
    }
}