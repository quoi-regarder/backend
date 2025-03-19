package fr.quoi_regarder.exception.exceptions.sse;

import lombok.Getter;

/**
 * Base exception for Server-Sent Events related errors.
 */
@Getter
public class SseException extends RuntimeException {
    private final String userId;
    private final String eventType;

    public SseException(String message, String userId, String eventType) {
        super(message);
        this.userId = userId;
        this.eventType = eventType;
    }

    public SseException(String message, String userId, String eventType, Throwable cause) {
        super(message, cause);
        this.userId = userId;
        this.eventType = eventType;
    }
}