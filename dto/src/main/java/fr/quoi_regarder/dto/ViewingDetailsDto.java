package fr.quoi_regarder.dto;

import fr.quoi_regarder.commons.enums.ContextType;
import fr.quoi_regarder.commons.enums.Emotion;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewingDetailsDto implements Serializable {
    private Long id;
    private Long platformId;
    @Min(0)
    private Integer rating;
    private Boolean liked;
    private Emotion emotion;
    private ContextType contextType;
    private Long contextId;
    private UUID userId;
}