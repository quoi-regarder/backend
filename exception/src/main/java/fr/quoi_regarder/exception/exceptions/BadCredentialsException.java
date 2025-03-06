package fr.quoi_regarder.exception.exceptions;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class BadCredentialsException extends RuntimeException {
    private final Long attemptsLeft;
    @Nullable
    private final Long timeout;

    public BadCredentialsException(Long attemptsLeft, @Nullable Long timeout) {
        super("badCredentials");
        this.attemptsLeft = attemptsLeft;
        this.timeout = timeout;
    }
}
