package fr.quoi_regarder.commons.enums;

/**
 * Enum representing various error status codes for the API
 */
public enum ErrorStatus {
    // 400s: Client Errors
    RESSOURCE_NOT_FOUND,
    BAD_REQUEST,
    VALIDATION_FAILED,
    INVALID_LANGUAGE,
    INVALID_TOKEN,

    // 401s: Authentication Errors
    UNAUTHORIZED,
    AUTHENTICATION_FAILED,
    BAD_CREDENTIALS,
    OAUTH_AUTHENTICATION_FAILED,
    JWT_TOKEN_EXPIRED,

    // 403s: Authorization Errors
    FORBIDDEN,
    EMAIL_NOT_VERIFIED,

    // 409s: Conflict Errors
    CONFLICT,
    USER_ALREADY_EXISTS,

    // 500s: Server Errors
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE,
    RATE_LIMIT_EXCEEDED,
}