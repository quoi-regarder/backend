package fr.quoi_regarder.dto.serie;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerieTranslationDto implements Serializable {
    @NotNull
    private Long tmdbId;
    @NotNull
    private String language;
    private String name;
    private String overview;
    private Date createdAt;
    private Date updatedAt;
}