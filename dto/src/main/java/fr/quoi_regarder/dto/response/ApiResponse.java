package fr.quoi_regarder.dto.response;

import fr.quoi_regarder.commons.enums.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorMeta error;
    private LocalDateTime timestamp;
    private int status;

    /**
     * Creates a success response with data
     */
    public static <T> ApiResponse<T> success(String message, T data, HttpStatus statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(statusCode.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a success response without data
     */
    public static <T> ApiResponse<T> success(String message, HttpStatus statusCode) {
        return success(message, null, statusCode);
    }

    /**
     * Creates an error response with details
     */
    public static <T> ApiResponse<T> error(String message, ErrorStatus status,
                                           Object details, HttpStatus statusCode) {
        ErrorMeta.ErrorMetaBuilder errorMetaBuilder = ErrorMeta.builder()
                .status(status);

        if (details != null) {
            errorMetaBuilder.details(details);
        }

        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorMetaBuilder.build())
                .status(statusCode.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with field errors
     */
    public static <T> ApiResponse<T> error(String message, ErrorStatus status,
                                           Map<String, Object> fieldErrors, HttpStatus statusCode) {
        ErrorMeta.ErrorMetaBuilder errorMetaBuilder = ErrorMeta.builder()
                .status(status);

        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            errorMetaBuilder.errors(fieldErrors);
        }

        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorMetaBuilder.build())
                .status(statusCode.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a simple error response with just a message and status
     */
    public static <T> ApiResponse<T> error(String message, ErrorStatus status, HttpStatus statusCode) {
        return error(message, status, (Object) null, statusCode);
    }

    /**
     * Adds field-specific errors to an existing error response
     */
    public ApiResponse<T> addFieldError(String field, Object value) {
        if (this.error == null) {
            this.error = new ErrorMeta();
        }

        if (this.error.errors == null) {
            this.error.errors = new HashMap<>();
        }

        this.error.errors.put(field, value);
        return this;
    }
}