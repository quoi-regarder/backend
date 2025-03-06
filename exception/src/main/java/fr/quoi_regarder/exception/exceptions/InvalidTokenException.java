package fr.quoi_regarder.exception.exceptions;

import fr.quoi_regarder.commons.enums.TokenType;
import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final TokenType tokenType;

    public InvalidTokenException(TokenType tokenType) {
        super("Invalid token");
        this.tokenType = tokenType;
    }
}
