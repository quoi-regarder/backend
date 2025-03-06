package fr.quoi_regarder.commons.utils;

import fr.quoi_regarder.commons.enums.LanguageIsoType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Utility for extracting Locale objects from language codes.
 */
public class ExtractLocale {

    private static final Map<String, Locale> LOCALE_MAP = new HashMap<>();

    static {
        // Initialize the map with code -> Locale mappings
        LOCALE_MAP.put(LanguageIsoType.EN_US.getCode(), Locale.US);
        LOCALE_MAP.put(LanguageIsoType.FR_FR.getCode(), Locale.FRANCE);
        // Add more mappings as needed
    }

    /**
     * Extracts a Locale object from a language code.
     *
     * @param localeCode the language code (e.g., "en-US", "fr-FR")
     * @return the corresponding Locale object or the default locale if the code is not recognized
     */
    public static Locale extractLocale(String localeCode) {
        return Optional.ofNullable(localeCode)
                .map(LOCALE_MAP::get)
                .orElse(Locale.getDefault());
    }

}