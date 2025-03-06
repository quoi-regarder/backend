package fr.quoi_regarder.exception.exceptions;

import lombok.Getter;

@Getter
public class UserAlreadyExists extends RuntimeException {
    private final String fieldName;
    private final String fieldValue;

    public UserAlreadyExists(String fieldName, String fieldValue) {
        super("User already exists with " + fieldName + " : " + fieldValue);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
