package fr.quoi_regarder.exception.exceptions.sse;

/**
 * Exception thrown when a connection to SSE fails to establish.
 */
public class SseConnectionException extends SseException {

    public SseConnectionException(String userId) {
        super("Failed to establish SSE connection", userId, "CONNECT");
    }

    public SseConnectionException(String userId, Throwable cause) {
        super("Failed to establish SSE connection", userId, "CONNECT", cause);
    }
}