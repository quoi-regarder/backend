package fr.quoi_regarder.exception.exceptions;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EntityNotExistsException extends RuntimeException {
    private final Class<?> entityClass;
    private final String entityProperty;

    public EntityNotExistsException(Class<?> entityClass, @NotBlank @Email String entityProperty) {
        super(String.format("Entity of type %s with %s does not exist",
                entityClass.getSimpleName(), entityProperty));
        this.entityClass = entityClass;
        this.entityProperty = entityProperty;
    }
}