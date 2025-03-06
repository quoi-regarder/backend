package fr.quoi_regarder.exception.exceptions;

import lombok.Getter;

/**
 * Exception thrown when an uploaded file is invalid (wrong format, size, etc.)
 */
@Getter
public class InvalidFileException extends RuntimeException {

    private final String reason;
    private final String fileDetail;

    public InvalidFileException(String message, String reason) {
        super(message);
        this.reason = reason;
        this.fileDetail = null;
    }

    public InvalidFileException(String message, String reason, String fileDetail) {
        super(message);
        this.reason = reason;
        this.fileDetail = fileDetail;
    }
}