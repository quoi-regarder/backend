package fr.quoi_regarder.dto.response;

import fr.quoi_regarder.commons.enums.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorMeta {
    public Map<String, Object> errors;      // Field-specific errors
    private ErrorStatus status;             // Enum status code
    private Object details;                 // Additional error details
}