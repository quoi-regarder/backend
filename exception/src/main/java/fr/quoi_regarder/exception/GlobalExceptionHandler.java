package fr.quoi_regarder.exception;

import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.exception.exceptions.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import javax.naming.AuthenticationException;
import java.util.*;

/**
 * Global exception handler for the REST API.
 * Intercepts specific exceptions and converts them into appropriate HTTP responses.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Handles Bean validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorsMap = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                String fieldName = fieldError.getField();
                String errorCode = Objects.requireNonNull(fieldError.getCode()).toLowerCase();

                @SuppressWarnings("unchecked")
                List<String> fieldErrors = (List<String>) errorsMap.computeIfAbsent(fieldName, k -> new ArrayList<String>());
                fieldErrors.add(errorCode);
            }
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Validation failed for some fields",
                        ErrorStatus.VALIDATION_FAILED,
                        errorsMap,
                        HttpStatus.BAD_REQUEST
                ));
    }

    /**
     * Handles invalid language exceptions.
     */
    @ExceptionHandler(InvalidLanguageException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLanguageException(InvalidLanguageException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Invalid language: " + ex.getProvidedLanguage(),
                        ErrorStatus.INVALID_LANGUAGE,
                        ex.getProvidedLanguage(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    /**
     * Handles authentication exceptions.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Authentication failed: " + ex.getMessage(),
                        ErrorStatus.AUTHENTICATION_FAILED,
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED
                ));
    }

    /**
     * Handles unverified email exceptions.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        "Your email address must be verified before proceeding",
                        ErrorStatus.EMAIL_NOT_VERIFIED,
                        null,
                        HttpStatus.FORBIDDEN
                ));
    }

    /**
     * Handles user already exists exceptions.
     */
    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(UserAlreadyExists ex) {
        Map<String, Object> errorsMap = new HashMap<>();
        errorsMap.put("field", ex.getFieldName());
        errorsMap.put("value", ex.getFieldValue());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        String.format("A user with %s = '%s' already exists", ex.getFieldName(), ex.getFieldValue()),
                        ErrorStatus.USER_ALREADY_EXISTS,
                        errorsMap,
                        HttpStatus.CONFLICT
                ));
    }

    /**
     * Handles invalid token exceptions.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "The provided token is invalid or has expired",
                        ErrorStatus.INVALID_TOKEN,
                        null,
                        HttpStatus.BAD_REQUEST
                ));
    }

    /**
     * Handles entity not exists exceptions.
     */
    @ExceptionHandler(EntityNotExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotExistsException(EntityNotExistsException ex) {
        Map<String, Object> errorsMap = new HashMap<>();
        errorsMap.put("entityClass", ex.getEntityClass().getSimpleName());
        errorsMap.put("entityProperty", ex.getEntityProperty());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        String.format("Entity of type %s with property value %s does not exist",
                                ex.getEntityClass().getSimpleName(), ex.getEntityProperty()),
                        ErrorStatus.RESSOURCE_NOT_FOUND,
                        errorsMap,
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * Handles bad credentials exceptions.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> errorsMap = new HashMap<>();
        errorsMap.put("attemptsLeft", ex.getAttemptsLeft());
        if (ex.getTimeout() != null) {
            errorsMap.put("timeout", ex.getTimeout());
        }

        String message = "Invalid credentials";
        if (ex.getAttemptsLeft() > 0) {
            message += String.format(". %d attempt(s) remaining", ex.getAttemptsLeft());
        } else if (ex.getTimeout() != null) {
            message += ". Account temporarily locked";
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        message,
                        ErrorStatus.BAD_CREDENTIALS,
                        errorsMap,
                        HttpStatus.UNAUTHORIZED
                ));
    }

    /**
     * Handles OAuth authentication exceptions.
     */
    @ExceptionHandler(OAuthAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOAuthAuthenticationException(OAuthAuthenticationException ex) {
        Map<String, Object> errorsMap = new HashMap<>();
        if (ex.getProvider() != null) {
            errorsMap.put("provider", ex.getProvider());
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        ErrorStatus.OAUTH_AUTHENTICATION_FAILED,
                        errorsMap,
                        HttpStatus.UNAUTHORIZED
                ));
    }

    /**
     * Handles REST client exceptions during OAuth flows.
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(RestClientException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Failed to communicate with the authentication provider",
                        ErrorStatus.SERVICE_UNAVAILABLE,
                        ex.getMessage(),
                        HttpStatus.SERVICE_UNAVAILABLE
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> errorsMap = new HashMap<>();

        if (ex.getResourceId() != null) {
            errorsMap.put("resourceId", ex.getResourceId());
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        ErrorStatus.RESSOURCE_NOT_FOUND,
                        errorsMap.isEmpty() ? null : errorsMap,
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * Handles invalid file exceptions.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileException(InvalidFileException ex) {
        Map<String, Object> errorsMap = new HashMap<>();
        errorsMap.put("reason", ex.getReason());

        if (ex.getFileDetail() != null) {
            errorsMap.put("fileDetail", ex.getFileDetail());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        ErrorStatus.BAD_REQUEST,
                        errorsMap,
                        HttpStatus.BAD_REQUEST
                ));
    }
}