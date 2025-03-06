package fr.quoi_regarder.commons.enums;

import lombok.Getter;

@Getter
public enum LanguageIsoType {
    FR_FR("fr-FR"),
    EN_US("en-US");

    private final String code;

    LanguageIsoType(String code) {
        this.code = code;
    }

    public static LanguageIsoType findByCode(String code) {
        for (LanguageIsoType language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Invalid language code: " + code);
    }
}