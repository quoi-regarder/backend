package fr.quoi_regarder.exception.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a requested resource cannot be found
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceId = null;
    }

    public ResourceNotFoundException(String message, String resourceId) {
        super(message);
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message, String resourceId, Throwable cause) {
        super(message, cause);
        this.resourceId = resourceId;
    }
}