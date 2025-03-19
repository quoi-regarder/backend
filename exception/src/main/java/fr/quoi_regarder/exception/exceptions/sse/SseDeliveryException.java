package fr.quoi_regarder.exception.exceptions.sse;

/**
 * Exception thrown when delivery of an SSE event fails.
 */
public class SseDeliveryException extends SseException {
    private final Object payload;

    public SseDeliveryException(String userId, String eventType, Object payload) {
        super("Failed to deliver SSE event", userId, eventType);
        this.payload = payload;
    }

    public SseDeliveryException(String userId, String eventType, Object payload, Throwable cause) {
        super("Failed to deliver SSE event", userId, eventType, cause);
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }
}