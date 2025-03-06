package fr.quoi_regarder.exception.exceptions;

public class InvalidLanguageException extends RuntimeException {
    private final String providedLanguage;

    public InvalidLanguageException(String providedLanguage) {
        super("Invalid language: " + providedLanguage);
        this.providedLanguage = providedLanguage;
    }

    public String getProvidedLanguage() {
        return providedLanguage;
    }
}