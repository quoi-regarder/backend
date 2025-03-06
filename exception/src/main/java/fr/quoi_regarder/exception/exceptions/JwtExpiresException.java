package fr.quoi_regarder.exception.exceptions;

public class JwtExpiresException extends RuntimeException {
    public JwtExpiresException(String message) {
        super(message);
    }
}
