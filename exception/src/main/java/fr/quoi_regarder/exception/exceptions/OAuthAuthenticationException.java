package fr.quoi_regarder.exception.exceptions;

/**
 * Generic exception for OAuth authentication errors.
 * Covers all types of OAuth authentication failures with a single exception class.
 */
public class OAuthAuthenticationException extends RuntimeException {
    private final String provider;

    /**
     * Creates a new OAuth authentication exception.
     *
     * @param message Error message
     */
    public OAuthAuthenticationException(String message) {
        super(message);
        this.provider = null;
    }

    /**
     * Creates a new OAuth authentication exception with provider information.
     *
     * @param message  Error message
     * @param provider OAuth provider (e.g. "google")
     */
    public OAuthAuthenticationException(String message, String provider) {
        super(message);
        this.provider = provider;
    }

    /**
     * Creates a new OAuth authentication exception with cause.
     *
     * @param message Error message
     * @param cause   Cause of the exception
     */
    public OAuthAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
    }

    /**
     * Creates a new OAuth authentication exception with provider information and cause.
     *
     * @param message  Error message
     * @param provider OAuth provider (e.g. "google")
     * @param cause    Cause of the exception
     */
    public OAuthAuthenticationException(String message, String provider, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    /**
     * Gets the OAuth provider name.
     *
     * @return Provider name or null if not specified
     */
    public String getProvider() {
        return provider;
    }
}