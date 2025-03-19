package fr.quoi_regarder.exception.exceptions.sse;

/**
 * Exception thrown when SSE authentication fails.
 */
public class SseAuthenticationException extends SseException {

    public SseAuthenticationException(String token) {
        super("Invalid or expired authentication token for SSE connection", "unknown", "AUTHENTICATION");
    }

    public SseAuthenticationException(String userId, Throwable cause) {
        super("Authentication failed for SSE connection", userId, "AUTHENTICATION", cause);
    }
}